/**
 * 
 */
package datamining.machinelearningprojectsminer.miner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.dao.CommitDAO;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateCommitDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.models.Commit;
import datamining.machinelearningprojectsminer.models.Repository;


public class GHAPICommitsMiner {

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	// Method to count the number of commits in the defalt branch of a repository
    public static int countCommitsInRepoDefaultBranch(Repository repo) {				
        try{

		String token = GHAPIProcessor.getToken().getToken();

            // Define the GraphQL query         
         String query = "{ \"query\": \"query { repository(owner: \\\"" + repo.getOwner() + "\\\", name: \\\"" + repo.getName() + "\\\") { object(expression: \\\"{defaulBranch}\\\") { ... on Commit { history { totalCount } } } } }\" }";
         query = query.replace("{defaulBranch}", repo.getDefaultBranch());
 
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
                         int commitCount = history.get("totalCount").getAsInt();
                         return commitCount;
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
        return 0;
    }

	private static Commit commitExtractorFromJson(JsonObject json){		
		
		String sha = json.get("sha").isJsonNull()? null: json.get("sha").getAsString();
		String url = json.get("url").isJsonNull()? null: json.get("url").getAsString();

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
		Integer commentCount = json.getAsJsonObject("commit").get("comment_count").getAsInt();
		
		Commit commit = new Commit(sha, url, committerName, committerEmail, committerDate,
			 				authorName, authorEmail, authorDate, null, message, commentCount);
		return commit;
	}
	
	public static void ghRepositoriesCommitsMiner(List<Repository> repositories){
		CommitDAO commitDAO = new HibernateCommitDAO();

		int count = 1;		
		for(Repository repo: repositories){

			HashSet<String> repoCommitsSHA = new HashSet<String>();
			repo.getCommits().forEach(c -> repoCommitsSHA.add(c.getSha()));
						
			try {				
				int commitsCountInDefaultBranch = countCommitsInRepoDefaultBranch(repo);
				int lastPage = commitsCountInDefaultBranch / 100;
				if(commitsCountInDefaultBranch % 100 != 0){
					lastPage++;
				}
				int alreadyProcessedCommits = repoCommitsSHA.size();
				
				boolean repoCommitsAvailable = true;
				int page = lastPage - (int)(alreadyProcessedCommits / 100);

				while(page > 0 && repoCommitsAvailable) {
					HibernateUtil.beginTransaction();

					String entryPoint = "https://api.github.com/repos/{fullName}/commits?sha={defaultBranch}&per_page=100&page={page}";
					entryPoint = entryPoint.replace("{fullName}", repo.getFullName());
					entryPoint = entryPoint.replace("{defaultBranch}", repo.getDefaultBranch());
					entryPoint = entryPoint.replace("{page}", String.valueOf(page));

					System.out.printf("PROJECT %s/%s %s - PAGE %s/%s\n", count, repositories.size(), repo.getFullName(), lastPage - page + 1, lastPage);					
					
					HttpURLConnection conn = GHAPIProcessor.ghAPIEntryPointProcessor(entryPoint, 5);
																							
					if(conn.getResponseCode() == 400) {						
						repoCommitsAvailable = false;					
						System.out.println(" - COMMIT DATA NOT FOUND!");
					}else{
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						Gson gson = new Gson();
						JsonArray commits = gson.fromJson(reader, JsonArray.class);						

						if(commits.isEmpty()){
							// PAGINATION HAS ENDDED
							System.out.println(" NO COMMIT TO PROCESS");
							repoCommitsAvailable = false;							
						}else{																																																																																										
							for(int i = 0; i < commits.size(); i++) {
								JsonObject commitJson = commits.get(i).getAsJsonObject();
								Commit commit = commitExtractorFromJson(commitJson);
								commit.setBranch(repo.getDefaultBranch());
								commit.setRepo(repo);
								if(!repoCommitsSHA.contains(commit.getSha())){
									commitDAO.save(commit);
								}																
							}							
						}
						page--;
						HibernateUtil.commitTransaction(); // commit after read 100 commits
					}																			
				}				
			}catch (Exception e) {
				e.printStackTrace();
			}
			count++;		
		}		
	}

	public static void main(String[] args) {
		
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
				
		// List<Repository> repositories = repoDAO.listAll()
		// 									   .stream()
		// 									   .filter(repo -> repo.getTravisBuildsCount() != null &&
		// 									   				   repo.getTravisBuildsCount() > 0 &&
		// 													   repo.getCommits().isEmpty()
		// 													   )
		// 									   .collect(Collectors.toList());

		// Query query = HibernateUtil.getSession().createQuery("from repository " +
		// 													 "where commitsCountInMaster_ghapi > 0 AND " +
		// 													  		"ghActions_workflows_count = None AND " +
		// 													  		"defaultBranch = 'master'");
		// List<Repository> repositories = query.list();

		// List<Repository> repositories = repoDAO.listAll()
		// 									   .stream()
		// 									   .filter(repo -> 
		// 									   				   repo.getCreated_at() != null &&
		// 													   repo.getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi() != null &&											   				   
		// 													   repo.getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi() >= 100 &&
		// 									   				   repo.getDefaultBranch() != null &&
		// 													   (repo.getDefaultBranch().equals("master") || repo.getDefaultBranch().equals("main"))															   
		// 													   )
		// 									   // sort by number of commits in default branch (ascending)
		// 									   .sorted((r1, r2) -> r1.getCommitsCountInDefaultBranch_ghapi().compareTo(r2.getCommitsCountInDefaultBranch_ghapi()))											   
		// 									   .collect(Collectors.toList());


		long[] repoIDs = {
			// 22, 123, 331, 420, 474, 493, 518, 538, 575, 582, 584, 620, 653, 
			// 707, 777, 950, 1410, 1519, 1617, 1618, 1662, 1704, 1817, 1894, 1897, 
			// 2014, 2074, 2252, 2338, 2344, 2483, 2486, 2595, 2609, 2680, 2886, 2953, 
			
			// 2954, 2961, 2974, 2987, 2994, 3024, 3052, 3125, 3134, 3154, 3184, 3185, 
			// 3218, 3245, 3246, 3263, 3267, 3294, 3295, 3319, 3342, 3352, 3389, 3397, 
			// 3419, 3501, 3526, 3527, 3548, 3552, 3582, 3597, 3598, 3623, 3631, 3679, 
			
			// 3706, 3709, 3711, 3738, 3753, 3766, 3796, 3827, 3840, 3841, 3849, 3891, 
			// 3893, 3903, 3905, 3939, 3981, 4002, 4023, 4029, 4849, 5260, 5666, 5715, 
			
			// 6154, 6331, 6438, 6560, 6707, 6712, 6745, 6834, 6971, 7024, 7130, 7142, 
			// 7176, 7387, 7523, 7540, 7903, 7996

			3837, 4741
		};

		// NEW PROJECTS IDS
		// long[] repoIDs = {
		// 	6086963, 6086968, 6086969, 6086971, 6086977, 6086981, 6086991, 
		// 	6086992, 6086997, 6087001, 6087003, 6087007, 6087013, 6087020, 
		// 	6087021, 6087023, 6087026, 6087029, 6087030, 6087036, 6087038, 
		// 	6087048, 6087050, 6087051, 6087054, 6087055, 6087059, 6087060, 
			
		// 	6087067, 6087072, 6087075, 6087078, 6087082, 6087388, 6087389, 
		// 	6087390, 6087391, 6087394, 6087395, 6087406, 6087407, 6087410, 
		// 	6087412, 6087414, 6087416, 6087418, 6087419, 6087422, 6087423, 
		// 	6087428, 6087432, 6087433, 6087434, 6087438, 6087441, 6087446, 
			
		// 	6087449, 6087451, 6087452, 6087454, 6087459, 6087463, 6087464, 
		// 	6087466, 6087467, 6087468, 6087471, 6087474, 6087475, 6087476, 
		// 	6087477, 6087478, 6087491, 6087492, 6087494, 6087497, 6087498, 
			
		// 	6087499, 6087501, 6087502, 6087504, 6087505, 6087509, 6087510, 
		// 	6092138, 6092142, 6092144, 6092147, 6092148, 6092150, 6092152, 
		// 	6092159, 6092163, 6092164, 6092169, 6092171, 6092172, 6092173, 
		// 	6092175, 6092176, 6092178
		// };		

		Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);

		List<Repository> repositories = new ArrayList<Repository>();
		for(Long id : repoIDsLong) {
			repositories.add(repoDAO.get(id));
		}
						
		System.out.println(repositories.size() + " repositories to be processed");
			
		ghRepositoriesCommitsMiner(repositories);
		System.exit(0);
	}
}