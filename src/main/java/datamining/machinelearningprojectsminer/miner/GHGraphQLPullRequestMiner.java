package datamining.machinelearningprojectsminer.miner;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.annotations.Type;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.dao.PullRequestDAO;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernatePullRequestDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.miner.helper.GHAPIProcessor;
import datamining.machinelearningprojectsminer.miner.helper.GHUtil;
import datamining.machinelearningprojectsminer.models.PullRequest;
import datamining.machinelearningprojectsminer.models.PullRequestComment;
import datamining.machinelearningprojectsminer.models.Repository;

public class GHGraphQLPullRequestMiner {

    // Define the GraphQL query      
    
    private static int perPage = 50;
    private static String queryStructure = "{ \"query\": \"query {" +
            "        repository(owner: \\\"{OWNER}\\\", name: \\\"{NAME}\\\") {" +
                                
            "          pullRequestCount: pullRequests{" +
            "            totalCount" +
            "          }" +                  
            "          pullRequests(before: {CURSOR}, last: " + perPage +", orderBy: {field: CREATED_AT, direction: DESC}) {" +                  
            "          pageInfo { endCursor startCursor hasPreviousPage }" +  
            "            nodes {" +        
            "              id" +
            "              number" +
            "              title" +
            "              state" +  
            "              body" +  
            "              isDraft" +              
            "              url" +
            "              author{" +
            "                login" +          
            "              }" +
            "              authorAssociation" +
            "              locked" +                      
            "              createdAt" +
            "              updatedAt" +
            "              mergedAt" +
            "              closedAt" +
            "              mergeCommit {" +
            "                oid" +          
            "              }" +
                            
            // "              labels(last:100) {" +
            // "                nodes{" +            
            // "                  id" +          
            // "                  name" +
            // "                  color" +
            // "                  description" +
            // "                  isDefault" +            
            // "                  createdAt" +
            // "                  url" +            
            // "                }" +
            // "              }" +  

            "              baseRepository {" +
            "                nameWithOwner" +
            "              }" +        
            "              baseRefName" +
            "              headRepository {" +
            "                nameWithOwner" +
            "              }" +        
            "              headRefName" +
                                                    
            "              additions" +
            "              deletions" +
            "              changedFiles" +      
            "              totalCommentsCount" +                                
            "              commitsCount: commits{" +
            "                totalCount" +
            "              }" +
                            
            "              commits(last:100){" +
            "                nodes{" +
            "                  commit{" +
            "                    oid" +
            "                  }" +
            "                }" +
            "              }" +
                            
            "              reactions {" +
            "                totalCount" +
            "              }" +
                            
            "              commentsCount: comments {" +
            "                totalCount" +
            "              }" +  
            "              comments(last: 100) {" +
            "                  nodes {" +
            "                      id" +            
            "                      author{" +
            "                         login" +
            "                      }" +            
            "                      authorAssociation" +            
            "                      bodyText" +
            "                      createdAt" +
            "                      url" +
            "                  }" +
            "              }" +              
            "              merged" +
            "              mergedBy{" +
            "                login" +
            "              }" +
            "              mergeable" +        
            "            }" +
            "          }" +
            "       }" +
            "    }\"" +
            "}";            

