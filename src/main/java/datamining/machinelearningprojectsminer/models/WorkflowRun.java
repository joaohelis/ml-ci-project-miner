package datamining.machinelearningprojectsminer.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

@Entity(name="gha_workflowRun")
public class WorkflowRun {

    @Id @GeneratedValue
    private Long id;

    private Long ghId;

    @Type(type="text")
    private String name;
    private String status;
    private String conclusion;
    private Long duration;
    private String head_branch;
    private String headSha;
    private Date createdAt;
    private Date updatedAt;
    private Long diffInSecondsBetweenCreatedAtAndUpdatedAt;
    private Date head_commit_timestamp;
    private String event;
    private Long workflow_ghID;
    private String url;
    private Integer jobs_count;
    private Date run_started_at;

    @ManyToOne
	  private Repository repo;
    
    @ManyToOne
	  private Workflow workflow;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "run")
	  private List<Job> jobs;

    public WorkflowRun(){
      this.jobs = new ArrayList<Job>();
    }
    
    public WorkflowRun(Long ghId, String name, String status, String conclusion, Long duration,
      String head_branch, String headSha, Date createdAt, Date updatedAt, 
      Date head_commit_timestamp, String event) {
      this.ghId = ghId;
      this.name = name;
      this.status = status;
      this.conclusion = conclusion;
      this.duration = duration;
      this.head_branch = head_branch;
      this.headSha = headSha;
      this.createdAt = createdAt;
      this.updatedAt = updatedAt;
      this.head_commit_timestamp = head_commit_timestamp;
      this.event = event;
      this.jobs = new ArrayList<Job>();
    }

    public WorkflowRun(Long ghId, String name, String status, String conclusion, Long duration,
      String head_branch, String headSha, Date createdAt, Date updatedAt, 
      Date head_commit_timestamp, String event, Workflow workflow) {
        this(ghId, name, status, conclusion, duration, head_branch, headSha, createdAt, updatedAt, head_commit_timestamp, event);
        this.workflow = workflow;
    }

    public void setDiffInSecondsBetweenCreatedAtAndUpdatedAt(Long diffInSecondsBetweenCreatedAtAndUpdatedAt) {
        this.diffInSecondsBetweenCreatedAtAndUpdatedAt = diffInSecondsBetweenCreatedAtAndUpdatedAt;
    }

    public Long getDiffInSecondsBetweenCreatedAtAndUpdatedAt() {
        return diffInSecondsBetweenCreatedAtAndUpdatedAt;
    }

    public void setRun_started_at(Date run_started_at) {
        this.run_started_at = run_started_at;
    }

    public Date getRun_started_at() {
        return run_started_at;
    }

    public Integer getJobs_count() {
        return jobs_count;
    }

    public void setJobs_count(Integer jobs_count) {
        this.jobs_count = jobs_count;
    }

    public boolean isFromCiWorkflow(){
      if(this.workflow != null && this.workflow.isCiWorkflow()){
        return true;
      }
      return false;
    }

    public Date getHead_commit_timestamp() {
      return head_commit_timestamp;
    }

    public void setHead_commit_timestamp(Date head_commit_timestamp) {
      this.head_commit_timestamp = head_commit_timestamp;
    }

    public Repository getRepo() {
      return repo;
    }

    public void setRepo(Repository repo) {
      this.repo = repo;
    }
    
  public String getHead_branch() {
    return head_branch;
  }

  public void setHead_branch(String head_branch) {
    this.head_branch = head_branch;
  }

  public Long getId() {
    return id;
  }

  public String getStatus() {
    return status;
  }

  public String getConclusion() {
    return conclusion;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public String getHeadSha() {
    return headSha;
  }

  public boolean isPassing() {
    return "completed".equals(status) && "success".equals(conclusion);
  }

  public Long getDuration() {
    return duration;
  }

  public String getReference() {
    return headSha.substring(0, 7);
  }

  public Long getGhId() {
    return ghId;
  }

  public void setGhId(Long ghId) {
    this.ghId = ghId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setConclusion(String conclusion) {
    this.conclusion = conclusion;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public void setHeadSha(String headSha) {
    this.headSha = headSha;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    this.workflow = workflow;
  }

  public Long getWorkflow_ghID() {
    return workflow_ghID;
  }

  public void setWorkflow_ghID(Long workflow_ghID) {
    this.workflow_ghID = workflow_ghID;
  }  

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<Job> getJobs() {
      return jobs;
  }

  public void setJobs(List<Job> jobs) {
      this.jobs = jobs;
  }

  @Override
  public String toString() {
    return "WorkflowRun [(" + event + ") " + getReference() + " name=" + name +" - " + conclusion + ", " + duration
        + "s, createdAt=" + createdAt + ", updatedAt="
        + updatedAt + "]";
  }
  
}