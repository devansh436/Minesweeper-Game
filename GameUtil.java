import java.awt.event.*;
import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class GameUtil extends JPanel implements KeyListener, MouseListener{
    protected int rows;
    protected int cols;
    protected Cell cells[][];
    protected int totalMines;
    protected int totalSafeCells;
    protected int flagsUsed;
    protected int revealedCells;
    protected int score;
    protected int wrongFlags;
    protected boolean victory;
    JPanel buttonPanel;

    GameUtil(int boardHeight, int boardWidth) {
        rows = boardHeight;
        cols = boardWidth ;
        cells = new Cell[rows][cols];
        flagsUsed = 0;
        totalMines = 0;
        totalSafeCells = 0;
        revealedCells = 0;
        wrongFlags = 0;
        victory = false;
        buttonPanel = null;

        createBoard();

        // prepare JPanel for proper display
        this.setLayout(new GridLayout(rows, cols));
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);
        this.addMouseListener(this);
    } 

    // calculates difficulty based on grid size
    String calculateDifficulty() {
        if (rows <= 7 && cols <= 7) {
            return "Easy";
        } else if (rows <= 15 && cols <= 15) {
            return "Medium";
        } else if (rows <= 25 && cols <= 25){
            return "Hard";
        }
        
        return "Undefined";
    }


    void calculateScore() {
        score = 0;

        int bonus = victory ? 100 : 0; // bonus points for victory
        int safeCellScore = revealedCells * 10; // points for revealed safe cells
        int mineFlagsScore = (flagsUsed - wrongFlags) * 50; // points for correct flags
        int penalty = wrongFlags * 50; // penalty for wrong flags
        score = safeCellScore + bonus + mineFlagsScore - penalty;
        score = Math.max(score, 0); // to ensure that score > 0

        // add entry in UserData.csv
        UserData.updateScore(this, score);
    }


    JPanel createButtonPanel() {
         // Create a reset score button
         JButton resetScoreButton = new JButton("Reset Score");
         resetScoreButton.setFocusable(false);
         resetScoreButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                    UserData.resetScore(); // Reset the score in the GameUtil class
                    JButton scoreButton = (JButton) buttonPanel.getComponent(3);
                    scoreButton.setText(String.valueOf(0));
             }
         });
 
         // Create a reset game button
         JButton resetGameButton = new JButton("Reset Game");
         resetGameButton.setFocusable(false);
         resetGameButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 resetBoard(); // Reset board in the GameUtil class
             }
         });
 
        // Create a high score button
        JButton highScoreButton = new JButton();
        highScoreButton.setFocusable(false);
        highScoreButton.setText(String.valueOf(UserData.getHighScore()));

        // Create change difficulty button
        JButton diffButton = new JButton("Easy");
        diffButton.setFocusable(false);
        diffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int d = changeDifficulty(); // Reset board in the GameUtil class
                if (d == 0) {
                    diffButton.setText("Easy");
                } else if (d == 1) {
                    diffButton.setText("Medium");
                } else {
                    diffButton.setText("Hard");
                }
            }
        });

         
         // Create a panel for the buttons
         buttonPanel = new JPanel();
         buttonPanel.add(diffButton);
         buttonPanel.add(resetGameButton);
         buttonPanel.add(resetScoreButton);
         buttonPanel.add(highScoreButton);

         return buttonPanel;
    }


    // change difficulty then reset grid
    int changeDifficulty() {
        String diff = calculateDifficulty();
        int d;

        if (diff.equals("Easy")) {
            rows = 12;
            cols = 12;
            d = 1;
        } else if (diff.equals("Medium")) {
            rows = 17;
            cols = 17;
            d = 2;
        } else {
            rows = 5;
            cols = 5;
            d = 0;
        }
        this.setLayout(new GridLayout(rows, cols));
        resetBoard();
        return d;
    }


    void createBoard() {
        createButtonPanel();
        generateCells();
    }
    
    // player lost the game
    void gameOver() {
        victory = false;
        calculateScore();

        // print score
        System.out.println("Flags used = " + flagsUsed);
        System.out.println("Score = " + score);
        System.out.println("Wrong flags = " + wrongFlags);
        System.out.println("Penalty = " + wrongFlags * 50);

        // defeat message
        JOptionPane.showMessageDialog(this, "You Lost! Your score is " + score
        + ".\nPress Esc to Restart.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        
        resetBoard();
    }

    void generateCells() {
        Random random = new Random();
        cells = new Cell[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (random.nextInt(100) < 15) { 
                    cells[i][j] = new MineCell(i, j, this);
                    totalMines++;
                } else {
                    cells[i][j] = new SafeCell(i, j, this);
                    totalSafeCells++;
                }   
                add(cells[i][j].button);
            }
        }

        if (totalSafeCells == rows * cols) {
            resetBoard();
        }
    }

    // recreates board after win/loss
    void recreateBoard() {
        resetValues();
        generateCells();
        UserData.updateHighScoreButton(this);
    }
    
    void resetValues() {
        flagsUsed = 0;
        totalMines = 0;
        totalSafeCells = 0;
        revealedCells = 0;
        wrongFlags = 0;
        score = 0;
    }
    // reset GameUtil values

    void revealAllSafeCells() {
        for (Cell cellArr[] : cells) {
            for (Cell cell : cellArr) {
                if (!cell.isMine) {
                    cell.reveal();
                }
            }
        }
    }

    // player clicked on a mine so reveal all other mines as well as flagged safe cells
    void revealAllMines() {
        for (Cell cellArr[] : cells) {
            for (Cell cell : cellArr) {
                if (cell.isFlagged) {
                    revealWrongFlag(cell);
                } else if (cell.isMine) {
                    cell.isRevealed = true;
                    cell.button.setText("M");
                    cell.button.setBackground(Color.DARK_GRAY);
                    cell.button.setForeground(Color.white);
                }
            }
        }

        // player lost the game
        gameOver();
    }

    // reveal flagged cell if it was safe
    void revealWrongFlag(Cell cell) {
        if (!cell.isMine) {
            cell.button.setText("WF");
            cell.button.setBackground(Color.RED);
            cell.button.setForeground(Color.white);
        }
    }

    // reset game
    void resetBoard() {
        removeAll();     // remove all buttons panel
        recreateBoard(); 
        
        // refresh the JPanel
        revalidate();    // refresh layout
        repaint();       // repaint the panel
    }

    // player won the game
    void victory() {
        victory = true;
        revealAllSafeCells();
        calculateScore();
        
        // // print score
        // System.out.println("Flags used = " + flagsUsed);
        // System.out.println("Score = " + score);
        // System.out.println("Wrong flags = " + wrongFlags);
        // System.out.println("Penalty = " + wrongFlags * 50);
        
        // display victory msg
        String msg = "You won! Your score is " + score 
                    + "\nPress Esc to Restart";
        JOptionPane.showMessageDialog(this, msg, "Victory!", JOptionPane.INFORMATION_MESSAGE);
        SwingUtilities.invokeLater(() -> resetBoard());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // reset board if user presses 'R'
        if (e.getKeyCode() == KeyEvent.VK_R) {
            resetBoard();
        }
        // close program if user presses 'X'
        else if (e.getKeyCode() == KeyEvent.VK_X) {
            System.out.println("Okay bye!");
            System.exit(0);
        }
    }   
    
    @Override
    public void mouseClicked(MouseEvent e) {}    
    @Override
    public void keyTyped(KeyEvent e) {}    
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}


