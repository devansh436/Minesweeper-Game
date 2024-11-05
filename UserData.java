import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;

public class UserData {
    static String filePath = "UserData.csv";
    static int highScore = 0;

    static int getHighScore() {
        int highScoreFromFile = 0;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip the header line
    
            // Read each line in the file
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(","); // Split by comma
                // obtain high score present in column-1
                highScoreFromFile = Math.max(highScoreFromFile, Integer.parseInt(row[1].trim()));
            }
            
        } catch (IOException e) {
            System.err.println("Error reading the file.");
        }
    
        highScore = highScoreFromFile; 
        return highScoreFromFile;
    }
    

    // Update high score column
    static void updateScore(GameUtil g, int score) {
        UserData.highScore = Math.max(highScore, score);

        // write highScore & score to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) { // 'true' appends to file
            
            // prepare new data to be appended to csv file
            String outcome = g.victory ? "Win" : "Loss";
            String difficulty = g.calculateDifficulty();

            String[] data = {difficulty, String.valueOf(highScore), String.valueOf(score), outcome};
            String line = String.join(",", data);
            
            // write this data and move to next line 
            writer.write(line);
            writer.newLine();

        } catch (IOException e) {
            System.out.println("Error reading the file.");
            e.printStackTrace();
        }
    }


    static void updateHighScoreButton(GameUtil g) {
        JButton hsButton = (JButton) g.buttonPanel.getComponent(3);
        hsButton.setText(String.valueOf(highScore));
    }


    static void resetScore() {
        UserData.highScore = 0;
        String firstRow = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Read the first line
            firstRow = reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the first row back to the file
            if (firstRow != null) {
                writer.write(firstRow);
                writer.newLine(); // Add a newline if you want to maintain the format
            }
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }
}
