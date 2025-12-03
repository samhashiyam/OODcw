package Logic;

import filehandling.CSVFileHandler;
import model.Participant;
import java.util.*;

public class Survey {
    private Scanner scanner;
    private CSVFileHandler fileHandler;
    private List<Participant> participants;
    private int lastIdNumber; //track last ID to generate new ID
    private static final String PARTICIPANTS_FILE="datafiles/participants_sample.csv";



    //Constructor
    public Survey() {
        this.scanner = new Scanner(System.in);
        this.fileHandler = new CSVFileHandler();
        this.participants = fileHandler.loadParticipants(PARTICIPANTS_FILE);
        this.lastIdNumber = getLastIdNumber();
    }

    public void runMultipleSurveys() {
        System.out.println("\n   NEW MEMBER REGISTRATION SURVEY   ");

        System.out.print("How many participants do you want to add? ");
        int count = getValidInt(1, 50);

        for (int i = 0; i < count; i++) {
            System.out.println("\n--- Participant " + (i + 1) + " ---");
            runSurvey(); //
        }

        fileHandler.saveParticipants(participants, PARTICIPANTS_FILE);
        System.out.println("\n Successfully added \" + count + \" participant(s) and saved data to: \" + PARTICIPANTS_FILE");
    }




    //Method that runs survey and returns a participant
    public Participant runSurvey() {

        System.out.print("Enter Name: ");
        String name = getValidName();

        System.out.print("Enter Email: ");
        String email = getValidEmail();


    // Personality Questions
    System.out.println("\nRate the following statements from 1 (Strongly Disagree) to 5 (Strongly Agree):");

        int q1 = askQuestion("1. I enjoy taking the lead and guiding others.");
        int q2 = askQuestion("2. I prefer analyzing situations.");
        int q3 = askQuestion("3. I work well with others.");
        int q4 = askQuestion("4. I am calm under pressure.");
        int q5 = askQuestion("5. I like making quick decisions.");

        int totalRaw = q1 + q2 + q3 + q4 + q5;
        int finalScore = totalRaw * 4;

        // Determine Type
        String type = "Balanced"; // Default value(Balanced as it's the middle type)
        if (finalScore >= 90) {
            type = "Leader";
        } else if (finalScore < 70) {
            type = "Thinker";

        }

        System.out.println("\n Your Score: " + finalScore + "\nYour Personality type: " + type );

        String game = getValidGame();

        String role = getValidRole();

        System.out.print("Enter Skill Level (1-10): ");
        int skill = getValidInt(1, 10);

        String id = generateId();

        Participant newParticipant = new Participant(id, name, email, game, skill, role, finalScore, type);

        participants.add(newParticipant);


        return newParticipant;
    }

    private int askQuestion(String question) {
        System.out.println(question);
        return getValidInt(1, 5);
    }

    // Input Validation (Error Handling)
    private int getValidInt(int min, int max) {
        int input = -1;
        while (true) {
            try {
                String text = scanner.nextLine();
                input = Integer.parseInt(text);
                if (input >= min && input <= max) {
                    break;
                }
                System.out.println("Invalid input. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        return input;
    }

    //ID generation Logic
    private int getLastIdNumber() {
        int lastNumber = 0;
        for (Participant p : participants) {
            try {
                int num = Integer.parseInt(p.getId().substring(1)); // remove 'P'
                if (num > lastNumber) lastNumber = num;
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid ID: " + p.getId());
            }
        }
        return lastNumber;
    }

    private String generateId() {
        lastIdNumber++;
        return String.format("P%03d", lastIdNumber);
    }
    private String getValidName() {
        String name;
        while (true) {
            name = scanner.nextLine().trim();
            if (!name.isEmpty() && name.matches("[A-Za-z ]+")) {
                return name;
            }
            System.out.print("Invalid name. Enter again: ");
        }
    }

    private String getValidEmail() {
        String email;
        while (true) {
            email = scanner.nextLine().trim();
            if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return email;
            }
            System.out.print("Invalid email. Enter again: ");
        }
    }

    private String getValidGame() {
        List<String> validGames = Arrays.asList(
                "Chess", "FIFA", "Basketball", "CS:GO", "DOTA 2", "Valorant"
        );

        while (true) {
            System.out.print("Enter Preferred Game " + validGames + ": ");
            String input = scanner.nextLine().trim();

            // Case-insensitive check
            for (String g : validGames) {
                if (g.equalsIgnoreCase(input)) {
                    return g;  // return correct formatted version
                }
            }

            System.out.println("Invalid game. Please choose from: " + validGames);
        }
    }
    private String getValidRole() {
        List<String> validRoles = Arrays.asList(
                "Strategist", "Attacker", "Defender", "Supporter", "Coordinator"
        );

        while (true) {
            System.out.print("Enter Preferred Role " + validRoles + ": ");
            String input = scanner.nextLine().trim();

            for (String r : validRoles) {
                if (r.equalsIgnoreCase(input)) {
                    return r;
                }
            }

            System.out.println("Invalid role. Please choose from: " + validRoles);
        }
    }


}