    private static PullRequest pullRequestExtractorFromJson(JsonObject json){
        
        String oid = json.get("id").isJsonNull()? null: json.get("id").getAsString();
        Integer number = json.get("number").isJsonNull()? null: json.get("number").getAsInt();
        String title = json.get("title").isJsonNull()? null: json.get("title").getAsString();
        String body = json.get("body").isJsonNull()? null: json.get("body").getAsString();
        String state = json.get("state").isJsonNull()? null: json.get("state").getAsString();
        String url = json.get("url").isJsonNull()? null: json.get("url").getAsString();
        String authorLogin = json.get("author").isJsonNull()? null: json.getAsJsonObject("author").get("login").getAsString();          
        String authorAssociation = json.get("authorAssociation").isJsonNull()? null: json.get("authorAssociation").getAsString();
        Boolean locked = json.get("locked").isJsonNull()? null: json.get("locked").getAsBoolean();
        Date createdAt = null;
        try {
			createdAt = GHUtil.simpleDateFormat.parse(json.get("createdAt").getAsString());
		} catch (Exception e) {			
			// e.printStackTrace();
		}
        Date updatedAt = null;
        try {
			updatedAt = GHUtil.simpleDateFormat.parse(json.get("updatedAt").getAsString());
		} catch (Exception e) {			
			// e.printStackTrace();
		}
        Date mergedAt = null;
        try {
			mergedAt = GHUtil.simpleDateFormat.parse(json.get("mergedAt").getAsString());
		} catch (Exception e) {			
			// e.printStackTrace();
		}
        Date closedAt = null;
        try {
			closedAt = GHUtil.simpleDateFormat.parse(json.get("closedAt").getAsString());
		} catch (Exception e) {			
			// e.printStackTrace();
		}

        String mergeCommitSHA = json.get("mergeCommit").isJsonNull()? null: json.getAsJsonObject("mergeCommit").get("oid").getAsString();
        String baseReposFullName = json.get("baseRepository").isJsonNull()? null: json.getAsJsonObject("baseRepository").get("nameWithOwner").getAsString();
        String baseRefName = json.get("baseRefName").isJsonNull()? null: json.get("baseRefName").getAsString();
        String headReposFullName = json.get("headRepository").isJsonNull()? null: json.getAsJsonObject("headRepository").get("nameWithOwner").getAsString();
        String headRefName = json.get("headRefName").isJsonNull()? null: json.get("headRefName").getAsString();
        Integer additions = json.get("additions").isJsonNull()? null: json.get("additions").getAsInt();
        Integer deletions = json.get("deletions").isJsonNull()? null: json.get("deletions").getAsInt();
        Integer changedFiles = json.get("number").isJsonNull()? null: json.get("number").getAsInt();
        Integer totalCommentsCount = json.get("totalCommentsCount").isJsonNull()? null: json.get("totalCommentsCount").getAsInt();
        Integer commitsCount = json.get("commitsCount").isJsonNull()? null: json.getAsJsonObject("commitsCount").get("totalCount").getAsInt();
        Integer reactionsCount = json.get("reactions").isJsonNull()? null: json.getAsJsonObject("reactions").get("totalCount").getAsInt();
        Integer commentsCount = json.get("commentsCount").isJsonNull()? null: json.getAsJsonObject("commentsCount").get("totalCount").getAsInt();
        Boolean merged = json.get("merged").isJsonNull()? null: json.get("merged").getAsBoolean();
        String mergedByLogin = json.get("mergedBy").isJsonNull()? null: json.getAsJsonObject("mergedBy").get("login").getAsString();
        Boolean mergeable = json.get("mergeable").isJsonNull()? null: json.get("mergeable").getAsBoolean();
        Boolean isDraft = json.get("isDraft").isJsonNull()? null: json.get("isDraft").getAsBoolean();
		
        // List<PullRequestLabel> labels;
        String commitsSHAsList = null;      
        
        JsonArray commitSHAsArray = json.get("commits").isJsonNull()? null : json.getAsJsonObject("commits").get("nodes").getAsJsonArray();

        if(commitSHAsArray != null){
            commitsSHAsList = "";

            for (int i = 0; i < commitSHAsArray.size(); i++) {
                JsonObject commitJson = commitSHAsArray.get(i).getAsJsonObject();                
                String commitSHA = commitJson.getAsJsonObject("commit").get("oid").getAsString();
                commitsSHAsList += commitSHA;
                if(i < commitSHAsArray.size() - 1){
                    commitsSHAsList += " ";
                }
            }
        }

        PullRequest pullRequest = new PullRequest();

        pullRequest.setOid(oid);
        pullRequest.setNumber(number);
        pullRequest.setTitle(title);
        pullRequest.setBody(body);
        pullRequest.setState(state);
        pullRequest.setUrl(url);
        pullRequest.setAuthorLogin(authorLogin);
        pullRequest.setAuthorAssociation(authorAssociation);
        pullRequest.setLocked(locked);
        pullRequest.setCreatedAt(createdAt);
        pullRequest.setUpdatedAt(updatedAt);
        pullRequest.setMergedAt(mergedAt);
        pullRequest.setClosedAt(closedAt);
        pullRequest.setMergeCommitSHA(mergeCommitSHA);
        pullRequest.setBaseReposFullName(baseReposFullName);
        pullRequest.setBaseRefName(baseRefName);
        pullRequest.setHeadReposFullName(headReposFullName);
        pullRequest.setHeadRefName(headRefName);
        pullRequest.setAdditions(additions);
        pullRequest.setDeletions(deletions);
        pullRequest.setChangedFiles(changedFiles);
        pullRequest.setTotalCommentsCount(totalCommentsCount);
        pullRequest.setCommitsCount(commitsCount);
        pullRequest.setReactionsCount(reactionsCount);
        pullRequest.setCommentsCount(commentsCount);
        pullRequest.setMerged(merged);
        pullRequest.setMergedByLogin(mergedByLogin);
        pullRequest.setMergeable(mergeable);
        pullRequest.setIsDraft(isDraft);
        pullRequest.setCommitsSHAsList(commitsSHAsList);

        JsonArray comments = json.get("comments").isJsonNull()? null : json.getAsJsonObject("comments").get("nodes").getAsJsonArray();

        List<PullRequestComment> commentsList = new ArrayList<PullRequestComment>();

        if(comments != null){
            for (int i = 0; i < comments.size(); i++) {
                JsonObject commentJson = comments.get(i).getAsJsonObject();
                PullRequestComment comment = PullRequestCommentExtractorFromJson(commentJson);
                comment.setPullRequest(pullRequest);
                commentsList.add(comment);
            }
        }

        pullRequest.setComments(commentsList);

        return pullRequest;
	}

