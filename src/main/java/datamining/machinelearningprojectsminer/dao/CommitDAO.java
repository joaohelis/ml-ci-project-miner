package datamining.machinelearningprojectsminer.dao;

import datamining.machinelearningprojectsminer.models.Commit;
import datamining.machinelearningprojectsminer.models.Repository;

public interface CommitDAO extends GenericDAO<Commit, Long> {

    public Commit getCommitByRepositorySHA(Repository repo, String sha);

}