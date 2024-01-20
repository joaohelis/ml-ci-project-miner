package datamining.machinelearningprojectsminer.dao.hibernate;

import org.hibernate.Query;

import datamining.machinelearningprojectsminer.dao.PullRequestDAO;
import datamining.machinelearningprojectsminer.models.PullRequest;

public class HibernatePullRequestDAO extends HibernateDAO<PullRequest, Long> implements PullRequestDAO {

	public HibernatePullRequestDAO(){
		super(PullRequest.class);
	}
	
	@Override
	public PullRequest getByNumber(Integer number) {
		Query query = HibernateUtil.getSession().createQuery("from "+getTypeClass().getName()+" where number = :number");
		query.setParameter("number", number);
		return (PullRequest) query.uniqueResult();
	}
}