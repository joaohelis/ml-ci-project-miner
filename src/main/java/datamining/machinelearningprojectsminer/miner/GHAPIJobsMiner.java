package datamining.machinelearningprojectsminer.miner;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datamining.machinelearningprojectsminer.config.Config;
import datamining.machinelearningprojectsminer.dao.JobDAO;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowRunDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateJobDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowRunDAO;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.miner.helper.GHToken;
import datamining.machinelearningprojectsminer.models.Job;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.WorkflowRun;

public class GHAPIJobsMiner {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static Date parseDateTime(String dateTimeStr) {
			try {
					return new SimpleDateFormat(DATE_FORMAT).parse(dateTimeStr);
			} catch (ParseException e) {
					throw new RuntimeException("Failed to parse date/time string: " + dateTimeStr, e);
			}
	}

	public static void ghRepositoriesWorkflowsRunsJobsMiner(List<Repository> repositories){    

		JobDAO jobDAO = new HibernateJobDAO();
		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();															

		int count = 1;		
		for(Repository repo: repositories){

			// Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo and SIZE(jobs) = 0");
			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo and head_branch = :head_branch and (SIZE(jobs) = 0 AND jobs_count IS NULL)");
			query.setParameter("repo", repo);
			query.setParameter("head_branch", repo.getDefaultBranch());
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
												
					int page = 1;
					int lastPage = 1;
					while(page <= lastPage){   
						String entryPoint = "https://api.github.com/repos/{fullName}/actions/runs/{runId}/jobs?per_page=100&page={page}";
						entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
						entryPoint = entryPoint.replace("{runId}", String.valueOf(run.getGhId()));
						entryPoint = entryPoint.replace("{page}", String.valueOf(page));
						
						System.out.printf("PROJ %s/%s - RUN %s/%s\n", count, repositories.size(), runCount, workflowRuns.size());																																			

						HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
						
						if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
							System.out.println("Failed: HTTP error code : " + conn.getResponseCode());
							page++;
							run.setJobs_count(-conn.getResponseCode());
							continue;
						}
																		
						JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
									.getAsJsonObject();
															
						JsonArray jobsArray = json.get("jobs").getAsJsonArray();
						int totalJobsCount = json.get("total_count").getAsInt();
						System.out.println("Total Jobs: " + totalJobsCount);

						run.setJobs_count(totalJobsCount);

						lastPage = (int) Math.ceil(totalJobsCount / 100.0);			
													
						for (JsonElement jobElement : jobsArray) {							
							JsonObject jobObject = jobElement.getAsJsonObject();														
							
							JsonArray stepsArray = jobObject.get("steps").getAsJsonArray();
							int steps_count = stepsArray.size();

							Date created_at = null;
							Date started_at = null;
							Date completed_at = null;
							
							created_at = jobObject.get("created_at").isJsonNull() ? null : parseDateTime(jobObject.get("created_at").getAsString());            
							started_at = jobObject.get("started_at").isJsonNull() ? null : parseDateTime(jobObject.get("started_at").getAsString());
							completed_at = jobObject.get("completed_at").isJsonNull() ? null : parseDateTime(jobObject.get("completed_at").getAsString());

							Long duration = null;
							if(completed_at != null && started_at != null){
								// duration in seconds
								duration = (completed_at.getTime() - started_at.getTime()) / 1000;              
							}
							
							Job job = new Job(jobObject.get("id").isJsonNull() ? null : jobObject.get("id").getAsLong(),
											  jobObject.get("run_id").isJsonNull() ? null : jobObject.get("run_id").getAsLong(),
											  jobObject.get("name").isJsonNull() ? null : jobObject.get("name").getAsString(),
											  jobObject.get("workflow_name").isJsonNull() ? null : jobObject.get("workflow_name").getAsString(),
											  jobObject.get("head_branch").isJsonNull() ? null : jobObject.get("head_branch").getAsString(),
											  jobObject.get("run_attempt").isJsonNull() ? null : jobObject.get("run_attempt").getAsInt(),											
											  jobObject.get("head_sha").isJsonNull() ? null : jobObject.get("head_sha").getAsString(),
											  jobObject.get("url").isJsonNull() ? null : jobObject.get("url").getAsString(),
											  jobObject.get("status").isJsonNull() ? null : jobObject.get("status").getAsString(),
											  jobObject.get("conclusion").isJsonNull() ? null : jobObject.get("conclusion").getAsString(),
											  created_at, 
											  started_at,
											  completed_at, 
											  duration,
											  steps_count);
																																	
							job.setRun(run);																	
							jobDAO.save(job);															
						}						
						page++;																							
					} // end of jobs pagination for each run					
					
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

	public static void main(String[] args) {
							
		Query query = HibernateUtil.getSession().createQuery("from repository " + 
																		  "where workflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi >= 100 and " + 
																		  "commitsCountInDefaultBranch_ghapi >= 100 and " + 
																		  "(defaultBranch = 'master' or defaultBranch = 'main') " +
																		  "order by workflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi asc");		
		List<Repository> repositories = query.list();	
	
		System.out.println(repositories.size() + " repositories to be processed");
		ghRepositoriesWorkflowsRunsJobsMiner(repositories);
			
		System.exit(0);
	}
}