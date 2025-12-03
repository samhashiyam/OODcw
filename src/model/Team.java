package model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String teamName;
    private List<Participant> members;

    public Team(String teamName) {
        this.teamName = teamName;
        this.members = new ArrayList<>();
    }

    public void addMember(Participant p) {
        members.add(p);
    }

    public List<Participant> getMembers() {
        return members;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getTeamSize() {
        return members.size();
    }

    public double getAverageSkill() {
        if (members.isEmpty()) return 0;
        int total = 0;
        for (Participant p : members) {
            total += p.getSkillLevel();
        }
        return (double) total / members.size();
    }

    @Override
    public String toString() {
        return "Team: " + teamName + " | Size: " + members.size() + " | Avg Skill: " + getAverageSkill();
    }
}