    private static PullRequestComment PullRequestCommentExtractorFromJson(JsonObject commentJson) {
        String oid = commentJson.get("id").isJsonNull()? null: commentJson.get("id").getAsString();
        String body = commentJson.get("bodyText").isJsonNull()? null: commentJson.get("bodyText").getAsString();
        String authorLogin = commentJson.get("author").isJsonNull()? null: commentJson.get("author").isJsonNull()? null: commentJson.getAsJsonObject("author").get("login").getAsString();
        String authorAssociation = commentJson.get("authorAssociation").isJsonNull()? null: commentJson.get("authorAssociation").getAsString();
        String url = commentJson.get("url").isJsonNull()? null: commentJson.get("url").getAsString();
        
        Date createdAt = null;
        try {
			createdAt = GHUtil.simpleDateFormat.parse(commentJson.get("createdAt").getAsString());
		} catch (Exception e) {			
			// e.printStackTrace();
		}

        PullRequestComment comment = new PullRequestComment(oid, url, authorLogin, authorAssociation, body, createdAt);

        return comment;
    }

    public static Integer countRepoPullRequestsGHAPI(Repository repo) {				
        try{                        
            String query = "{ \"query\": \"query {" +
            "        repository(owner: \\\"{OWNER}\\\", name: \\\"{NAME}\\\") {" +
                                
            "          pullRequestCount: pullRequests{" +
            "            totalCount" +
            "          }" +                  
            "        }" +
            "    }\"" +
            "}";

            query = query.replace("{OWNER}", repo.getFullName().split("/")[0]);
            query = query.replace("{NAME}", repo.getFullName().split("/")[1]);

            Map<String, Object> connectionResult = GHAPIProcessor.ghGraphQlAPIQueryProcessor(query, 10);
            HttpURLConnection conn = (HttpURLConnection) connectionResult.get("connection");

            if (conn != null && conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                
                String response = GHAPIProcessor.getReponseFromAPIQueryConnection(conn);

                JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
                JsonObject data = json.getAsJsonObject("data");             
                
                JsonObject repository = data.getAsJsonObject("repository");
                
                int pullRequestTotal = repository.get("pullRequestCount").getAsJsonObject().get("totalCount").getAsInt();
                
                return pullRequestTotal;
                                                                                
            } else {
                throw new Exception("Failed to fetch pullRequests information");             
            }
        }catch(Exception e){
            System.out.println("Failed to fetch pullRequests information. Exception: " + e);
            e.printStackTrace();
        }  
        return null;      
    }

