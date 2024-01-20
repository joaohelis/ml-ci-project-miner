package datamining.machinelearningprojectsminer.miner;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datamining.machinelearningprojectsminer.config.Config;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowRunDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowRunDAO;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.miner.helper.GHToken;
import datamining.machinelearningprojectsminer.miner.helper.GHUtil;
import datamining.machinelearningprojectsminer.miner.helper.GHUtil.BestTokenOption;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.Workflow;
import datamining.machinelearningprojectsminer.models.WorkflowRun;

public class GHAPIWorkflowRunsMiner {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static Date parseDateTime(String dateTimeStr) {
			try {
					return new SimpleDateFormat(DATE_FORMAT).parse(dateTimeStr);
			} catch (ParseException e) {
					throw new RuntimeException("Failed to parse date/time string: " + dateTimeStr, e);
			}
	}

	public static void referenceWorkflowsRunsToTheirWorkflowsByName(List<Repository> repositories){
		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();

		String currentRepositoryInProcess = "williamFalcon/pytorch-lightning";
		int index = -1;
		for(int i = 0; i < repositories.size(); i++){
			if(repositories.get(i).getFullName().equals(currentRepositoryInProcess)){
				index = i;
				break;
			}
		}
		repositories = repositories.subList(index, index+1);

		int count = 0;
		for (Repository repo : repositories) {
			count++;
			int workflowsRunsReferenced = 0;
			HashMap<String, Workflow> workflowsMAP = new HashMap<>();
			repo.getWorkflows().forEach(w -> workflowsMAP.put(w.getName(), w));
			System.out.printf("%s/%s [%s] - ", count, repositories.size(), repo.getFullName());
			List<WorkflowRun> workflowsRuns = repo.getWorkflowsRuns()
												  .stream()
												  .filter(run -> run.getWorkflow() == null)
												  .collect(Collectors.toList());

			System.out.printf("Processing %s WorkflowRuns - ", workflowsRuns.size());
			
			int workflowRunCount = 0;
			workflowRunDAO.beginTransaction();			
			for (WorkflowRun workflowRun : workflowsRuns) {
				String workflowName = workflowRun.getName();
				if(workflowsMAP.containsKey(workflowName)){
					workflowRun.setWorkflow(workflowsMAP.get(workflowName));
					workflowRunDAO.save(workflowRun);
					workflowsRunsReferenced++;
				}else{
					// GitHubWorkflowMiner.getGHWorkflowByGHId(repo, null, null);
				}
				if(++workflowRunCount % 100 == 0){
					workflowRunDAO.commitTransaction();
					workflowRunDAO.beginTransaction();
				}
			}
			workflowRunDAO.commitTransaction();
			System.out.printf("(%s/%s) retrieved.\n", workflowsRunsReferenced, workflowsRuns.size());
		}		
	}

