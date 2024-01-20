package datamining.machinelearningprojectsminer.dao.hibernate;

import org.hibernate.Query;

import datamining.machinelearningprojectsminer.dao.WorkflowDAO;
import datamining.machinelearningprojectsminer.models.Workflow;

public class HibernateWorkflowDAO extends HibernateDAO<Workflow, Long> implements WorkflowDAO {

	public HibernateWorkflowDAO(){
		super(Workflow.class);
	}

	@Override
	public Workflow getByGHId(Long workflowGHId) {
		Query query = HibernateUtil.getSession().createQuery("from "+getTypeClass().getName()+" where ghId = :ghId");
		query.setParameter("ghId", workflowGHId);
		return (Workflow) query.uniqueResult();		
	}
}