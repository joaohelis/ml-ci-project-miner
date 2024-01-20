package datamining.machinelearningprojectsminer.importter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.models.Repository;

public class MachineLearningVerseDatasetImportter {

    public static void repoMetadataMLVerseImportter(String filepath){
		
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
		
		Reader in;
		int count = 0;					        

        try {

            RepositoryDAO repoDAO = new HibernateRepositoryDAO();
            repoDAO.beginTransaction();
								        
            in = new FileReader(filepath);
                        
            System.out.println(" _____________________________________________________________");
            System.out.println("| #     |       full_name         | language |   created_at  |");
            System.out.println("|_______|_________________________|__________|_______________|");
            
            Iterable<CSVRecord> records;
        
            records = CSVFormat.RFC4180.withFirstRecordAsHeader().withNullString("").parse(in);        

            for (CSVRecord record : records) {
                                                
                Repository repo = new Repository();
                repo.setFullName(record.get("full_name"));
                repo.setName(record.get("name"));
                repo.setOwner(record.get("owner"));
                repo.setOwnerType(record.get("owner_type"));
                repo.setLanguage(record.get("language"));
                repo.setLicense(record.get("license"));
                repo.setProjectCategory(record.get("category"));
                repo.setGhId(Long.parseLong(record.get("gh_id")));
                Date created_at = null;
                try {
                    created_at = ft.parse(record.get("created_at"));
                } catch (Exception e) {
                    e.printStackTrace();    
                }
                repo.setCreated_at(created_at);

                try{
					repoDAO.save(repo);
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}
                            
                System.out.println(String.format("| %-5s |  %-40s |  %-15s | %-20s |" , ++count,
                        repo.getFullName(), repo.getLanguage(), repo.getCreated_at()));
                
                if(count % 100 == 0){
                    repoDAO.commitTransaction();
                    repoDAO.beginTransaction();
                }
                                        
                System.out.println(" ___________________________________________________________________________________________________");
            }

            repoDAO.commitTransaction();
            repoDAO.closeSession();			

        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){			
			e.printStackTrace();
		}			
	}

    // public static void main(String[] args) {

    //     String filePath = System.getProperty("user.dir") +
    //                       "/src/main/java/datamining/machinelearningprojectsminer/data" +
    //                       "/repo-metadata.csv";
        
    //     repoMetadataMLVerseImportter(filePath);
        
    //     System.exit(0);
    // }   
}