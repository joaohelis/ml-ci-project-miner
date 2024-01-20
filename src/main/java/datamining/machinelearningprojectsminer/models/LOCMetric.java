package datamining.machinelearningprojectsminer.models;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity(name="locMetric")
public class LOCMetric {

    @Id @GeneratedValue
    private Long id;
    
    private String language;
    private Integer files;
    @Column(name="total_lines")
    private Integer lines;
    private Integer blanks;
    private Integer comments;
    private Integer linesOfCode;

    @ManyToOne(cascade = CascadeType.ALL)
    private Repository repo;

    public LOCMetric() {
        super();
    }

    public LOCMetric(String language, Integer files, Integer lines, Integer blanks, Integer comments,
            Integer linesOfCode, Repository repo) {
        this(language, files, lines, blanks, comments, linesOfCode);
        this.repo = repo;
    }

    public LOCMetric(String language, Integer files, Integer lines, Integer blanks, Integer comments,
            Integer linesOfCode) {
        this.language = language;
        this.files = files;
        this.lines = lines;
        this.blanks = blanks;
        this.comments = comments;
        this.linesOfCode = linesOfCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public Integer getBlanks() {
        return blanks;
    }

    public void setBlanks(Integer blanks) {
        this.blanks = blanks;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public Integer getLinesOfCode() {
        return linesOfCode;
    }

    public void setLinesOfCode(Integer linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }

    @Override
    public String toString() {
        return "LOCMetric [blanks=" + blanks + ", comments=" + comments + ", files=" + files + ", language=" + language
                + ", lines=" + lines + ", linesOfCode=" + linesOfCode + ", repo=" + (repo == null? null: repo.getFullName()) + "]";
    }
}