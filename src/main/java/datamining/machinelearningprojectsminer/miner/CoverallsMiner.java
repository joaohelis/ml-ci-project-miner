/**
 * 
 */
package datamining.machinelearningprojectsminer.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.config.Config;
import datamining.machinelearningprojectsminer.dao.CoverageBuildDAO;
import datamining.machinelearningprojectsminer.dao.CoverallsRepoInfoDAO;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateCoverallsBuildDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateCoverallsRepoInfoDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.models.CoverageBuild;
import datamining.machinelearningprojectsminer.models.CoverallsRepoInfo;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.CoverageBuild.COVERAGE_SERVICE;

public class CoverallsMiner {
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
	private static CoverallsRepoInfo coverallsRepoInfoExtractor(JsonObject json){

		Date created_at = null;
		Date updated_at = null;
		if (!json.get("created_at").isJsonNull())
			try {
				created_at = simpleDateFormat.parse(json.get("created_at").getAsString());
			} catch (ParseException e) { 
				e.printStackTrace();
			}
		if (!json.get("updated_at").isJsonNull())
		try {
			updated_at = simpleDateFormat.parse(json.get("updated_at").getAsString());
		} catch (ParseException e) { 
			e.printStackTrace();
		}
		String service = json.get("service").isJsonNull()? null: json.get("service").getAsString();
		String name = json.get("name").isJsonNull()? null: json.get("name").getAsString();
		Boolean comment_on_pull_requests = json.get("comment_on_pull_requests").isJsonNull()? null: json.get("comment_on_pull_requests").getAsBoolean();
		Boolean send_build_status = json.get("send_build_status").isJsonNull()? null: json.get("send_build_status").getAsBoolean();
		Float commit_status_fail_threshold = json.get("commit_status_fail_threshold").isJsonNull()? null: json.get("commit_status_fail_threshold").getAsFloat();
		Float commit_status_fail_change_threshold = json.get("commit_status_fail_change_threshold").isJsonNull()? null: json.get("commit_status_fail_change_threshold").getAsFloat();		

		CoverallsRepoInfo coverallsRepoInfo = new CoverallsRepoInfo();
		coverallsRepoInfo.setService(service);
		coverallsRepoInfo.setName(name);
		coverallsRepoInfo.setComment_on_pull_requests(comment_on_pull_requests);
		coverallsRepoInfo.setSend_build_status(send_build_status);
		coverallsRepoInfo.setCommit_status_fail_change_threshold(commit_status_fail_change_threshold);
		coverallsRepoInfo.setCommit_status_fail_threshold(commit_status_fail_threshold);
		coverallsRepoInfo.setCreated_at(created_at);
		coverallsRepoInfo.setUpdated_at(updated_at);

		return coverallsRepoInfo;
	}
	
	private static CoverageBuild coverallsBuildExtractor(JsonObject json) {
								
		Date created_at = null;
		if (!json.get("created_at").isJsonNull())
			try {
				created_at = simpleDateFormat.parse(json.get("created_at").getAsString());
			} catch (ParseException e) { 
				e.printStackTrace();
			}
		String url = json.get("url").isJsonNull()? null: json.get("url").getAsString();
		String commit_message = json.get("commit_message").isJsonNull()? null: json.get("commit_message").getAsString();
		String branch = json.get("branch").isJsonNull()? null: json.get("branch").getAsString();
		String committer_name = json.get("committer_name").isJsonNull()? null: json.get("committer_name").getAsString();
		String committer_email = json.get("committer_email").isJsonNull()? null: json.get("committer_email").getAsString();
		String commit_sha = json.get("commit_sha").isJsonNull()? null: json.get("commit_sha").getAsString();
		String repo_name = json.get("repo_name").isJsonNull()? null: json.get("repo_name").getAsString();
		String badge_url = json.get("badge_url").isJsonNull()? null: json.get("badge_url").getAsString();
		Float coverage_change = json.get("coverage_change").isJsonNull()? null: json.get("coverage_change").getAsFloat();
		Float covered_percent = json.get("covered_percent").isJsonNull()? null: json.get("covered_percent").getAsFloat();
		
		CoverageBuild build = new CoverageBuild();
		build.setCreated_at(created_at);
		build.setUrl(url);
		build.setCommit_message(commit_message);
		build.setBranch(branch);
		build.setCommitter_name(committer_name);
		build.setCommitter_email(committer_email);
		build.setCommit_sha(commit_sha);
		build.setRepo_name(repo_name);
		build.setBadge_url(badge_url);
		build.setCoverage_change(coverage_change);
		build.setCovered_percent(covered_percent);
		build.setCoverage_service(COVERAGE_SERVICE.COVERALLS);
		
		return build;
	}

