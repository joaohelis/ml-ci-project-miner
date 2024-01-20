package datamining.machinelearningprojectsminer.dao.hibernate;

import datamining.machinelearningprojectsminer.dao.CodecovRepoInfoDAO;
import datamining.machinelearningprojectsminer.models.CodecovRepoInfo;

public class HibernateCodecovRepoInfoDAO extends HibernateDAO<CodecovRepoInfo, Long> implements CodecovRepoInfoDAO {

	public HibernateCodecovRepoInfoDAO(){
		super(CodecovRepoInfo.class);
	}
}