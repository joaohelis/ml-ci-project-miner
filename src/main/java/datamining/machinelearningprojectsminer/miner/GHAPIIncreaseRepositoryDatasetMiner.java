/**
 * 
 */
package datamining.machinelearningprojectsminer.miner;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.models.Repository;

public class GHAPIIncreaseRepositoryDatasetMiner {

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

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
		
		RepositoryDAO repositoryDAO = new HibernateRepositoryDAO();

		HibernateUtil.beginTransaction();

		int count = 0;
		for(Repository repo: repositories){		
			if(repo.getCommitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi() != null){
				System.out.printf("(%s/%s) - %s \n", ++count, repositories.size(), repo.getFullName());
				continue;
			}	
			try {								
				GHAPIRepositoryMiner.setWorkflowRunsCountAndDateOfFirstAndLastRunFromGitHubAPI(repo);
				GHAPIRepositoryMiner.setHasTravisConfigurationFileMiner(repo);
				GHAPIRepositoryMiner.setHasGHActionsConfigurationFileMiner(repo);
				GHAPIRepositoryMiner.setWorkflowRunsTriggeredByPushOrPrInDefaultBranchCountMiner(repo);
				GHAPIRepositoryMiner.setCommitsCountInRepoDefaultBranchAfterGHAAdaptionPeriodMiner(repo);
				GHAPIRepositoryMiner.setCommitsCountInRepoDefaultBranchMiner(repo);
				repositoryDAO.save(repo);
				System.out.printf("(%s/%s) - %s \n", ++count, repositories.size(), repo.getFullName());				
				System.out.printf("hasGHAConfFile: %s, Workflows: %d, runs: %d, Commits: %d, CommitsAfterGHA: %s \n", repo.getHasGHActionsfigurationFile().toString(), 
																	  repo.getWorkflowsCountGHApi(),
																	  repo.getWorkflowRunsCount_ghapi(),
																	  repo.getCommitsCountInDefaultBranch_ghapi(),
																	  repo.getCommitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi());
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

	public static void ghSearchNewNonMLReposMiner(){    

		RepositoryDAO repoDao = new HibernateRepositoryDAO();

		// list of repositories in the database for ignoring them in the search
		Query query = HibernateUtil.getSession().createQuery(" SELECT fullName FROM repository");			
		List<String> repoNames = query.list();	

		int maxNumberOfRepoInSearch = 350;
		
		try {																						
			int lastPage = maxNumberOfRepoInSearch / 100;
			if(maxNumberOfRepoInSearch % 100 != 0){
				lastPage++;
			}
		
			int page = 1;

			// System.out.printf("PROJ %s/%s #%s - Page %s/%s\n", count, repositories.size(), repo.getId(), lastPage - page + 1, lastPage);
			int repoCount = 0;
			boolean breakPagination = false;
			while(page <= lastPage && !breakPagination){   

				HibernateUtil.beginTransaction();				
									
				String entryPoint = "https://api.github.com/search/repositories?q=stars:%3E=5+forks:%3E=5+pushed:%3E2023-01-01&sort=stars&order=desc&per_page=100&page={page}";				
				entryPoint = entryPoint.replace("{page}", String.valueOf(page));
				
				// System.out.printf("PROJ %s/%s #%s - Page %s/%s\n", count, repositories.size(), repo.getId(), lastPage - page + 1, lastPage);

				HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 5);						
					
				if(conn == null || (conn != null && conn.getResponseCode() != 200)){					
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}				
				
				JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
								.getAsJsonObject();

				JsonArray reposArray = json.get("items").getAsJsonArray();					

				for (JsonElement repoElement : reposArray) {
					
					if(++repoCount > maxNumberOfRepoInSearch){
						breakPagination = true;
						break;
					}

					JsonObject reposJson = repoElement.getAsJsonObject();

					String full_name = reposJson.get("full_name").isJsonNull()? null: reposJson.get("full_name").getAsString();

					if(full_name == null || repoNames.contains(full_name)){
						System.out.printf("Repo %s/%s - %s - ALREADY IN DATABASE\n", repoCount, maxNumberOfRepoInSearch, full_name);						
						continue;
					}

					String default_branch = reposJson.get("default_branch").isJsonNull()? null: reposJson.get("default_branch").getAsString();
					
					if(default_branch == null){
						System.out.printf("Repo %s/%s - %s - NO DEFAULT BRANCH\n", repoCount, maxNumberOfRepoInSearch, full_name);						
						continue;
					}else if(!(default_branch.equals("master") || default_branch.equals("main"))){
						System.out.printf("Repo %s/%s - %s - DEFAULT BRANCH IS NOT MASTER OR MAIN\n", repoCount, maxNumberOfRepoInSearch, full_name);						
						continue;
					}

					Date created_at = null;
					Date updated_at = null;
					Date pushed_at = null;

					if (!reposJson.get("created_at").isJsonNull()){
						try {
							created_at = simpleDateFormat.parse(reposJson.get("created_at").getAsString());
						} catch (ParseException e) { 
							e.printStackTrace();
						}
					}
					if (!reposJson.get("updated_at").isJsonNull()){
						try {
							updated_at = simpleDateFormat.parse(reposJson.get("updated_at").getAsString());
						} catch (ParseException e) { 
							e.printStackTrace();
						}
					}
					if (!reposJson.get("pushed_at").isJsonNull()){
						try {
							pushed_at = simpleDateFormat.parse(reposJson.get("pushed_at").getAsString());
						} catch (ParseException e) { 
							e.printStackTrace();
						}
					}

					Long ghId = reposJson.get("id").isJsonNull()? null: reposJson.get("id").getAsLong();									
					String urlAdress = reposJson.get("url").isJsonNull()? null: reposJson.get("url").getAsString();					
					String html_url = reposJson.get("html_url").isJsonNull()? null: reposJson.get("html_url").getAsString();				
					String visibility = reposJson.get("visibility").isJsonNull()? null: reposJson.get("visibility").getAsString();				
					Boolean isPrivate = reposJson.get("private").isJsonNull()? null: reposJson.get("private").getAsBoolean();
					String description = reposJson.get("description").isJsonNull()? null: reposJson.get("description").getAsString();
					Long size = reposJson.get("size").isJsonNull()? null: reposJson.get("size").getAsLong();
					Integer forks = reposJson.get("forks").isJsonNull()? null: reposJson.get("forks").getAsInt();
					Integer open_issues = reposJson.get("open_issues").isJsonNull()? null: reposJson.get("open_issues").getAsInt();
					Integer watchers = reposJson.get("watchers").isJsonNull()? null: reposJson.get("watchers").getAsInt();		
					// Integer network_count = reposJson.get("network_count").isJsonNull()? null: reposJson.get("network_count").getAsInt();							
					String language = reposJson.get("language").isJsonNull()? null: reposJson.get("language").getAsString();
					Boolean fork = reposJson.get("fork").isJsonNull()? null: reposJson.get("fork").getAsBoolean();		

					String license = reposJson.get("license").isJsonNull()? null: reposJson.getAsJsonObject("license").get("name").getAsString();

					String name = null;
					String owner = null;
					if(full_name != null){
						owner = full_name.split("/")[0];
						name = full_name.split("/")[1];						
					}

					Repository repo = new Repository();
					repo.setGhId(ghId);
					repo.setFullName(full_name);
					repo.setName(name);
					repo.setOwner(owner);
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
					// repo.setNetwork_count(network_count);	
					repo.setFork(fork);
					repo.setHtmlUrl(html_url);
					repo.setVisibility(visibility);
					repo.setOutOfMLDataset(true);
					repoDao.save(repo);
					
					System.out.printf("Repo %s/%s - %s Watchers: %s - SAVED\n", repoCount, maxNumberOfRepoInSearch, full_name, watchers);
				}
				HibernateUtil.commitTransaction(); // commit after read 100 workflows runs					
				page++;																							
			}
			HibernateUtil.beginTransaction();
			HibernateUtil.commitTransaction();														
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		// To do: Read the paper "Characterizing Travis CI builds for GitHub repositories"

		/*
		 * This class is responsible for mining information from GitHub API to the 
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
			
		// Query query = HibernateUtil.getSession().createQuery("from repository where isOutOfMLDataset = true");					
		// List<Repository> repositories = query.list();	
				
		// repositories = repositories.subList(260, repositories.size());

		List<Repository> repositories = new HibernateRepositoryDAO().listAll();

		Collections.reverse(repositories);

		System.out.println("Repositories: " + repositories.size());		

		// ghSearchNewNonMLReposMiner();
		ghRepositoryInfoMiner(repositories);
		
		// LOCMetricMinerCodeTabs.locMetricRepoMiner(repositories);
		// LOCMetricMinerCodeTabs.calculateRepositorySLOC(repositories);

		// GHAPIWorkflowMiner.ghRepositoriesWorkflowsMiner(repositories);
		// GHAPIWorkflowMiner.setJobAndStepsCountToRepositoriesWorkflows(repositories);
		// WorkflowChecker.repositoriesWorkflowCIChecker(repositories);

		System.exit(0);
	}
}