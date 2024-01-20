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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.config.Config;
import datamining.machinelearningprojectsminer.dao.CodecovRepoInfoDAO;
import datamining.machinelearningprojectsminer.dao.CoverageBuildDAO;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateCodecovRepoInfoDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateCoverallsBuildDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.models.CodecovRepoInfo;
import datamining.machinelearningprojectsminer.models.CoverageBuild;
import datamining.machinelearningprojectsminer.models.CoverageBuild.COVERAGE_SERVICE;
import datamining.machinelearningprojectsminer.models.Repository;

public class CodecovMiner {
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
	private static CodecovRepoInfo codecovRepoInfoExtractor(JsonObject json){

		String name = json.get("name").isJsonNull()? null: json.get("name").getAsString();		
		String branch = json.get("branch").isJsonNull()? null: json.get("branch").getAsString();		
		
		Boolean active = json.get("active").isJsonNull()? null: json.get("active").getAsBoolean();
		Boolean activated = json.get("activated").isJsonNull()? null: json.get("activated").getAsBoolean();

		JsonObject totalsJson = json.get("totals").isJsonNull()? null : json.get("totals").getAsJsonObject();

		Integer files = totalsJson == null || totalsJson.get("files").isJsonNull()? null: totalsJson.get("files").getAsInt();
		Integer lines = totalsJson == null || totalsJson.get("lines").isJsonNull()? null: totalsJson.get("lines").getAsInt();
		Integer hits = totalsJson == null || totalsJson.get("hits").isJsonNull()? null: totalsJson.get("hits").getAsInt();
		Integer misses = totalsJson == null || totalsJson.get("misses").isJsonNull()? null: totalsJson.get("misses").getAsInt();
		Integer partials = totalsJson == null || totalsJson.get("partials").isJsonNull()? null: totalsJson.get("partials").getAsInt();
		Integer branches = totalsJson == null || totalsJson.get("branches").isJsonNull()? null: totalsJson.get("branches").getAsInt();
		Integer methods = totalsJson == null || totalsJson.get("methods").isJsonNull()? null: totalsJson.get("methods").getAsInt();
		Integer sessions = totalsJson == null || totalsJson.get("sessions").isJsonNull()? null: totalsJson.get("sessions").getAsInt();		
		Float coverage = totalsJson == null || totalsJson.get("coverage").isJsonNull()? null: totalsJson.get("coverage").getAsFloat();
										
		CodecovRepoInfo codecovRepoInfo = new CodecovRepoInfo(name, 
			null, 
			branch, active, activated, files, lines, hits, misses, 
			partials, branches, methods, sessions, coverage);
		
		return codecovRepoInfo;
	}
	
