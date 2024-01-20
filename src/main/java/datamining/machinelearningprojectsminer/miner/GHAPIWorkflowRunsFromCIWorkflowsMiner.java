package datamining.machinelearningprojectsminer.miner;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Query;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowRunDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowRunDAO;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.Workflow;
import datamining.machinelearningprojectsminer.models.WorkflowRun;

public class GHAPIWorkflowRunsFromCIWorkflowsMiner {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static Date parseDateTime(String dateTimeStr) {
			try {
					return new SimpleDateFormat(DATE_FORMAT).parse(dateTimeStr);
			} catch (ParseException e) {
					throw new RuntimeException("Failed to parse date/time string: " + dateTimeStr, e);
			}
	}

	public static void ghRepositoriesWorkflowsRunsFromCiWorkflowsMiner(List<Repository> repositories){    

		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();						

		int count = 1;		
		for(Repository repo: repositories){

			List<Workflow> ciWorkflows = repo.getWorkflows()
										     .stream()
							   				 .filter(r -> !r.isCiWorkflow())
											 .collect(Collectors.toList());

			System.out.printf("PROJ %s/%s (#%s) %s - CIWorkflows: %s\n", count, repositories.size(), repo.getId(), repo.getFullName(), ciWorkflows.size());

			int workflowCount = 1;
			for(Workflow workflow: ciWorkflows){

				Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo and workflow = :workflow");
				query.setParameter("repo", repo);						
				query.setParameter("workflow", workflow);
				List<WorkflowRun> workflowRuns = query.list();			
	
				// System.out.println(" WorkflowRuns: " + workflowRuns.size());
	
				HashMap<Long, WorkflowRun> workflowRunGHId = new HashMap<>();
				workflowRuns.forEach(run -> workflowRunGHId.put(run.getGhId(), run));
						
				try {				
					int totalRuns = GHAPIWorkflowMiner.getWorkflowRunsCount(repo, workflow.getGhId());														
					int lastPage = totalRuns / 100;
					if(totalRuns % 100 != 0){
						lastPage++;
					} 
	
					int alreadyProcessedRuns = workflowRunGHId.size();										
					int page = lastPage - (int)(alreadyProcessedRuns / 100);
	
					Date dataMinerDate = parseDateTime("2023-06-04T00:00:00Z");
					boolean breakPagination = false;
	
					while(page > 0 && !breakPagination){   
	
						HibernateUtil.beginTransaction();	
						
						System.out.printf("PROJ %s/%s (#%s) %s - Workflow %s/%s - PAGE %s/%s\n", count, repositories.size(), repo.getId(), repo.getFullName(), workflowCount, ciWorkflows.size(), lastPage - page + 1, lastPage);						
											
						String entryPoint = "https://api.github.com/repos/{fullName}/actions/workflows/{workflowGHI}/runs?per_page=100&page={page}";
						entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
						entryPoint = entryPoint.replace("{workflowGHI}", String.valueOf(workflow.getGhId()));
						entryPoint = entryPoint.replace("{page}", String.valueOf(page));
												
						Map<String, Object> connectionResult = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 10, 30);
                    	HttpURLConnection conn = (HttpURLConnection) connectionResult.get("connection");
                                                                                                            							
						if(conn == null || (conn != null && conn.getResponseCode() != HttpURLConnection.HTTP_OK)){					
							throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
						}

						String response = (String) connectionResult.get("connectionResponse");
												
						JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
						
						JsonArray runsArray = json.get("workflow_runs").getAsJsonArray();
	
						for (JsonElement runElement : runsArray) {
							JsonObject runObject = runElement.getAsJsonObject();
	
							Long workflowRunID = runObject.get("id").getAsLong();
	
							String event = runObject.get("event").getAsString();
	
							Date createdAt = null;
							Date updated_at = null;							
							Date run_started_at = null;
	
							try {
								run_started_at = parseDateTime(runObject.get("run_started_at").getAsString());            								
							} catch (Exception e) {}
	
							try {
								createdAt = parseDateTime(runObject.get("created_at").getAsString());            	
							} catch (Exception e) {}
							
							try {								
								updated_at = parseDateTime(runObject.get("updated_at").getAsString());
							} catch (Exception e) {}
	
							Long duration = null;
							Long diffInSecondsBetweenCreatedAtAndUpdatedAt = null;
							if(createdAt != null && updated_at != null){
								diffInSecondsBetweenCreatedAtAndUpdatedAt = (updated_at.getTime() - createdAt.getTime()) / 1000;              
							}
	
							if(run_started_at != null && updated_at != null){
								// duration in seconds
								duration = (updated_at.getTime() - run_started_at.getTime()) / 1000;              
							}
							
							Date head_commit_timestamp = null;
							try{
								head_commit_timestamp = parseDateTime(
															runObject.getAsJsonObject("head_commit")
													.get("timestamp").getAsString());
							}catch(Exception e){
								System.out.println("HEAD COMMIT TIMESTAMP IS NULL");
							}
								
							if(createdAt != null && createdAt.after(dataMinerDate)){						
								// breakPagination = true;								
							}
	
							if(!workflowRunGHId.containsKey(workflowRunID)){							
																				
								WorkflowRun workflowRun = new WorkflowRun(
									workflowRunID,
									runObject.get("name").getAsString(),
									runObject.get("status").getAsString(),
									runObject.get("conclusion").isJsonNull() ? null : runObject.get("conclusion").getAsString(),
									duration,
									runObject.get("head_branch").isJsonNull() ? null : runObject.get("head_branch").getAsString(),							
									runObject.get("head_sha").getAsString(),
									createdAt,
									updated_at,
									head_commit_timestamp,
									event
								);

								workflowRun.setDuration(duration);
								workflowRun.setCreatedAt(createdAt);
								workflowRun.setUpdatedAt(updated_at);
								workflowRun.setRun_started_at(run_started_at);
	
								workflowRun.setDiffInSecondsBetweenCreatedAtAndUpdatedAt(diffInSecondsBetweenCreatedAtAndUpdatedAt);
								workflowRun.setUrl(runObject.get("url").isJsonNull() ? null : runObject.get("url").getAsString());
								workflowRun.setWorkflow_ghID(workflow.getGhId());
								workflowRun.setWorkflow(workflow);
								workflowRun.setRepo(repo);										
								workflowRunDAO.save(workflowRun);																																
							}																																					
						}
						HibernateUtil.beginTransaction();
						HibernateUtil.commitTransaction(); // commit after read 100 workflows runs
						
						page--;																							
					}																
				}catch (Exception e) {
					e.printStackTrace();
				}
				workflowCount++;
			}
			count++;		
		}		
	}

	public static void main(String[] args) {
		
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
		
		long[] repoIDs = {
			// // 6086963, 6086968, 6086969, 6086971, 6086977, 6086981, 6086991, 
			// // 6086992, 6086997, 
			// 6087001, 6087003, 6087007, 6087013, 6087020, 
			// 6087021, 6087023, 6087026, 6087029, 6087030, 6087036, 6087038, 
			// 6087048, 6087050, 6087051, 6087054, 6087055, 6087059, 6087060, 
			
			// // 6087067, 6087072, 6087075, 6087078, 6087082, 6087388, 6087389, 
			// // 6087390, 6087391, 6087394, 6087395, 6087406, 6087407, 
			6087410, 6087412, 6087414, 6087416, 6087418, 6087419, 6087422, 6087423, 
			6087428, 6087432, 6087433, 6087434, 6087438, 6087441, 6087446, 
						
			// 6087449, 6087451, 6087452, 6087454, 6087459, 6087463, 6087464, 
			// 6087466, 6087467, 6087468, 6087471, 6087474, 6087475, 6087476, 
			// 6087477, 6087478, 6087491, 6087492, 6087494, 6087497, 6087498, 
						
			// 6087499, 6087501, 6087502, 6087504, 6087505, 6087509, 6087510, 
			// 6092138, 6092142, 6092144, 6092147, 6092148, 6092150, 6092152, 
			// 6092159, 6092163, 6092164, 6092169, 6092171, 6092172, 6092173, 
			// 6092175, 6092176, 6092178
		};		

		Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);

		List<Repository> repositories = new ArrayList<Repository>();
		for(Long id : repoIDsLong) {
			repositories.add(repoDAO.get(id));
		}
						
		System.out.println(repositories.size() + " repositories to be processed");
		ghRepositoriesWorkflowsRunsFromCiWorkflowsMiner(repositories);
			
		System.exit(0);
	}
}