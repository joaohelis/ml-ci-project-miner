package datamining.machinelearningprojectsminer.dao.hibernate;

import org.hibernate.Query;

import datamining.machinelearningprojectsminer.dao.JobDAO;
import datamining.machinelearningprojectsminer.models.Job;
import datamining.machinelearningprojectsminer.models.Workflow;

public class HibernateJobDAO extends HibernateDAO<Job, Long> implements JobDAO {

	public HibernateJobDAO(){
		super(Job.class);
	}

	@Override
	public Workflow getByGHId(Long workflowGHId) {
		Query query = HibernateUtil.getSession().createQuery("from "+getTypeClass().getName()+" where ghId = :ghId");
		query.setParameter("ghId", workflowGHId);
		return (Workflow) query.uniqueResult();		
	}
}