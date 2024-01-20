/**
 * 
 */
package datamining.machinelearningprojectsminer.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

/**
 * @author Joao Helis Bernardo
 *
 * 2023
 */

@Entity(name="repository")
public class Repository {
		
	@Id @GeneratedValue
	private Long id;

	private Long ghId;	

	private Boolean isToyProjectOrStudyGuide;

	private String owner;
	private String name;	
	private String fullName;
	private String projectCategory;	
	private String ownerType;

	private String url;
	private Boolean isPrivate;
	private String htmlUrl;
	
	@Type(type="text")
	private String description;
	private Boolean fork;
	private String visibility;
	private Integer forks;
	private Integer open_issues;
	private Integer watchers;	
	private String license;
	private String defaultBranch;

	private Integer totalContributorsGitHubPage;
	private Integer totalReleasesGitHubPage;

	private Boolean hasTravisConfigurationFile;
	private Boolean hasGHActionsConfigurationFile;
	private Integer workflowsCountGHApi;
	private Integer workflowRunsCount_ghapi;		
	private Integer workflowRunsTriggeredByPushInDefaultBranchCount_ghapi;
	private Integer workflowRunsTriggeredByPrInDefaultBranchCount_ghapi;
	private Integer workflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi;

	private Integer commitsCountInDefaultBranch_ghapi;	
	private Integer commitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi;	

	private Date firstWorkflowRunAt_ghapi;
	private Date lastWorkflowRunAt_ghapi;
	private Integer firstAndLastWorkflowRunIntervalInDays_ghapi;

	private Date firstCIWorkflowRunAt;
	private Date lastCIWorkflowRunAt;
	private Integer firstAndLastCIWorkflowRunIntervalInDays;

	private Integer pullRequestsCount_ghapi;

	@Temporal(TemporalType.TIMESTAMP)
	private Date firstTravisCIBuildIn;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastTravisCIBuildIn;

	private Integer firstAndLastTravisBuildIntervalInDays;

	// @Temporal(TemporalType.TIMESTAMP)
	// private Date firstWorkflowRunInDefaultBranchAt;

	// @Temporal(TemporalType.TIMESTAMP)
	// private Date lastWorkflowRunInDefaultBranchAt;

	// private Integer firstAndLastWorkflowRunInDefaultBranchIntervalInDays;
	
	private Integer coverallsBuildsCount;
	private Integer codecovBuildsCount;

	private Integer travisBuildsCount;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startedWithCoveralls;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date startedWithTravis;	

	private Boolean isCodecovActivated;
	private Boolean isCodecovActive;

	@OneToOne
	private CodecovRepoInfo codecovRepoInfo;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created_at;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated_at;

	@Temporal(TemporalType.TIMESTAMP)
	private Date pushed_at;

	private Long size;
	
	private String language;

	private String countLOCLanguage;

	private Long sloc;

	private String loc_size;

	private boolean isOutOfMLDataset;

