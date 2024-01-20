/**
 * 
 */
package datamining.machinelearningprojectsminer.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hibernate.Query;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.miner.helper.GHUtil;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.TravisBuild;
import datamining.machinelearningprojectsminer.models.WorkflowRun;

public class GHAPIRepositoryMiner {

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private static Integer getContributorsCount(String repositoryUrl) throws IOException {

		try {
			String entryPoint = String.format(repositoryUrl);
			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						

			if(conn.getResponseCode() == 200) {											
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();
				
				String line;

				// Read each line and append it to the StringBuilder
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				reader.close();

				// Get the final string
				String result = stringBuilder.toString();

				Document doc = Jsoup.parse(result);

				Elements contributorsElements = doc.select("a:contains(Contributors) > span.Counter");

				System.out.println(contributorsElements);

				if(!contributorsElements.isEmpty()){					
					Element contributorElement = contributorsElements.first();

					if (contributorElement != null) {

						String contributorCount = contributorElement.text();
						contributorCount = contributorCount.replace(",", "");					

						System.out.println(repositoryUrl);
						System.out.println("STRING >>>> " + contributorCount);
						// Extract the count from the text content of the span element "Contributors X"
						return Integer.parseInt(contributorCount);
					}			 
				}
				return 1;
			}			
		} catch (IOException e) {			
			e.printStackTrace();
		}
        		        
		throw new RuntimeException("Contributors count element not found in HTML");        
    }

	private static Integer getReleasesCount(String repositoryUrl) throws IOException {

		try {
			String entryPoint = String.format(repositoryUrl);
			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						

			if(conn.getResponseCode() == 200) {											
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();
				
				String line;

				// Read each line and append it to the StringBuilder
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				reader.close();

				// Get the final string
				String result = stringBuilder.toString();

				Document doc = Jsoup.parse(result);

				Elements releasesElements = doc.select("a:contains(Releases) > span.Counter");

				System.out.println(releasesElements);

				if(!releasesElements.isEmpty()){					
					Element releaseElement = releasesElements.first();

					if (releaseElement != null) {

						String releaseCount = releaseElement.text();
						releaseCount = releaseCount.replace(",", "");					

						System.out.println(repositoryUrl);
						System.out.println("STRING >>>> " + releaseCount);
						// Extract the count from the text content of the span element "Contributors X"
						return Integer.parseInt(releaseCount);
					}			 
				}				
			}			
		} catch (IOException e) {		
			e.printStackTrace();
		}
        		        
		throw new RuntimeException("Releases count element not found in HTML");        
    }

