package datamining.machinelearningprojectsminer.dao.hibernate;

import datamining.machinelearningprojectsminer.dao.WorkflowConfFileChangeDAO;
import datamining.machinelearningprojectsminer.models.WorkflowConfFileChange;

public class HibernateWorkflowFileChangeDAO extends HibernateDAO<WorkflowConfFileChange, Long> implements WorkflowConfFileChangeDAO {

	public HibernateWorkflowFileChangeDAO(){
		super(WorkflowConfFileChange.class);
	}
}