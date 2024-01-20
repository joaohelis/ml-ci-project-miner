package datamining.machinelearningprojectsminer.miner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.WorkflowDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateWorkflowDAO;
import datamining.machinelearningprojectsminer.miner.helper.CIWorkflowCheckerResult;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.Workflow;

public class WorkflowChecker {
	
	public static boolean existsDocumentationWordsInString(String name){
		
		String regex = ".*[^a-zA-Z0-9]{WORD}[^a-zA-Z0-9].*|^{WORD}[^a-zA-Z0-9].*|.*[^a-zA-Z0-9]{WORD}$|^{WORD}$|.*{WORD}$";

		String[] ciNotRelatedWords = new String[]{
			"docs",
			"documentation",
			"doc"
		};

		for (String word : ciNotRelatedWords) {
			boolean matches = name.toLowerCase().matches(regex.replace("{WORD}", word));
			if (matches) {
				return true;
			}	
		} 
		return false;
	}

	public static boolean existsCIWordsOcurrenceInString(String name){		
		
		if(name != null)
			name = name.toLowerCase();					

		String regex = ".*[^a-zA-Z0-9]{WORD}[^a-zA-Z0-9].*|^{WORD}[^a-zA-Z0-9].*|.*[^a-zA-Z0-9]{WORD}$|^{WORD}$|.*{WORD}";

		String[] ciRelatedWords = new String[]{
			"ci",
			"continuous integration",
			"continuousintegration",
			"continuous-integration",
			"continuous_integration",
			// "build",	
			"test",			
		};

		for (String ciRelatedWord : ciRelatedWords) {
			boolean matches = name.matches(regex.replace("{WORD}", ciRelatedWord));
			if (matches) {
				return true;
			}	
		}

		String[] buildAndTestWords = new String[]{			
			// "build",	
			"test",			
		};

		// String regexBuildAndTestWords = ".*[^a-zA-Z0-9]{WORD}[^a-zA-Z0-9].*|^{WORD}[^a-zA-Z0-9].*|.*[^a-zA-Z0-9]{WORD}$|^{WORD}$|.*{WORD}.*";
		String regexBuildAndTestWords = ".*{WORD}.*";

		for (String ciRelatedWord : buildAndTestWords) {
			boolean matches = name.matches(regexBuildAndTestWords.replace("{WORD}", ciRelatedWord));
			if (matches) {
				return true;
			}	
		}

		return false;
	}
	
	public static void extractMostCommonCIRunCommandsPerLanguage(){
		Map<String, Map<String, Integer>> runCommandsPerLanguage = new HashMap();		
		WorkflowDAO workflowDAO = new HibernateWorkflowDAO();
		List<Workflow> workflows = workflowDAO.listAll()
											  .stream()
											  .filter(w -> isCiWorkflow(w).getCheckResult())
											  .collect(Collectors.toList());
		
		for (Workflow workflow : workflows) {
			String repoLanguage = workflow.getRepo().getLanguage();
			System.out.println(workflow);
			if(repoLanguage == null)
				continue;
			
			if(workflow.getFileContent() != null){
				Yaml yaml = new Yaml();
				Map<String, Object> workflowYaml = null;
				try {
					workflowYaml = yaml.load(workflow.getFileContent());					
				} catch (Exception e) {
					System.out.println(e.getClass() + " - Workflow could not be parsed!");
					continue;
				}		
				if(workflowYaml == null)
					continue;
				
				Map<String, Object> jobs = (Map<String, Object>) workflowYaml.get("jobs");		
				for (Map.Entry<String, Object> entry : jobs.entrySet()) {
					Map<String, Object> job = (Map<String, Object>) entry.getValue();									
					List<Map<String, Object>> steps = (List<Map<String, Object>>) job.get("steps");
					if(steps != null){
						for (Map<String, Object> step : steps) {
							String runCommand = null;
							try {
								runCommand = (String) step.get("run");
							} catch (Exception e) {
								System.err.println("Command could not be read!");
							}
							List<String> skipCommands = Arrays.asList(new String[]{"#", "cd", "mkdir"});																												
							if(runCommand != null && !runCommand.isEmpty()){
								for(String commandPart: runCommand.split(" ")){
									// String commandPart = runCommand.split(" ")[0];
									if(skipCommands.contains( commandPart )){
										continue;
									}
									if(!runCommandsPerLanguage.containsKey(repoLanguage)){								
										runCommandsPerLanguage.put(repoLanguage, new HashMap<>());									
									}
									if(runCommandsPerLanguage.get(repoLanguage).get(commandPart) == null){									
										runCommandsPerLanguage.get(repoLanguage).put(commandPart, 1);									
									}else{
										int count = runCommandsPerLanguage.get(repoLanguage).get(commandPart);
										runCommandsPerLanguage.get(repoLanguage).put(commandPart, count+1);
									}								
								}
							}
						}
					}
				}
			}		
		}	
		// System.out.println(runCommandsPerLanguage);			

		List<String> mostPopularMLLanguages = Arrays.asList(new String[]{
			"Java",
			"JavaScript",
			"Python",
			"Jupyter",			
			"R",
			"HTML",
			"Go",
			"C",
			"C#",
			"C++"
		});

		for (Map.Entry<String,Map<String, Integer>> entry : runCommandsPerLanguage.entrySet()){

			if(!mostPopularMLLanguages.contains(entry.getKey())){
				continue;
			}

			System.out.println("---------------------------------------");
			System.out.println("Language: " + entry.getKey());
			System.out.println("---------------------------------------");

			List<Map.Entry<String, Integer>> list = new ArrayList<>(entry.getValue().entrySet());

			// Sort the list based on the value of each entry
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});
	