	private static CoverageBuild codecovBuildExtractor(JsonObject json) {		
								
		Date created_at = null;
		if (!json.get("timestamp").isJsonNull())
			try {
				String dateString = json.get("timestamp").getAsString();
				String pattern = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}).*Z";
				String replacement = "$1Z";
				dateString = dateString.replaceAll(pattern, replacement);
				created_at = simpleDateFormat.parse(dateString);
			} catch (ParseException e) { 
				e.printStackTrace();
			}
		
		String state = json.get("state").isJsonNull()? null: json.get("state").getAsString();			
		String branch = json.get("branch").isJsonNull()? null: json.get("branch").getAsString();		
		Boolean ci_passed = json.get("ci_passed").isJsonNull()? null: json.get("ci_passed").getAsBoolean();

		String commit_message = json.get("message").isJsonNull()? null: json.get("message").getAsString();
		String commit_sha = json.get("commitid").isJsonNull()? null: json.get("commitid").getAsString();

		JsonObject authorJson = json.get("author").isJsonNull()? null : json.get("author").getAsJsonObject();
		String committer_username = null;
		String committer_name = null;

		if(authorJson != null){
			committer_username = authorJson.get("username").isJsonNull()? null: authorJson.get("username").getAsString();
			committer_name = authorJson.get("name").isJsonNull()? null: authorJson.get("name").getAsString();
		}	
						
		JsonObject totalsJson = json.get("totals").isJsonNull()? null : json.get("totals").getAsJsonObject();
	
		Integer files = totalsJson == null || totalsJson.get("files").isJsonNull()? null: totalsJson.get("files").getAsInt();
		Integer lines = totalsJson == null || totalsJson.get("lines").isJsonNull()? null: totalsJson.get("lines").getAsInt();
		Integer hits = totalsJson == null || totalsJson.get("hits").isJsonNull()? null: totalsJson.get("hits").getAsInt();
		Integer misses = totalsJson == null || totalsJson.get("misses").isJsonNull()? null: totalsJson.get("misses").getAsInt();
		Integer partials = totalsJson == null || totalsJson.get("partials").isJsonNull()? null: totalsJson.get("partials").getAsInt();
		Integer branches = totalsJson == null || totalsJson.get("branches").isJsonNull()? null: totalsJson.get("branches").getAsInt();
		Integer methods = totalsJson == null || totalsJson.get("methods").isJsonNull()? null: totalsJson.get("methods").getAsInt();
		Integer sessions = totalsJson == null || totalsJson.get("sessions").isJsonNull()? null: totalsJson.get("sessions").getAsInt();		
		Integer complexity = totalsJson == null || totalsJson.get("complexity").isJsonNull()? null: totalsJson.get("complexity").getAsInt();		
		Integer complexity_total = totalsJson == null || totalsJson.get("complexity_total").isJsonNull()? null: totalsJson.get("complexity_total").getAsInt();		
		Integer complexity_ratio = totalsJson == null || totalsJson.get("complexity_ratio").isJsonNull()? null: totalsJson.get("complexity_ratio").getAsInt();		
		
		Float coverage = totalsJson == null || totalsJson.get("coverage").isJsonNull()? null: totalsJson.get("coverage").getAsFloat();
		Float diff = totalsJson == null || totalsJson.get("diff").isJsonNull()? null: totalsJson.get("diff").getAsFloat();				
											
		CoverageBuild build = new CoverageBuild();
		build.setCreated_at(created_at);		
		build.setCommit_message(commit_message);
		build.setBranch(branch);
		build.setCi_passed(ci_passed);	
		build.setState(state);

		build.setCommitter_name(committer_name);
		build.setCommitter_username(committer_username);
		build.setCommit_sha(commit_sha);
			
		build.setFiles(files);
		build.setLines(lines);
		build.setHits(hits);
		build.setMisses(misses);
		build.setPartials(partials);
		build.setMethods(methods);
		build.setComplexity(complexity);
		build.setComplexity_ratio(complexity_ratio);
		build.setComplexity_total(complexity_total);
		build.setBranches(branches);
		build.setSessions(sessions);
		build.setCoverage_change(diff);
		build.setCovered_percent(coverage);
			
		build.setCoverage_service(COVERAGE_SERVICE.CODE_COV);
		return build;
	}

	public static void codecovRepoInfoDataMiner(List<Repository> repositories) throws IOException {		

		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
		CodecovRepoInfoDAO codecovRepoInfoDAO = new HibernateCodecovRepoInfoDAO();						

		System.out.println("Repositories size: " + repositories.size());

		int count = 0;
		HibernateUtil.beginTransaction();		

		for(Repository repo: repositories){

			String fullName = repo.getFullName();
			String owner = fullName.split("/")[0];
			String repoName = fullName.split("/")[1];	
							
			String entryPoint = String.format("https://api.codecov.io/api/v2/github/{owner}/repos/{repo}/");
			entryPoint = entryPoint.replace("{owner}", owner);
			entryPoint = entryPoint.replace("{repo}", repoName);
			URL url = new URL(entryPoint);

			System.out.println("-----------------------------------------------------------");
			System.out.printf("%s / %s -- %s -- Loading Codecov Repo Info --> ", ++count, repositories.size(), repo.getFullName());			

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();												
			conn.addRequestProperty("accept", "application/json");
			conn.addRequestProperty("authorization", "Bearer " + Config.codecovToken);
			
			if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.out.println("FAIL WHILE RETRIEVING CODECOV INFO");
			}else{
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				Gson gson = new Gson();
				JsonObject json = gson.fromJson(reader, JsonObject.class);

				CodecovRepoInfo codecovRepoInfo = codecovRepoInfoExtractor(json);								
				codecovRepoInfoDAO.save(codecovRepoInfo);

				repo.setIsCodecovActivated(codecovRepoInfo.getActivated());
				repo.setIsCodecovActive(codecovRepoInfo.getActive());
				repo.setCodecovRepoInfo(codecovRepoInfo);
				repoDAO.save(repo);

				if(codecovRepoInfo.getActivated()){
					System.out.println("THE REPOSITORY HAS CODECOV DATA!");
					System.out.println(codecovRepoInfo);
				}else{
					System.out.println("THE REPOSITORY DOES NOT HAVE CODECOV DATA!");
				}
			}
						
			if(count % 20 == 0){
				HibernateUtil.commitTransaction();
				HibernateUtil.beginTransaction();				
			}
		}	
		HibernateUtil.commitTransaction();					
	}
	
	public static void codecovBuildsMiner(List<Repository> repositories) throws IOException {
		
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();	
		CoverageBuildDAO coverageBuildDAO = new HibernateCoverallsBuildDAO();	
		
		repositories =  repositories.stream()
									.filter(r -> r.getIsCodecovActive() != null)
									.filter(r -> r.getIsCodecovActive() == true)
									.collect(Collectors.toList());
																		
		int count = 1;

		for(Repository repo: repositories){

			String fullName = repo.getFullName();
			String owner = fullName.split("/")[0];
			String repoName = fullName.split("/")[1];												
																			
			boolean repoBuildInfoAvailable = true;
			int page = 1;			
			Integer totalBuilds = null;
			
			int buildsCount = 1;
									
			try {					

				System.out.println("-----------------------------------------------------------");
				System.out.printf("%s / %s -- %s -- Loading CodeCov Repo Builds Info --> ", count++, repositories.size(), repoName);

				HibernateUtil.beginTransaction();
				
				while(repoBuildInfoAvailable) {					

					String entryPoint = "https://api.codecov.io/api/v2/github/{owner}/repos/{repo}/commits/?branch={branch}&page_size=100&page={page}";
					entryPoint = entryPoint.replace("{owner}", owner);
					entryPoint = entryPoint.replace("{repo}", repoName);
					entryPoint = entryPoint.replace("{branch}", repo.getDefaultBranch());
					entryPoint = entryPoint.replace("{page}", Integer.toString(page));
					URL url = new URL(entryPoint);					

					System.out.println(entryPoint);
			  				
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();												
					conn.addRequestProperty("accept", "application/json");
					conn.addRequestProperty("authorization", "Bearer " + Config.codecovToken);
										
					if(conn.getResponseCode() == 404) {
						System.out.println(" REPOSITORY BUILD INFO NOT FOUND");
						repoBuildInfoAvailable = false;																					
					}else{
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						Gson gson = new Gson();
						JsonObject json = gson.fromJson(reader, JsonObject.class);

						totalBuilds = json.get("count").getAsInt();
						String nextPageUrl = json.get("next").isJsonNull()? null : json.get("next").getAsString();

						int totalPages = (int)(totalBuilds / 100);
						if(totalBuilds != 0 && totalBuilds % 100 != 0){
							totalPages++;
						}
						System.out.println("TOTAL PAGES: " + totalPages);

						JsonArray builds = json.getAsJsonArray("results");
						
						if(nextPageUrl == null){
							// PAGINATION HAS ENDDED
							System.out.println(" NO BUILD TO PROCESS");
							repoBuildInfoAvailable = false;							
						}
																																																																																															
						for(int i = 0; i < builds.size(); i++) {
							JsonObject buildJson = builds.get(i).getAsJsonObject();						
							CoverageBuild coverageBuild = codecovBuildExtractor(buildJson);
							coverageBuild.setRepo_name(repo.getFullName());
							coverageBuild.setRepo(repo);
							coverageBuildDAO.save(coverageBuild);
							System.out.printf("%s / %s ", buildsCount++, totalBuilds);
							System.out.println(coverageBuild);													
						}						
						
						page++;
					}					
					// Commits the transaction after the reading of each page with 5 builds
					HibernateUtil.commitTransaction();
					HibernateUtil.beginTransaction();													
				}
				
				repo.setCodecovBuildsCount(totalBuilds);
				repoDAO.save(repo);
				
				HibernateUtil.commitTransaction();
			}catch (Exception e) {
				e.printStackTrace();
			}			
		}		
	}
		
	public static void main(String[] args) {

		RepositoryDAO repoDAO = new HibernateRepositoryDAO();

		/**
		 * The coverals data for the repositories are retrieved in two steps:
		 * 1) The first step is to retrieve the repository info, which contains the date when the repository was added to codecov.
		 * 2) The second step is to retrieve the builds info, which contains the date when the build was added to codecov.
		 * 
		 * The coveralls data was retrived in 2023-05-06
		 */

		long[] repoIDs = {
			22,  123,  331,  420,  474,  493,  518,  538,  575,  582,  584,  620,  653,  707,  777,  950,  1410,  1519,  
			1617,  1618,  1662,  1704,  1894,  1897,  2014,  2074,  2252,  2338,  2344,  2483,  2486,  2595,  2609,  2680,  
			2886,  2953,  2954,  2961,  2974,  2987,  2994,  3024,  3052,  3125,  3154,  3184,  3185,  3218,  3245,  3246,  
			3263,  3267,  3294,  3295,  3319,  3342,  3352,  3389,  3397,  3419,  3501,  3526,  3527,  3548,  3552,  3597,  
			3598,  3623,  3631,  3679,  3706,  3709,  3711,  3738,  3766,  3796,  3827,  3837,  3840,  3841,  3849,  3891,  
			3893,  3903,  3905,  3939,  3981,  4002,  4023,  4029,  4741,  4849,  5260,  5666,  5715,  6154,  6331,  6560,  
			6707,  6712,  6745,  6834,  6971,  7024,  7130,  7142,  7176,  7387,  7540,  7903,  7996,  
			6086963,  6086968,  6086969,  6086971,  6086981,  6086991,  6086997,  6087001,  
			6087003,  6087007,  6087013,  6087020,  6087021,  6087023,  6087026,  6087030,  
			6087036,  6087038,  6087048,  6087050,  6087051,  6087054,  6087059,  6087067,  
			6087072,  6087078,  6087082,  6087389,  6087390,  6087391,  6087394,  6087395,  
			6087406,  6087412,  6087414,  6087416,  6087418,  6087419,  6087422,  6087428,  
			6087432,  6087433,  6087438,  6087441,  6087446,  6087449,  6087452,  6087454,  
			6087459,  6087466,  6087467,  6087468,  6087471,  6087475,  6087476,  6087477,  
			6087478,  6087494,  6087497,  6087499,  6087501,  6087504,  6087505,  6087509,  
			6087510,  6092138,  6092142,  6092144,  6092147,  6092148,  6092159,  6092163,  
			6092164,  6092171,  6092172,  6092173,  6092175,  6092178
		};

		Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);


		List<Repository> repositories = new ArrayList<Repository>();

		for(Long id : repoIDsLong) {
			Repository repo = repoDAO.get(id);
			repositories.add(repo);	
		}		

		try {
			codecovRepoInfoDataMiner(repositories);
			codecovBuildsMiner(repositories);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.exit(0);
	}
}