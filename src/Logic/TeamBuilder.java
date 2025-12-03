package Logic;

import model.Participant;
import model.Team;

import java.util.*;
import java.util.concurrent.Callable;

//callable interface allows running team building inside a thread
public class TeamBuilder implements Callable<List<Team>> {
    private List<Participant> participants;
    private List<Team> formedTeams;
    private int teamSize;
    private Random random = new Random();

    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.formedTeams = new ArrayList<>();//initialising
    }

    public List<Team> getFormedTeams() {
        return formedTeams;
    }

    @Override
    public List<Team> call() throws Exception {
        System.out.println("[Thread-" + Thread.currentThread().getId() + "] Starting team formation");
        formBalancedTeams();
        System.out.println("[Thread-" + Thread.currentThread().getId() + "] Team formation completed. Formed " + formedTeams.size() + " teams.");
        return formedTeams;
    }

    private void formBalancedTeams() {
        // Categorize by personality type
        List<Participant> leaders = new ArrayList<>();
        List<Participant> thinkers = new ArrayList<>();
        List<Participant> balanced = new ArrayList<>();

        for (Participant p : participants) {
            switch (p.getPersonalityType()) {
                case "Leader": leaders.add(p); break;
                case "Thinker": thinkers.add(p); break;
                case "Balanced": balanced.add(p); break;
            }
        }

        System.out.printf("Personality distribution: %d Leaders, %d Thinkers, %d Balanced%n",
                leaders.size(), thinkers.size(), balanced.size());

        // Shuffle for randomness
        Collections.shuffle(leaders);
        Collections.shuffle(thinkers);
        Collections.shuffle(balanced);

        int totalParticipants = participants.size();
        int numTeams = (int) Math.ceil((double) totalParticipants / teamSize);

        // Create empty teams
        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= numTeams; i++) {
            teams.add(new Team("Team_" + i));
        }

        // Phase 1: Distribute Leaders (1 per team)
        distributeByPersonality(teams, leaders, "Leader", 1);

        // Phase 2: Distribute Thinkers (1-2 per team)
        distributeByPersonality(teams, thinkers, "Thinker", 2);

        // Phase 3: Fill with Balanced participants considering game/role/average skill balance
        fillWithConstraints(teams, balanced);

        // Phase 4: Handle any remaining participants
        handleRemainingParticipants(teams);

        this.formedTeams = teams;
    }

    private void distributeByPersonality(List<Team> teams, List<Participant> participants, String type, int maxPerTeam) {
        if (participants.isEmpty()) {
            System.out.println("No " + type + " participants available");
            return;
        }

        int teamIndex = 0;
        for (Participant p : new ArrayList<>(participants)) {
            boolean placed = false;

            
            for (int attempt = 0; attempt < teams.size() * 2 && !placed; attempt++) {
                Team team = teams.get(teamIndex);

                if (team.getTeamSize() < teamSize &&
                        countPersonalityInTeam(team, type) < maxPerTeam &&
                        countGameInTeam(team, p.getPreferredGame()) < 2) {

                    team.addMember(p);
                    participants.remove(p);
                    placed = true;
                    System.out.println("Placed " + type + " " + p.getName() + " in " + team.getTeamName());
                }

                teamIndex = (teamIndex + 1) % teams.size();
            }
        }
    }

    private void fillWithConstraints(List<Team> teams, List<Participant> balancedParticipants) {
        if (balancedParticipants.isEmpty()) return;

        Collections.shuffle(balancedParticipants);

        for (Participant p : new ArrayList<>(balancedParticipants)) {
            Team bestTeam = findOptimalTeam(teams, p);
            if (bestTeam != null) {
                bestTeam.addMember(p);
                balancedParticipants.remove(p);
                System.out.println("Placed Balanced " + p.getName() + " in " + bestTeam.getTeamName());
            }
        }
    }

    private Team findOptimalTeam(List<Team> teams, Participant p) {
        Team bestTeam = null;
        int bestScore = -1;

        for (Team team : teams) {
            if (team.getTeamSize() >= teamSize) continue;
            //Skip teams that already has 2 or more members who prefer same game
            if (countGameInTeam(team, p.getPreferredGame()) >= 2) continue;

            int score = calculateTeamScore(team, p);
            if (score > bestScore) {
                bestScore = score;
                bestTeam = team;
            }
        }

        return bestTeam;
    }

    private int calculateTeamScore(Team team, Participant p) {
        int score = 0;

        // Prefer teams with lower average skill (for balancing)
        if (team.getAverageSkill() < 5) score += 3;

        // Prefer teams that need this role
        if (!hasRole(team, p.getPreferredRole())) score += 5;

        // Prefer teams with fewer members
        if (team.getTeamSize() < teamSize - 2) score += 2;

        return score;
    }

    private void handleRemainingParticipants(List<Team> teams) {
        // Find all assigned participants
        Set<Participant> assigned = new HashSet<>();
        for (Team team : teams) {
            assigned.addAll(team.getMembers());
        }

        // Find unassigned participants
        List<Participant> unassigned = new ArrayList<>(participants);
        unassigned.removeAll(assigned);

        // Place unassigned participants in any available team
        for (Participant p : unassigned) {
            for (Team team : teams) {
                if (team.getTeamSize() < teamSize) {
                    team.addMember(p);
                    System.out.println("Force-placed " + p.getName() + " in " + team.getTeamName());
                    break;
                }
            }
        }
    }

    private int countPersonalityInTeam(Team team, String personalityType) {
        return (int) team.getMembers().stream()
                .filter(p -> p.getPersonalityType().equals(personalityType))
                .count();
    }

    private int countGameInTeam(Team team, String game) {
        return (int) team.getMembers().stream()
                .filter(p -> p.getPreferredGame().equals(game))
                .count();
    }

    private boolean hasRole(Team team, String role) {
        return team.getMembers().stream()
                .anyMatch(p -> p.getPreferredRole().equals(role));
    }
}