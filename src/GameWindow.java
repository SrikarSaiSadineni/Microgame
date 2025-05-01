import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GameWindow extends JFrame implements KeyListener, ActionListener {

    private Timer timer;
    private int delay = 20;

    private GamePanel panel;

    private static GameStatus gameStatus = new GameStatus(1, 3);
    private static MegaMan megaMan = new MegaMan();
    private static BeeCopter beeCopter = new BeeCopter();

    private int playerX = 100;
    private int playerY;
    private final int playerSize = 40;
    private final int floorY = 430;
    private int velocityY = 0;
    private boolean isJumping = false;

    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private final int projectileSize = 20;
    private final int chargeShotSize = 40;

    private long chargeStartTime = -1;
    private boolean isCharging = false;

    private final int enemyX = 500;
    private int enemyY = floorY - (playerSize + 100);
    private final int enemySize = 40;

    private boolean isEnemyProjectileActive = false;
    private int enemyProjectileX;
    private int enemyProjectileY;

    private boolean gameOver = false;
    private boolean playerWon = false;

    private long levelStartTime;
    private long timeLimitMs;

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

    @Override public void keyTyped(KeyEvent e) {}
}
