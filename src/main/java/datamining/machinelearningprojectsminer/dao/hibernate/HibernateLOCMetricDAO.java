package datamining.machinelearningprojectsminer.dao.hibernate;

import datamining.machinelearningprojectsminer.dao.LOCMetricDAO;
import datamining.machinelearningprojectsminer.models.LOCMetric;

public class HibernateLOCMetricDAO extends HibernateDAO<LOCMetric, Long> implements LOCMetricDAO {

	public HibernateLOCMetricDAO(){
		super(LOCMetric.class);
	}
}