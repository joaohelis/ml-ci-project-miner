/**
 * 
 */
package datamining.machinelearningprojectsminer.miner;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datamining.machinelearningprojectsminer.dao.CommitDAO;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowConfFileChangeDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateCommitDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowFileChangeDAO;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.models.Commit;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.Workflow;
import datamining.machinelearningprojectsminer.models.WorkflowConfFileChange;

public class GHAPIWorkflowConfFileChangeMiner {

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private static WorkflowConfFileChange commitExtractorFromJson(JsonObject json){		
		
		String sha = json.get("sha").isJsonNull()? null: json.get("sha").getAsString();

		String authorName = json.getAsJsonObject("commit")
								   .getAsJsonObject("author")
								   .get("name").getAsString();
		String authorEmail = json.getAsJsonObject("commit")
								   .getAsJsonObject("author")
								   .get("email").getAsString();
		
		Date authorDate = null;
		try {
			authorDate = simpleDateFormat.parse(json.getAsJsonObject("commit")
									   .getAsJsonObject("author")
									   .get("date").getAsString());
		} catch (ParseException e) {			
			e.printStackTrace();
		}
		
		String committerName = json.getAsJsonObject("commit")
								   .getAsJsonObject("committer")
								   .get("name").getAsString();
		String committerEmail = json.getAsJsonObject("commit")
								   .getAsJsonObject("committer")
								   .get("email").getAsString();
		
		Date committerDate = null;
		try {
			committerDate = simpleDateFormat.parse(json.getAsJsonObject("commit")
									   .getAsJsonObject("committer")
									   .get("date").getAsString());
		} catch (ParseException e) {			
			e.printStackTrace();
		}				

		String message = json.getAsJsonObject("commit").get("message").getAsString();
		
		WorkflowConfFileChange worflowConfFileChange = new WorkflowConfFileChange(sha, committerName, committerEmail, committerDate, authorName, authorEmail, authorDate, message, null, null, null);				
		return worflowConfFileChange;
	}		

	public static void ghRepositoriesCIWorkflowsConfFileChangeMiner(List<Repository> repositories){    
		
		WorkflowConfFileChangeDAO workflowConfFileChangeDAO = new HibernateWorkflowFileChangeDAO();
		CommitDAO commitDAO = new HibernateCommitDAO();

		int count = 1;		
		for(Repository repo: repositories){

			List<Workflow> ciWorkflows = repo.getWorkflows()
										     .stream()
							   				 .filter(w -> w.isCiWorkflow() != null && w.isCiWorkflow())
											 .filter(w -> w.getHasConfigFile() != null && w.getHasConfigFile())
											 .collect(Collectors.toList());

			System.out.printf("PROJ %s/%s (#%s) %s - CIWorkflows: %s\n", count, repositories.size(), repo.getId(), repo.getFullName(), ciWorkflows.size());
									
						
			int workflowCount = 1;
			for(Workflow workflow: ciWorkflows){
				
				HibernateUtil.beginTransaction();

				try {
					System.out.printf("PROJ %s/%s (#%s) %s - Workflow %s/%s\n", count, repositories.size(), repo.getId(), repo.getFullName(), workflowCount, ciWorkflows.size());						
				
					String entryPoint = "https://api.github.com/repos/{fullName}/commits?path=/.github/workflows/{fileName}";
										
					entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
					entryPoint = entryPoint.replace("{fileName}", workflow.getFileName());
																
					HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 3);						
									
					if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
						throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
					}

					JsonArray commits = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
								.getAsJsonArray();
																																						
					if(commits.isEmpty()){
						System.out.println(" NO COMMIT TO PROCESS");						
					}else{																																																																																										
						for(int i = 0; i < commits.size(); i++) {
							JsonObject commitJson = commits.get(i).getAsJsonObject();							
							WorkflowConfFileChange workflowConfFileChange = commitExtractorFromJson(commitJson);							
							Commit commit = null;

							try {
								commit = commitDAO.getCommitByRepositorySHA(repo, workflowConfFileChange.getSha());	
							} catch (Exception e) {
								System.out.println("WARNING: COMMIT NOT ASSOCIATED!");
							}
													
							workflowConfFileChange.setRepo(repo);
							workflowConfFileChange.setWorkflow(workflow);
							workflowConfFileChange.setCommit(commit);
							
							workflowConfFileChangeDAO.save(workflowConfFileChange);							
						}							
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}																															
				HibernateUtil.commitTransaction(); 				
				workflowCount++;
			}
			HibernateUtil.beginTransaction();							
			HibernateUtil.commitTransaction(); 
			count++;		
		}		
	}

	public static void main(String[] args) {
		
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
				
		long[] repoIDs = {
			22, 123, 331, 420, 474, 493, 518, 538, 575, 582, 584, 620, 653, 707, 777, 
			950, 1410, 1519, 1617, 1618, 1662, 1704, 1894, 1897, 2014, 2074, 2252, 2344, 
			2483, 2486, 2595, 2609, 2680, 2886, 2953, 2954, 2961, 2974, 2987, 2994, 3024, 
			3052, 3125, 3154, 3184, 3185, 3218, 3245, 3246, 3263, 3267, 3294, 3295, 3319, 
			3342, 3352, 3389, 3397, 3419, 3501, 3526, 3527, 3548, 3552, 3597, 3598, 3623, 
			3631, 3679, 3706, 3709, 3711, 3738, 3766, 3796, 3827, 3837, 3841, 3849, 3891, 
			3893, 3903, 3905, 3939, 3981, 4002, 4023, 4029, 4741, 5260, 5666, 5715, 6154, 
			6331, 6560, 6707, 6712, 6745, 6834, 6971, 7024, 7130, 7142, 7176, 7387, 7540, 
			7903, 7996, 6086963, 6086968, 6086969, 6086971, 6086981, 6086991, 6086997, 
			6087001, 6087003, 6087007, 6087013, 6087020, 6087021, 6087023, 6087026, 
			6087030, 6087036, 6087038, 6087048, 6087050, 6087051, 6087054, 6087059, 
			6087067, 6087072, 6087078, 6087082, 6087389, 6087390, 6087391, 6087394, 
			6087395, 6087406, 6087412, 6087414, 6087416, 6087418, 6087419, 6087422, 
			6087428, 6087432, 6087433, 6087438, 6087441, 6087446, 6087449, 6087452, 
			6087454, 6087459, 6087466, 6087467, 6087468, 6087471, 6087475, 6087476, 
			6087477, 6087478, 6087494, 6087497, 6087499, 6087501, 6087504, 6087505, 
			6087509, 6087510, 6092138, 6092142, 6092144, 6092147, 6092148, 6092159, 
			6092163, 6092164, 6092171, 6092172, 6092173, 6092175, 6092178			
		};
			
		Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);

		List<Repository> repositories = new ArrayList<Repository>();
		for(Long id : repoIDsLong) {
			repositories.add(repoDAO.get(id));
		}
						
		System.out.println(repositories.size() + " repositories to be processed");
			
		ghRepositoriesCIWorkflowsConfFileChangeMiner(repositories);
		System.exit(0);
	}
}