/**
 * 
 */
package datamining.machinelearningprojectsminer.miner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import datamining.machinelearningprojectsminer.dao.LOCMetricDAO;
import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateLOCMetricDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.models.LOCMetric;
import datamining.machinelearningprojectsminer.models.Repository;

public class LOCMetricMinerCodeTabs {
	
	private static LOCMetric locMetricJsonExtractor(JsonObject json){		
		String language = json.get("language").isJsonNull()? null: json.get("language").getAsString();
		Integer files = json.get("files").isJsonNull()? null: json.get("files").getAsInt();		
		Integer lines = json.get("lines").isJsonNull()? null: json.get("lines").getAsInt();		
		Integer blanks = json.get("blanks").isJsonNull()? null: json.get("blanks").getAsInt();		
		Integer comments = json.get("comments").isJsonNull()? null: json.get("comments").getAsInt();		
		Integer linesOfCode = json.get("linesOfCode").isJsonNull()? null: json.get("linesOfCode").getAsInt();		

		LOCMetric locMetric = new LOCMetric(language, files, lines, blanks, comments, linesOfCode);
		return locMetric;
	}

	public static void locMetricRepoMiner(List<Repository> repositories){		

		LOCMetricDAO locMetricDAO = new HibernateLOCMetricDAO();

		int count = 0;
		HibernateUtil.beginTransaction();	

		for(Repository repo: repositories){

			String repoName = repo.getFullName();
			
			try {
				String entryPoint = "https://api.codetabs.com/v1/loc/?github={fullName}&branch={defaultBranch}&ignored=docs";
				entryPoint = entryPoint.replace("{fullName}", repoName);
				entryPoint = entryPoint.replace("{defaultBranch}", repo.getDefaultBranch());
				URL url = new URL(entryPoint);				

				System.out.println("-----------------------------------------------------------");
				System.out.printf("%s / %s -- %s -- Loading LOC Metric Info --> ", ++count, repositories.size(), repoName);

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();																		

				if(conn.getResponseCode() == 404) {
					System.out.println("NO DATA FOR THE PROJECT!");															
				}else{
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					Gson gson = new Gson();

					JsonArray locMetricsPerLanguage = gson.fromJson(reader, JsonArray.class);						

					if(locMetricsPerLanguage.isEmpty()){					
						System.out.println(" NO DATA TO PROCESS");												
					}else{				
						System.out.println("DATA SUCESSFULLY RETRIEVED!");																																																																																						
						for(int i = 0; i < locMetricsPerLanguage.size(); i++) {
							JsonObject locMetricJson = locMetricsPerLanguage.get(i).getAsJsonObject();
							LOCMetric locMetric = locMetricJsonExtractor(locMetricJson);
							locMetric.setRepo(repo);											
							locMetricDAO.save(locMetric);						
							System.out.println("----> " + locMetric);
						}							
					}				
				}									
				HibernateUtil.commitTransaction();
				HibernateUtil.beginTransaction();
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {				
					e.printStackTrace();
				}
			} catch (Exception e1) {				
				e1.printStackTrace();
			} 
		}	
		HibernateUtil.commitTransaction();					
	}

	private static Map<String, String> getLanguageType(){
		Map<String, String> languageTypeMAP = new HashMap<>();
		try{
			String userDirectory = System.getProperty("user.dir") + 
								   "/src/main/java/datamining/machinelearningprojectsminer/data/";			
			File myObj = new File(userDirectory + "loc_language_type.csv");
			Scanner myReader = new Scanner(myObj);
			boolean isHeadLine = true;
			while (myReader.hasNextLine()) {
				if(isHeadLine){
					isHeadLine = false;
					continue;
				}
				String line = myReader.nextLine();
				String[] languageType = line.split(";");
				languageTypeMAP.put(languageType[0], languageType[1]);
			}
			myReader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return languageTypeMAP;
	}

	public static void calculateRepositorySLOC(List<Repository> repositories){
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();		

		Map<String, String> languageTypeMAP = getLanguageType();
		
		Set<String> accountableLanguageTypes = new HashSet<String>();
		accountableLanguageTypes.add("programming");
		accountableLanguageTypes.add("markup");

		int count = 1;
		HibernateUtil.beginTransaction();

		for(Repository repo: repositories){
			Long repoLOC = null;

			List<LOCMetric> locMetricsRepo = repo.getLocMetrics();
			
			if(!locMetricsRepo.isEmpty()){
				List<LOCMetric> locMetricsFromAccountableLanguages = repo.getLocMetrics()
											.stream()
											.filter(loc -> !loc.getLanguage().equals("Total"))
											.filter(loc -> accountableLanguageTypes.contains(languageTypeMAP.get(loc.getLanguage())))
											.collect(Collectors.toList());
				Collections.sort(locMetricsFromAccountableLanguages, (l1, l2) -> l2.getLinesOfCode().compareTo(l1.getLinesOfCode()));											
				
				if(!locMetricsFromAccountableLanguages.isEmpty()){
					// getting the language with the highest number of LOC
					repo.setCountLOCLanguage(locMetricsFromAccountableLanguages.get(0).getLanguage());

					// getting the total number of LOC
					repoLOC = 0L;
					for(LOCMetric loc: locMetricsFromAccountableLanguages){				
						repoLOC += loc.getLinesOfCode();
					}
				}
			}
												
			repo.setSloc(repoLOC);
			
			if(repoLOC != null){
				if(repoLOC > 1000000){
					repo.setLoc_size("very large");
				}else if(repoLOC > 100000){
					repo.setLoc_size("large");
				}else if(repoLOC > 10000){
					repo.setLoc_size("medium");
				}else if(repoLOC > 1000){
					repo.setLoc_size("small");
				}else if(repoLOC > 0){
					repo.setLoc_size("very small");
				}
			}

			repoDAO.save(repo);

			System.out.printf("(%s/%s) Repository [name=%s, language=%s, countLocLang=%s, LOC=%s]\n",
										count, repositories.size(), repo.getFullName(), repo.getLanguage(), repo.getCountLOCLanguage(), repo.getSloc());

			if(count % 250 == 0){
				HibernateUtil.commitTransaction();
				HibernateUtil.beginTransaction();
			}				
			count++;						
		}
		HibernateUtil.commitTransaction();		
	}
		
	public static void main(String[] args) {

		/*
		 * This class is responsible for call the CodeTabs API to calculate
		 * the LOC metric for each repository.		 		 
		 * 
		 * The mining was performed in 2023-05-05 and 2023-05-06		 
		 */

		// get the list of all repositories that have not been processed yet
		RepositoryDAO repoDAO = new HibernateRepositoryDAO();
		List<Repository> repositories = repoDAO.listAll()
											   .stream()											   
											   .filter(
													repo -> 
											   		repo.getSize() != null // filtering out repositories that are not available in GitHub anymore
													// && repo.getSize() / 100 < 500
											   		&& !repo.getLocMetrics().isEmpty()
													&& repo.getSloc() == null
											   	)											
											   .collect(Collectors.toList());	
																				   						
		// locMetricRepoMiner(repositories);
		calculateRepositorySLOC(repositories);
		
		System.exit(0);
	}
}