package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Skill entity
 */
@Entity
@Table(name = "skills_table")
public class Skill {

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;

    /** The list of evidence this skill is associated with */
    @JsonIgnore
    @ManyToMany(mappedBy = "skills", fetch = FetchType.EAGER)
    private final List<Evidence> evidence = new ArrayList<>();


    /**
     * Generic constructor used by JPA
     */
    protected Skill() {}


    /**
     * Constructor for a new skill.
     *
     * @param name - The name of the skill.
     */
    public Skill(String name) {
        this.name = name;
    }


    /**
     * Constructor for testing skills.
     *
     * @param name - The name of the skill.
     */
    public Skill(Integer id, String name) {
        this.id = id;
        this.name = name;
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Evidence> getEvidence() {
        return evidence;
    }


    /**
     * For testing returns the expected json string of the object.
     *
     * @return the expected json string of the object.
     */
    public String toJsonString() {
        return "{\"id\":" + id +
                ",\"name\":\"" + name + "\"}";
    }
}
