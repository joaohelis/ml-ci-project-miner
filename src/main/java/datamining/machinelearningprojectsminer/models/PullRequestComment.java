package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

@Entity(name="pullComment")
public class PullRequestComment {
 
    @Id @GeneratedValue
    private Long id;

    private String oid;
    private String url;
    private String authorLogin;
    private String authorAssociation;
    @Type(type="text")
    private String bodyText;
    private Date createdAt;

    @ManyToOne
    private PullRequest pullRequest;

    public PullRequestComment(){
        
    }

    public PullRequestComment(String oid, String url, String authorLogin, String authorAssociation, String bodyText,
            Date createdAt) {
        this.oid = oid;
        this.url = url;
        this.authorLogin = authorLogin;
        this.authorAssociation = authorAssociation;
        this.bodyText = bodyText;
        this.createdAt = createdAt;
    }

    public void setPullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
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

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PullRequestComment [authorLogin=" + authorLogin + ", bodyText=" + bodyText + ", createdAt=" + createdAt
                + "]";
    }
}
