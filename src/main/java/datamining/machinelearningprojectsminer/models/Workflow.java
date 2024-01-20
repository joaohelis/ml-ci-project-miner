package datamining.machinelearningprojectsminer.models;

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

@Entity
public class Workflow {
    
    @Id @GeneratedValue
    private Long id;

    private Long ghId;
    private String name;    
    private String state;
    private Date createdAt;
    private Date updatedAt;
    private String html_url;
    private String path;
    private String fileName;

    private Integer jobsCountInYAMLFile;
    private Integer stepsCountInYAMLFile;

    @Type(type="text")
    private String fileContent;

    private Boolean hasConfigFile;

    private Boolean isCiWorkflow;

    @Type(type="text")
    private String isCiWorkflowCheckLog;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "workflow")
	private List<WorkflowRun> workflowsRuns;

    private Integer workflowRunsCount_ghapi;

    @ManyToOne
	private Repository repo;

    public Workflow(){        
    }

    public Workflow(Long ghId, String name, String state, Date createdAt, Date updatedAt, String html_url, String path,
            String fileName, Boolean hasConfigFile) {
        this.ghId = ghId;
        this.name = name;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.html_url = html_url;
        this.path = path;
        this.fileName = fileName;
        this.hasConfigFile = hasConfigFile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGhId() {
        return ghId;
    }

    public void setGhId(Long ghId) {
        this.ghId = ghId;
    }

    public void setWorkflowRunsCount_ghapi(Integer workflowRunsCount_ghapi) {
        this.workflowRunsCount_ghapi = workflowRunsCount_ghapi;
    }

    public Integer getWorkflowRunsCount_ghapi() {
        return workflowRunsCount_ghapi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public Boolean isCiWorkflow() {
        return isCiWorkflow;
    }

    public void setCiWorkflow(boolean isCiWorkflow) {
        this.isCiWorkflow = isCiWorkflow;
    }

    public Boolean getHasConfigFile() {
        return hasConfigFile;
    }

    public void setHasConfigFile(Boolean hasConfigFile) {
        this.hasConfigFile = hasConfigFile;
    }

    public List<WorkflowRun> getWorkflowsRuns() {
        return workflowsRuns;
    }

    public void setWorkflowsRuns(List<WorkflowRun> workflowsRuns) {
        this.workflowsRuns = workflowsRuns;
    }

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }    

    public Boolean getIsCiWorkflow() {
        return isCiWorkflow;
    }

    public void setIsCiWorkflow(Boolean isCiWorkflow) {
        this.isCiWorkflow = isCiWorkflow;
    }

    public String getIsCiWorkflowCheckLog() {
        return isCiWorkflowCheckLog;
    }

    public void setIsCiWorkflowCheckLog(String isCiWorkflowCheckLog) {
        this.isCiWorkflowCheckLog = isCiWorkflowCheckLog;
    }

    public Integer getJobsCountInYAMLFile() {
        return jobsCountInYAMLFile;
    }

    public void setJobsCountInYAMLFile(Integer jobsInYAMLFile) {
        this.jobsCountInYAMLFile = jobsInYAMLFile;
    }

    public Integer getStepsCountInYAMLFile() {
        return stepsCountInYAMLFile;
    }

    public void setStepsCountInYAMLFile(Integer stepsInYAMLFile) {
        this.stepsCountInYAMLFile = stepsInYAMLFile;
    }

    @Override
    public String toString() {
        return "Workflow [id=" + id +", name=" + name + ", state=" + state + ", hasConfigFile=" + hasConfigFile + ", isCiWorkflow="
                + isCiWorkflow + "]";
    }

}