package datamining.machinelearningprojectsminer.importter;

import java.io.FileReader;
import java.io.Reader;

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

public class SLOCDatasetImportter {

    public static void importSLOCDataset(){		

        String filePath = System.getProperty("user.dir") +
                "/src/main/java/datamining/machinelearningprojectsminer/data" +
                "/manual_sloc_processing/" +
                "manual_sloc_dataset.json";

		LOCMetricDAO locMetricDAO = new HibernateLOCMetricDAO();
        RepositoryDAO repoDAO = new HibernateRepositoryDAO();		
        Reader reader = null;		

        try{
            reader = new FileReader(filePath);
        }catch(Exception e){
            System.out.println("Error while reading file " + filePath);
            e.printStackTrace();
            return;
        }
                
        Gson gson = new Gson();

        JsonArray locMetricsPerRepo = gson.fromJson(reader, JsonArray.class);	

        if(locMetricsPerRepo.isEmpty()){					
            System.out.println(" NO DATA TO PROCESS");												
        }else{				
            System.out.println("DATA SUCESSFULLY RETRIEVED!");
                        		                
            for(int i = 0; i < locMetricsPerRepo.size(); i++) {
                
                JsonObject repoJsonObject = locMetricsPerRepo.get(i).getAsJsonObject();
                String fullName = repoJsonObject.get("fullName").isJsonNull()? null: repoJsonObject.get("fullName").getAsString();                
                JsonArray slocDataPerRepoJsonArray = repoJsonObject.get("slocData").getAsJsonArray();
                
                Repository repo = repoDAO.getRepositoryByFullName(fullName);
                if(repo == null){
                    System.out.println("Repository " + fullName + " not found in the database!");
                    continue;
                }
                HibernateUtil.beginTransaction();

                LOCMetric totalLOCMetric = new LOCMetric("Total", 0, 0, 0, 0, 0);
                
                for(int metricIndex = 0; metricIndex < slocDataPerRepoJsonArray.size(); metricIndex++) {

                    JsonObject json = slocDataPerRepoJsonArray.get(metricIndex).getAsJsonObject();

                    String language = json.get("Name").isJsonNull()? null: json.get("Name").getAsString();
                    Integer files = json.get("Count").isJsonNull()? null: json.get("Count").getAsInt();		
                    Integer lines = json.get("Lines").isJsonNull()? null: json.get("Lines").getAsInt();		
                    Integer blanks = json.get("Blank").isJsonNull()? null: json.get("Blank").getAsInt();		
                    Integer comments = json.get("Comment").isJsonNull()? null: json.get("Comment").getAsInt();		
                    Integer linesOfCode = json.get("Code").isJsonNull()? null: json.get("Code").getAsInt();		

		            LOCMetric locMetric = new LOCMetric(language, files, lines, blanks, comments, linesOfCode);                    
                    locMetric.setRepo(repo);											
                    locMetricDAO.save(locMetric);						
                    System.out.println("----> " + locMetric);

                    totalLOCMetric.setFiles(totalLOCMetric.getFiles() + files);
                    totalLOCMetric.setLines(totalLOCMetric.getLines() + lines);
                    totalLOCMetric.setBlanks(totalLOCMetric.getBlanks() + blanks);
                    totalLOCMetric.setComments(totalLOCMetric.getComments() + comments);
                    totalLOCMetric.setLinesOfCode(totalLOCMetric.getLinesOfCode() + linesOfCode);                                                            
                }
                
                totalLOCMetric.setRepo(repo);											
                System.out.println("----> " + totalLOCMetric);
                locMetricDAO.save(totalLOCMetric);
                HibernateUtil.commitTransaction();					
            }							            
        }																							
	}
    
    // public static void main(String[] args) {
    //     importSLOCDataset();
    //     System.exit(0);
    // }
}