	public static void coverallsRepoInfoDataMiner() throws IOException {		

		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
		CoverallsRepoInfoDAO coverallsRepoInfoDAO = new HibernateCoverallsRepoInfoDAO();				

		List<Repository> repositories = repoDAO.listAll();

		System.out.println(repositories.size());

		int count = 0;		
		HibernateUtil.beginTransaction();		

		for(Repository repo: repositories){

			String repoName = repo.getFullName();
							
			String entryPoint = String.format("https://coveralls.io/api/repos/github/%s", repoName);
			URL url = new URL(entryPoint);

			System.out.println("-----------------------------------------------------------");
			System.out.printf("%s / %s -- %s -- Loading Coveralls Repo Info --> ", ++count, repositories.size(), repoName);			

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();												
			conn.addRequestProperty("Content-Type", "application/json");
			conn.addRequestProperty("Authorization", "token " + Config.coverallsToken);

			if(conn.getResponseCode() == 404) {
				System.out.println("NO DATA FOR THE PROJECT!");								
				repo.setStartedWithCoveralls(null);
				repoDAO.save(repo);				
			}else{
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				Gson gson = new Gson();
				JsonObject json = gson.fromJson(reader, JsonObject.class);

				CoverallsRepoInfo coverallsRepoInfo = coverallsRepoInfoExtractor(json);
				coverallsRepoInfo.setRepo(repo);
				coverallsRepoInfoDAO.save(coverallsRepoInfo);

				repo.setStartedWithCoveralls(coverallsRepoInfo.getCreated_at());
				repoDAO.save(repo);

				System.out.println("DATA SUCESSFULLY RETRIEVED!");
			}
						
			if(count % 50 == 0){
				HibernateUtil.commitTransaction();
				HibernateUtil.beginTransaction();				
			}
		}	
		HibernateUtil.commitTransaction();					
	}
	
	public static void coverallsBuildsMiner() throws IOException {

		RepositoryDAO repoDAO = new HibernateRepositoryDAO();	
		CoverageBuildDAO coverallsBuildDAO = new HibernateCoverallsBuildDAO();	
		
		List<Repository> repositories =  repoDAO.listAll()
												.stream()
												.filter(r -> r.getStartedWithCoveralls() != null)
												.collect(Collectors.toList());
																
		int count = 1;

		for(Repository repo: repositories){

			String repoName = repo.getFullName();												
																			
			boolean repoBuildInfoAvailable = true;
			int page = 1;			
			Integer totalBuilds = null;
			
			int buildsCount = 1;
									
			try {					

				System.out.println("-----------------------------------------------------------");
				System.out.printf("%s / %s -- %s -- Loading Coveralls Repo Builds Info --> ", count++, repositories.size(), repoName);

				HibernateUtil.beginTransaction();
				
				while(repoBuildInfoAvailable) {
			  				
					String entryPoint = String.format("https://coveralls.io/github/%s.json?page=%s", repoName, page);
					URL url = new URL(entryPoint);

					HttpURLConnection conn = (HttpURLConnection) url.openConnection();												
					conn.addRequestProperty("Content-Type", "application/json");
					conn.addRequestProperty("Authorization", "token " + Config.coverallsToken);
										
					if(conn.getResponseCode() == 404) {
						System.out.println(" REPOSITORY BUILD INFO NOT FOUND");
						repoBuildInfoAvailable = false;																					
					}else{
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						Gson gson = new Gson();
						JsonObject json = gson.fromJson(reader, JsonObject.class);

						JsonArray builds = json.getAsJsonArray("builds");
						totalBuilds = json.get("total").getAsInt();

						if(builds.isEmpty()){
							// PAGINATION HAS ENDDED
							System.out.println(" NO BUILD TO PROCESS");
							repoBuildInfoAvailable = false;							
						}else{
																																																																																			
							for(int i = 0; i < builds.size(); i++) {
								JsonObject buildJson = builds.get(i).getAsJsonObject();						
								CoverageBuild coverallsBuild = coverallsBuildExtractor(buildJson);
								coverallsBuild.setRepo(repo);
								coverallsBuildDAO.save(coverallsBuild);
								System.out.printf("%s / %s ", buildsCount++, totalBuilds);
								System.out.println(coverallsBuild);													
							}						
						}
						page++;
					}					
					// Commits the transaction after the reading of each page with 5 builds
					HibernateUtil.commitTransaction();
					HibernateUtil.beginTransaction();													
				}
				
				repo.setCoverallsBuildsCount(totalBuilds);
				repoDAO.save(repo);
				
				HibernateUtil.commitTransaction();
			}catch (Exception e) {
				e.printStackTrace();
			}			
		}		
	}
		
	public static void main(String[] args) {

		/**
		 * The coverals data for the repositories are retrieved in two steps:
		 * 1) The first step is to retrieve the repository info, which contains the date when the repository was added to coveralls.
		 * 2) The second step is to retrieve the builds info, which contains the date when the build was added to coveralls.
		 * 
		 * The coveralls data was retrived in 2023-05-06
		 */

		try {
			coverallsRepoInfoDataMiner();
			coverallsBuildsMiner();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.exit(0);
	}
	
}