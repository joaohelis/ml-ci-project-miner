/**
 * 
 */
package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Joao Helis Bernardo
 *
 * 2023
 */
@Entity(name="travisBuild")
public class TravisBuild {
	
	@Id @GeneratedValue
	private Long id;
	private Long trID;
	private String number;
				
	@Temporal(TemporalType.TIMESTAMP)
	private Date startedAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishedAt;

	private Integer duration;
	private String eventType;
	private String previousState;

	private Integer pullRequestNumber;
	private  Boolean priority;
	private String commitSha;
			
	private String state;
	private String branch;

	@OneToOne
	private CoverageBuild coverallsBuild;

	@ManyToOne
	private Repository repo;	
	
	public TravisBuild(){}	

	public TravisBuild(Long trID, String number, Date startedAt, Date finishedAt, Integer duration, String eventType,
			String previousState, Integer pullRequestNumber, Boolean priority, String commitSha, String state,
			String branch) {
		this.trID = trID;
		this.number = number;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.duration = duration;
		this.eventType = eventType;
		this.previousState = previousState;
		this.pullRequestNumber = pullRequestNumber;
		this.priority = priority;
		this.commitSha = commitSha;
		this.state = state;
		this.branch = branch;
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

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date started_at) {
		this.startedAt = started_at;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finished_at) {
		this.finishedAt = finished_at;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}	

	public Long getTrID() {
		return trID;
	}

	public void setTrID(Long trID) {
		this.trID = trID;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getPreviousState() {
		return previousState;
	}

	public void setPreviousState(String previousState) {
		this.previousState = previousState;
	}

	public Integer getPullRequestNumber() {
		return pullRequestNumber;
	}

	public void setPullRequestNumber(Integer pullRequestNumber) {
		this.pullRequestNumber = pullRequestNumber;
	}

	public Boolean getPriority() {
		return priority;
	}

	public void setPriority(Boolean priority) {
		this.priority = priority;
	}

	public String getCommitSha() {
		return commitSha;
	}

	public void setCommitSha(String commitSha) {
		this.commitSha = commitSha;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}	

	public CoverageBuild getCoverallsBuild() {
		return coverallsBuild;
	}

	public void setCoverallsBuild(CoverageBuild coverallsBuild) {
		this.coverallsBuild = coverallsBuild;
	}

	@Override
	public String toString() {
		return "TravisBuild [id=" + id + ", repo=" + repo + ", started_at=" + startedAt
				+ ", finished_at=" + finishedAt + ", branch=" + branch + ", state=" + state + "]";
	}

}