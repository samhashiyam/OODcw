package filehandling;

import Exceptions.InvalidDataException;
import model.Participant;
import model.Team;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVFileHandler {

    //Load participants method
    public List<Participant> loadParticipants(String filePath) {
        List<Participant> list = new ArrayList<>();
        String line; //temporary variable used for reading each line of CSV

        //Check if file exists
        File f = new File(filePath);
        if(!f.exists()) {
            System.out.println("Warning: File not found at " + filePath);
            return list;
        }
        //Open files and ensure the file closes automatically
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header

            //Read each row
            while ((line = br.readLine()) != null) {
                try {
                    //Split each row by , and store values in an array
                    String[] data = line.split(",");
                    if (data.length < 8) {
                        throw new InvalidDataException("Row has missing data");
                    }

                    //Reads each value
                    String id = data[0].trim();
                    String name = data[1].trim();
                    String email = data[2].trim();
                    String game = data[3].trim();
                    int skill = Integer.parseInt(data[4].trim());
                    String role = data[5].trim();
                    int score = Integer.parseInt(data[6].trim());
                    String type = data[7].trim(); // Read as String directly


                    list.add(new Participant(id, name, email, game, skill, role, score, type));

                } catch (Exception e) {
                    // Catch both number format errors and  custom invalid data error
                    System.out.println("Skipping invalid row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return list;
    }

    //Save participants method
    public void saveParticipants(List<Participant> participants, String outputPath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
            // Write CSV header
            bw.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            bw.newLine();

            for (Participant p : participants) {
                String line = String.format("%s,%s,%s,%s,%d,%s,%d,%s",
                        p.getId(),
                        p.getName(),
                        p.getEmail(),
                        p.getPreferredGame(),
                        p.getSkillLevel(),
                        p.getPreferredRole(),
                        p.getPersonalityScore(),
                        p.getPersonalityType());
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }


    public void saveTeams(List<Team> teams, String outputPath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
            bw.write("Team Name,ID,Name,Role,Personality,Game,Skill\n");

            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    String line = String.format("%s,%s,%s,%s,%s,%s,%d",
                            team.getTeamName(),
                            p.getId(),
                            p.getName(),
                            p.getPreferredRole(),
                            p.getPersonalityType(),
                            p.getPreferredGame(),
                            p.getSkillLevel());
                    bw.write(line);
                    bw.newLine();
                }
            }
            System.out.println("Teams saved successfully to " + outputPath);
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}