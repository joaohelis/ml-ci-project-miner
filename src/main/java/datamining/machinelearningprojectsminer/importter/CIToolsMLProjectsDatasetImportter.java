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


/**
 * This class imports the Tools and Applied ML projects dataset
 */
public class CIToolsMLProjectsDatasetImportter {

    private static RepositoryDAO repoDAO = new HibernateRepositoryDAO();

    /**
     * This method imports the Tools and Applied ML projects dataset
     * from the paper "Characterizing the usage of CI tools in ML projects"
     */
    public static void breadthCorpusCIandMLProjectsDatasetImportter(){
        
        // This condition is necessary to avoid the importation of the same dataset twice
        if(!repoDAO.listAll().isEmpty()){
            System.out.println("The Tools and Applied ML projects dataset was already imported!");
            return;
        }

        String fileName = System.getProperty("user.dir") +
                "/src/main/java/datamining/machinelearningprojectsminer/data" +
                "/ci_tools_ml_projects_dataset/" +
                "RQ1-BreadthCorpus.csv";

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

                String repoFullName = record.get("ProjectName");
                String repoCategory = record.get("ProjectType");

                System.out.println(String.format("| %-5s | %-15s | %-40s |" , ++count, repoCategory, repoFullName));
                
                Repository repo = new Repository(repoFullName);
                repo.setProjectCategory(repoCategory);
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
        breadthCorpusCIandMLProjectsDatasetImportter();
        System.exit(0);
    }
}