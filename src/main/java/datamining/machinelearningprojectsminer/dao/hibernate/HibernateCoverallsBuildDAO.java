package datamining.machinelearningprojectsminer.dao.hibernate;

import org.hibernate.Query;

import datamining.machinelearningprojectsminer.dao.CoverageBuildDAO;
import datamining.machinelearningprojectsminer.models.CoverageBuild;

public class HibernateCoverallsBuildDAO extends HibernateDAO<CoverageBuild, Long> implements CoverageBuildDAO {

	public HibernateCoverallsBuildDAO(){
		super(CoverageBuild.class);
	}

	@Override
	public CoverageBuild getCoverageBuildByRepositorySHA(String fullName, String sha) {
		Query query = HibernateUtil.getSession().createQuery("from "+getTypeClass().getName()+" where repo_name = :fullName and commit_sha = :sha");
		query.setParameter("fullName", fullName);
		query.setParameter("sha", sha);	
		if(!query.list().isEmpty())
			return (CoverageBuild) query.list().get(query.list().size() -1 );
		else
			return null;
	}

	@Override
	public CoverageBuild getCoverageBuildByRepositorySHA(String fullName, String branch, String sha) {
		Query query = HibernateUtil.getSession().createQuery("from "+getTypeClass().getName()+ " where repo_name = :fullName and commit_sha = :sha and branch = :branch");
		query.setParameter("fullName", fullName);
		query.setParameter("sha", sha);	
		query.setParameter("branch", branch);
		if(!query.list().isEmpty())
			return (CoverageBuild) query.list().get(query.list().size() -1 );
		else
			return null;
	}
}