	public static void saveUnreferencedWorkflows(List<Repository> repositories){

		// String currentRepositoryInProcess = "williamFalcon/pytorch-lightning";
		// // String currentRepositoryInProcess = "opencv/dldt";
		
		// int index = -1;
		// for(int i = 0; i < repositories.size(); i++){
		// 	if(repositories.get(i).getFullName().equals(currentRepositoryInProcess)){
		// 		index = i;
		// 		break;
		// 	}
		// }
		// repositories = repositories.subList(index, repositories.size());

		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();
								
		int repoCount = 1;
		for (Repository repo : repositories) {

			System.out.printf("[%s/%s] %s - Unreferenced WorkflowRuns: ", repoCount++, repositories.size(), repo.getFullName());

			// if(
			// 	repo.getFullName().equals("williamFalcon/pytorch-lightning")||
			//    repo.getFullName().equals("opencv/dldt")){
			// 	System.out.println("PASSED");
			// 	continue;
			// }

			HashMap<String, Workflow> workflowsMAP = new HashMap<>();
			repo.getWorkflows().forEach(w -> workflowsMAP.put(w.getName(), w));					

			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo and workflow IS NULL and head_branch = 'master'");
			query.setParameter("repo", repo);
			List<WorkflowRun> workflowRunsWithoutWorkflowRef = query.list();									

			System.out.println(workflowRunsWithoutWorkflowRef.size());
			if(workflowRunsWithoutWorkflowRef.isEmpty()){				
				continue;
			}
			
			List<GHToken> tokens = Config.gitHubTokens;													   
			GHToken token = GHUtil.bestToken(tokens, BestTokenOption.REMAING);
			int remaining = token.getRemaining();
			
			int runCount = 1;					
			int referencedWorkflowRuns = 0;	
			workflowRunDAO.beginTransaction();
			for (WorkflowRun run : workflowRunsWithoutWorkflowRef) {
				if(remaining <= 300){
					token = GHUtil.bestToken(tokens, BestTokenOption.REMAING);
					remaining = token.getRemaining();
				}
				System.out.printf("%s/%s %s - Run %s/%s ",repoCount, repositories.size(), repo.getFullName(), runCount, workflowRunsWithoutWorkflowRef.size());
				if(workflowsMAP.containsKey(run.getName())){
					run.setWorkflow(workflowsMAP.get(run.getName()));
					workflowRunDAO.save(run);
					referencedWorkflowRuns++;
					System.out.println(" - Ref. by Workflow Name");
				}else{					
					Workflow workflow = getAndSaveUnreferencedWorkflowFromWorkflowRun(run);
					remaining-=2;
					if(workflow != null){					
						run.setWorkflow(workflow);
						run.setWorkflow_ghID(workflow.getGhId());
						workflowsMAP.put(workflow.getName(), workflow);
						workflowRunDAO.save(run);
						referencedWorkflowRuns++;
					}
				}				
				if(runCount++ % 100 == 0){
					workflowRunDAO.beginTransaction();
					workflowRunDAO.commitTransaction();
					workflowRunDAO.beginTransaction();									
				}				
				// Workflow workflow = getAndSaveUnreferencedWorkflowFromWorkflowRun(run, token.getToken());
				// if(workflow != null){					
				// 	run.setWorkflow(workflow);
				// 	run.setWorkflow_ghID(workflow.getGhId());

				// 	workflowsMAP.put(workflow.getName(), workflow);

				// 	workflowRunDAO.save(run);
				// 	if(runCount++ % 100 == 0){
				// 		workflowRunDAO.beginTransaction();
				// 		workflowRunDAO.commitTransaction();
				// 		workflowRunDAO.beginTransaction();									
				// 	}				
				// 	referencedWorkflowRuns++;					
				// }				
			}
			workflowRunDAO.beginTransaction();	
			workflowRunDAO.commitTransaction();	
			System.out.printf("%s/%s referenced\n", referencedWorkflowRuns, workflowRunsWithoutWorkflowRef.size());												   					
		}
	}