			int count = 1;
			for(Map.Entry<String, Integer> listEntry: list){
				System.out.printf("%s: %s\n", listEntry.getKey(), listEntry.getValue());
				if(count++ == 15)
					break;
			}
		}                
	}

	public static boolean isWorflowTriggeredByPushOrPRChecker(String workflowContent){
		Yaml yaml = new Yaml();
		Map<String, Object> workflow = null;
		try {
			workflow = yaml.load(workflowContent);	
		} catch (Exception e) {
			System.out.println("CAN NOT LOAD THE WORKFLOW AS AN YAML OBJECT");
		}				
		
		if(workflow != null){			

			// Check if the workflow triggers on events typically used for CI, such as pull
			// requests or pushes to the repository
			
			Object value = null;
			if(workflow.containsKey("on")){
				value = workflow.get("on");
			}else if(workflow.containsKey(true)){
				value = workflow.get(true);
			}
			
			if(value != null){
				if(value instanceof Map){
					Map<String, Object> events = (Map<String, Object>) value;
					for (Object eventsKey: events.keySet()) {
						if (eventsKey != null && ( eventsKey.toString().equalsIgnoreCase("push") || eventsKey.toString().equalsIgnoreCase("pull_request") ) ){
							return true;
						}
					}
				}else{
					// when the branches are not declared
					if(value instanceof ArrayList) {
						for (Object valueItem : (ArrayList) value){
							if (valueItem != null && ( valueItem.toString().equalsIgnoreCase("push") || valueItem.toString().equalsIgnoreCase("pull_request") ) ){
								return true;
							}
						}
					}else{
						// direct the element   on : push
						if (value != null && ( value.toString().equalsIgnoreCase("push") || value.toString().equalsIgnoreCase("pull_request") ) ){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static CIWorkflowCheckerResult ciWorkflowCheckerByContent(String workflowContent){		

		boolean isWorflowTriggeredByPushOrPR = false;		
		boolean hasCIWordsInWorkflowJobsNames = false;
		boolean hasCIWordsInWorkflowStepNames = false;
		boolean hasCIKnownRunCommands = false;
		boolean hasCheckoutAction = false;

		Yaml yaml = new Yaml();
		Map<String, Object> workflow = null;
		try {
			workflow = yaml.load(workflowContent);	
		} catch (Exception e) {
			System.out.println("CAN NOT LOAD THE WORKFLOW AS AN YAML OBJECT");
		}				
		
		if(workflow != null){			

			// Check if the workflow triggers on events typically used for CI, such as pull
			// requests or pushes to the repository

			isWorflowTriggeredByPushOrPR = isWorflowTriggeredByPushOrPRChecker(workflowContent);			
			
			String[] frequentRunCommandsInCI = new String[]{
				// "pack",
				// "dotnet",
				// "Release",
				// "msbuild",
				// "build",
				// "test",
				// "install",
				// "sudo",
				// "apt-get",
				// "cmake",
				// "make",
				// "sed",
				// "go",
				// "get",
				// "pip",
				// "python",
				// "git",
				// "executor",
				// "install.packages",
				// "saveRDS",
				// "covr::codecov",
				// "npm",
				// "run",
				// "ci",
				// "lint",
				// "yarn",
				// "dependency",
				// "pytest",
				// "./gradlew",
				// "mvn",
				// "package"
			};
						
			// Check if the workflow includes job definitions focused on building, testing,
			// and validating code changes
			Map<String, Object> jobs = (Map<String, Object>) workflow.get("jobs");		
			for (Map.Entry<String, Object> entry : jobs.entrySet()) {
				Map<String, Object> job = (Map<String, Object>) entry.getValue();
				String jobKey = entry.getKey();
				String jobName = (String) job.get("name");
				if(existsCIWordsOcurrenceInString(jobKey) || 
				(jobName != null && existsCIWordsOcurrenceInString(jobName))){
					hasCIWordsInWorkflowJobsNames = true;
				}			
				List<Map<String, Object>> steps = (List<Map<String, Object>>) job.get("steps");
				if(steps != null){
					for (Map<String, Object> step : steps) {

						System.out.println(step);
						
						String reusableAction = (String) step.get("uses");
						if(reusableAction != null && hasCheckoutAction == false &&
						reusableAction.toLowerCase().startsWith("actions/checkout")){
							hasCheckoutAction = true;	
						}

						if(reusableAction != null && existsCIWordsOcurrenceInString(reusableAction)){
							hasCIWordsInWorkflowStepNames = true;
						}

						String stepName = (String) step.get("name");						
						if(stepName != null && existsCIWordsOcurrenceInString(stepName)){
							hasCIWordsInWorkflowStepNames = true;
						}

						String runCommand = (String) step.get("run");
						if (runCommand != null && !runCommand.isEmpty() && 
							hasCIKnownRunCommands == false){
							for(String frequentCommand: frequentRunCommandsInCI){
								if(runCommand.toLowerCase().contains(frequentCommand)){
									hasCIKnownRunCommands = true;
								}
							}
						}					
					}
				}
				
				String reusableAction = (String) job.get("uses");
				if(reusableAction != null && hasCheckoutAction == false &&
					reusableAction.toLowerCase().startsWith("actions/checkout")){
					hasCheckoutAction = true;	
				}

				if(reusableAction != null && existsCIWordsOcurrenceInString(reusableAction)){
					hasCIWordsInWorkflowStepNames = true;
				}
			}
		}
		// hasCIKnownRunCommands = false;
		// boolean result = (isWorflowTriggeredByPushOrPR && hasCheckoutAction && (hasCIWordsInWorkflowJobsNames || hasCIWordsInWorkflowStepNames)); // || hasCIKnownRunCommands));
		boolean result = (isWorflowTriggeredByPushOrPR && (hasCIWordsInWorkflowJobsNames || hasCIWordsInWorkflowStepNames));

		CIWorkflowCheckerResult checkerResult = new CIWorkflowCheckerResult(result);		
		checkerResult.getCheckLogs().add("isWorflowTriggeredByPushOrPR: " + isWorflowTriggeredByPushOrPR);
		// checkerResult.getCheckLogs().add("hasCheckoutAction: " + hasCheckoutAction);
		checkerResult.getCheckLogs().add("hasCIWordsInWorkflowJobsNames: " + hasCIWordsInWorkflowJobsNames);
		checkerResult.getCheckLogs().add("hasCIWordsInWorkflowStepNames: " + hasCIWordsInWorkflowStepNames);
		// checkerResult.getCheckLogs().add("hasCIKnownRunCommands: " + hasCIKnownRunCommands);
		
		return checkerResult;				
	}

	public static boolean isWorkflowForDeployingStaticWebsiteInGHPages(String workflowName, String workflowPath){
		if(workflowName != null &&
		   workflowName.equalsIgnoreCase("pages-build-deployment")){
			return true;
		}else if(workflowPath != null &&
		workflowPath.equalsIgnoreCase("dynamic/pages/pages-build-deployment")){
		 	return true;
		}
		return false;
	}

	public static CIWorkflowCheckerResult isCiWorkflow(Workflow workflow) {				
		boolean isWorkflowForDeployingStaticWebsiteInGHPages =isWorkflowForDeployingStaticWebsiteInGHPages(workflow.getName(), workflow.getPath());
		boolean existsDocumentationWordsInWorkflowName = existsDocumentationWordsInString(workflow.getName());
		boolean existsDocumentationWordsInWorkflowFileName = existsDocumentationWordsInString(workflow.getFileName());
		boolean hasCIWordsOcurrenceInWorkflowName = existsCIWordsOcurrenceInString(workflow.getName());
		boolean hasCIWordsOcurrenceInWorkflowFileName = existsCIWordsOcurrenceInString(workflow.getFileName());		
		boolean isWorkflowYAMLFileAvailable = false;

		CIWorkflowCheckerResult workflowYMLFileContentCheck = new CIWorkflowCheckerResult();
			
		if(workflow.getFileContent() != null){
			try {
				workflowYMLFileContentCheck = ciWorkflowCheckerByContent(workflow.getFileContent());
				isWorkflowYAMLFileAvailable = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		boolean result = !isWorkflowForDeployingStaticWebsiteInGHPages && 
						 !existsDocumentationWordsInWorkflowFileName && 
						 !existsDocumentationWordsInWorkflowName &&
					     (isWorkflowYAMLFileAvailable && workflowYMLFileContentCheck.getCheckResult());
		
		CIWorkflowCheckerResult isCIworkflowCheck = new CIWorkflowCheckerResult();
		isCIworkflowCheck.setCheckResult(result);		
		isCIworkflowCheck.getCheckLogs().add("isWorkflowForDeployingStaticWebsiteInGHPages: " + isWorkflowForDeployingStaticWebsiteInGHPages);
		isCIworkflowCheck.getCheckLogs().add("existsDocumentationWordsInWorkflowFileName: " + existsDocumentationWordsInWorkflowFileName);
		isCIworkflowCheck.getCheckLogs().add("existsDocumentationWordsInWorkflowName: " + existsDocumentationWordsInWorkflowName);
		isCIworkflowCheck.getCheckLogs().add("isWorkflowYAMLFileAvailable: " + isWorkflowYAMLFileAvailable);
		// isCIworkflowCheck.getCheckLogs().add("hasCIWordsOcurrenceInWorkflowName: " + hasCIWordsOcurrenceInWorkflowName);
		// isCIworkflowCheck.getCheckLogs().add("hasCIWordsOcurrenceInWorkflowFileName: " + hasCIWordsOcurrenceInWorkflowFileName);
		isCIworkflowCheck.getCheckLogs().add("workflowYMLFileContentCheck: " + workflowYMLFileContentCheck.getCheckResult());		
		isCIworkflowCheck.getCheckLogs().add("----------------------------------");
		isCIworkflowCheck.getCheckLogs().addAll(workflowYMLFileContentCheck.getCheckLogs());

		return isCIworkflowCheck;
	}

	public static void main2(String[] args) {
		// String[] words = new String[]{			
		// 	"docs",
		// 	"unittests",
		// 	"BuildFailed",
		// 	"DockerCompose",
		// 	"dockerbuilding"
		// };
		// for(String word: words){					
		// 	System.out.println("existsDocumentationWordsInString: " + word + ": " + existsDocumentationWordsInString(word));
		// 	System.out.println("existsCIWordsOcurrenceInString: " + word + ": " + existsCIWordsOcurrenceInString(word));			
		// }	
		WorkflowDAO workflowDAO = new HibernateWorkflowDAO();
		Workflow workflow = workflowDAO.get(995410L);

		CIWorkflowCheckerResult result = isCiWorkflow(workflow);		
		System.out.println(result.toString());	
		
		System.exit(0);	
	}
	
	public static void repositoriesWorkflowCIChecker(List<Repository> repositories){
		WorkflowDAO workflowDAO = new HibernateWorkflowDAO(); 
		workflowDAO.beginTransaction();
		int workflowCount = 1;									   
		int repoCount = 1;
		for (Repository repo : repositories) {
			List<Workflow> repoWorkflows = repo.getWorkflows();
			System.out.printf("%s/%s %s - WorkflowsCount: %s\n", repoCount++, repositories.size(), repo.getFullName(), repoWorkflows.size());			
			for(Workflow workflow: repoWorkflows){			
				System.out.println("############################################");														
				CIWorkflowCheckerResult isCIworkflowCheck = isCiWorkflow(workflow);				
				workflow.setIsCiWorkflow(isCIworkflowCheck.getCheckResult());

				System.out.println(workflow);
						
				String isCIWorkflowRule = "!isWorkflowForDeployingStaticWebsiteInGHPages && \n" +
										  "!existsDocumentationWordsInWorkflowFileName && \n" +
										  "!existsDocumentationWordsInWorkflowName && \n" +
										  "(isWorkflowYAMLFileAvailable && workflowYMLFileContentCheck)";										  
				String workflowContentCheckRule = "isWorflowTriggeredByPushOrPR && (hasCIWordsInWorkflowJobsNames || hasCIWordsInWorkflowStepNames)"; // || hasCIKnownRunCommands)";
				String checkRules = "----------------------------------\n" +
									">>> RULES\n"  +
									"checkResult=" + isCIWorkflowRule + "\n\n" + 
									"workflowContentCheck=" + workflowContentCheckRule;
				workflow.setIsCiWorkflowCheckLog(isCIworkflowCheck.toString() + checkRules);
				workflowDAO.save(workflow);

				System.out.println(isCIworkflowCheck);
				System.out.println("############################################");		
				if(workflowCount % 100 == 0){
					workflowDAO.commitTransaction();
					workflowDAO.beginTransaction();
				}
				workflowCount++;				
			}			
		}
		workflowDAO.commitTransaction();
	}

	public static void main(String[] args) {	

		// list all repositories with workflows and check if they are CI workflows

		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
		

		// list all repositories with workflows
		List<Repository> repositories = repoDAO.listAll()
											   .stream()
											   .filter(repo -> !repo.getWorkflows().isEmpty())											   
											   .collect(Collectors.toList());

		
		repositoriesWorkflowCIChecker(repositories);
		// extractMostCommonCIRunCommandsPerLanguage();

		System.exit(0);
	}
}