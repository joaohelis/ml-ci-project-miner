package datamining.machinelearningprojectsminer.dao;

import datamining.machinelearningprojectsminer.models.Repository;

public interface RepositoryDAO extends GenericDAO<Repository, Long> {
	
	Repository getRepositoryByFullName(String fullName);
	long getWorkflowsRunsCount(String fullName);
	long getCommitsCount(String fullName);

}