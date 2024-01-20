package datamining.machinelearningprojectsminer.dao.hibernate;

import org.hibernate.Query;

import datamining.machinelearningprojectsminer.dao.RepositoryDAO;
import datamining.machinelearningprojectsminer.models.Repository;

public class HibernateRepositoryDAO extends HibernateDAO<Repository, Long> implements RepositoryDAO {

	public HibernateRepositoryDAO(){
		super(Repository.class);
	}
	
	@Override
	public Repository getRepositoryByFullName(String fullName) {
		Query query = HibernateUtil.getSession().createQuery("from "+getTypeClass().getName()+" where fullName = :fullName");
		query.setParameter("fullName", fullName);
		return (Repository) query.uniqueResult();
	}

	public long getWorkflowsRunsCount(String fullName){
		Repository repo = getRepositoryByFullName(fullName);
		Query query = HibernateUtil.getSession().createQuery("SELECT COUNT(*) from gha_workflowRun where repo = :repo");
		query.setParameter("repo", repo);
		Long count = (Long) query.uniqueResult();
		return count;
	}

	public long getCommitsCount(String fullName){
		Repository repo = getRepositoryByFullName(fullName);
		Query query = HibernateUtil.getSession().createQuery("SELECT COUNT(*) from Commit where repo = :repo");
		query.setParameter("repo", repo);
		Long count = (Long) query.uniqueResult();
		return count;
	}
}