	private boolean isPartOfFinalStudyDataset;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "repo")
	private List<TravisBuild> travisBuilds;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "repo")
	private List<CoverageBuild> coverallsBuilds;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "repo")
	private List<Commit> commits;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "repo")
	private List<LOCMetric> locMetrics;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "repo")
	private List<WorkflowRun> workflowsRuns;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "repo")
	private List<Workflow> workflows;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "repo")
	private List<PullRequest> pulls;	
	
	public Repository(){
		this.travisBuilds = new ArrayList<TravisBuild>();
		this.coverallsBuilds = new ArrayList<CoverageBuild>();
		this.commits = new ArrayList<Commit>();
		this.locMetrics = new ArrayList<LOCMetric>();
		this.workflowsRuns = new ArrayList<WorkflowRun>();
		this.workflows = new ArrayList<Workflow>();
		this.pulls = new ArrayList<PullRequest>();
	}

	public Repository(String fullName) {
		this();
		this.fullName = fullName;
	}			

	public Long getGhId() {
		return ghId;
	}

	public void setGhId(Long ghId) {
		this.ghId = ghId;
	}

	public void setLoc_size(String loc_size) {
		this.loc_size = loc_size;
	}

	public String getLoc_size() {
		return loc_size;
	}

	public Integer getCommitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi() {
		return commitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi;
	}

	public void setCommitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi(
			Integer commitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi) {
		this.commitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi = commitsCountInDefaultBranchAfterGHAAdaptionPeriod_ghapi;
	}

	public void setOutOfMLDataset(boolean isOutOfMLDataset) {
		this.isOutOfMLDataset = isOutOfMLDataset;
	}

	public boolean isOutOfMLDataset() {
		return isOutOfMLDataset;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String html_url) {
		this.htmlUrl = html_url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getFork() {
		return fork;
	}

	public void setFork(Boolean fork) {
		this.fork = fork;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public int getForks() {
		return forks;
	}

	public void setForks(int forks) {
		this.forks = forks;
	}

	public int getOpen_issues() {
		return open_issues;
	}

	public void setOpen_issues(Integer open_issues) {
		this.open_issues = open_issues;
	}

	public void setWatchers(Integer watchers) {
		this.watchers = watchers;
	}	

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public Date getStartedWithCoveralls() {
		return startedWithCoveralls;
	}

	public void setStartedWithCoveralls(Date startedWithCoveralls) {
		this.startedWithCoveralls = startedWithCoveralls;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}

	public Date getPushed_at() {
		return pushed_at;
	}

	public void setPushed_at(Date pushed_at) {
		this.pushed_at = pushed_at;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getWatchers() {
		return watchers;
	}	

	public Date getStartedWithTravis() {
		return startedWithTravis;
	}

	public void setStartedWithTravis(Date startedWithTravis) {
		this.startedWithTravis = startedWithTravis;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}	

	public List<TravisBuild> getTravisBuilds() {
		return travisBuilds;
	}

	public void setTravisBuilds(List<TravisBuild> travisBuilds) {
		this.travisBuilds = travisBuilds;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	

	public Integer getWorkflowsCountGHApi() {
		return workflowsCountGHApi;
	}

	public void setWorkflowsCountGHApi(Integer ghActions_workflows_count) {
		this.workflowsCountGHApi = ghActions_workflows_count;
	}

	public Integer getWorkflowRunsCount_ghapi() {
		return workflowRunsCount_ghapi;
	}

	public void setWorkflowRunsCount_ghapi(Integer workflow_runs_count) {
		this.workflowRunsCount_ghapi = workflow_runs_count;
	}

	public String getProjectCategory() {
		return projectCategory;
	}

	public void setProjectCategory(String projectCategory) {
		this.projectCategory = projectCategory;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}	

	public Integer getCoverallsBuildsCount() {
		return coverallsBuildsCount;
	}

	public void setCoverallsBuildsCount(Integer coverallsBuildsCount) {
		this.coverallsBuildsCount = coverallsBuildsCount;
	}	

	public Boolean getIsToyProjectOrStudyGuide() {
		return isToyProjectOrStudyGuide;
	}

	public void setIsToyProjectOrStudyGuide(Boolean isToyProjectOrStudyGuide) {
		this.isToyProjectOrStudyGuide = isToyProjectOrStudyGuide;
	}		

	public Integer getTravisBuildsCount() {
		return travisBuildsCount;
	}

	public void setTravisBuildsCount(Integer travisBuildsCount) {
		this.travisBuildsCount = travisBuildsCount;
	}	

	public void setForks(Integer forks) {
		this.forks = forks;
	}

	public Date getFirstTravisCIBuildIn() {
		return firstTravisCIBuildIn;
	}

	public void setFirstTravisCIBuildIn(Date firstTravisCIBuildIn) {
		this.firstTravisCIBuildIn = firstTravisCIBuildIn;
	}

	public Date getLastTravisCIBuildIn() {
		return lastTravisCIBuildIn;
	}

	public void setLastTravisCIBuildIn(Date lastTravisCIBuildIn) {
		this.lastTravisCIBuildIn = lastTravisCIBuildIn;
	}

	public Boolean getHasTravisConfigurationFile() {
		return hasTravisConfigurationFile;
	}

	public void setHasGHActionsConfigurationFile(Boolean hasGHActionsConfigurationFile) {
		this.hasGHActionsConfigurationFile = hasGHActionsConfigurationFile;
	}

	public Boolean getHasGHActionsfigurationFile() {
		return hasGHActionsConfigurationFile;
	}

	public void setHasTravisConfigurationFile(Boolean hasTravisConfigurationFile) {
		this.hasTravisConfigurationFile = hasTravisConfigurationFile;
	}

	public Integer getFirstAndLastTravisBuildIntervalInDays() {
		return firstAndLastTravisBuildIntervalInDays;
	}

	public void setFirstAndLastTravisBuildIntervalInDays(Integer firstAndLastTravisBuildIntervalInDays) {
		this.firstAndLastTravisBuildIntervalInDays = firstAndLastTravisBuildIntervalInDays;
	}

	public List<CoverageBuild> getCoverallsBuilds() {
		return coverallsBuilds;
	}

	public void setCoverallsBuilds(List<CoverageBuild> coverallsBuilds) {
		this.coverallsBuilds = coverallsBuilds;
	}

	public List<Commit> getCommits() {
		return commits;
	}

	public void setCommits(List<Commit> commits) {
		this.commits = commits;
	}	

	public List<LOCMetric> getLocMetrics() {
		return locMetrics;
	}

	public void setLocMetrics(List<LOCMetric> locMetrics) {
		this.locMetrics = locMetrics;
	}

	public Long getSloc() {
		return sloc;
	}

	public void setSloc(Long sloc) {
		this.sloc = sloc;
	}

	public String getCountLOCLanguage() {
		return countLOCLanguage;
	}

	public void setCountLOCLanguage(String countLOCLanguage) {
		this.countLOCLanguage = countLOCLanguage;
	}
	
	public Boolean getHasGHActionsConfigurationFile() {
		return hasGHActionsConfigurationFile;
	}

	public List<WorkflowRun> getWorkflowsRuns() {
		return workflowsRuns;
	}

	public void setWorkflowsRuns(List<WorkflowRun> workflowsRuns) {
		this.workflowsRuns = workflowsRuns;
	}

	public Integer getCommitsCountInDefaultBranch_ghapi() {
		return commitsCountInDefaultBranch_ghapi;
	}

	public void setCommitsCountInDefaultBranch_ghapi(Integer commitsCountInMaster_ghapi) {
		this.commitsCountInDefaultBranch_ghapi = commitsCountInMaster_ghapi;
	}

	public List<Workflow> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
	}

	public Integer getWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi() {
		return workflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi;
	}

	public void setWorkflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi(
			Integer workflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi) {
		this.workflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi = workflowRunsTriggeredByPushOrPrInDefaultBranchCount_ghapi;
	}

	public void setWorkflowRunsTriggeredByPushCount_ghapi(Integer workflow_runs_by_event_count) {
		this.workflowRunsTriggeredByPushInDefaultBranchCount_ghapi = workflow_runs_by_event_count;
	}

	public Integer getWorkflowRunsTriggeredByPushInDefaultBranchCount_ghapi() {
		return workflowRunsTriggeredByPushInDefaultBranchCount_ghapi;
	}

	public void setWorkflowRunsTriggeredByPrCount_ghapi(Integer workflow_runs_by_event_count) {
		this.workflowRunsTriggeredByPrInDefaultBranchCount_ghapi = workflow_runs_by_event_count;
	}

	public Integer getWorkflowRunsTriggeredByPrInDefaultBranchCount_ghapi() {
		return workflowRunsTriggeredByPrInDefaultBranchCount_ghapi;
	}

	// public void setFirstWorkflowRunInDefaultBranchAt(Date firstWorkflowRunInDefaultBranchAt) {
	// 	this.firstWorkflowRunInDefaultBranchAt = firstWorkflowRunInDefaultBranchAt;
	// }

	public void setFirstAndLastWorkflowRunIntervalInDays_ghapi(Integer firstAndLastWorkflowRunIntervalInDays_ghapi) {
		this.firstAndLastWorkflowRunIntervalInDays_ghapi = firstAndLastWorkflowRunIntervalInDays_ghapi;
	}

	public Integer getFirstAndLastWorkflowRunIntervalInDays_ghapi() {
		return firstAndLastWorkflowRunIntervalInDays_ghapi;
	}

	public void setFirstWorkflowRunAt_ghapi(Date firstWorkflowRunAt_ghapi) {
		this.firstWorkflowRunAt_ghapi = firstWorkflowRunAt_ghapi;
	}

	public Date getFirstWorkflowRunAt_ghapi() {
		return firstWorkflowRunAt_ghapi;
	}

	public void setLastWorkflowRunAt_ghapi(Date lastWorkflowRunAt_ghapi) {
		this.lastWorkflowRunAt_ghapi = lastWorkflowRunAt_ghapi;
	}

	public Date getLastWorkflowRunAt_ghapi() {
		return lastWorkflowRunAt_ghapi;
	}

	public void setPulls(List<PullRequest> pulls) {
		this.pulls = pulls;
	}

	public List<PullRequest> getPulls() {
		return pulls;
	}

	// public Date getFirstWorkflowRunInDefaultBranchAt() {
	// 	return firstWorkflowRunInDefaultBranchAt;
	// }

	// public void setLastWorkflowRunInDefaultBranchAt(Date lastWorkflowRunInDefaultBranchAt) {
	// 	this.lastWorkflowRunInDefaultBranchAt = lastWorkflowRunInDefaultBranchAt;
	// }

	// public Date getLastWorkflowRunInDefaultBranchAt() {
	// 	return lastWorkflowRunInDefaultBranchAt;
	// }

	// public void setFirstAndLastWorkflowRunInDefaultBranchIntervalInDays(Integer firstAndLastWorkflowRunInDefaultBranchIntervalInDays) {
	// 	this.firstAndLastWorkflowRunInDefaultBranchIntervalInDays = firstAndLastWorkflowRunInDefaultBranchIntervalInDays;
	// }

	// public Integer getFirstAndLastWorkflowRunInDefaultBranchIntervalInDays() {
	// 	return firstAndLastWorkflowRunInDefaultBranchIntervalInDays;
	// }
	
	public void setPullRequestsCount_ghapi(Integer pullRequestsCount) {
		this.pullRequestsCount_ghapi = pullRequestsCount;
	}

	public Integer getPullRequestsCount_ghapi() {
		return pullRequestsCount_ghapi;
	}

	public void setWorkflowRunsTriggeredByPushInDefaultBranchCount_ghapi(
			Integer workflowRunsTriggeredByPushInDefaultBranchCount_ghapi) {
		this.workflowRunsTriggeredByPushInDefaultBranchCount_ghapi = workflowRunsTriggeredByPushInDefaultBranchCount_ghapi;
	}

	public void setWorkflowRunsTriggeredByPrInDefaultBranchCount_ghapi(
			Integer workflowRunsTriggeredByPrInDefaultBranchCount_ghapi) {
		this.workflowRunsTriggeredByPrInDefaultBranchCount_ghapi = workflowRunsTriggeredByPrInDefaultBranchCount_ghapi;
	}

	public Boolean getIsCodecovActivated() {
		return isCodecovActivated;
	}

	public void setIsCodecovActivated(Boolean isCodecovActivated) {
		this.isCodecovActivated = isCodecovActivated;
	}

	public Boolean getIsCodecovActive() {
		return isCodecovActive;
	}

	public void setIsCodecovActive(Boolean isCodecovActive) {
		this.isCodecovActive = isCodecovActive;
	}

	public String toString() {
		return "Repository [created_at=" + created_at + ", fullName=" + fullName + ", language=" + language + ", size="
				+ size + ", startedWithCoveralls=" + startedWithCoveralls + "]";
	}

	public CodecovRepoInfo getCodecovRepoInfo() {
		return codecovRepoInfo;
	}

	public void setCodecovRepoInfo(CodecovRepoInfo codecovRepoInfo) {
		this.codecovRepoInfo = codecovRepoInfo;
	}

	public Integer getCodecovBuildsCount() {
		return codecovBuildsCount;
	}

	public void setCodecovBuildsCount(Integer codecovBuildsCount) {
		this.codecovBuildsCount = codecovBuildsCount;
	}

	public boolean isPartOfFinalStudyDataset() {
		return isPartOfFinalStudyDataset;
	}

	public void setIsPartOfFinalStudyDataset(boolean isPartOfFinalStudyDataset) {
		this.isPartOfFinalStudyDataset = isPartOfFinalStudyDataset;
	}

	public Date getFirstCIWorkflowRunAt() {
		return firstCIWorkflowRunAt;
	}

	public void setFirstCIWorkflowRunAt(Date firstCIWorkflowRunAt) {
		this.firstCIWorkflowRunAt = firstCIWorkflowRunAt;
	}

	public Date getLastCIWorkflowRunAt() {
		return lastCIWorkflowRunAt;
	}

	public void setLastCIWorkflowRunAt(Date lastCIWorkflowRunAt) {
		this.lastCIWorkflowRunAt = lastCIWorkflowRunAt;
	}

	public Integer getFirstAndLastCIWorkflowRunIntervalInDays() {
		return firstAndLastCIWorkflowRunIntervalInDays;
	}

	public void setFirstAndLastCIWorkflowRunIntervalInDays(Integer firstAndLastCIWorkflowRunIntervalInDays) {
		this.firstAndLastCIWorkflowRunIntervalInDays = firstAndLastCIWorkflowRunIntervalInDays;
	}

	public void setPartOfFinalStudyDataset(boolean isPartOfFinalStudyDataset) {
		this.isPartOfFinalStudyDataset = isPartOfFinalStudyDataset;
	}

	public void setTotalContributorsGitHubPage(Integer totalContributorsGitHubPage) {
		this.totalContributorsGitHubPage = totalContributorsGitHubPage;
	}

	public Integer getTotalContributorsGitHubPage() {
		return totalContributorsGitHubPage;
	}

	public void setTotalReleasesGitHubPage(Integer totalReleasesGitHubPage) {
		this.totalReleasesGitHubPage = totalReleasesGitHubPage;
	}

	public Integer getTotalReleasesGitHubPage() {
		return totalReleasesGitHubPage;
	}
}