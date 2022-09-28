package nz.ac.canterbury.seng302.portfolio.model.dto;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;

public class SkillDTO extends Skill {
    private double frequency;

    public SkillDTO(Skill skill){
        super(skill.getId(), skill.getName());
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getFrequency(){
        return this.frequency;
    }
}
