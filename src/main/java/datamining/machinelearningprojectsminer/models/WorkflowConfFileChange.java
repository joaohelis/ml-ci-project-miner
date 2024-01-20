package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;

@Entity
public class WorkflowConfFileChange {

    @Id @GeneratedValue
	private Long id;

    private String sha;

    private String committerName;
    private String committerEmail;
    private Date committerDate;
    
    private String authorName;
    private String authorEmail;
    private Date authorDate;

    @Type(type="text")
    private String message;

    @ManyToOne(cascade=CascadeType.ALL)
	private Repository repo;

    @OneToOne
	private Commit commit;

    @ManyToOne
	private Workflow workflow;

    public WorkflowConfFileChange(){
        super();
    }

    public WorkflowConfFileChange(String sha, String committerName, String committerEmail, Date committerDate,
            String authorName, String authorEmail, Date authorDate, String message, Repository repo,
            Workflow workflow, Commit commit) {
        this.sha = sha;
        this.committerName = committerName;
        this.committerEmail = committerEmail;
        this.committerDate = committerDate;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.authorDate = authorDate;
        this.message = message;
        this.repo = repo;
        this.workflow = workflow;
        this.commit = commit;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String commiterName) {
        this.committerName = commiterName;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String commiterEmail) {
        this.committerEmail = commiterEmail;
    }

    public Date getCommitterDate() {
        return committerDate;
    }

    public void setCommitterDate(Date commiterDate) {
        this.committerDate = commiterDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }    

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
    
    public Date getAuthorDate() {
        return authorDate;
    }
    
    public void setAuthorDate(Date authorDate) {
        this.authorDate = authorDate;    
    }    

    @Override
    public String toString() {
        return "Commit [repo=" + ((repo == null)? null : repo.getFullName()) + "commiterDate=" + committerDate + ", sha=" + sha + "]";
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }
   
}