package datamining.machinelearningprojectsminer.models;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class Release {
    
    private Long id;
    private Long ghId;
    private String node_id;

    private String name;
    private String tag_name;
    
    private String url;
    private String html_url;
    
    private String author_login;    
    private String target_commitsh;    
    
    private Boolean draft;
    private Boolean prerelease;
    
    private Date created_at;
    private Date published_at;
    
    private String description;

    private Integer total_commits;
    private Integer total_files;


    



}