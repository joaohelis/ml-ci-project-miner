/**
 * 
 */
package datamining.machinelearningprojectsminer.miner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.config.Config;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.TravisBuildDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateTravisBuildDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.models.Repository;
import datamining.machinelearningprojectsminer.models.TravisBuild;

public class TravisBuildMiner {

	private static SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					
	private static TravisBuild travisBuildExtractor(JsonObject json) {

		Date startedAt = null;
		Date finishedAt = null;

		try {
			startedAt = json.get("started_at").isJsonNull()? null: ft.parse(json.get("started_at").getAsString());	
		} catch (Exception e) {			
			e.printStackTrace();
		}

		try {
			finishedAt = json.get("finished_at").isJsonNull()? null: ft.parse(json.get("finished_at").getAsString());	
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		TravisBuild build = new TravisBuild(
			json.get("id").isJsonNull()? null: json.get("id").getAsLong(), 
			json.get("number").isJsonNull()? null: json.get("number").getAsString(), 
			startedAt, 
			finishedAt, 
			json.get("duration").isJsonNull()? null: json.get("duration").getAsInt(),
			json.get("event_type").isJsonNull()? null: json.get("event_type").getAsString(),
			json.get("previous_state").isJsonNull()? null: json.get("previous_state").getAsString(),
			json.get("pull_request_number").isJsonNull()? null: json.get("pull_request_number").getAsInt(),
			json.get("priority").isJsonNull()? null: json.get("priority").getAsBoolean(),
			json.getAsJsonObject("commit").get("sha").getAsString(),
			json.get("state").isJsonNull()? null: json.get("state").getAsString(),
			json.getAsJsonObject("branch").get("name").getAsString());			
		return build;
	}
	
	public static String convertRepoNameToStandardURLEncoding(String repoName) {		
		return URLEncoder.encode(repoName);
	}
	
	public static void travisBuildMiner(List<Repository> repositories) {				
				
		String travisApiURL = "https://api.travis-ci.com";	
		
		int projectIndex = 0;
		for(Repository repo: repositories) {

			projectIndex++;
			
			String entryPoint = String.format("/repo/%s/builds?limit=100&sort_by=number:asc", convertRepoNameToStandardURLEncoding(repo.getFullName()));
			
			boolean paginationIsLast = false;
			boolean repoBuildInfoAvailable = true;

			RepositoryDAO repositoryDAO = new HibernateRepositoryDAO();
			TravisBuildDAO travisBuildDAO = new HibernateTravisBuildDAO();


			HashMap<Long, TravisBuild> travisBuildIDMap = new HashMap<>();
			repo.getTravisBuilds().forEach(build -> travisBuildIDMap.put(build.getId(), build));

			HibernateUtil.beginTransaction();
											
			try {							
				
				int count = 0;
				while(!paginationIsLast && repoBuildInfoAvailable) {
			  				
					URL url = new URL(travisApiURL + entryPoint);	
					
					System.out.println(url);
	
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();												
					conn.addRequestProperty("Travis-API-Version", "3");
					conn.addRequestProperty("Authorization", "token " + Config.travisToken);
													
					System.out.printf("(%s/%s) Repository[fullName='%s']", projectIndex, repositories.size(), repo.getFullName());
					
					if(conn.getResponseCode() == 404) {
						System.out.println(" --- REPOSITORY NOT FOUND");
						repoBuildInfoAvailable = false;
						repo.setTravisBuildsCount(0);
						repositoryDAO.save(repo);
						continue;
					}								
			
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					Gson gson = new Gson();
					JsonObject json = gson.fromJson(reader, JsonObject.class);														
					
					int totalBuilds = json.getAsJsonObject("@pagination").get("count").getAsInt();
					paginationIsLast = json.getAsJsonObject("@pagination").get("is_last").getAsBoolean();
					
					if(!paginationIsLast) {
						entryPoint = json.getAsJsonObject("@pagination")
													.getAsJsonObject("next")
													.get("@href").getAsString();
					}
					
					System.out.println(" --- TOTAL BUILDS: " + totalBuilds);

					if(repo.getTravisBuildsCount() == null){
						repo.setTravisBuildsCount(totalBuilds);
						repositoryDAO.save(repo);
					}
																					
					JsonArray builds = json.getAsJsonArray("builds");					
										
					for(int i = 0; i < builds.size(); i++) {
						JsonObject buildJson = builds.get(i).getAsJsonObject();																		
						try {
							count++;
							TravisBuild build = null;
							if(!buildJson.get("id").isJsonNull() && travisBuildIDMap.containsKey(buildJson.get("id").getAsLong())) {
								// the build was already processed and is in the database
								build = travisBuildIDMap.get(buildJson.get("id").getAsLong());
							}else{
								// the build was not processed yet
								build = travisBuildExtractor(buildJson);
								build.setRepo(repo);
								travisBuildDAO.save(build);
							}							
							System.out.printf("(%s/%s) -- %s -- ", projectIndex, repositories.size(), repo.getFullName());												
							System.out.printf("%s/%s ---> TravisBuild [number=%s, state=%s]\n", count, totalBuilds, build.getNumber(), build.getState());							
						}catch (Exception ex) {
							System.out.println("BUILD EXTRACTOR ERROR");							
						}
					}										
					HibernateUtil.commitTransaction();
					HibernateUtil.beginTransaction();
				}				
			}catch (Exception e) {
				e.printStackTrace();
			}
			HibernateUtil.commitTransaction();
		}		
	}

	public static void main(String[] args) {

		/**
		 * The following code is used to extract Travis CI builds information from the Travis API.
		 * 
		 * The data was extracted in 2023-05-07.
		 */

		RepositoryDAO repositoryDAO = new HibernateRepositoryDAO();
		List<Repository> repositories = repositoryDAO.listAll();
		
		// repositories =  repositories
		// 						.stream()
		// 						.filter(r -> r.getTravisBuildsCount() == null).collect(Collectors.toList());		
		
		travisBuildMiner(repositories);		
		System.exit(0);
	}
}