package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

@Entity
public class Commit {

    @Id @GeneratedValue
	private Long id;

    private String sha;
    private String url;   

    private String committerName;
    private String committerEmail;
    private Date committerDate;
    
    private String authorName;
    private String authorEmail;
    private Date authorDate;
    
    private String branch;

    @Type(type="text")
    private String message;
    
    private Integer commentCount;
    private Integer churn;
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;

    @ManyToOne(cascade=CascadeType.ALL)
	private Repository repo;

    @ManyToOne
	private PullRequest pullRequest;

    public Commit(){
        super();
    }

    public Commit(String sha, String url, String committerName, String committerEmail, Date committerDate,
            String authorName, String authorEmail, Date authorDate, String branch, String message,
            Integer commentCount) {
        this.sha = sha;
        this.url = url;
        this.committerName = committerName;
        this.committerEmail = committerEmail;
        this.committerDate = committerDate;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.authorDate = authorDate;
        this.branch = branch;
        this.message = message;
        this.commentCount = commentCount;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public Integer getChurn() {
        return churn;
    }

    public void setChurn(Integer churn) {
        this.churn = churn;
    }

    public Integer getAdditions() {
        return additions;
    }

    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    public Integer getDeletions() {
        return deletions;
    }

    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public Integer getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(Integer changedFiles) {
        this.changedFiles = changedFiles;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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
}