    public static void ghGraphQLPullRequestsMiner(List<Repository> repositories){
        

        PullRequestDAO pullRequestDAO = new HibernatePullRequestDAO();
        
        int count = 1;		
		for(Repository repo: repositories){            

            Query hibernateQ1 = HibernateUtil.getSession().createQuery("from pull where repo = :repo");
			hibernateQ1.setParameter("repo", repo);
			List<PullRequest> pulls = hibernateQ1.list();

            Set<Integer> pullNumbers = new HashSet<Integer>();
            pulls.forEach(pull -> pullNumbers.add(pull.getNumber()));

			Query hibernateQuery = HibernateUtil.getSession().createQuery("select beforeCursorGraphQL from pull where repo = :repo order by createdAt desc");
            hibernateQuery.setParameter("repo", repo)
                          .setMaxResults(1);						
            
            String beforeCursorGraphQL = null;            
            
            try {
                beforeCursorGraphQL = (String) hibernateQuery.uniqueResult();			                
            } catch (Exception e) {
                // TODO: handle exception
            }            

            System.out.printf("PROJ %s/%s (#%s) - %s - Processed PRs: %s\n", count, repositories.size(), repo.getId(), repo.getFullName(), pulls.size());

            try{                        
                boolean hasNextPage = true;
                String cursor = null;

                if(beforeCursorGraphQL != null){
                    cursor = beforeCursorGraphQL;
                }

                int alreadyProcessedpulls = pulls.size();										

                int pullRequestCount = alreadyProcessedpulls;
                int pullRequestTotal = 0;
                while(hasNextPage){

                    String query = queryStructure;

                    query = query.replace("{OWNER}", repo.getFullName().split("/")[0]);
                    query = query.replace("{NAME}", repo.getFullName().split("/")[1]);
                    query = query.replace("{CURSOR}", cursor != null ? "\\\"" + cursor + "\\\"" : "null");
                    System.out.println("cursor: " + cursor);

                    // System.out.println(query);
                            
                    Map<String, Object> connectionResult = GHAPIProcessor.ghGraphQlAPIQueryProcessor(query, 30);
                    HttpURLConnection conn = (HttpURLConnection) connectionResult.get("connection");
                                            
                    if (conn != null && conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        
                        // String response = GHAPIProcessor.getReponseFromAPIQueryConnection(conn);
                        String response = (String) connectionResult.get("connectionResponse");
        
                        JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
                        JsonObject data = json.getAsJsonObject("data");             
                        
                        JsonObject repository = data.getAsJsonObject("repository");
                        
                        pullRequestTotal = repository.get("pullRequestCount").getAsJsonObject().get("totalCount").getAsInt();
                        
                        JsonObject pageInfo = repository.getAsJsonObject("pullRequests").getAsJsonObject("pageInfo");
                        hasNextPage = pageInfo.get("hasPreviousPage").getAsBoolean();
                        if(hasNextPage){
                            cursor = pageInfo.get("startCursor").getAsString();
                        }
                        
                        JsonArray pullRequestsArray = repository.getAsJsonObject("pullRequests").get("nodes").getAsJsonArray();
                        
                        pullRequestDAO.beginTransaction();

                        for (int i = pullRequestsArray.size() -1; i >= 0; i--) {
                            JsonElement jsonElement = pullRequestsArray.get(i);
                            JsonObject pullRequestJson = jsonElement.getAsJsonObject();

                            PullRequest pull = pullRequestExtractorFromJson(pullRequestJson);
                            pull.setRepo(repo);
                            pull.setBeforeCursorGraphQL(cursor);

                            if(!pullNumbers.contains(pull.getNumber())){                                
                                pullRequestDAO.save(pull);
                                pullNumbers.add(pull.getNumber());
                                pullRequestCount++;
                            }
                            // System.out.println(pullRequestCount + "/" + pullRequestTotal + " - " + pull);
                        }   
                        
                        pullRequestDAO.commitTransaction();
                                                                                    
                    } else {
                        throw new Exception("Failed to fetch pullRequests information");             
                    }

                    System.out.printf("PROJ %s/%s (#%s) %s - Processed PRs %s/%s\n", count, repositories.size(), repo.getId(), repo.getFullName(), pullRequestCount, pullRequestTotal);
                }            
            }catch(Exception e){
                System.out.println("Failed to fetch pullRequests information. Exception: " + e);
                e.printStackTrace();
            }
            count++;
        }

    }

