/**
 * 
 */
package datamining.machinelearningprojectsminer.dao;

import datamining.machinelearningprojectsminer.models.Workflow;

public interface WorkflowDAO extends GenericDAO<Workflow, Long> {

    Workflow getByGHId(Long workflowGHId);

}