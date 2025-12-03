package model;

public class Participant {
    private String id;
    private String name;
    private String email;
    private String prefferedGame;
    private int skillLevel;
    private String prefferedRole;
    private int personalityScore;
    private String personalityType;

    public Participant(String id, String name, String email, String preferredGame,
                       int skillLevel, String preferredRole, int personalityScore,
                       String personalityType){
        this.id = id;
        this.name = name;
        this.email = email;
        this.prefferedGame = preferredGame;
        this.skillLevel = skillLevel;
        this.prefferedRole = preferredRole;
        this.personalityScore = personalityScore;
        this.personalityType = personalityType;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPreferredGame() {
        return prefferedGame;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public String getPreferredRole() {
        return prefferedRole;
    }

    public int getPersonalityScore() {
        return personalityScore;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    @Override
    public String toString() {
        return id +"(" + personalityType + ") - " + prefferedGame;
    }
}
