/**
 * 
 */
package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author joaohelis
 *
 */

@Entity(name="codecovRepoInfo")
public class CodecovRepoInfo {
	
	@Id @GeneratedValue
	private Long id;

	private String name;			
	
	private String branch;	
	
	private Boolean active;
	private Boolean activated;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updatestamp;
	
	private Integer files;

	@Column(name = "total_lines")
	private Integer lines;

	private Integer hits;
	private Integer misses;
	private Integer partials;
	private Integer branches;
	private Integer methods;
	private Integer sessions;
	
	private Float coverage;	

	public CodecovRepoInfo(){}

	public CodecovRepoInfo(String name, Date updatestamp, String branch, Boolean active, Boolean activated,
			Integer files, Integer lines, Integer hits, Integer misses, Integer partials, Integer branches,
			Integer methods, Integer sessions, Float coverage) {
		this.name = name;
		this.updatestamp = updatestamp;
		this.branch = branch;
		this.active = active;
		this.activated = activated;
		this.files = files;
		this.lines = lines;
		this.hits = hits;
		this.misses = misses;
		this.partials = partials;
		this.branches = branches;
		this.methods = methods;
		this.sessions = sessions;
		this.coverage = coverage;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getUpdatestamp() {
		return updatestamp;
	}

	public void setUpdatestamp(Date updatestamp) {
		this.updatestamp = updatestamp;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getActivated() {
		return activated;
	}

	public void setActivated(Boolean activated) {
		this.activated = activated;
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

	public Float getCoverage() {
		return coverage;
	}

	public void setCoverage(Float coverage) {
		this.coverage = coverage;
	}

	@Override
	public String toString() {
		return "CodecovRepoInfo [name=" + name + ", updatestamp=" + updatestamp + ", branch=" + branch + ", active="
				+ active + ", activated=" + activated + ", coverage=" + coverage + "]";
	}
}