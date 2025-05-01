import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.Timer;

public class GameWindow extends JFrame implements KeyListener, ActionListener {

    private Timer timer;
    private int delay = 20;

    // Game Status
    private static GameStatus gameStatus = new GameStatus(1, 3);
    private static MegaMan megaMan = new MegaMan();
    private static BeeCopter beeCopter = new BeeCopter();

    private int playerX = 100;
    private int playerY;
    private final int playerSize = 40;
    private final int floorY = 430;
    private int velocityY = 0;
    private boolean isJumping = false;

    // Player projectile
    private boolean isProjectileActive = false;
    private int projectileX;
    private int projectileY;
    private final int projectileSize = 20;

    // Enemy
    private final int enemyX = 500;
    private int enemyY = floorY - (playerSize + 70);
    private final int enemySize = 40;

    // Enemy projectile
    private boolean isEnemyProjectileActive = false;
    private int enemyProjectileX;
    private int enemyProjectileY;

    private boolean gameOver = false;
    private boolean playerWon = false;

    // Time tracking
    private long levelStartTime;
    private long timeLimitMs;

    public GameWindow() {
        setTitle("Mega Man X");
        setSize(700, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        playerY = floorY - playerSize;

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        timer = new Timer(delay, this);
        timer.start();

        resetGame(); // Start the first level
        setVisible(true);
    }

    public void paint(Graphics g) {
        super.paint(g);

        // Background
        g.setColor(Color.cyan);
        g.fillRect(0, 0, 695, 592);

        // Floor
        g.setColor(Color.gray);
        g.fillRect(0, floorY, 695, 50);

        // Player
        g.setColor(Color.blue);
        g.fillRect(playerX, playerY, playerSize, playerSize);

        // Player projectile
        if (isProjectileActive) {
            g.setColor(Color.red);
            g.fillOval(projectileX, projectileY, projectileSize, projectileSize);
        }

        // Enemy
        if (beeCopter.getHp() > 0) {
            g.setColor(Color.black);
            g.fillRect(enemyX, enemyY, enemySize, enemySize);
        }

        // Enemy projectile
        if (isEnemyProjectileActive) {
            g.setColor(Color.orange);
            g.fillOval(enemyProjectileX, enemyProjectileY, projectileSize, projectileSize);
        }

        // UI
        g.setColor(Color.white);
        g.setFont(new Font("serif", Font.BOLD, 20));
        g.drawString("Player Health: " + megaMan.getHealth(), 20, 50);
        g.drawString("Enemy Health: " + beeCopter.getHp(), 500, 50);
        g.drawString("Lives: " + gameStatus.getPlayerLives(), 20, 80);
        g.drawString("Level: " + gameStatus.getCurrentLevel(), 20, 110);

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - levelStartTime;
        long remaining = Math.max(0, (timeLimitMs - elapsed) / 1000); // in seconds

        g.drawString("Time Left: " + remaining + "s", 500, 80);

        if (gameOver) {
            g.setFont(new Font("serif", Font.BOLD, 40));
            g.setColor(Color.RED);

            // Center the text horizontally and vertically
            String gameOverText = playerWon ? "You Won!" : "You Lost!";
            int textWidth = g.getFontMetrics().stringWidth(gameOverText);
            int xPosition = (getWidth() - textWidth) / 2;
            g.drawString(gameOverText, xPosition, 280);

            // Display Restart option
            g.setFont(new Font("serif", Font.BOLD, 24));
            g.setColor(Color.WHITE);
            String restartText = "Press R to Restart";
            int restartTextWidth = g.getFontMetrics().stringWidth(restartText);
            int restartXPosition = (getWidth() - restartTextWidth) / 2;
            g.drawString(restartText, restartXPosition, 330);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Time check
        long currentTime = System.currentTimeMillis();
        if (currentTime - levelStartTime >= timeLimitMs && beeCopter.getHp() > 0) {
            gameStatus.loseLife();
            if (gameStatus.getPlayerLives() > 0) {
                resetGame();
            } else {
                gameOver = true;
                playerWon = false;
                repaint();
            }
            return;
        }

        // Gravity / jump
        if (isJumping) {
            velocityY += 1;
            playerY += velocityY;

            if (playerY >= floorY - playerSize) {
                playerY = floorY - playerSize;
                isJumping = false;
                velocityY = 0;
            }
        }

        // Player projectile movement
        if (isProjectileActive) {
            projectileX += megaMan.getAttackSpeed();

            // Collision with enemy
            if (beeCopter.getHp() > 0 &&
                    projectileX + projectileSize > enemyX &&
                    projectileX < enemyX + enemySize &&
                    projectileY + projectileSize > enemyY &&
                    projectileY < enemyY + enemySize) {

                beeCopter.setHp(beeCopter.getHp() - 1);
                isProjectileActive = false;

                if (beeCopter.getHp() <= 0) {
                    gameStatus.levelUp();
                    resetGame(); // Move to next level
                    return;
                }
            }

            if (projectileX > getWidth()) {
                isProjectileActive = false;
            }
        }

        // Enemy projectile
        if (!isEnemyProjectileActive && beeCopter.getHp() > 0) {
            isEnemyProjectileActive = true;
            enemyProjectileX = enemyX;
            enemyProjectileY = enemyY + enemySize / 2 - projectileSize / 2;
        }

        if (isEnemyProjectileActive) {
            enemyProjectileX -= beeCopter.getAttackSpeed();

            // Collision with player
            if (enemyProjectileX < playerX + playerSize &&
                    enemyProjectileX + projectileSize > playerX &&
                    enemyProjectileY + projectileSize > playerY &&
                    enemyProjectileY < playerY + playerSize) {

                megaMan.setHealth(megaMan.getHealth() - 1);
                isEnemyProjectileActive = false;

                if (megaMan.getHealth() <= 0) {
                    gameStatus.loseLife();
                    if (gameStatus.getPlayerLives() > 0) {
                        resetGame();
                    } else {
                        gameOver = true;
                        playerWon = false;
                    }
                }
            }

            if (enemyProjectileX < 0) {
                isEnemyProjectileActive = false;
            }
        }

        repaint();
    }

    private void resetGame() {

        if (gameStatus.getCurrentLevel() >= 4) {
            // The game ends after level 3
            playerWon = true;
            gameOver = true;
            repaint();
            return;
        }

        megaMan.setHealth(3);

        int enemyBaseHp = 5;
        int scaledHp = enemyBaseHp + (gameStatus.getCurrentLevel() - 1) * 2;
        beeCopter.setHp(scaledHp);

        int baseSpeed = 10;
        int scaledSpeed = baseSpeed + gameStatus.getCurrentLevel();
        beeCopter.setAttackSpeed(scaledSpeed);

        playerY = floorY - playerSize;
        velocityY = 0;
        isJumping = false;

        isProjectileActive = false;
        isEnemyProjectileActive = false;

        timeLimitMs = gameStatus.getTimerPerLevel() * 1000L;
        levelStartTime = System.currentTimeMillis();

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                gameOver = false;
                playerWon = false;
                gameStatus = new GameStatus(1, 3);
                megaMan = new MegaMan();
                beeCopter = new BeeCopter();
                resetGame();
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE && !isJumping) {
            isJumping = true;
            velocityY = -15;
        }

        if (e.getKeyCode() == KeyEvent.VK_M && !isProjectileActive) {
            isProjectileActive = true;
            projectileX = playerX + playerSize;
            projectileY = playerY + playerSize / 2 - projectileSize / 2;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
