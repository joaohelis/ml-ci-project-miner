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

@Entity(name="pull")
public class PullRequest {

    @Id @GeneratedValue
    private Long id;
    
    private String oid;
    private Integer number;
    
    @Type(type="text")
    private String title;

    @Type(type="text")
    private String body;    

    private String state;
    private String url;
    private String authorLogin;            
    private String authorAssociation;

    private Boolean locked;
    private Date createdAt;
    private Date updatedAt;
    private Date mergedAt;
    private Date closedAt;
        
    private String mergeCommitSHA;

    // @ManyToMany(mappedBy = "pullRequests")
    // private List<PullRequestLabel> labels;        

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "pullRequest", cascade = CascadeType.ALL)
    private List<PullRequestComment> comments;

    @Type(type="text")
    private String commentsAsText;
        
    private String baseReposFullName;
    private String baseRefName;
        
    private String headReposFullName;
    private String headRefName;
        
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private Integer totalCommentsCount;

    private Integer commitsCount;

    @Type(type="text")
    private String commitsSHAsList;  
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "pullRequest")
    private List<Commit> commits;
        
    private Integer reactionsCount;
    private Integer commentsCount;
    private Boolean merged;
    private String mergedByLogin;

    private Boolean mergeable;
    private Integer openIssuesWhenPrWasSubmitted;
    private Boolean isDraft;

    private String beforeCursorGraphQL;

    @ManyToOne
    private Repository repo;

    public PullRequest() {
        this.comments = new ArrayList<PullRequestComment>();
    }

    public List<PullRequestComment> getComments() {
        return comments;
    }
    public void setComments(List<PullRequestComment> comments) {
        this.comments = comments;
    }    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOid() {
        return oid;
    }
    public void setOid(String oid) {
        this.oid = oid;
    }
    public void setBeforeCursorGraphQL(String beforeCursorGraphQL) {
        this.beforeCursorGraphQL = beforeCursorGraphQL;
    }
    public String getBeforeCursorGraphQL() {
        return beforeCursorGraphQL;
    }
    public Repository getRepo() {
        return repo;
    }
    public void setRepo(Repository repo) {
        this.repo = repo;
    }
    public Integer getNumber() {
        return number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getAuthorLogin() {
        return authorLogin;
    }
    public void setAuthorLogin(String authorLogin) {
        this.authorLogin = authorLogin;
    }
    public String getAuthorAssociation() {
        return authorAssociation;
    }
    public void setAuthorAssociation(String authorAssociation) {
        this.authorAssociation = authorAssociation;
    }
    public Boolean getLocked() {
        return locked;
    }
    public void setLocked(Boolean locked) {
        this.locked = locked;
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
    public Date getMergedAt() {
        return mergedAt;
    }
    public void setMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt;
    }
    public Date getClosedAt() {
        return closedAt;
    }
    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }
    public String getMergeCommitSHA() {
        return mergeCommitSHA;
    }
    public void setMergeCommitSHA(String mergeCommitSHA) {
        this.mergeCommitSHA = mergeCommitSHA;
    }
    // public List<PullRequestLabel> getLabels() {
    //     return labels;
    // }
    // public void setLabels(List<PullRequestLabel> labels) {
    //     this.labels = labels;
    // }
    public String getBaseReposFullName() {
        return baseReposFullName;
    }
    public void setBaseReposFullName(String baseReposFullName) {
        this.baseReposFullName = baseReposFullName;
    }
    public String getBaseRefName() {
        return baseRefName;
    }
    public void setBaseRefName(String baseRefName) {
        this.baseRefName = baseRefName;
    }
    public String getHeadReposFullName() {
        return headReposFullName;
    }
    public void setHeadReposFullName(String headReposFullName) {
        this.headReposFullName = headReposFullName;
    }
    public String getHeadRefName() {
        return headRefName;
    }
    public void setHeadRefName(String headRefName) {
        this.headRefName = headRefName;
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
    public Integer getTotalCommentsCount() {
        return totalCommentsCount;
    }
    public void setTotalCommentsCount(Integer totalCommentsCount) {
        this.totalCommentsCount = totalCommentsCount;
    }
    public Integer getCommitsCount() {
        return commitsCount;
    }
    public void setCommitsCount(Integer commitsCount) {
        this.commitsCount = commitsCount;
    }
    public String getCommitsSHAsList() {
        return commitsSHAsList;
    }
    public void setCommitsSHAsList(String commitsSHAsList) {
        this.commitsSHAsList = commitsSHAsList;
    }
    public List<Commit> getCommits() {
        return commits;
    }
    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }
    public Integer getReactionsCount() {
        return reactionsCount;
    }
    public void setReactionsCount(Integer reactionsCount) {
        this.reactionsCount = reactionsCount;
    }
    public Integer getCommentsCount() {
        return commentsCount;
    }
    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }
    public Boolean getMerged() {
        return merged;
    }
    public void setMerged(Boolean merged) {
        this.merged = merged;
    }
    public String getMergedByLogin() {
        return mergedByLogin;
    }
    public void setMergedByLogin(String mergedByLogin) {
        this.mergedByLogin = mergedByLogin;
    }
    public Boolean getMergeable() {
        return mergeable;
    }
    public void setMergeable(Boolean mergeable) {
        this.mergeable = mergeable;
    }
    public Integer getOpenIssuesWhenPrWasSubmitted() {
        return openIssuesWhenPrWasSubmitted;
    }
    public void setOpenIssuesWhenPrWasSubmitted(Integer openIssuesWhenPrWasSubmitted) {
        this.openIssuesWhenPrWasSubmitted = openIssuesWhenPrWasSubmitted;
    }
    public Boolean getIsDraft() {
        return isDraft;
    }
    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }

    

    @Override
    public String toString() {
        return "PullRequest [\"number=" + number + ", createdAt=" + createdAt + ", additions="
                + additions + ", deletions=" + deletions + ", changedFiles=" + changedFiles + "]";
    }

    public String getCommentsAsText() {
        return commentsAsText;
    }

    public void setCommentsAsText(String commentsAsText) {
        this.commentsAsText = commentsAsText;
    }
}




    

