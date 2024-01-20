package datamining.machinelearningprojectsminer.dao;

import datamining.machinelearningprojectsminer.models.PullRequest;

public interface PullRequestDAO extends GenericDAO<PullRequest, Long> {
	
	PullRequest getByNumber(Integer number);
	
}