package datamining.machinelearningprojectsminer.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowDAO;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.Workflow;

public class GHAPIWorkflowMiner {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static String getWorkflowContent(Repository repo, String workflowPath) throws IOException {

		String entryPoint = "https://raw.githubusercontent.com/{fullName}/{defaultBranch}/{workflowPath}";
		entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
		entryPoint = entryPoint.replace("{defaultBranch}", repo.getDefaultBranch());
		entryPoint = entryPoint.replace("{workflowPath}", workflowPath);

		HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
    
        if (conn.getResponseCode() == 404) {
            System.out.println("Workflow Configuration File Doesn't exists!");
            return null; // Workflow file doesn't exist
        }
    
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
    
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            response.append("\n");
        }
    
        in.close();
    
        return response.toString();
    }

    public static Workflow workflowJsonExtractor(Repository repo, JsonObject workflowJsonObject){
        Long workflowID = workflowJsonObject.get("id").getAsLong();        
        String name = workflowJsonObject.get("name").getAsString();
        String state = workflowJsonObject.get("state").getAsString();
        String path = workflowJsonObject.get("path").getAsString();
        String html_url = workflowJsonObject.get("html_url").getAsString();
        String fileName = path.substring(path.lastIndexOf("/") + 1);

        Date createdAt = null;
        Date updated_at = null;
        
        try {
            createdAt = formatter.parse(workflowJsonObject.get("created_at").getAsString());
            updated_at = formatter.parse(workflowJsonObject.get("updated_at").getAsString());
        } catch (ParseException e) {            
            e.printStackTrace();
        }
                                                                                        
        Workflow workflow = new Workflow(workflowID, 
                                        name, 
                                        state, 
                                        createdAt, 
                                        updated_at, 
                                        html_url, 
                                        path, 
                                        fileName, 
                                        false);

        workflow.setRepo(repo);
        
        String fileContent = null;
        try {
            if(workflow.getPath() != null && !workflow.getPath().isEmpty()){
                fileContent = getWorkflowContent(repo, path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(fileContent != null){
            workflow.setFileContent(fileContent);
            workflow.setHasConfigFile(true);
        }
        return workflow;
    }

    public static Workflow getGHWorkflowByGHId(Repository repo, Long workflowGHId){
        WorkflowDAO workflowDAO = new HibernateWorkflowDAO();
        Workflow workflow = workflowDAO.getByGHId(workflowGHId);
        
        if(workflow == null){            
            String entryPoint = String.format("https://api.github.com/repos/%s/actions/workflows/%s", 
									repo.getFullName(), 
									workflowGHId);
            try {
				HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
							
				if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
					System.out.println("Failed: HTTP error code : " + conn.getResponseCode());
				}else{
					JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
					.getAsJsonObject();    
					workflow = workflowJsonExtractor(repo, json);
				}            				
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        return workflow;
    }

	public static Integer getWorkflowRunsCount(Repository repo, Long workflowGHId) {
		Integer workflowRunsCount = 0;
		try {			
			String entryPoint = String.format("https://api.github.com/repos/%s/actions/workflows/%s/runs?per_page=1", 
										repo.getFullName(), 
										workflowGHId);
			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);
			if(conn != null && conn.getResponseCode() == 200){
				JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
				.getAsJsonObject();		
				workflowRunsCount = json.get("total_count").getAsInt();
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workflowRunsCount;
	}

    public static void ghRepositoriesWorkflowsMiner(List<Repository> repositories){            

		WorkflowDAO workflowDAO = new HibernateWorkflowDAO();

		int count = 1;		
		for(Repository repo: repositories){

			int workflowsCount = 0;
			HashSet<Long> workflowsGHIds = new HashSet();
			repo.getWorkflows().forEach(w -> workflowsGHIds.add(w.getGhId()));						

			try {				
				int page = 1;				

				while (true) {        
					HibernateUtil.beginTransaction();
                    
					String entryPoint = String.format("https://api.github.com/repos/%s/actions/workflows?per_page=100&page=%s", repo.getFullName(), page);					
					System.out.printf("(%s/%s) - %s\n", count, repositories.size(), repo.getFullName());
					
					HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
							
					if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
						throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
					}
					
					JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
								.getAsJsonObject();	
					JsonArray workflowsArray = json.get("workflows").getAsJsonArray();
					int totalWorkflowsCount = json.get("total_count").getAsInt();

					System.out.printf("Pages: %s/%s \n", page, (int) Math.ceil(((float)totalWorkflowsCount)/100));
												
					for (JsonElement workflowElement : workflowsArray) {
						JsonObject workflowObject = workflowElement.getAsJsonObject();

						Long workflowID = workflowObject.get("id").getAsLong();

						if(workflowsGHIds.contains(workflowID)){
							continue;
						}

                        Workflow workflow = workflowJsonExtractor(repo, workflowObject);
						Integer workflowRunsCount = getWorkflowRunsCount(repo, workflow.getGhId());
						workflow.setWorkflowRunsCount_ghapi(workflowRunsCount);

						workflowDAO.save(workflow);																								
						System.out.printf("[%s/%s] - %s\n", ++workflowsCount, totalWorkflowsCount, workflow);													
					}

					HibernateUtil.commitTransaction(); // commit after read 100 workflows runs
					page++;					
							
					if (workflowsArray.size() == 0) {
							break; // end of results
					}

					// get the link header to check if there are more pages
					String linkHeader = conn.getHeaderField("Link");
					if (linkHeader == null) {
							break; // no more pages
					}

					String[] links = linkHeader.split(", ");
					boolean hasNextPage = false;
					for (String link : links) {
							if (link.endsWith("rel=\"next\"")) {
									hasNextPage = true;
									break;
							}
					}

					if (!hasNextPage) {
							break; // no more pages
					}		
				}																
			}catch (Exception e) {
				e.printStackTrace();
			}
			count++;		
		}		
	}

	public static void setJobAndStepsCountToRepositoriesWorkflows(List<Repository> repositories){
		WorkflowDAO workflowDAO = new HibernateWorkflowDAO();
		int count = 1;
		int totalWorkflowsCount = 1;
		workflowDAO.beginTransaction();
		for(Repository repo: repositories){
			System.out.println("-----------------------------------------------------");
			System.out.printf("PROJECT %s/%s\n", count, repositories.size());
			int workflowCount = 1;
			for(Workflow workflow: repo.getWorkflows()){
				System.out.printf("WORKFLOW %s/%s - #%s\n", workflowCount, repo.getWorkflows().size(), workflow.getGhId());
				setJobAndStepsCountFromYAMLWorkflowFile(workflow);
				System.out.println("Jobs: " + workflow.getJobsCountInYAMLFile());
				System.out.println("Steps: " + workflow.getStepsCountInYAMLFile());
				System.out.println("-----------------------------------------------------");
				workflowDAO.save(workflow);
				if(totalWorkflowsCount % 100 == 0){
					HibernateUtil.commitTransaction();
					workflowDAO.beginTransaction();
				}
				totalWorkflowsCount++;
			}
			count++;
		}
		HibernateUtil.commitTransaction();
	}

	private static void setJobAndStepsCountFromYAMLWorkflowFile(Workflow workflow){
		String workflowContent = workflow.getFileContent();
		
		if(workflowContent != null){

			Yaml yaml = new Yaml();
			Map<String, Object> workflowYAML = null;
			try {
				workflowYAML = yaml.load(workflowContent);	
			} catch (Exception e) {
				System.out.println("CAN NOT LOAD THE WORKFLOW AS AN YAML OBJECT");
			}				
			
			int jobsCount = 0;
			int stepsCount = 0;
			if(workflowYAML != null){

				Map<String, Object> jobs = (Map<String, Object>) workflowYAML.get("jobs");
	
				if(jobs != null){
					jobsCount = jobs.size();
					for (Map.Entry<String, Object> entry : jobs.entrySet()) {
						Map<String, Object> job = (Map<String, Object>) entry.getValue();
						List<Map<String, Object>> steps = (List<Map<String, Object>>) job.get("steps");
						if(steps != null){
							stepsCount += steps.size();
						}
					}
				}
				
			}
			workflow.setJobsCountInYAMLFile(jobsCount);
			workflow.setStepsCountInYAMLFile(stepsCount);
		}		
	}

	public static void main(String[] args) {
		
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();				
		
		// List<Repository> repositories = repoDAO.listAll()
		// 									   .stream()
		// 									   .filter(repo -> repo.getWorkflowRunsCount_ghapi() != null &&											   				   
		// 									   				   repo.getWorkflowRunsCount_ghapi() > 0 &&                                                               
        //                                                        !repo.getFullName().equals("pytorch/pytorch") &&                                                											
		// 													   !repo.getFullName().equals("microsoft/vscode") &&
		// 													   !repo.getFullName().equals("pytorch/pytorch"))
		// 									   .collect(Collectors.toList());

		// String[] repoNames = {															
		// 	// "tensorflow/tensorflow", // Server Error - Error 502 recurrently from GH API
		// 	// "microsoft/vscode", // Server Error - Error 502 recurrently from GH API		 
		// 	// "pytorch/pytorch" // Server Error - Error 502 recurrently from GH API        
		// };

		/**
		 * This method was not used because the Workflows were retrieved 
		 * when was executed the GHAPIWorkflowRunsMiner.
		 * As each WorkflowRun has a Workflow, the Workflows were retrieved by using this association.
		 *
		 * Additionally, this methods only return the Workflows that has an configuration file available
		 * but when we use the GHAPIWorkflowRunsMiner, we can retrieve all Workflows, even the ones that
		 * dont have configuration file available (i.e., those that were deleted)
		 */        

		List<Repository> repositories = repoDAO.listAll()
											   .stream()
											   .filter(repo -> !repo.getWorkflows().isEmpty())
											   .collect(Collectors.toList());
			
		System.out.println("REPOSITORIES: " + repositories.size());		

		ghRepositoriesWorkflowsMiner(repositories);
		setJobAndStepsCountToRepositoriesWorkflows(repositories);
		System.exit(0);
	}
}