	private static void setReleasesCount(Repository repo) {

		String repositoryUrl = "https://github.com/{fullName}";
		repositoryUrl = repositoryUrl.replace("{fullName}", repo.getFullName());        

		Integer releasesCount = null;
		try {
			releasesCount = getReleasesCount(repositoryUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		repo.setTotalReleasesGitHubPage(releasesCount);
    }

	private static void setContributorsCount(Repository repo) {

		String repositoryUrl = "https://github.com/{fullName}";
		repositoryUrl = repositoryUrl.replace("{fullName}", repo.getFullName());        

		Integer contributorsNumber = null;
		try {
			contributorsNumber = getContributorsCount(repositoryUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		repo.setTotalContributorsGitHubPage(contributorsNumber);
    }

	public static void setHasGHActionsConfigurationFileMiner(Repository repo){
		boolean hasGHActionsConfFile = false;
		int workflowsCount = 0;
		try {
			String entryPoint = String.format("https://api.github.com/repos/%s/contents/.github/workflows/", repo.getFullName());
			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						

			if(conn.getResponseCode() == 200) {											
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				Gson gson = new Gson();
				JsonArray workflows = gson.fromJson(reader, JsonArray.class);
				if(!workflows.isEmpty()){
					hasGHActionsConfFile = true;
				}
				workflowsCount = workflows.size();
			}			
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		repo.setHasGHActionsConfigurationFile(hasGHActionsConfFile);
		repo.setWorkflowsCountGHApi(workflowsCount);
    }	

	// Method to count the number of Workflow Runs of a repository
	public static void setWorkflowRunsCountAndDateOfFirstAndLastRunFromGitHubAPI(Repository repo){

		int workflowRunsCount = 0;
		Date dateOfFirstWorkflowRun = null;
		Date dateOfLastWorkflowRun = null;		

		try {
			String entryPoint = String.format("https://api.github.com/repos/%s/actions/runs", repo.getFullName());

			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
						
			if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}						
			
			if(conn.getResponseCode() == 200) {											
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				Gson gson = new Gson();
				JsonObject json = gson.fromJson(reader, JsonObject.class);
				if(!json.isJsonNull()){
					workflowRunsCount = json.get("total_count").isJsonNull()? null: json.get("total_count").getAsInt();							
					JsonArray runsArray = json.get("workflow_runs").getAsJsonArray();					
					if(!runsArray.isEmpty()){
						JsonObject runObject = runsArray.get(0).getAsJsonObject();
						Date created_at = null;
						try {
							created_at = GHUtil.parseDateTime(runObject.get("created_at").getAsString());            															
						} catch (Exception e) {
							// TODO: handle exception
						}
						dateOfLastWorkflowRun = created_at;
					}					
				}								
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
		if(workflowRunsCount > 0){
			try {
				int lastPage = workflowRunsCount / 100;
				if(workflowRunsCount % 100 != 0){
					lastPage++;
				}
	
				String entryPoint = String.format("https://api.github.com/repos/%s/actions/runs?per_page=100&page=%s", repo.getFullName(), lastPage);
	
				HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
							
				if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
					System.out.println("Failed : HTTP error code : " + conn.getResponseCode());
					return;
					// throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}						
				
				if(conn.getResponseCode() == 200) {											
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					Gson gson = new Gson();
					JsonObject json = gson.fromJson(reader, JsonObject.class);
					if(!json.isJsonNull()){
						JsonArray runsArray = json.get("workflow_runs").getAsJsonArray();					
						if(!runsArray.isEmpty()){
							JsonObject runObject = runsArray.get(runsArray.size() - 1).getAsJsonObject();
							Date created_at = null;
							try {
								created_at = GHUtil.parseDateTime(runObject.get("created_at").getAsString());            															
							} catch (Exception e) {
								// TODO: handle exception
							}
							dateOfFirstWorkflowRun = created_at;
						}					
					}								
				}
			} catch (IOException e) {			
				e.printStackTrace();
			}		
		}		
		repo.setWorkflowRunsCount_ghapi(workflowRunsCount);
		repo.setFirstWorkflowRunAt_ghapi(dateOfFirstWorkflowRun);
		repo.setLastWorkflowRunAt_ghapi(dateOfLastWorkflowRun);
		// get the interval in days between the first and last workflow run
		if(dateOfFirstWorkflowRun != null && dateOfLastWorkflowRun != null){
			long diffInMillies = Math.abs(dateOfLastWorkflowRun.getTime() - dateOfFirstWorkflowRun.getTime());
		    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		    repo.setFirstAndLastWorkflowRunIntervalInDays_ghapi((int) diff);
		}	
	}
	

	// Method to count the number of Workflow Runs in the default branch of a repository
	public static Integer getWorkflowRunsCountFromGitHubAPI(Repository repo){

		int workflowRunsCount = 0;
		try {
			String entryPoint = String.format("https://api.github.com/repos/%s/actions/runs", repo.getFullName());

			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
						
			if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}						
			
			if(conn.getResponseCode() == 200) {											
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				Gson gson = new Gson();
				JsonObject json = gson.fromJson(reader, JsonObject.class);
				if(!json.isJsonNull()){
					workflowRunsCount = json.get("total_count").isJsonNull()? null: json.get("total_count").getAsInt();							
				}								
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		return workflowRunsCount;		
	}	

	// Method to count the number of Workflow Runs triggered by push or pull request in the default branch of a repository	
	public static Integer getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount(Repository repo){
		String[] events = {"push", "pull_request"};
		Integer workflowRunsTriggeredByPushOrPrCount = 0;
		for (String event : events) {
			int workflowRunsByEventCount = 0;
			try {
				String entryPoint = "https://api.github.com/repos/{fullName}/actions/runs?branch={branch}&status=completed&event={event}";
				entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
				entryPoint = entryPoint.replace("{branch}", repo.getDefaultBranch());
				entryPoint = entryPoint.replace("{event}", event);			
				
				HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
				
				if(conn.getResponseCode() == 200) {											
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					Gson gson = new Gson();
					JsonObject json = gson.fromJson(reader, JsonObject.class);
					if(!json.isJsonNull()){
						workflowRunsByEventCount = json.get("total_count").isJsonNull()? null: json.get("total_count").getAsInt();							
					}								
				}
			} catch (IOException e) {			
				e.printStackTrace();
			}
			if(event.equals("push"))
				repo.setWorkflowRunsTriggeredByPushCount_ghapi(workflowRunsByEventCount);
			else if(event.equals("pull_request"))
				repo.setWorkflowRunsTriggeredByPrCount_ghapi(workflowRunsByEventCount);
			
			workflowRunsTriggeredByPushOrPrCount += workflowRunsByEventCount;
		}
		return workflowRunsTriggeredByPushOrPrCount;
	}

	public static void setWorkflowRunsCountMiner(Repository repo){
		Integer workflow_runs_count = getWorkflowRunsCountFromGitHubAPI(repo);		
		repo.setWorkflowRunsCount_ghapi(workflow_runs_count);	
	}	
	
    public static void setCommitsCountInRepoDefaultBranchMiner(Repository repo){
		int commitsCount = GHAPICommitsMiner.countCommitsInRepoDefaultBranch(repo);		
		repo.setCommitsCountInDefaultBranch_ghapi(commitsCount);
   }
   
   public static void setPullRequestsCountMiner(Repository repo){
		Integer pullRequestsCount = GHGraphQLPullRequestMiner.countRepoPullRequestsGHAPI(repo);
		repo.setPullRequestsCount_ghapi(pullRequestsCount);
   }

   public static void setCommitsCountInRepoDefaultBranchAfterGHAAdaptionPeriodMiner(Repository repo) {				
		Integer commitCount = null;
		try{
			String token = GHAPIProcessor.getToken().getToken();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(repo.getFirstWorkflowRunAt_ghapi());
			calendar.add(Calendar.DATE, 15);
			Date ghaAdaptionPeriodLimitDate = calendar.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			String formattedDate = sdf.format(ghaAdaptionPeriodLimitDate);

			// Define the GraphQL query         
			String query = "{ \"query\": \"query { repository(owner: \\\"" + repo.getOwner() + "\\\", name: \\\"" + repo.getName() + "\\\") { object(expression: \\\"{defaulBranch}\\\") { ... on Commit { history(since: \\\"{since}\\\") { totalCount } } } } }\" }";
			query = query.replace("{defaulBranch}", repo.getDefaultBranch());
			query = query.replace("{since}", formattedDate); 
					
			//   Send POST request to GitHub GraphQL API
			URL url = new URL("https://api.github.com/graphql");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Bearer <TOKEN>".replace("<TOKEN>", token)); // Replace with your GitHub access token
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(query.getBytes());
			outputStream.flush();
			outputStream.close();    	
									
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// Parse JSON response
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();
				while ((inputLine = bufferedReader.readLine()) != null) {
					response.append(inputLine);
				}
				bufferedReader.close();

				JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
				JsonObject data = json.getAsJsonObject("data");             
				JsonObject repository = null;
				try {
					repository = data.getAsJsonObject("repository");                
					try {
						JsonElement object = repository.get("object"); // Use get() instead of getAsJsonObject() to handle null values
						if (object != null && object.isJsonObject()) {
							JsonObject history = object.getAsJsonObject().getAsJsonObject("history");
							commitCount = history.get("totalCount").getAsInt();							
						}
					} catch (Exception e) {
						System.out.printf("Failed to fetch commits in '%s' branch!\n", repo.getDefaultBranch());
					}
				} catch (Exception e) {
					System.out.println("Failed to fetch commit count. Repository not found!");
				}                                                                      
			} else {
				System.out.println("Failed to fetch commit count. Response Code: " + responseCode);             
			}
		}catch(Exception e){
			System.out.println("Failed to fetch commit count. Exception: " + e.getMessage());
		} 
		repo.setCommitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi(commitCount);
	}
	
	public static void setWorkflowRunsTriggeredByPushOrPrInDefaultBranchCountMiner(Repository repo){
		Integer workflowRunsTriggeredByPushOrPrCount = getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount(repo);	
		repo.setWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi(workflowRunsTriggeredByPushOrPrCount);	
	}


	// public static void setHasGHActionsConfigurationFileMiner(String token, Repository repo){
	// 	boolean hasGHActionsConfFile = false;
	// 	int workflowsCount = 0;
	// 	try {
	// 		String entryPoint = String.format("https://api.github.com/repos/%s/contents/.github/workflows/", repo.getFullName());
			
	// 		URL url = new URL(entryPoint);
				
	// 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();															
	// 		conn.addRequestProperty("Authorization", "token " + token);

	// 		if(conn.getResponseCode() == 200) {											
	// 			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	// 			Gson gson = new Gson();
	// 			JsonArray workflows = gson.fromJson(reader, JsonArray.class);
	// 			if(!workflows.isEmpty()){
	// 				hasGHActionsConfFile = true;
	// 			}
	// 			workflowsCount = workflows.size();
	// 		}			
	// 	} catch (IOException e) {			
	// 		e.printStackTrace();
	// 	}		
	// 	repo.setHasGHActionsConfigurationFile(hasGHActionsConfFile);
	// 	repo.setWorkflowsCountGHApi(workflowsCount);
    // }

	public static void setHasTravisConfigurationFileMiner(Repository repo){
		boolean hasTravisConfFile = false;
		try {
			String entryPoint = String.format("https://api.github.com/repos/%s/contents/.travis.yml", repo.getFullName());
			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);									
			
			if(conn.getResponseCode() == 200) {											
				hasTravisConfFile = true;				
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		repo.setHasTravisConfigurationFile(hasTravisConfFile);		
	}

	// // Method to count the number of Workflow Runs in the default branch of a repository
	// public static Integer getWorkflowRunsCountFromGitHubAPI(Repository repo){

	// 	int workflowRunsCount = 0;
	// 	try {
	// 		String entryPoint = String.format("https://api.github.com/repos/%s/actions/runs", repo.getFullName());

	// 		HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 1);						
						
	// 		if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
	// 			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
	// 		}						
			
	// 		if(conn.getResponseCode() == 200) {											
	// 			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	// 			Gson gson = new Gson();
	// 			JsonObject json = gson.fromJson(reader, JsonObject.class);
	// 			if(!json.isJsonNull()){
	// 				workflowRunsCount = json.get("total_count").isJsonNull()? null: json.get("total_count").getAsInt();							
	// 			}								
	// 		}
	// 	} catch (IOException e) {			
	// 		e.printStackTrace();
	// 	}		
	// 	return workflowRunsCount;		
	// }	

	// Method to count the number of Workflow Runs triggered by push or pull request in the default branch of a repository	
	private static Integer getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount(String token, Repository repo){
		String[] events = {"push", "pull_request"};
		Integer workflowRunsTriggeredByPushOrPrCount = 0;
		for (String event : events) {
			int workflowRunsByEventCount = 0;
			try {
				String entryPoint = "https://api.github.com/repos/{fullName}/actions/runs?branch={branch}&status=completed&event={event}";
				entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
				entryPoint = entryPoint.replace("{branch}", repo.getDefaultBranch());
				entryPoint = entryPoint.replace("{event}", event);			
				URL url = new URL(entryPoint);
				
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();															
				conn.addRequestProperty("Authorization", "token " + token);	
				
				if(conn.getResponseCode() == 200) {											
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					Gson gson = new Gson();
					JsonObject json = gson.fromJson(reader, JsonObject.class);
					if(!json.isJsonNull()){
						workflowRunsByEventCount = json.get("total_count").isJsonNull()? null: json.get("total_count").getAsInt();							
					}								
				}
			} catch (IOException e) {			
				e.printStackTrace();
			}
			if(event.equals("push"))
				repo.setWorkflowRunsTriggeredByPushCount_ghapi(workflowRunsByEventCount);
			else if(event.equals("pull_request"))
				repo.setWorkflowRunsTriggeredByPrCount_ghapi(workflowRunsByEventCount);
			
			workflowRunsTriggeredByPushOrPrCount += workflowRunsByEventCount;
		}
		return workflowRunsTriggeredByPushOrPrCount;
	}

	public static void setWorkflowRunsCountMiner(String token, Repository repo){
		Integer workflow_runs_count = getWorkflowRunsCountFromGitHubAPI(repo);		
		repo.setWorkflowRunsCount_ghapi(workflow_runs_count);	
	}	
	
    public static void setCommitsCountInRepoDefaultBranchMiner(String token, Repository repo){				
		int commitsCount = GHAPICommitsMiner.countCommitsInRepoDefaultBranch(repo);		
		repo.setCommitsCountInDefaultBranch_ghapi(commitsCount);
   }    
	
	public static void setWorkflowRunsTriggeredByPushOrPrInDefaultBranchCountMiner(String token, Repository repo){
		Integer workflowRunsTriggeredByPushOrPrCount = getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount(token, repo);	
		repo.setWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi(workflowRunsTriggeredByPushOrPrCount);	
	}

	/**
	 * Method to update the information of the repositories in the database
	 * 
	 * This method uses the GitHub API to get several information about the repositories.
	 * The information that are retrieved are:
	 * 1) Number of commits in the default branch of the repository
	 * 2) Number of workflow runs in the default branch of the repository
	 * 3) Number of workflow runs triggered by push or pull request in the default branch of the repository
	 * 4) Whether the repository has a Travis configuration file or not
	 * 5) Whether the repository has a GitHub Actions configuration file or not
	 * 6) General information about the repository (e.g., number of stars, number of forks, etc.) 
	 */
	public static void ghRepositoryInfoMiner(List<Repository> repositories){
		HibernateUtil.beginTransaction();
		RepositoryDAO repositoryDAO = new HibernateRepositoryDAO();

		int count = 0;
		for(Repository repo: repositories){					
						
			try {		
				GHAPIRepositoryMiner.setContributorsCount(repo);
				GHAPIRepositoryMiner.setReleasesCount(repo);
				GHAPIRepositoryMiner.setGitHubRepoGeneralInfoMinner(repo);
				GHAPIRepositoryMiner.setWorkflowRunsCountAndDateOfFirstAndLastRunFromGitHubAPI(repo);
				GHAPIRepositoryMiner.setHasTravisConfigurationFileMiner(repo);
				GHAPIRepositoryMiner.setHasGHActionsConfigurationFileMiner(repo);
				GHAPIRepositoryMiner.setWorkflowRunsTriggeredByPushOrPrInDefaultBranchCountMiner(repo);
				GHAPIRepositoryMiner.setCommitsCountInRepoDefaultBranchAfterGHAAdaptionPeriodMiner(repo);
				GHAPIRepositoryMiner.setCommitsCountInRepoDefaultBranchMiner(repo);
				GHAPIRepositoryMiner.setPullRequestsCountMiner(repo);

				repositoryDAO.save(repo);
				System.out.printf("(%s/%s) - %s \n", ++count, repositories.size(), repo.getFullName());				
				System.out.printf("contributors: %s \n", repo.getTotalContributorsGitHubPage());
				System.out.printf("releases: %s \n", repo.getTotalReleasesGitHubPage());
				System.out.printf("hasGHAConfFile: %s, Workflows: %d, Runs: %d, PRs: %s \n", repo.getHasGHActionsfigurationFile().toString(), 
																	  repo.getWorkflowsCountGHApi(),
																	  repo.getWorkflowRunsCount_ghapi(),
																	  repo.getPullRequestsCount_ghapi());
			} catch (RuntimeException e) {						
				e.printStackTrace();
			}									

			if(count % 5 == 0){
				HibernateUtil.commitTransaction();
				HibernateUtil.beginTransaction();				
			}
		}
		HibernateUtil.commitTransaction();
	}

	private static void setGitHubRepoGeneralInfoMinner(Repository repo){

		try {
			String entryPoint = String.format("https://api.github.com/repos/%s", repo.getFullName());
			
			HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 5);						
					
			if(conn.getResponseCode() >= 400 && conn.getResponseCode() < 500) {										
				System.out.println(" - REPOSITORY DATA NOT FOUND!");
			}else if(conn.getResponseCode() == 200){
				System.out.println(" - REPOSITORY DATA RETRIEVED!");

				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				Gson gson = new Gson();
				JsonObject json = gson.fromJson(reader, JsonObject.class);

				Date created_at = null;
				Date updated_at = null;
				Date pushed_at = null;

				if (!json.get("created_at").isJsonNull()){
					try {
						created_at = simpleDateFormat.parse(json.get("created_at").getAsString());
					} catch (ParseException e) { 
						e.printStackTrace();
					}
				}
				if (!json.get("updated_at").isJsonNull()){
					try {
						updated_at = simpleDateFormat.parse(json.get("updated_at").getAsString());
					} catch (ParseException e) { 
						e.printStackTrace();
					}
				}
				if (!json.get("pushed_at").isJsonNull()){
					try {
						pushed_at = simpleDateFormat.parse(json.get("pushed_at").getAsString());
					} catch (ParseException e) { 
						e.printStackTrace();
					}
				}

				Long ghId = json.get("id").isJsonNull()? null: json.get("id").getAsLong();
				String urlAdress = json.get("url").isJsonNull()? null: json.get("url").getAsString();
				String html_url = json.get("html_url").isJsonNull()? null: json.get("html_url").getAsString();				
				String visibility = json.get("visibility").isJsonNull()? null: json.get("visibility").getAsString();				
				Boolean isPrivate = json.get("private").isJsonNull()? null: json.get("private").getAsBoolean();
				String description = json.get("description").isJsonNull()? null: json.get("description").getAsString();
				Long size = json.get("size").isJsonNull()? null: json.get("size").getAsLong();
				Integer forks = json.get("forks").isJsonNull()? null: json.get("forks").getAsInt();
				Integer open_issues = json.get("open_issues").isJsonNull()? null: json.get("open_issues").getAsInt();
				Integer watchers = json.get("watchers").isJsonNull()? null: json.get("watchers").getAsInt();		
				String default_branch = json.get("default_branch").isJsonNull()? null: json.get("default_branch").getAsString();
				String language = json.get("language").isJsonNull()? null: json.get("language").getAsString();
				Boolean fork = json.get("fork").isJsonNull()? null: json.get("fork").getAsString().equals("true"); 

				String license = json.get("license").isJsonNull()? null: json.getAsJsonObject("license").get("name").getAsString();

				repo.setGhId(ghId);
				repo.setCreated_at(created_at);
				repo.setUpdated_at(updated_at);
				repo.setPushed_at(pushed_at);
				repo.setDefaultBranch(default_branch);
				repo.setLanguage(language);
				repo.setLicense(license);
				repo.setWatchers(watchers);
				repo.setOpen_issues(open_issues);
				repo.setForks(forks);
				repo.setSize(size);
				repo.setDescription(description);
				repo.setIsPrivate(isPrivate);
				repo.setUrl(urlAdress);					
				repo.setFork(fork);
				repo.setHtmlUrl(html_url);
				repo.setVisibility(visibility);				
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}			
	}

	// public static void updateDateOfFirstAndLastWorkflowRun(List<Repository> repositories){
	// 	RepositoryDAO repositoryDAO = new HibernateRepositoryDAO();
	// 	HibernateUtil.beginTransaction();

	// 	for (int i = 0; i < repositories.size(); i++) {
	// 		Repository repository = repositories.get(i);

	// 		if((repository.getWorkflowRunsCount_ghapi() == null || 
	// 		   repository.getWorkflowRunsCount_ghapi() == 0)){
	// 			System.out.printf("(%s/%s) %s - NO WORKFLOW RUNS\n",
	// 										i+1, repositories.size(),
	// 										repository.getFullName());
	// 			continue;
	// 		}

	// 		if(repository.getWorkflowRunsCount_ghapi() != null &&
	// 		   repository.getWorkflowRunsCount_ghapi() > 0 &&
	// 		   repository.getFirstWorkflowRunInDefaultBranchAt() != null){
	// 			System.out.printf("(%s/%s) %s - ALREADY UPDATED\n",
	// 										i+1, repositories.size(),
	// 										repository.getFullName());
	// 			continue;
	// 		}

	// 		System.out.printf("(%s/%s) %s",
	// 									i+1, repositories.size(),
	// 									repository.getFullName());

	// 		Date firstWorkflowRunAt = null,
	// 			 lastWorkflowRunAt = null;
				 
	// 		Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo ORDER BY createdAt");
	// 		query.setParameter("repo", repository);
	// 		query.setMaxResults(1);
			
	// 		try {				
	// 			firstWorkflowRunAt = ((WorkflowRun) query.uniqueResult()).getCreatedAt();
	// 		} catch (Exception e) {
	// 			// TODO: handle exception
	// 		}

	// 		WorkflowRun lastWorkflowRun = null;
	// 		query = HibernateUtil.getSession().createQuery("from gha_workflowRun where repo = :repo ORDER BY createdAt DESC");
	// 		query.setParameter("repo", repository);
	// 		query.setMaxResults(1);
			
	// 		try {				
	// 			lastWorkflowRunAt = ((WorkflowRun) query.uniqueResult()).getCreatedAt();
	// 		} catch (Exception e) {
	// 			// TODO: handle exception
	// 		}
			
	// 		Integer firstAndLastWorkflowRunIntervalInDays = null;
			
	// 		if(firstWorkflowRunAt != null && lastWorkflowRunAt != null){				
	// 			long timeDiff = Math.abs(lastWorkflowRunAt.getTime() - firstWorkflowRunAt.getTime());
	// 			firstAndLastWorkflowRunIntervalInDays = (int) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
	// 		}
			
	// 		System.out.printf(" - First Run At: %s - Last Run At: %s\n", 							 
	// 						  firstWorkflowRunAt, lastWorkflowRunAt);

	// 		repository.setFirstWorkflowRunInDefaultBranchAt(firstWorkflowRunAt);
	// 		repository.setLastWorkflowRunInDefaultBranchAt(lastWorkflowRunAt);
	// 		repository.setFirstAndLastWorkflowRunInDefaultBranchIntervalInDays(firstAndLastWorkflowRunIntervalInDays);
	// 		repositoryDAO.save(repository);							

	// 		if(i+1 % 50 == 0){
	// 			HibernateUtil.commitTransaction();
	// 			HibernateUtil.beginTransaction();				
	// 		}
	// 	}
	// 	HibernateUtil.commitTransaction();
	// }

	public static void updateDateOfFirstAndLasCItWorkflowRun(List<Repository> repositories){
		RepositoryDAO repositoryDAO = new HibernateRepositoryDAO();
		HibernateUtil.beginTransaction();

		for (int i = 0; i < repositories.size(); i++) {
			Repository repository = repositories.get(i);

			if((repository.getWorkflowRunsCount_ghapi() == null || 
			   repository.getWorkflowRunsCount_ghapi() == 0)){
				System.out.printf("(%s/%s) %s - NO WORKFLOW RUNS\n",
											i+1, repositories.size(),
											repository.getFullName());
				continue;
			}

			if(repository.getWorkflowRunsCount_ghapi() != null &&
			   repository.getWorkflowRunsCount_ghapi() > 0 &&
			   repository.getFirstCIWorkflowRunAt() != null){
				System.out.printf("(%s/%s) %s - ALREADY UPDATED\n",
											i+1, repositories.size(),
											repository.getFullName());
				continue;
			}

			System.out.printf("(%s/%s) %s",
										i+1, repositories.size(),
										repository.getFullName());

			Date firstWorkflowRunAt = null,
				 lastWorkflowRunAt = null;
				 
			Query query = HibernateUtil.getSession().createQuery("from gha_workflowRun run " + 
														   "where repo = :repo and " + 
														   "true = (SELECT w.isCiWorkflow FROM Workflow w WHERE w = run.workflow) " +
														   "ORDER BY createdAt");
			query.setParameter("repo", repository);
			query.setMaxResults(1);
			
			try {				
				firstWorkflowRunAt = ((WorkflowRun) query.uniqueResult()).getCreatedAt();
			} catch (Exception e) {
				// TODO: handle exception
			}

			WorkflowRun lastWorkflowRun = null;
			query = HibernateUtil.getSession().createQuery("from gha_workflowRun run " + 
														   "where repo = :repo and " + 
														   "true = (SELECT w.isCiWorkflow FROM Workflow w WHERE w = run.workflow) " +
														   "ORDER BY createdAt DESC");
			query.setParameter("repo", repository);
			query.setMaxResults(1);
			
			try {				
				lastWorkflowRunAt = ((WorkflowRun) query.uniqueResult()).getCreatedAt();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			Integer firstAndLastWorkflowRunIntervalInDays = null;
			
			if(firstWorkflowRunAt != null && lastWorkflowRunAt != null){				
				long timeDiff = Math.abs(lastWorkflowRunAt.getTime() - firstWorkflowRunAt.getTime());
				firstAndLastWorkflowRunIntervalInDays = (int) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
			}
			
			System.out.printf(" - First Run At: %s - Last Run At: %s\n", 							 
							  firstWorkflowRunAt, lastWorkflowRunAt);

			repository.setFirstCIWorkflowRunAt(firstWorkflowRunAt);
			repository.setLastCIWorkflowRunAt(lastWorkflowRunAt);
			repository.setFirstAndLastCIWorkflowRunIntervalInDays(firstAndLastWorkflowRunIntervalInDays);
			repositoryDAO.save(repository);							

			if(i+1 % 10 == 0){
				HibernateUtil.commitTransaction();
				HibernateUtil.beginTransaction();				
			}
		}
		HibernateUtil.commitTransaction();
	}

	public static void updateDateOfFirstAndLastTravisBuild(List<Repository> repositories){
		RepositoryDAO repositoryDAO = new HibernateRepositoryDAO();
		HibernateUtil.beginTransaction();

		for (int i = 0; i < repositories.size(); i++) {
			Repository repository = repositories.get(i);
			List<TravisBuild> travisBuilds = repository.getTravisBuilds();
			travisBuilds = travisBuilds.stream().filter(build -> build.getStartedAt() != null).collect(Collectors.toList());
			Collections.sort(travisBuilds, (b1, b2) -> b1.getStartedAt().compareTo(b2.getStartedAt()));
			Date firstTravisCIBuildIn = null,
				 lastTravisCIBuildIn = null;
			Integer firstAndLastTravisBuildIntervalInDays = null;
			
			if (!travisBuilds.isEmpty()){
				firstTravisCIBuildIn = null;				
				int index = 0;
				while(firstTravisCIBuildIn == null && index < travisBuilds.size()){
					firstTravisCIBuildIn = travisBuilds.get(index).getStartedAt();
					index++;
				}

				lastTravisCIBuildIn = null;
				index = travisBuilds.size() - 1;
				while(lastTravisCIBuildIn == null && index >= 0){
					lastTravisCIBuildIn = travisBuilds.get(index).getStartedAt();
					index--;
				}								
			}
			
			if(firstTravisCIBuildIn != null && lastTravisCIBuildIn != null){				
				long timeDiff = Math.abs(lastTravisCIBuildIn.getTime() - firstTravisCIBuildIn.getTime());
				firstAndLastTravisBuildIntervalInDays = (int) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
			}
			
			System.out.printf("(%s/%s) %s - First Build In: %s - Last Build In: %s\n", 
							  i+1, repositories.size(),
							  repository.getFullName(), 
							  firstTravisCIBuildIn, lastTravisCIBuildIn);

			repository.setFirstTravisCIBuildIn(firstTravisCIBuildIn);
			repository.setLastTravisCIBuildIn(lastTravisCIBuildIn);
			repository.setFirstAndLastTravisBuildIntervalInDays(firstAndLastTravisBuildIntervalInDays);
			repositoryDAO.save(repository);							

			if(i+1 % 50 == 0){
				HibernateUtil.commitTransaction();
				HibernateUtil.beginTransaction();				
			}
		}
		HibernateUtil.commitTransaction();
	}

	public static void main(String[] args) {

		/*
		 * This class is responsible for mining information from GitHub API for the 
		 * repositories of the dataset imported from the paper "Characterizing the usage of CI tools in ML projects"
		 * 
		 *  The mining process is divided into two steps:
		 * 1) Get the list of repositories that have not been mined yet
		 * 2) For each repository, get the information from GitHub API
		 * 
		 * The mining was performed in 2023-05-05
		 * - getRepositoryInfoFromGitHubAPI: 1st step
		 * - The class LOCMetricsMiner was used to get the LOC metrics for each repository		
		 */
		
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();					
		Query query = HibernateUtil.getSession().createQuery("FROM repository WHERE size IS NOT NULL AND pullRequestsCount_ghapi IS NULL");

		// Query query = HibernateUtil.getSession().createQuery("FROM repository WHERE isPartOfFinalStudyDataset = true AND totalContributorsGitHubPage IS NULL");
		// Query query = HibernateUtil.getSession().createQuery("FROM repository WHERE isPartOfFinalStudyDataset = true AND totalReleasesGitHubPage = 1");
		List<Repository> repositories = query.list();												
						
		// mine information from GitHub API for each repository	
		System.out.println("REPOSITORIES COUNT: " + repositories.size());
		ghRepositoryInfoMiner(repositories);		
		// updateDateOfFirstAndLasCItWorkflowRun(repositories);		
		System.exit(0);
	}
}