	private static Workflow getAndSaveUnreferencedWorkflowFromWorkflowRun(WorkflowRun workflowRun) {		

		String entryPoint = workflowRun.getUrl();					
		HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
					
		Workflow workflow = null;
		try {			
			if(conn != null && conn.getResponseCode() == 200){					
				
				JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
					.getAsJsonObject();	
					
				Long workflow_id = json.get("workflow_id").getAsLong();		
	
				WorkflowDAO workflowDAO = new HibernateWorkflowDAO();				
				
				try {
					workflow = workflowDAO.getByGHId(workflow_id);			
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(workflow == null){			
					workflow = GHAPIWorkflowMiner.getGHWorkflowByGHId(workflowRun.getRepo(), workflow_id);
					if(workflow != null){
						workflowDAO.beginTransaction();
						workflowDAO.save(workflow);
						workflowDAO.commitTransaction();
					}
				}			
			}								
		} catch (Exception e) {
			// TODO: handle exception
		}
        return workflow;
    }
	
	public static void referenceWorkflowsRunsToTheirWorkflowsByGHId(List<Repository> repositories){

		int count = 0;
		for (Repository repo : repositories) {
			count++;
			int workflowsRunsReferenced = 0;
			HashMap<Long, Workflow> workflowsMAP = new HashMap<>();
			repo.getWorkflows().forEach(w -> workflowsMAP.put(w.getGhId(), w));
			System.out.printf("%s [%s] Trying to reference WorkflowsRuns to Workflows - ", count, repo.getFullName());
			List<WorkflowRun> workflowsRuns = repo.getWorkflowsRuns();
			for (WorkflowRun workflowRun : workflowsRuns) {
				Long workflowGHId = workflowRun.getWorkflow_ghID();
				if(workflowsMAP.containsKey(workflowGHId)){
					workflowRun.setWorkflow(workflowsMAP.get(workflowGHId));
					workflowsRunsReferenced++;
				}else{
					System.out.println(workflowGHId);
				}
			}
			System.out.printf("(%s/%s) retrieved.\n", workflowsRunsReferenced, workflowsRuns.size());
		}		
	}

	public static void ghRepositoriesWorkflowsRunsMiner(List<Repository> repositories){    

		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();						

		int count = 1;		
		for(Repository repo: repositories){

			System.out.printf("PROJ %s/%s (#%s) %s ", count, repositories.size(), repo.getId(), repo.getFullName());

			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo");			
			query.setParameter("repo", repo);					
			List<WorkflowRun> workflowRuns = query.list();			

			System.out.println(" WorkflowRuns: " + workflowRuns.size());

			HashMap<Long, WorkflowRun> workflowRunGHId = new HashMap<>();
			workflowRuns.forEach(run -> workflowRunGHId.put(run.getGhId(), run));

			HashMap<Long, Workflow> workflowGHIdMap = new HashMap<>();
			repo.getWorkflows().forEach(w -> workflowGHIdMap.put(w.getGhId(), w));

			try {																		
				int totalRuns = GHAPIRepositoryMiner.getWorkflowRunsCountFromGitHubAPI(repo);
				int lastPage = totalRuns / 100;
				if(totalRuns % 100 != 0){
					lastPage++;
				} 

				int alreadyProcessedRuns = workflowRunGHId.size();		
				System.out.println("Already processed runs: " + alreadyProcessedRuns);
				System.out.println("Total runs: " + totalRuns);

				int page = lastPage - (int)(alreadyProcessedRuns / 100);

				Date dataMinerDate = parseDateTime("2023-06-04T00:00:00Z");
				boolean breakPagination = false;				

				while(page > 0 && !breakPagination){   

					HibernateUtil.beginTransaction();	
					
					System.out.printf("PROJ %s/%s (#%s) %s - PAGE %s/%s\n", count, repositories.size(), repo.getId(), repo.getFullName(), lastPage - page + 1, lastPage);
										
					String entryPoint = "https://api.github.com/repos/{fullName}/actions/runs?per_page=100&page={page}";
					entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
					entryPoint = entryPoint.replace("{page}", String.valueOf(page));
					
					HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 10);						
						
					if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
						throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
					}
					
					JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
									.getAsJsonObject();
					
					JsonArray runsArray = json.get("workflow_runs").getAsJsonArray();
					// int totalRunsCount = json.get("total_count").getAsInt();

					// System.out.printf("Pages: %s/%s \n", lastPage - page + 1, lastPage);
					
					for (JsonElement runElement : runsArray) {
						JsonObject runObject = runElement.getAsJsonObject();

						Long workflowRunID = runObject.get("id").getAsLong();
						Long workflow_id = runObject.get("workflow_id").getAsLong();

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
							// duration in seconds
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

						if(workflowRunGHId.containsKey(workflowRunID)){
							WorkflowRun run = workflowRunGHId.get(workflowRunID);							
							run.setDuration(duration);
							run.setCreatedAt(createdAt);
							run.setUpdatedAt(updated_at);
							run.setRun_started_at(run_started_at);
							workflowRunDAO.save(run);																																						
						}

						// if(createdAt != null && createdAt.after(dataMinerDate)){						
						// 	breakPagination = true;								
						// }

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
							workflowRun.setRun_started_at(run_started_at);

							workflowRun.setDiffInSecondsBetweenCreatedAtAndUpdatedAt(diffInSecondsBetweenCreatedAtAndUpdatedAt);
							workflowRun.setUrl(runObject.get("url").isJsonNull() ? null : runObject.get("url").getAsString());
							workflowRun.setWorkflow_ghID(workflow_id);
							workflowRun.setRepo(repo);	

							if(workflowGHIdMap.containsKey(workflow_id)){
								workflowRun.setWorkflow(workflowGHIdMap.get(workflow_id));
							}else{
								System.out.printf("GETTING UNSAVED WORKFLOW: ");
								Workflow workflow = getAndSaveUnreferencedWorkflowFromWorkflowRun(workflowRun);
								System.out.println(workflow);
								workflowRun.setWorkflow(workflow);
								if(workflow != null){
									workflowGHIdMap.put(workflow.getGhId(), workflow);
								}
							}																		
							workflowRunDAO.save(workflowRun);																								
							// System.out.printf("[%s/%s] - %s\n", ++workflowsRunsCount, totalRunsCount, workflowRun);
						}																																					
					}
					HibernateUtil.beginTransaction();
					HibernateUtil.commitTransaction(); // commit after read 100 workflows runs
					
					page--;																							
				}																
			}catch (Exception e) {
				e.printStackTrace();
			}
			count++;		
		}		
	}

	public static void ghRepositoriesGHActionsBuildsMiner(List<Repository> repositories){
		
		for (Repository repo : repositories) {			
			
			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo and workflow IS NOT NULL and head_branch = 'master'");
			query.setParameter("repo", repo);
			List<WorkflowRun> workflowRuns = query.list();	

			Map<String, List<WorkflowRun>> groupedRuns = workflowRuns.stream()
							.collect(Collectors.groupingBy(WorkflowRun::getHeadSha));

			for (Map.Entry<String, List<WorkflowRun>> entry : groupedRuns.entrySet()) {
				String reference = entry.getKey();
				List<WorkflowRun> runs = entry.getValue();
				int totalRuns = runs.size();
				int passedRuns = (int) runs.stream().filter(WorkflowRun::isPassing).count();
				int failedRuns = totalRuns - passedRuns;
				
				List<WorkflowRun> ciRelatedRuns = runs.stream()
													.filter(WorkflowRun::isFromCiWorkflow)
													.collect(Collectors.toList());

				int totalCiRelatedRuns = ciRelatedRuns.size();
				int passedCiRelatedRuns = (int) ciRelatedRuns.stream().filter(WorkflowRun::isPassing).count();
				int failedCiRuns = totalCiRelatedRuns - passedCiRelatedRuns;
								
				Date build_started_at = runs.stream()
												.min(Comparator.comparing(WorkflowRun::getCreatedAt))
												.get().getCreatedAt();

				long build_started_at_time = build_started_at.getTime();

				System.out.println("Build started at: " + build_started_at);

				Date build_finished_at = runs.stream()
												.max(Comparator.comparing(WorkflowRun::getUpdatedAt))
												.get().getUpdatedAt();

				long build_finished_at_time = build_finished_at.getTime();

				System.out.println("Build finished at: " + build_finished_at);

				long buildDurationInSeconds = (build_finished_at_time - build_started_at_time) / 1000;

				
				Long ci_build_started_at_time = null;
				Long ci_build_finished_at_time = null;
				Long ciBuildDurationInSeconds = null;
				if(ciRelatedRuns.size() > 0){								
					Date ci_build_started_at = ciRelatedRuns.stream()
													.min(Comparator.comparing(WorkflowRun::getCreatedAt))
													.get().getCreatedAt();				

					System.out.println("CI Build started at: " + ci_build_started_at);

					Date ci_build_finished_at = ciRelatedRuns.stream()
													.max(Comparator.comparing(WorkflowRun::getUpdatedAt))
													.get().getUpdatedAt();

					System.out.println("CI Build finished at: " + ci_build_finished_at);

					ci_build_started_at_time = ci_build_started_at.getTime();
					ci_build_finished_at_time = ci_build_finished_at.getTime();
					ciBuildDurationInSeconds = (ci_build_finished_at_time - ci_build_started_at_time) / 1000;				
				}
				// long totalTime = runs.stream().mapToLong(WorkflowRun::getDuration).sum();

				String event = null;
				if(!runs.isEmpty()){
					WorkflowRun run = runs.get(0);
					event = run.getEvent();
				}
				
				System.out.println(String.format("%s - %s [%s] %d workflows_runs, %d passed, %d failed, %ds total",
						repo.getFullName(), event, reference.substring(0, 7), totalRuns, passedRuns, failedRuns, buildDurationInSeconds));
				System.out.println(String.format("%s - %s [%s] %d CI workflows_runs, %d passed, %d failed, %ds total",
						repo.getFullName(), event, reference.substring(0, 7), totalCiRelatedRuns, passedCiRelatedRuns, failedCiRuns, ciBuildDurationInSeconds));
			}
		}
	}

	public static void main2(String[] args) {
		
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
				
		// List<Repository> repositories = repoDAO.listAll()
		// 									   .stream()
		// 									   .filter(repo -> repo.getTravisBuildsCount() != null &&
		// 									   				   repo.getTravisBuildsCount() > 0 &&
		// 													   repo.getCommits().isEmpty()
		// 													   )
		// 									   .collect(Collectors.toList());
		
		// List<Repository> repositories = repoDAO.listAll()
		// 									   .stream()
		// 									   .filter(repo -> repo.getWorkflow_runs_count() != null &&											   				   
		// 									   				   repo.getWorkflow_runs_count() > repoDAO.getWorkflowsRunsCount(repo.getFullName()))
		// 									   .sorted(Comparator.comparingInt(Repository::getWorkflow_runs_count))
		// 									   .collect(Collectors.toList());

		/**
		 * 1. Get all repositories with more than 100 workflow runs triggered by push or PR in default branch
		 * 2. Sort by number of commits in default branch (ascending)
		 * 3. Get the first 100 repositories
		 * 4. Get all workflow runs for the repository
		 * 
		 * The data mining started at 2023-05-06
		 */

		// List<Repository> repositories = repoDAO.listAll()
		// 									   .stream()
		// 									   .filter(
		// 											repo -> 
		// 											repo.getCreated_at() != null &&
		// 											repo.getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi() != null &&											   				   
		// 											repo.getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi() >= 100 &&
		// 											repo.getDefaultBranch() != null &&
		// 											(repo.getDefaultBranch().equals("master") || 
		// 											 repo.getDefaultBranch().equals("main"))															   
		// 										)
		// 										.filter(
		// 											// filter out repositories that are not working
		// 											// these repositories are getting "Server Error - Error 502" recurrently from GH API
		// 											repo ->
		// 											!repo.getFullName().equals("tensorflow/tensorflow") &&
		// 											!repo.getFullName().equals("microsoft/vscode") &&
		// 											!repo.getFullName().equals("pytorch/pytorch")
		// 										)
		// 									   // sort by number of commits in default branch (ascending)
		// 									   .sorted((r1, r2) -> r1.getWorkflowRunsCount_ghapi().compareTo(r2.getWorkflowRunsCount_ghapi()))											   
		// 									   .collect(Collectors.toList());

		// List<Repository> repositories = repoDAO.listAll()
		// 									   .stream()
		// 									   .filter(repo ->
		// 									   		   repo.getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi() != null &&
		// 											   repo.getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi() >= 100 &&
		// 											   repo.getFirstWorkflowRunAt() == null
		// 									   	)
		// 									   .collect(Collectors.toList());
		
		// long[] repoIDs = {
		// 	// 22, 123, 331, 393, 420, 474, 493, 518, 538, 575, 582, 584, 620, 653, 707, 777, 950, 992, 1410, 1415, 
		// 	// 1519, 1618, 1662, 1704, 1817, 1820, 1892, 1894, 1897, 1899, 2010, 2014, 2074, 2252, 2338, 2344, 
		// 	// 2483, 2486, 2595, 2609, 2680, 2736, 2759, 2886, 2953, 2954, 2961, 2974, 2984, 2987, 2994, 3006, 
		// 	// 3010, 3024, 3052, 3125, 3134, 3154, 3389, 3397, 3419, 3501, 3526, 3527, 3548, 3552, 4029, 3294, 
		// 	// 3295, 
		// 	// 3319, 3342, 3352, 3841, 3849, 3891, 3893, 3903, 3905, 3939, 3975, 4002, 4023, 
		// 	// 3184, 3185, 
		// 	// 3218, 3245, 3246, 3263, 3267, 3553, 3582, 3597, 3598, 
		// 	// 3623, 3679, 4504, 3706, 3709, 3711, 3730, 
		// 	// 3738, 3766, 3796, 3827, 3840, 4849, 5168, 5260, 5666, 5715, 6154, 6189, 6331, 6438, 6560, 6712, 
		// 	// 6745, 6791, 6834, 6971, 7024, 7130, 7142, 7147, 7176, 7387, 7523, 7540, 7903, 7996, 
		// 	// 3981
		// };		


		long[] repoIDs = {
			// 1617,
			// 2575, 4856, 3107, 4890, 3307, 4741, 1368, 3211, 7662, 1294, 6344, 
			// 5917, 6784, 6136, 2843, 1600, 6528, 4012, 913, 1564, 1621, 2965, 			
			// 2380, 6241, 296, 3837, 453, 3075, 139, 5612, 909, 5030, 2581, 
			
			// 3586, 4959, 3449, 5558, 4736, 3450, 3609, 4436, 1950, 5038, 976, 
			// 661, 2498, 6950, 1982, 3948, 88, 3194, 3284, 2246, 3946, 3550, 
			// 2060, 3344, 1398, 1783, 3143, 1983, 62, 3753, 5490, 3708, 
			
			// 6714, 
			// 6707, 
			
			// 3631
		};

		Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);

		List<Repository> repositories = new ArrayList<Repository>();
		for(Long id : repoIDsLong) {
			repositories.add(repoDAO.get(id));
		}
						
		System.out.println(repositories.size() + " repositories to be processed");
		ghRepositoriesWorkflowsRunsMiner(repositories);
			
		System.exit(0);
	}

	public static void main(String[] args) {

		RepositoryDAO repoDAO = new HibernateRepositoryDAO();

		Query repoQuery = HibernateUtil.getSession().createQuery("from repository where workflowRunsCount_ghapi >= 100");									
		List<Repository> repositories = repoQuery.list();			

		// long[] repoIDs = {
		// 	3631
		// };

		// Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);

		// List<Repository> repositories = new ArrayList<Repository>();
		// for(Long id : repoIDsLong) {
		// 	repositories.add(repoDAO.get(id));
		// }
		
		WorkflowRunDAO workflowRunDAO = new HibernateWorkflowRunDAO();						

		int count = 1;		
		for(Repository repo: repositories){

			if(count < 288) {
				count++;
				continue;
			}

			System.out.printf("PROJ %s/%s (#%s) %s ", count++, repositories.size(), repo.getId(), repo.getFullName());

			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo");			
			query.setParameter("repo", repo);					
			List<WorkflowRun> workflowRuns = query.list();			

			System.out.println(" WorkflowRuns: " + workflowRuns.size());

			Set<Long> workflowRunGHId = new HashSet<Long>();
			int uncommitedDeletions = 0;
			int deletions = 0;

			workflowRunDAO.beginTransaction();			
			for(WorkflowRun workflowRun : workflowRuns) {
				if(workflowRunGHId.contains(workflowRun.getGhId())) {
					System.out.println("Duplicate workflowRun: " + workflowRun.getGhId());
					workflowRunDAO.delete(workflowRun);
					uncommitedDeletions++;
					deletions++;
				} else {
					workflowRunGHId.add(workflowRun.getGhId());
				}				
				if(uncommitedDeletions > 0 && uncommitedDeletions % 50 == 0) {
					System.out.println("uncommitedDeletions: " + uncommitedDeletions);
					System.out.println("deletions: " + deletions);
					workflowRunDAO.commitTransaction();
					workflowRunDAO.beginTransaction();
					uncommitedDeletions = 0;
				}
			}
			workflowRunDAO.commitTransaction();
			System.out.println("deletions: " + deletions);
		}
		System.exit(0);
	}
}