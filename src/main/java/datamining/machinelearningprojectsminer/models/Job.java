package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity(name="job")
public class Job{
    
    @Id
    private Long ghId;

    private Long run_ghId;
    private String name;
    private String workflow_name;
    private String head_branch;
    private Integer run_attempt;
    private String head_sha;
    private String url;
    private String status;
    private String conclusion;
    private Date created_at;
    private Date started_at;
    private Date completed_at;
    private Long durationInSeconds;
    private Integer steps_count;

    @ManyToOne(fetch=FetchType.LAZY)
	private WorkflowRun run;
    
    public Job(Long ghID){
        this.ghId = ghID;
    }

    public Job(Long ghId, Long run_ghId, String name, String workflow_name, String head_branch, Integer run_attempt,
            String head_sha, String url, String status, String conclusion, Date created_at, Date started_at,
            Date completed_at, Long durationInSeconds, Integer steps_count) {
        this.ghId = ghId;
        this.run_ghId = run_ghId;
        this.name = name;
        this.workflow_name = workflow_name;
        this.head_branch = head_branch;
        this.run_attempt = run_attempt;
        this.head_sha = head_sha;
        this.url = url;
        this.status = status;
        this.conclusion = conclusion;
        this.created_at = created_at;
        this.started_at = started_at;
        this.completed_at = completed_at;
        this.durationInSeconds = durationInSeconds;
        this.steps_count = steps_count;
    }

    public Long getGhId() {
        return ghId;
    }

    public void setGhId(Long ghID) {
        this.ghId = ghID;
    }

    public Long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public Long getRun_ghId() {
        return run_ghId;
    }

    public void setRun_ghId(Long run_id) {
        this.run_ghId = run_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkflow_name() {
        return workflow_name;
    }

    public void setWorkflow_name(String workflow_name) {
        this.workflow_name = workflow_name;
    }

    public String getHead_branch() {
        return head_branch;
    }

    public void setHead_branch(String head_branch) {
        this.head_branch = head_branch;
    }

    public Integer getRun_attempt() {
        return run_attempt;
    }

    public void setRun_attempt(Integer run_attempt) {
        this.run_attempt = run_attempt;
    }

    public String getHead_sha() {
        return head_sha;
    }

    public void setHead_sha(String head_sha) {
        this.head_sha = head_sha;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getStarted_at() {
        return started_at;
    }

    public void setStarted_at(Date started_at) {
        this.started_at = started_at;
    }

    public Date getCompleted_at() {
        return completed_at;
    }

    public void setCompleted_at(Date completed_at) {
        this.completed_at = completed_at;
    }

    public WorkflowRun getRun() {
        return run;
    }

    public void setRun(WorkflowRun run) {
        this.run = run;
    }

    public Integer getSteps_count() {
        return steps_count;
    }

    public void setSteps_count(Integer steps_count) {
        this.steps_count = steps_count;
    }

    @Override
    public String toString() {
        return "Jobs [completed_at=" + completed_at + ", conclusion=" + conclusion + ", created_at=" + created_at
                + ", ghID=" + ghId + ", head_branch=" + head_branch + ", head_sha=" + head_sha + ", name=" + name
                + ", run_attempt=" + run_attempt + ", run_id=" + run_ghId + ", started_at=" + started_at + ", status="
                + status + ", url=" + url + ", workflow_name=" + workflow_name + "]";
    }
}