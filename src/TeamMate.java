import filehandling.CSVFileHandler;
import Logic.TeamBuilder;
import model.Participant;
import model.Team;
import Logic.Survey;


import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeamMate {
    private static CSVFileHandler fileHandler = new CSVFileHandler();
    private static List<Participant> participants;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("║           TEAMMATE SYSTEM            ║");
        System.out.println("║    Intelligent Team Formation        ║");


        loadParticipants(); // Load data automatically on startup

        boolean exit = false;
        while (!exit) {
            displayMainMenu();
            int choice = getMenuChoice();

            switch (choice) {
                case 1:
                    viewParticipants();
                    break;
                case 2:
                    Survey surveySystem = new Survey(); // 1. Create the object
                    surveySystem.runMultipleSurveys();  // 2. Call the method
                    loadParticipants();
                    break;
                case 3:
                    formTeams();
                    break;
                case 4:
                    viewTeamStatistics();
                    break;
                case 5:
                    reloadData();
                    break;
                case 6:
                    System.out.println("\nThank you for using TeamMate System!");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
        scanner.close();
    }

    private static void displayMainMenu() {
        System.out.println("\n════════════════ MAIN MENU ════════════════");
        System.out.println("1. View All Participants");
        System.out.println("2. Complete Survey");
        System.out.println("3. Form Teams");
        System.out.println("4. View Team Statistics");
        System.out.println("5. Reload  Participant Data from CSV");
        System.out.println("6. Exit");
        System.out.println("════════════════════════════════════════════");
        System.out.print("Enter your choice (1-6): ");
    }

    private static int getMenuChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; // Invalid choice
        }
    }

    private static void loadParticipants() {
        String inputPath = "datafiles/participants_sample.csv";
        System.out.println("\nLoading participants from: " + inputPath);
        participants = fileHandler.loadParticipants(inputPath);
        System.out.println(" Successfully loaded " + participants.size() + " participants");
    }

    private static void viewParticipants() {
        if (participants.isEmpty()) {
            System.out.println("\nNo participants loaded");
            return;
        }

        System.out.println("\n════════════════ ALL PARTICIPANTS ════════════════");
        System.out.printf("%-8s %-20s %-15s %-12s %-6s %-12s%n",
                "ID", "Name", "Game", "Role", "Skill", "Personality");
        System.out.println("─────────────────────────────────────────────────────────");

        for (Participant p : participants) {
            System.out.printf("%-8s %-20s %-15s %-12s %-6d %-12s%n",
                    p.getId(),
                    p.getName().length() > 18 ? p.getName().substring(0, 15) + "..." : p.getName(),
                    p.getPreferredGame(),
                    p.getPreferredRole(),
                    p.getSkillLevel(),
                    p.getPersonalityType());
        }

        // Show statistics
        showParticipantStatistics();

        System.out.print("\nPress Enter to continue.");
        scanner.nextLine();
    }

    private static void showParticipantStatistics() {
        long leaders = participants.stream().filter(p -> p.getPersonalityType().equals("Leader")).count();
        long thinkers = participants.stream().filter(p -> p.getPersonalityType().equals("Thinker")).count();
        long balanced = participants.stream().filter(p -> p.getPersonalityType().equals("Balanced")).count();

        System.out.println("\nPARTICIPANT STATISTICS:");
        System.out.println("   Leaders: " + leaders + " | Thinkers: " + thinkers + " | Balanced: " + balanced);

        // Show games distribution
        System.out.print("   Games: ");
        participants.stream()
                .map(Participant::getPreferredGame)
                .distinct()
                .forEach(game -> {
                    long count = participants.stream().filter(p -> p.getPreferredGame().equals(game)).count();
                    System.out.print(game + "(" + count + ") ");
                });
        System.out.println();
    }

    private static void formTeams() {
        if (participants.isEmpty()) {
            System.out.println("\nNo participants available to form teams");
            return;
        }

        System.out.println("\n════════════════ FORM TEAMS ════════════════");
        System.out.print("Enter team size: ");

        int teamSize;
        try {
            teamSize = Integer.parseInt(scanner.nextLine().trim());
            if (teamSize <= 0) {
                System.out.println(" Team size must be positive!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number,Using default size 5.");
            teamSize = 5;
        }

        // Check if we have enough participants
        if (participants.size() < teamSize) {
            System.out.println("Not enough participants! Need at least " + teamSize + ", but have " + participants.size());
            return;
        }

        System.out.println("\nForming teams of size " + teamSize + ".");

        // Run team formation with concurrency
        TeamBuilder builder = new TeamBuilder(participants, teamSize);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            System.out.print("Processing");
            java.util.concurrent.Future<List<Team>> future = executor.submit(builder);

            // Loading animation
            while (!future.isDone()) {
                System.out.print(".");
                Thread.sleep(300);
            }


            List<Team> teams = future.get();

            // Save teams to file
            String outputPath = "datafiles/formed_teams.csv";
            fileHandler.saveTeams(teams, outputPath);

            System.out.println(" Successfully formed " + teams.size() + " teams!");
            System.out.println(" Teams saved to: " + outputPath);

            // Show quick summary
            System.out.println(" TEAM SUMMARY:");
            for (Team team : teams) {
                System.out.printf("   %s: %d members, Avg Skill: %.1f%n",
                        team.getTeamName(), team.getTeamSize(), team.getAverageSkill());
            }

        } catch (Exception e) {
            System.out.println(" Error forming teams: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        System.out.print("\nPress Enter to continue");
        scanner.nextLine();
    }

    private static void viewTeamStatistics() {
        String teamsPath = "datafiles/formed_teams.csv";
        java.io.File file = new java.io.File(teamsPath);

        if (!file.exists()) {
            System.out.println("\nNo teams found. Please form teams first.");
            return;
        }

        System.out.println("\n===================== TEAM STATISTICS =====================");
        System.out.println("Loaded from: " + teamsPath);

        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());

            if (lines.isEmpty()) {
                System.out.println("Teams file is empty.");
                return;
            }

            java.util.Map<String, Team> teamMap = new java.util.HashMap<>();

            for (String line : lines.subList(1, lines.size())) {
                String[] data = line.split(",");

                if (data.length < 7) continue;

                String teamName = data[0].trim();
                String id = data[1].trim();
                String name = data[2].trim();
                String role = data[3].trim();
                String personality = data[4].trim();
                String game = data[5].trim();
                int skill = Integer.parseInt(data[6].trim());

                Participant p = new Participant(
                        id,
                        name,
                        "",          // email not stored
                        game,
                        skill,
                        role,
                        0,           // personality score not needed
                        personality
                );

                teamMap.putIfAbsent(teamName, new Team(teamName));
                teamMap.get(teamName).addMember(p);
            }

            // Print statistics
            System.out.println("\nTEAM SUMMARY:");
            for (Team team : teamMap.values()) {
                System.out.println("\n------------------------------------------------------");
                System.out.println("Team: " + team.getTeamName());
                System.out.println("Members: " + team.getTeamSize());
                System.out.printf("Average Skill: %.2f%n", team.getAverageSkill());

                long leaders = team.getMembers().stream().filter(p -> p.getPersonalityType().equals("Leader")).count();
                long thinkers = team.getMembers().stream().filter(p -> p.getPersonalityType().equals("Thinker")).count();
                long balanced = team.getMembers().stream().filter(p -> p.getPersonalityType().equals("Balanced")).count();

                System.out.println("Personality Breakdown:");
                System.out.println("   Leaders: " + leaders);
                System.out.println("   Thinkers: " + thinkers);
                System.out.println("   Balanced: " + balanced);

                System.out.println("Role Distribution:");
                team.getMembers().stream()
                        .map(Participant::getPreferredRole)
                        .distinct()
                        .forEach(roleType -> {
                            long count = team.getMembers().stream()
                                    .filter(p -> p.getPreferredRole().equals(roleType))
                                    .count();
                            System.out.println("   " + roleType + ": " + count);
                        });

                System.out.println("Game Distribution:");
                team.getMembers().stream()
                        .map(Participant::getPreferredGame)
                        .distinct()
                        .forEach(gameType -> {
                            long count = team.getMembers().stream()
                                    .filter(p -> p.getPreferredGame().equals(gameType))
                                    .count();
                            System.out.println("   " + gameType + ": " + count);
                        });


            }

        } catch (Exception e) {
            System.out.println("Error reading teams: " + e.getMessage());
        }

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }


    private static void reloadData() {
        System.out.println("\nReloading data from CSV");
        loadParticipants();
        System.out.print("Press Enter to continue");
        scanner.nextLine();
    }
}