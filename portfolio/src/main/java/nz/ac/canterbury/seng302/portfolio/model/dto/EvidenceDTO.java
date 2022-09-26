package nz.ac.canterbury.seng302.portfolio.model.dto;

import java.util.List;

/**
 * Evidence Data Transfer Object, used for representing the data required to make a piece of evidence.
 */
public class EvidenceDTO {
    Integer id;
    String title;
    String date;
    String description;
    List<WebLinkDTO> webLinks;
    List<String> categories;
    List<String> skills;
    /**
     * The users associated with this piece of evidence.
     * Should NOT include the creator of the evidence.
     */
    List<Integer> associateIds;
    Long projectId;


    public EvidenceDTO(String title, String date, String description, List<WebLinkDTO> webLinks,
                       List<String> skills, List<String> categories, Long projectId, List<Integer> associateIds) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.webLinks = webLinks;
        this.projectId = projectId;
        this.skills = skills;
        this.categories = categories;
        this.associateIds = associateIds;
    }


    public EvidenceDTO(Integer id, String title, String date, String description, List<WebLinkDTO> webLinks,
                       List<String> skills, List<String> categories, Long projectId, List<Integer> associateIds) {
        this(title, date, description, webLinks, skills, categories, projectId, associateIds);
        this.id = id;
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

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<Integer> getAssociateIds() {
        return associateIds;
    }

    public void setAssociateIds(List<Integer> associateIds) {
        this.associateIds = associateIds;
    }

    public void addAssociatedId(Integer userId) {
        if (! associateIds.contains(userId)) {
            associateIds.add(userId);
        }
    }

    public void removeAssociatedId(Integer userId) {
            associateIds.remove(userId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
