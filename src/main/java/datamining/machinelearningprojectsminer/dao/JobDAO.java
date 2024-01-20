/**
 * 
 */
package datamining.machinelearningprojectsminer.dao;

import datamining.machinelearningprojectsminer.models.Job;
import datamining.machinelearningprojectsminer.models.Workflow;

public interface JobDAO extends GenericDAO<Job, Long> {

    Workflow getByGHId(Long workflowGHId);

}