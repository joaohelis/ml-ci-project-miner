package datamining.machinelearningprojectsminer.dao.hibernate;

import datamining.machinelearningprojectsminer.dao.WorkflowRunDAO;
import datamining.machinelearningprojectsminer.models.WorkflowRun;

public class HibernateWorkflowRunDAO extends HibernateDAO<WorkflowRun, Long> implements WorkflowRunDAO {

	public HibernateWorkflowRunDAO(){
		super(WorkflowRun.class);
	}
}