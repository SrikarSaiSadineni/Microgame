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

    private int playerX = 100;
    private int playerY;
    private final int playerSize = 40;
    private final int floorY = 430;
    private int velocityY = 0;
    private boolean isJumping = false;

    private int playerHealth = 3;
    private int score = 0;

    // Player projectile
    private boolean isProjectileActive = false;
    private int projectileX;
    private int projectileY;
    private final int projectileSize = 20;
    private final int projectileSpeed = 10;

    // Enemy
    private final int enemyX = 500;
    private int enemyY = floorY - playerSize;
    private final int enemySize = 40;
    private int enemyHealth = 5;

    // Enemy projectile
    private boolean isEnemyProjectileActive = false;
    private int enemyProjectileX;
    private int enemyProjectileY;
    private final int enemyProjectileSpeed = 7;

    private boolean gameOver = false;
    private boolean playerWon = false;

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
        if (enemyHealth > 0) {
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
        g.drawString("Player Health: " + playerHealth, 20, 30);
        g.drawString("Enemy Health: " + enemyHealth, 500, 30);

        if (gameOver) {
            g.setFont(new Font("serif", Font.BOLD, 40));
            g.setColor(Color.RED);
            if (playerWon) {
                g.drawString("You Won!", 250, 300);
            } else {
                g.drawString("You Lost!", 250, 300);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

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
            projectileX += projectileSpeed;

            // Collision with enemy
            if (enemyHealth > 0 &&
                    projectileX + projectileSize > enemyX &&
                    projectileX < enemyX + enemySize &&
                    projectileY + projectileSize > enemyY &&
                    projectileY < enemyY + enemySize) {

                enemyHealth--;
                isProjectileActive = false;

                if (enemyHealth <= 0) {
                    gameOver = true;
                    playerWon = true;
                }
            }

            if (projectileX > getWidth()) {
                isProjectileActive = false;
            }
        }

        // Enemy projectile movement
        if (!isEnemyProjectileActive && enemyHealth > 0) {
            isEnemyProjectileActive = true;
            enemyProjectileX = enemyX;
            enemyProjectileY = enemyY + enemySize / 2 - projectileSize / 2;
        }

        if (isEnemyProjectileActive) {
            enemyProjectileX -= enemyProjectileSpeed;

            // Collision with player
            if (
                    enemyProjectileX < playerX + playerSize &&
                            enemyProjectileX + projectileSize > playerX &&
                            enemyProjectileY + projectileSize > playerY &&
                            enemyProjectileY < playerY + playerSize) {

                playerHealth--;
                isEnemyProjectileActive = false;

                if (playerHealth <= 0) {
                    gameOver = true;
                    playerWon = false;
                }
            }

            if (enemyProjectileX < 0) {
                isEnemyProjectileActive = false;
            }
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

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

    public static void main(String[] args) {
        new GameWindow();
    }
}
