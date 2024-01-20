/**
 * 
 */
package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

/**
 * @author joaohelis
 *
 */
@Entity(name="coverageBuild")
public class CoverageBuild {	
		
	@Id @GeneratedValue
	private Long id;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private Repository repo;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date created_at;
	
	private String url;	
	
	@Type(type="text")
	private String commit_message;	
	private String branch;
	
	private String committer_username;
	private String committer_name;
	private String committer_email;
	
	private String commit_sha;
	
	private String repo_name;
	
	private String badge_url;
	
	private Float coverage_change;
	
	private Float covered_percent;

	private Boolean ci_passed;

	private String state;

	private Integer files;

	@Column(name = "total_lines")
	private Integer lines;
	
	private Integer hits;
	private Integer misses;
	private Integer partials;
	private Integer branches;
	private Integer methods;
	private Integer sessions;
	private Integer complexity;
	private Integer complexity_total;
	private Integer complexity_ratio;
	private Integer diff;

	public enum COVERAGE_SERVICE {
		COVERALLS,
		CODE_COV,		
	}

	@Enumerated(EnumType.STRING)
	private COVERAGE_SERVICE coverage_service;
	
	public CoverageBuild() {}

	public Date getCreated_at() {
		return created_at;
	}	

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCommit_message() {
		return commit_message;
	}

	public void setCommit_message(String commit_message) {
		this.commit_message = commit_message;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getCommitter_name() {
		return committer_name;
	}

	public void setCommitter_name(String committer_name) {
		this.committer_name = committer_name;
	}

	public String getCommitter_email() {
		return committer_email;
	}

	public void setCommitter_email(String committer_email) {
		this.committer_email = committer_email;
	}

	public String getCommit_sha() {
		return commit_sha;
	}

	public void setCommit_sha(String commit_sha) {
		this.commit_sha = commit_sha;
	}

	public String getRepo_name() {
		return repo_name;
	}

	public void setRepo_name(String repo_name) {
		this.repo_name = repo_name;
	}

	public String getBadge_url() {
		return badge_url;
	}

	public void setBadge_url(String badge_url) {
		this.badge_url = badge_url;
	}

	public Float getCoverage_change() {
		return coverage_change;
	}

	public void setCoverage_change(Float coverage_change) {
		this.coverage_change = coverage_change;
	}

	public Float getCovered_percent() {
		return covered_percent;
	}

	public void setCovered_percent(Float covered_percent) {
		this.covered_percent = covered_percent;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Repository getRepo() {
		return repo;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}	

	public COVERAGE_SERVICE getCoverage_service() {
		return coverage_service;
	}
	
	public void setCoverage_service(COVERAGE_SERVICE coverage_service) {
		this.coverage_service = coverage_service;
	}

	public Boolean getCi_passed() {
		return ci_passed;
	}

	public void setCi_passed(Boolean ci_passed) {
		this.ci_passed = ci_passed;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getFiles() {
		return files;
	}

	public void setFiles(Integer files) {
		this.files = files;
	}

	public Integer getLines() {
		return lines;
	}

	public void setLines(Integer lines) {
		this.lines = lines;
	}

	public Integer getHits() {
		return hits;
	}

	public void setHits(Integer hits) {
		this.hits = hits;
	}

	public Integer getMisses() {
		return misses;
	}

	public void setMisses(Integer misses) {
		this.misses = misses;
	}

	public Integer getPartials() {
		return partials;
	}

	public void setPartials(Integer partials) {
		this.partials = partials;
	}

	public Integer getBranches() {
		return branches;
	}

	public void setBranches(Integer branches) {
		this.branches = branches;
	}

	public Integer getMethods() {
		return methods;
	}

	public void setMethods(Integer methods) {
		this.methods = methods;
	}

	public Integer getSessions() {
		return sessions;
	}

	public void setSessions(Integer sessions) {
		this.sessions = sessions;
	}

	public Integer getComplexity() {
		return complexity;
	}

	public void setComplexity(Integer complexity) {
		this.complexity = complexity;
	}

	public Integer getComplexity_total() {
		return complexity_total;
	}

	public void setComplexity_total(Integer complexity_total) {
		this.complexity_total = complexity_total;
	}

	public Integer getComplexity_ratio() {
		return complexity_ratio;
	}

	public void setComplexity_ratio(Integer complexity_ratio) {
		this.complexity_ratio = complexity_ratio;
	}

	public Integer getDiff() {
		return diff;
	}

	public void setDiff(Integer diff) {
		this.diff = diff;
	}

	@Override
	public String toString() {
		return "CoverallsBuild [branch=" + branch + ", covered_percent=" + covered_percent + ", created_at="
				+ created_at + ", repo_name=" + repo_name + "]";
	}

	public String getCommitter_username() {
		return committer_username;
	}

	public void setCommitter_username(String committer_username) {
		this.committer_username = committer_username;
	}
}
