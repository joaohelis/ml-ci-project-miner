package datamining.machinelearningprojectsminer.models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Type;


@Entity
public class PullRequestLabel {

    @Id @GeneratedValue    
    private Long id;

    private String oid;    

    private String url;
    private String name;
    private String color;
    private Boolean isDefault;
    @Type(type="text")
    private String description;

    @ManyToMany
    private List<PullRequest> pullRequests;

    private Repository repo;

    public PullRequestLabel(String oid, String url, String name, String color, Boolean isDefault, String description,
            List<PullRequest> pullRequests, Repository repo) {
        this.oid = oid;
        this.url = url;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
        this.description = description;
        this.pullRequests = pullRequests;
        this.repo = repo;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public void setPullRequests(List<PullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }

    @Override
    public String toString() {
        return "PullRequestLabel [oid=" + oid + ", name=" + name + "]";
    }    
}