/**
 * 
 */
package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author joaohelis
 *
 */

@Entity(name="coverallsRepoInfo")
public class CoverallsRepoInfo {
	
	@Id @GeneratedValue
	private Long id;
		
	private String service;	
	private String name;
	private Boolean comment_on_pull_requests;
	private Boolean send_build_status;
	private Float commit_status_fail_threshold;
	private Float commit_status_fail_change_threshold;
	private Date created_at;
	private Date updated_at;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private Repository repo;
	
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getComment_on_pull_requests() {
		return comment_on_pull_requests;
	}
	public void setComment_on_pull_requests(Boolean comment_on_pull_requests) {
		this.comment_on_pull_requests = comment_on_pull_requests;
	}
	public Boolean getSend_build_status() {
		return send_build_status;
	}
	public void setSend_build_status(Boolean send_build_status) {
		this.send_build_status = send_build_status;
	}
	public Float getCommit_status_fail_threshold() {
		return commit_status_fail_threshold;
	}
	public void setCommit_status_fail_threshold(Float commit_status_fail_threshold) {
		this.commit_status_fail_threshold = commit_status_fail_threshold;
	}
	public Float getCommit_status_fail_change_threshold() {
		return commit_status_fail_change_threshold;
	}
	public void setCommit_status_fail_change_threshold(Float commit_status_fail_change_threshold) {
		this.commit_status_fail_change_threshold = commit_status_fail_change_threshold;
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

	@Override
	public String toString() {
		return "CoverallsRepoInfo [comment_on_pull_requests=" + comment_on_pull_requests
				+ ", commit_status_fail_change_threshold=" + commit_status_fail_change_threshold
				+ ", commit_status_fail_threshold=" + commit_status_fail_threshold + ", created_at=" + created_at
				+ ", id=" + id + ", name=" + name + ", send_build_status=" + send_build_status + ", service=" + service
				+ ", updated_at=" + updated_at + "]";
	}
	
}