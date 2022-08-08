package nz.ac.canterbury.seng302.portfolio.DTO;

import nz.ac.canterbury.seng302.portfolio.evidence.WebLinkDTO;

import java.util.List;

/**
 * Evidence Data Transfer Object, used for representing the data required to make a piece of evidence.
 */
public class EvidenceDTO {
    String title;
    String date;
    String description;
    List<WebLinkDTO> webLinks;
    List<String> categories;
    Long projectId;


    public EvidenceDTO(String title, String date, String description, List<WebLinkDTO> webLinks, List<String> categories, Long projectId) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.webLinks = webLinks;
        this.projectId = projectId;
        this.categories = categories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<WebLinkDTO> getWebLinks() {
        return webLinks;
    }

    public void setWebLinks(List<WebLinkDTO> webLinks) {
        this.webLinks = webLinks;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
