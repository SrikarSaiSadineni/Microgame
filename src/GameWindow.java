import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
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

    // Player projectiles
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private final int projectileSize = 20;
    private final int chargeShotSize = 40; // Larger projectile size for charge shot

    // Charge shot variables
    private long chargeStartTime = -1; // When the player starts charging
    private final long chargeTimeLimit = 1000; // Time limit for full charge in milliseconds
    private boolean isCharging = false;

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

    // Sprites
    private Image backgroundImage;
    private Image playerIdleSprite;
    private Image playerJumpSprite;
    private Image playerShootingSprite;
    private Image playerShootingAirSprite;
    private Image enemySprite;
    private Image bulletSprite;
    private Image chargeBulletSprite;


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

        backgroundImage = new ImageIcon("images/background.png").getImage();
        playerIdleSprite = new ImageIcon("images/player_idle.png").getImage();
        playerJumpSprite = new ImageIcon("images/player_jump.png").getImage();
        playerShootingSprite = new ImageIcon("images/player_shooting.png").getImage();
        playerShootingAirSprite = new ImageIcon("images/player_shooting_air.png").getImage();
        enemySprite = new ImageIcon("images/enemy.png").getImage();
        bulletSprite = new ImageIcon("images/bullet.png").getImage();
        chargeBulletSprite = new ImageIcon("images/charge_bullet.png").getImage();


        resetGame(); // Start the first level
        setVisible(true);
    }

    public void paint(Graphics g) {
        super.paint(g);

        // Draw the background
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

// Determine which sprite to draw
        Image currentPlayerSprite = playerIdleSprite;

        if (isJumping && isCharging) {
            currentPlayerSprite = playerShootingAirSprite;
        } else if (isJumping) {
            currentPlayerSprite = playerJumpSprite;
        } else if (isCharging) {
            currentPlayerSprite = playerShootingSprite;
        }

// Draw the player
        g.drawImage(currentPlayerSprite, playerX, playerY, playerSize, playerSize, this);


        for (Projectile p : projectiles) {
            if (p.isActive()) {
                Image projImage = p.getSize() == chargeShotSize ? chargeBulletSprite : bulletSprite;
                g.drawImage(projImage, p.getX(), p.getY(), p.getSize(), p.getSize(), this);
            }
        }


        if (beeCopter.getHp() > 0) {
            g.drawImage(enemySprite, enemyX, enemyY, enemySize, enemySize, this);
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
        ArrayList<Projectile> projectilesToRemove = new ArrayList<>();
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.move();

                // Collision with enemy
                if (beeCopter.getHp() > 0 &&
                        p.getX() + p.getSize() > enemyX &&
                        p.getX() < enemyX + enemySize &&
                        p.getY() + p.getSize() > enemyY &&
                        p.getY() < enemyY + enemySize) {
                    beeCopter.setHp(beeCopter.getHp() - p.getDamage());
                    p.setActive(false); // Deactivate the projectile
                    projectilesToRemove.add(p); // Add to list to remove

                    if (beeCopter.getHp() <= 0) {
                        gameStatus.levelUp();
                        resetGame(); // Move to next level
                        return;
                    }
                }

                if (p.getX() > getWidth()) {
                    projectilesToRemove.add(p);
                    p.setActive(false); // Deactivate the projectile when it goes off screen
                }
            }
        }

        // Remove inactive projectiles from the list
        projectiles.removeAll(projectilesToRemove);

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
        if (gameStatus.getCurrentLevel() >= 6) {
            // The game ends after level 5
            playerWon = true;
            gameOver = true;
            repaint();
            return;
        }

        megaMan.setHealth(3);

        int enemyBaseHp = 4;
        int scaledHp = enemyBaseHp + (gameStatus.getCurrentLevel() - 1) * 2;
        beeCopter.setHp(scaledHp);

        int baseSpeed = 10;
        int scaledSpeed = baseSpeed + gameStatus.getCurrentLevel();
        beeCopter.setAttackSpeed(scaledSpeed);

        playerY = floorY - playerSize;
        velocityY = 0;
        isJumping = false;

        projectiles.clear(); // Clear projectiles at the start of each level

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

        if (e.getKeyCode() == KeyEvent.VK_M) {
            // Start charging the shot
            if (!isCharging) {
                isCharging = true;
                chargeStartTime = System.currentTimeMillis();
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_M) {
            // Release the shot when the player releases the button
            if (isCharging) {
                long chargeDuration = System.currentTimeMillis() - chargeStartTime;
                boolean isChargeComplete = chargeDuration >= megaMan.getChargeTime() * 1000L;

                int damage = isChargeComplete ? 4 : 1;
                int size = isChargeComplete ? chargeShotSize : projectileSize;

                // Fire a charge shot
                projectiles.add(new Projectile(playerX + playerSize, playerY + playerSize / 2 - size / 2, damage, size));
                isCharging = false;
                chargeStartTime = -1;
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
}
