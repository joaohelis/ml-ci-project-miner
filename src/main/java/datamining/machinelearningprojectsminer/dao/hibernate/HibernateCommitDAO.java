package datamining.machinelearningprojectsminer.dao.hibernate;

import org.hibernate.Query;

import datamining.machinelearningprojectsminer.dao.CommitDAO;
import datamining.machinelearningprojectsminer.models.Commit;
import datamining.machinelearningprojectsminer.models.Repository;

public class HibernateCommitDAO extends HibernateDAO<Commit, Long> implements CommitDAO {

	public HibernateCommitDAO(){
		super(Commit.class);
	}

	@Override
	public Commit getCommitByRepositorySHA(Repository repo, String sha) {		
		Query query = HibernateUtil.getSession().createQuery("from "+getTypeClass().getName()+" where repo = :repo and sha = :sha");
		query.setParameter("repo", repo);
		query.setParameter("sha", sha);	
		if(!query.list().isEmpty())
			return (Commit) query.list().get(query.list().size() -1 );
		else
			return null;	
	}
}