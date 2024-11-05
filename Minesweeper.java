import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Toolkit;

public class Minesweeper {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Minesweeper");
        int rows = 5;
        int cols = 5;
        GameUtil gameBoard = new GameUtil(rows, cols);

        int extra = 30; // leave extra space for proper display
        frame.setSize(600, 600 + extra);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        
        // Add the game board and the panel with the buttons to the frame
        frame.setLayout(new BorderLayout());
        frame.add(gameBoard.createButtonPanel(), BorderLayout.NORTH);
        frame.add(gameBoard, BorderLayout.CENTER);
        
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage("myicon.png"));
        frame.setVisible(true);
    }
}
