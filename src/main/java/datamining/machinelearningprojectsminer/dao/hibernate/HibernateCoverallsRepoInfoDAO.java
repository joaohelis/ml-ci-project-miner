package datamining.machinelearningprojectsminer.dao.hibernate;

import datamining.machinelearningprojectsminer.dao.CoverallsRepoInfoDAO;
import datamining.machinelearningprojectsminer.models.CoverallsRepoInfo;

public class HibernateCoverallsRepoInfoDAO extends HibernateDAO<CoverallsRepoInfo, Long> implements CoverallsRepoInfoDAO {

	public HibernateCoverallsRepoInfoDAO(){
		super(CoverallsRepoInfo.class);
	}
}