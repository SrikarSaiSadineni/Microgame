import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * The {@code GameWindow} class represents the main game window of the Mega Man X minigame.
 * It extends {@link JFrame} and implements {@link KeyListener} and {@link ActionListener}.
 * This class manages the game state, updates the game logic, responds for user input,
 * and renders the game on the screen.
 */
public class GameWindow extends JFrame implements KeyListener, ActionListener {

    /**
     * Timer to control the game time.
     */
    private Timer timer;

    /**
     * Delay between game updates in milliseconds.
     */
    private int delay = 20;

    /**
     * The panel that contains the game rendering and handles painting.
     */
    private GamePanel panel;

    /**
     * The game status containing information about the current level and player's lives.
     */
    private static GameStatus gameStatus = new GameStatus(1, 3);

    /**
     * The player character, Mega Man X.
     */
    private static MegaMan megaMan = new MegaMan();

    /**
     * The enemy, BeeCopter.
     */
    private static BeeCopter beeCopter = new BeeCopter();

    /**
     * Player's X-coordinate position on the screen.
     */
    private int playerX = 100;

    /**
     * Player's Y-coordinate position on the screen.
     */
    private int playerY;

    /**
     * The size of the player character.
     */
    private final int playerSize = 40;

    /**
     * The Y-coordinate of the floor where the player stands.
     */
    private final int floorY = 430;

    /**
     * The player's vertical velocity.
     */
    private int velocityY = 0;

    /**
     * Flag indicating whether the player is currently jumping.
     */
    private boolean isJumping = false;

    /**
     * A list of projectiles fired by the player.
     */
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    /**
     * The size of a regular projectile.
     */
    private final int projectileSize = 20;

    /**
     * The size of a charged projectile.
     */
    private final int chargeShotSize = 40;

    /**
     * The time when charging started, used to determine the charge duration.
     */
    private long chargeStartTime = -1;

    /**
     * Flag indicating whether the player is charging a shot.
     */
    private boolean isCharging = false;

    /**
     * The X-coordinate of the enemy's starting position.
     */
    private final int enemyX = 500;

    /**
     * The Y-coordinate of the enemy's starting position.
     */
    private int enemyY = floorY - (playerSize + 100);

    /**
     * The size of the enemy character.
     */
    private final int enemySize = 40;

    /**
     * Flag indicating whether the enemy's projectile is active.
     */
    private boolean isEnemyProjectileActive = false;

    /**
     * The X-coordinate of the enemy's projectile.
     */
    private int enemyProjectileX;

    /**
     * The Y-coordinate of the enemy's projectile.
     */
    private int enemyProjectileY;

    /**
     * Flag indicating whether the game is over.
     */
    private boolean gameOver = false;

    /**
     * Flag indicating whether the player has won.
     */
    private boolean playerWon = false;

    /**
     * The start time of the current level.
     */
    private long levelStartTime;

    /**
     * The time limit for the current level in milliseconds.
     */
    private long timeLimitMs;

    /**
     * The background image used in the game.
     */
    private Image backgroundImage;

    /**
     * The sprite for the player character in the idle state.
     */
    private Image playerIdleSprite;

    /**
     * The sprite for the player character in the jump state.
     */
    private Image playerJumpSprite;

    /**
     * The sprite for the player character while shooting on the ground.
     */
    private Image playerShootingSprite;

    /**
     * The sprite for the player character while shooting in the air.
     */
    private Image playerShootingAirSprite;

    /**
     * The sprite for the enemy character.
     */
    private Image enemySprite;

    /**
     * The sprite for regular projectiles.
     */
    private Image bulletSprite;

    /**
     * The sprite for charged projectiles.
     */
    private Image chargeBulletSprite;

    /**
     * Constructs a new {@code GameWindow}.
     * Initializes the window properties, loads game assets, and starts the game.
     */
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

        panel = new GamePanel(this);
        add(panel);

        timer = new Timer(delay, this);
        timer.start();

        // Load sprites
        backgroundImage = new ImageIcon("images/background.png").getImage();
        playerIdleSprite = new ImageIcon("images/player_idle.png").getImage();
        playerJumpSprite = new ImageIcon("images/player_jump.png").getImage();
        playerShootingSprite = new ImageIcon("images/player_shooting.png").getImage();
        playerShootingAirSprite = new ImageIcon("images/player_shooting_air.png").getImage();
        enemySprite = new ImageIcon("images/enemy.png").getImage();
        bulletSprite = new ImageIcon("images/bullet.png").getImage();
        chargeBulletSprite = new ImageIcon("images/charge_bullet.png").getImage();

