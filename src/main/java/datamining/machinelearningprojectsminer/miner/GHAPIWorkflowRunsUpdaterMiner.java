package datamining.machinelearningprojectsminer.miner;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowRunDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowRunDAO;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.WorkflowRun;

public class GHAPIWorkflowRunsUpdaterMiner {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static Date parseDateTime(String dateTimeStr) {
		try {
				return new SimpleDateFormat(DATE_FORMAT).parse(dateTimeStr);
		} catch (ParseException e) {
				throw new RuntimeException("Failed to parse date/time string: " + dateTimeStr, e);
		}
	}


	public static void ghRepositoriesWorkflowsRunsIndividuallyUpdaterMiner(List<Repository> repositories){    

		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();															

		int count = 1;		
		for(Repository repo: repositories){			
			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo and run_started_at IS NULL");
			query.setParameter("repo", repo);
			// query.setParameter("head_branch", repo.getDefaultBranch());
			List<WorkflowRun> workflowRuns = query.list();									
			
			if(workflowRuns.size() == 0){ // || repo.getFullName().equals("uber/ludwig")){
				System.out.printf("PROJ %s/%s - ALL RUNS WERE PROCESSED!\n", count, repositories.size());
				count++;
				continue;
			}

			HibernateUtil.beginTransaction();
			try {					
				int runCount = 1;				
				for (WorkflowRun run : workflowRuns) {
												
					String entryPoint = run.getUrl();
					
					System.out.printf("PROJ %s/%s - RUN %s/%s\n", count, repositories.size(), runCount, workflowRuns.size());																																			

					HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
					
					if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
						System.out.println("Failed: HTTP error code : " + conn.getResponseCode());
						runCount++;
						continue;
					}
																	
					JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
								.getAsJsonObject();		
								
					Date run_started_at = null;
					try {
						run_started_at = parseDateTime(json.get("run_started_at").getAsString());            								
					} catch (Exception e) {
						// TODO: handle exception
					}
					run.setRun_started_at(run_started_at);																									
					
					Long duration = null;
					if(run_started_at != null && run.getUpdatedAt() != null){
						// duration in seconds
						duration = (run.getUpdatedAt().getTime() - run_started_at.getTime()) / 1000;              
					}
					run.setDuration(duration);
					workflowRunDAO.save(run);																					
					
					if(runCount % 50 == 0){
						HibernateUtil.commitTransaction(); // commit after read 50 runs
						HibernateUtil.beginTransaction();
					}					
					runCount++;
				}				
			}catch (Exception e) {
				e.printStackTrace();
			}
			HibernateUtil.commitTransaction(); // commit after read all jobs for a run
			count++;			
		}				
	}

	public static void ghRepositoriesWorkflowsRunsUpdaterMiner(List<Repository> repositories){    

		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();									

		int count = 1;		
		for(Repository repo: repositories){

			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo");			
			query.setParameter("repo", repo);						
			List<WorkflowRun> workflowRuns = query.list();			

			Query query2 = HibernateUtil.getSession().createQuery("SELECT COUNT(*) FROM gha_workflowRun where repo = :repo and run_started_at IS NOT NULL");
			query2.setParameter("repo", repo);
			Long updatedRunsCount = (long) query2.uniqueResult();			

			HashMap<Long, WorkflowRun> workflowRunGHId = new HashMap<>();
			workflowRuns.forEach(run -> workflowRunGHId.put(run.getGhId(), run));

			try {																						
				int totalRuns = GHAPIRepositoryMiner.getWorkflowRunsCountFromGitHubAPI(repo);
				int lastPage = totalRuns / 100;
				if(totalRuns % 100 != 0){
					lastPage++;
				} 

				int page = lastPage - (int)(updatedRunsCount / 100);
				// int page = lastPage;
			
				Date dataMinerDate = parseDateTime("2023-05-01T00:00:00Z");
				boolean breakPagination = false;

				System.out.printf("PROJ %s/%s #%s - Page %s/%s\n", count, repositories.size(), repo.getId(), lastPage - page + 1, lastPage);

				while(page > 0 && !breakPagination){   

					HibernateUtil.beginTransaction();				
										
					String entryPoint = "https://api.github.com/repos/{fullName}/actions/runs?per_page=100&page={page}";
					entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
					entryPoint = entryPoint.replace("{page}", String.valueOf(page));
						
					System.out.printf("PROJ %s/%s #%s - Page %s/%s\n", count, repositories.size(), repo.getId(), lastPage - page + 1, lastPage);

					HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 5);						
						
					if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
						throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
					}				
					
					JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
									.getAsJsonObject();

					JsonArray runsArray = json.get("workflow_runs").getAsJsonArray();					

					for (JsonElement runElement : runsArray) {
						JsonObject runObject = runElement.getAsJsonObject();

						Long workflowRunID = runObject.get("id").getAsLong();
						Date run_started_at = null;
						try {
							run_started_at = parseDateTime(runObject.get("run_started_at").getAsString());            								
						} catch (Exception e) {
							// TODO: handle exception
						}

						Date created_at = null;
						try {
							created_at = parseDateTime(runObject.get("created_at").getAsString());            								
							if(created_at.after(dataMinerDate)){
								breakPagination = true;
								break;
							}
						} catch (Exception e) {
							// TODO: handle exception
						}

						if(workflowRunGHId.containsKey(workflowRunID)){		

							WorkflowRun run = workflowRunGHId.get(workflowRunID);														
							run.setRun_started_at(run_started_at);																									
							// System.out.println("Workflow run updated: GHId #" + workflowRunID + " - " + run.getRun_started_at());
							Long duration = null;
							if(run_started_at != null && run.getUpdatedAt() != null){
									// duration in seconds
									duration = (run.getUpdatedAt().getTime() - run_started_at.getTime()) / 1000;              
							}
							run.setDuration(duration);
							workflowRunDAO.save(run);																					
						}else{
							System.out.println("Workflow run not found in database: GHId #" + workflowRunID + " - " + run_started_at);
						}																																					
					}
					HibernateUtil.commitTransaction(); // commit after read 100 workflows runs					
					page--;																							
				}														
			}catch (Exception e) {
				e.printStackTrace();
			}
			count++;		
		}		
	}

	public static void main(String[] args) {

		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
		
		long[] repoIDs = {
			22, 123, 331, 393, 420, 474, 493, 518, 538, 575, 582, 584, 620, 653, 707, 777, 950, 992, 1410, 1415, 
			1519, 1618, 1662, 1704, 1817, 1820, 1892, 1894, 1897, 1899, 2010, 2014, 2074, 2252, 2338, 2344, 
			
			2483, 2486, 2595, 2609, 2680, 2736, 2759, 2886, 2953, 2954, 2961, 2974, 2984, 2987, 2994, 3006, 			
			3010, 3024, 3052, 3125, 3134, 3154, 3389, 3397, 3419, 3501, 3526, 3527, 3548, 3552, 4029, 3294, 
			
			3295, 3319, 3342, 3352, 3841, 3849, 3891, 3893, 3903, 3905, 3939, 3975, 4002, 4023, 3184, 3185, 
			3218, 3245, 3246, 3263, 3267, 3553, 3582, 3597, 3598, 3623, 3679, 4504, 3706, 3709, 3711, 3730, 
			
			3738, 3766, 3796, 3827, 3840, 4849, 5168, 5260, 5666, 5715, 6154, 6189, 6331, 6438, 6560, 6712, 
			6745, 6791, 6834, 6971, 7024, 7130, 7142, 7147, 7176, 7387, 7523, 7540, 7903, 7996, 3981
		};		

		Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);

		List<Repository> repositories = new ArrayList<Repository>();
		for(Long id : repoIDsLong) {
			repositories.add(repoDAO.get(id));
		}
		
		System.out.println(repositories.size() + " repositories to be processed");
		ghRepositoriesWorkflowsRunsUpdaterMiner(repositories);
		ghRepositoriesWorkflowsRunsIndividuallyUpdaterMiner(repositories);
			
		System.exit(0);
	}
}