package datamining.machinelearningprojectsminer.dao;

import datamining.machinelearningprojectsminer.models.CoverageBuild;

public interface CoverageBuildDAO extends GenericDAO<CoverageBuild, Long> {

    public CoverageBuild getCoverageBuildByRepositorySHA(String fullName, String sha);

    public CoverageBuild getCoverageBuildByRepositorySHA(String fullName, String branch, String sha);

}