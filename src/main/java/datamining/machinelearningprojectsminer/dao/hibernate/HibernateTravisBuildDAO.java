package datamining.machinelearningprojectsminer.dao.hibernate;

import datamining.machinelearningprojectsminer.dao.TravisBuildDAO;
import datamining.machinelearningprojectsminer.models.TravisBuild;

public class HibernateTravisBuildDAO extends HibernateDAO<TravisBuild, Long> implements TravisBuildDAO {

	public HibernateTravisBuildDAO(){
		super(TravisBuild.class);
	}
}