        resetGame();
        setVisible(true);
    }

    /**
     * Draws the game graphics including the player, projectiles, enemies, and HUD elements.
     *
     * @param g The {@code Graphics} object used for drawing.
     */
    public void drawGame(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        Image currentPlayerSprite = playerIdleSprite;
        if (isJumping && isCharging) currentPlayerSprite = playerShootingAirSprite;
        else if (isJumping) currentPlayerSprite = playerJumpSprite;
        else if (isCharging) currentPlayerSprite = playerShootingSprite;

        g.drawImage(currentPlayerSprite, playerX, playerY,
                currentPlayerSprite.getWidth(null) * 2,
                currentPlayerSprite.getHeight(null) * 2, this);

        for (Projectile p : projectiles) {
            if (p.isActive()) {
                Image img = (p.getSize() == chargeShotSize) ? chargeBulletSprite : bulletSprite;
                g.drawImage(img, p.getX(), p.getY(), p.getSize(), p.getSize(), this);
            }
        }

        if (beeCopter.getHp() > 0) {
            g.drawImage(enemySprite, enemyX, enemyY,
                    enemySprite.getWidth(null), enemySprite.getHeight(null), this);
        }

        if (isEnemyProjectileActive) {
            g.setColor(Color.orange);
            g.fillOval(enemyProjectileX, enemyProjectileY, projectileSize, projectileSize);
        }

        g.setColor(Color.white);
        g.setFont(new Font("serif", Font.BOLD, 20));
        g.drawString("Player Health: " + megaMan.getHealth(), 20, 50);
        g.drawString("Enemy Health: " + beeCopter.getHp(), 500, 50);
        g.drawString("Lives: " + gameStatus.getPlayerLives(), 20, 80);
        g.drawString("Level: " + gameStatus.getCurrentLevel(), 20, 110);

        long elapsed = System.currentTimeMillis() - levelStartTime;
        long remaining = Math.max(0, (timeLimitMs - elapsed) / 1000);
        g.drawString("Time Left: " + remaining + "s", 500, 80);

        if (gameOver) {
            g.setFont(new Font("serif", Font.BOLD, 40));
            g.setColor(Color.RED);
            String text = playerWon ? "You Won!" : "You Lost!";
            int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
            g.drawString(text, x, 280);

            g.setFont(new Font("serif", Font.BOLD, 24));
            String restart = "Press R to Restart";
            int rx = (getWidth() - g.getFontMetrics().stringWidth(restart)) / 2;
            g.drawString(restart, rx, 330);
        }
    }

    /**
     * Called whenever the timer fires an action event. It updates the game state.
     *
     * @param e The {@code ActionEvent} triggered by the timer.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - levelStartTime >= timeLimitMs && beeCopter.getHp() > 0) {
            gameStatus.loseLife();
            if (gameStatus.getPlayerLives() > 0) {
                resetGame();
            } else {
                gameOver = true;
                playerWon = false;
            }
            panel.repaint();
            return;
        }

        if (isJumping) {
            velocityY += 1;
            playerY += velocityY;

            if (playerY >= floorY - playerSize) {
                playerY = floorY - playerSize;
                isJumping = false;
                velocityY = 0;
            }
        }

        ArrayList<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.move();
                if (beeCopter.getHp() > 0 &&
                        p.getX() + p.getSize() > enemyX &&
                        p.getX() < enemyX + enemySize &&
                        p.getY() + p.getSize() > enemyY &&
                        p.getY() < enemyY + enemySize) {
                    beeCopter.setHp(beeCopter.getHp() - p.getDamage());
                    p.setActive(false);
                    toRemove.add(p);

                    if (beeCopter.getHp() <= 0) {
                        gameStatus.levelUp();
                        resetGame();
                        return;
                    }
                }
                if (p.getX() > getWidth()) {
                    p.setActive(false);
                    toRemove.add(p);
                }
            }
        }
        projectiles.removeAll(toRemove);

        if (!isEnemyProjectileActive && beeCopter.getHp() > 0) {
            isEnemyProjectileActive = true;
            enemyProjectileX = enemyX;
            enemyProjectileY = enemyY + enemySize / 2 - projectileSize / 2;
        }

        if (isEnemyProjectileActive) {
            enemyProjectileX -= beeCopter.getAttackSpeed();
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

        panel.repaint();
    }

    /**
     * Resets the game to its initial state for the next level or when the game is restarted.
     */
    private void resetGame() {
        if (gameStatus.getCurrentLevel() >= 6) {
            gameOver = true;
            playerWon = true;
            panel.repaint();
            return;
        }

        megaMan.setHealth(3);
        beeCopter.setHp(4 + (gameStatus.getCurrentLevel() - 1) * 2);
        beeCopter.setAttackSpeed(10 + gameStatus.getCurrentLevel());

        playerY = floorY - playerSize;
        velocityY = 0;
        isJumping = false;

        projectiles.clear();
        isEnemyProjectileActive = false;

        timeLimitMs = gameStatus.getTimerPerLevel() * 1000L;
        levelStartTime = System.currentTimeMillis();

        panel.repaint();
    }

    /**
     * Handles key press events. Used to control the player's actions (e.g., jumping, shooting).
     *
     * @param e The {@code KeyEvent} that occurred.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
            gameOver = false;
            playerWon = false;
            gameStatus = new GameStatus(1, 3);
            megaMan = new MegaMan();
            beeCopter = new BeeCopter();
            resetGame();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE && !isJumping) {
            isJumping = true;
            velocityY = -15;
        }

        if (e.getKeyCode() == KeyEvent.VK_M) {
            if (!isCharging) {
                isCharging = true;
                chargeStartTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * Handles key release events. Used to fire projectiles when the shooting button is released.
     *
     * @param e The {@code KeyEvent} that occurred.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_M && isCharging) {
            long chargeDuration = System.currentTimeMillis() - chargeStartTime;
            boolean charged = chargeDuration >= megaMan.getChargeTime() * 1000L;
            int damage = charged ? 4 : 1;
            int size = charged ? chargeShotSize : projectileSize;

            projectiles.add(new Projectile(playerX + playerSize,
                    playerY + playerSize / 2 - size / 2, damage, size));

            isCharging = false;
            chargeStartTime = -1;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