abstract class Cell {
    int x, y; // position variables
    boolean isRevealed;
    boolean isMine;
    boolean isFlagged;
    JButton button; // each cell has a JButton
    GameUtil g;

    Cell(int a, int b, GameUtil g) {
        x = a;
        y = b;
        isRevealed = false;
        isMine = false;
        isFlagged = false;
        this.g = g;

        button = new JButton();
        // set left & right click listeners for button
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {        // Left click -> reveal
                    reveal(); 
                } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click -> flag
                    flag(); 
                }
            }
        });

        // set button font
        button.setFont(new Font("Arial", Font.BOLD, 12));
        // removes default margin, padding
        button.setMargin(new Insets(0, 0, 0, 0)); 
        button.setFocusable(false);
    }

    abstract void reveal();

    // right click operation
    void flag() {
        // revealed cell can't be flagged
        if (isRevealed) return;

        if (isFlagged) { // unflag
            button.setText("");
            button.setBackground(null);
            button.setForeground(null);
            isFlagged = false;
            g.flagsUsed--;
        } 
        else { // flag
            // flag visuals
            button.setText("F");
            button.setBackground(Color.yellow);
            button.setForeground(Color.RED);
            isFlagged = true;
            g.flagsUsed++;
        }
    }
}


class SafeCell extends Cell {
    // num keeps track of no. of adjacent mines
    int num;

    SafeCell(int x, int y, GameUtil g) {
        super(x,y,g);
        num = 0;
    }

    @Override
    void reveal() {
        // revealed or flagged cells can't be revealed
        if (isRevealed || isFlagged) return;

        g.revealedCells++;
        button.setBackground(Color.CYAN);
        isRevealed = true;

        // logic for checking surrounding cells
        int iStart = Math.max(0, x - 1);
        int iEnd = Math.min(g.rows - 1, x + 1);
        int jStart = Math.max(0, y - 1);
        int jEnd = Math.min(g.cols - 1, y + 1);

        for (int i = iStart; i <= iEnd; i++) {
            for (int j = jStart; j <= jEnd; j++) {
                if (g.cells[i][j].isMine) {
                    num += 1; 
                }
            }
        }

        // if a cell has 0 adjacent mines then recursively reveal adjacent cells 
        if (num == 0) {
            for (int i = iStart; i <= iEnd; i++) {
                for (int j = jStart; j <= jEnd; j++) {
                    g.cells[i][j].reveal();
                }
            }
        }
        button.setText(num > 0 ? String.valueOf(num) : "");

        // if all safe cells are revealed then player wins the game
        if (g.revealedCells == g.totalSafeCells) {
            g.revealAllSafeCells();
            g.victory();
        }
    }
    
    // a safecell is flagged so increment wrongFlags counter
    void flag() {
        super.flag();
        g.wrongFlags++;
    }
}


class MineCell extends Cell {
    MineCell(int x, int y, GameUtil g) {
        super(x,y,g);
        isMine = true;
    }

    @Override
    void reveal() {
        if (isRevealed || isFlagged) return;
        
        // if a mine is revealed player loses the game

        // mine display
        button.setText("M");
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.white);

        // reveal all other mines
        g.revealAllMines();
    }
}