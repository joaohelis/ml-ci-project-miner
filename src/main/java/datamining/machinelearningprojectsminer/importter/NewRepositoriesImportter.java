package datamining.machinelearningprojectsminer.importter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateRepositoryDAO;
import datamining.machinelearningprojectsminer.dao.hibernate.HibernateUtil;
import datamining.machinelearningprojectsminer.models.Repository;

public class NewRepositoriesImportter {

    private static RepositoryDAO repoDAO = new HibernateRepositoryDAO();

    public static void newRepositoriesClassificationDatasetImportter(){
                
        String fileName = System.getProperty("user.dir") +
                "/src/main/java/datamining/machinelearningprojectsminer/data" +
                "/manual_new_repos_classification/" +
                "new_repoistories_classification.csv";

		Reader in;
		int count = 0;		        
    
        try {                                                                
            in = new FileReader(fileName);
                        
            System.out.println(" __________________________________");
            System.out.println("| #     |       full_name         |");
            System.out.println("|_______|_________________________|");
            
            Iterable<CSVRecord> records;
        
            records = CSVFormat.RFC4180.withFirstRecordAsHeader().withNullString("").parse(in);                        

            HibernateUtil.beginTransaction();         

            for (CSVRecord record : records) {

                String repoFullName = record.get("fullName");
                String repoCategory = record.get("category");
                Boolean isToyOrTutorial = record.get("isToyOrTutorial").equals("1") ? true : false;

                System.out.println(String.format("| %-5s | %-15s | %-40s | toy: %-5s |" , ++count, repoCategory, repoFullName, isToyOrTutorial));

                Repository repo = repoDAO.getRepositoryByFullName(repoFullName);
                repo.setProjectCategory(repoCategory);
                repo.setIsToyProjectOrStudyGuide(isToyOrTutorial);
                repoDAO.save(repo);
                                                                                                                                                
                if(count % 100 == 0){
                    HibernateUtil.commitTransaction();
                    HibernateUtil.beginTransaction();
                }                                                                
            }                

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){			
            e.printStackTrace();
        }finally{
            HibernateUtil.commitTransaction();       
        }        
	}

    public static void main(String[] args) {                         
        newRepositoriesClassificationDatasetImportter();
        System.exit(0);
    }
}