    public static void main(String[] args) {  

        RepositoryDAO repoDAO = new HibernateRepositoryDAO();
		
		long[] repoIDs = {
			22, 123, 331, 420, 474, 493, 518, 538, 575, 582, 584, 620, 653, 707, 777, 950, 1410, 
            1617, 1618, 1662, 1704, 1894, 1897, 2014, 2074, 2252, 2344, 2483, 2486, 2595, 2609, 
            2680, 2886, 2953, 2954, 2961, 2974, 2987, 2994, 3024, 3052, 3125, 3154, 3184, 3185, 
            3218, 3245, 3246, 3263, 3267, 3294, 3295, 3319, 3342, 3352, 3389, 3397, 3419, 3501, 
            3526, 3527, 3548, 3552, 3597, 3598, 3623, 3631, 3679, 3706, 3709, 3711, 3738, 3766, 
            3796, 3827, 3837, 3841, 3849, 3891, 3893, 3903, 3905, 3939, 3981, 4002, 4023, 4029, 
            4741, 5260, 
            5666, 5715, 6154, 6331, 6560, 6707, 6712, 6745, 6834, 6971, 7024, 7130, 
            7142, 7176, 7387, 7540, 7903, 7996, 6086963, 6086968, 6086969, 6086971, 6086981, 
            6086991, 6086997, 6087001, 6087003, 6087007, 6087013, 6087020, 6087021, 6087023, 
            6087026, 
            6087030, 6087036, 6087038, 6087048, 6087050, 6087051, 6087054, 6087059, 
            6087067, 6087072, 6087078, 6087082, 6087389, 6087390, 6087391, 6087394, 6087395, 
            6087406, 6087412, 6087414, 6087416, 6087418, 6087419, 6087422, 6087428, 6087432, 
            6087433, 6087438, 6087441, 6087446, 6087449, 6087452, 6087454, 
            6087459, 6087466, 
            6087467, 6087468, 6087471, 6087475, 6087476, 6087477, 6087478, 6087494, 6087497, 
            6087499, 6087501, 6087504, 6087505, 6087509, 6087510, 6092138, 6092142, 6092144, 
            6092147, 6092148, 6092159, 
            6092163, 6092164, 6092171, 6092172, 6092173, 6092175, 
            6092178
		};

		Long[] repoIDsLong = Arrays.stream(repoIDs).boxed().toArray(Long[]::new);

		List<Repository> repositories = new ArrayList<Repository>();
		for(Long id : repoIDsLong) {
            Repository repo = repoDAO.get(id);
            repositories.add(repo);
            // if(repo != null && repo.getPullRequestsCount_ghapi() != null && repo.getPulls().size() < repo.getPullRequestsCount_ghapi()){
            //     repositories.add(repo);
            // }            
		}
						
		System.out.println(repositories.size() + " repositories to be processed");
		// ghGraphQLPullRequestsMiner(repositories);	
        addCommentBodyFieldDataToPullRequests(repositories);
		
        System.exit(0);    
    }

    public static void addCommentBodyFieldDataToPullRequests(List<Repository> repositories){

        PullRequestDAO pullRequestDAO = new HibernatePullRequestDAO();
        
        int count = 1;		
		for(Repository repo: repositories){            

            Query hibernateQ1 = HibernateUtil.getSession().createQuery("from pull where repo = :repo and commentsCount > 0 and commentsAsText IS NULL");
			hibernateQ1.setParameter("repo", repo);
			List<PullRequest> pulls = hibernateQ1.list();

            System.out.printf("PROJ %s/%s (#%s) - %s - Pull Requests with Comments: %s\n", count, repositories.size(), repo.getId(), repo.getFullName(), pulls.size());

            pullRequestDAO.beginTransaction();
            int pullsCount = 1;
            for(PullRequest pull: pulls){

                String commentsText = "";
                
                int commentIndex = 0;
                for(PullRequestComment comment: pull.getComments()){
                    
                    String commentInfo = "-----------------------------------------" + "\n" +
                                         "createdAt: " + comment.getCreatedAt() + "\n" +                                          
                                         "authorLogin: " + comment.getAuthorLogin() + "\n" + 
                                         "authorAssociation: " + comment.getAuthorAssociation() + "\n\n" + 
                                         "bodyText: \n" + comment.getBodyText() ;
                    
                    if(commentIndex < pull.getComments().size() - 1){
                        commentInfo += "\n\n";
                    }           
                    
                    commentsText += commentInfo;
                    
                    commentIndex++;
                }

                pull.setCommentsAsText(commentsText);

                pullRequestDAO.save(pull);

                if(pullsCount % 100 == 0){
                    pullRequestDAO.commitTransaction();
                    pullRequestDAO.beginTransaction();
                    
                    System.out.println("----- " +  pullsCount + "/" + pulls.size() + " PRs processed");                    
                }

                pullsCount++;
            }                                
            pullRequestDAO.commitTransaction();            

            System.out.println("----- " +  (pullsCount - 1) + "/" + pulls.size() + " PRs processed");                    

            count++;
        }
    }
}