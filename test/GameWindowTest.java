import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.awt.*;
import java.awt.event.*;

public class GameWindowTest {

    private GameWindow gameWindow;

    @BeforeEach
    public void setup() {
        gameWindow = Mockito.spy(new GameWindow());  // Create a mock instance of GameWindow
    }

    @Test
    public void testPlayerJump() {
        // Initial state of the player
        assertEquals(430 - 40, gameWindow.getPlayerY()); // Should be on the floor initially

        // Simulate a key press for jumping
        KeyEvent jumpEvent = new KeyEvent(gameWindow, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
        gameWindow.keyPressed(jumpEvent);

        // The player should start jumping
        assertTrue(gameWindow.isJumping());

        // Simulate a game tick that moves the player up
        gameWindow.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        // The player should have moved up, simulating a jump (velocity is negative)
        assertTrue(gameWindow.getPlayerY() < 430 - 40);
    }

    @Test
    public void testProjectileCreation() {
        // Simulate the key press for charging the shot
        KeyEvent chargeStartEvent = new KeyEvent(gameWindow, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_M, 'M');
        gameWindow.keyPressed(chargeStartEvent);

        // Simulate releasing the key after a short period (charge duration)
        try {
            Thread.sleep(200);  // Simulate a brief charge time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        KeyEvent chargeReleaseEvent = new KeyEvent(gameWindow, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_M, 'M');
        gameWindow.keyReleased(chargeReleaseEvent);

        // Assert that a projectile is created
        assertFalse(gameWindow.getProjectiles().isEmpty());  // Check that the projectile list is not empty
    }

    @Test
    public void testEnemyProjectileCollision() {
        // Setup initial health and positions
        int initialHealth = gameWindow.getMegaMan().getHealth();
        gameWindow.setEnemyProjectileActive(true);  // Simulate enemy projectile creation

        // Move the projectile to collide with the player
        gameWindow.setEnemyProjectileX(gameWindow.getPlayerX() + 20);  // Move projectile near player
        gameWindow.setEnemyProjectileY(gameWindow.getPlayerY() + 20);

        // Simulate action event (game tick)
        gameWindow.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        // Assert that the player's health has decreased
        assertTrue(gameWindow.getMegaMan().getHealth() < initialHealth);
    }

    @Test
    public void testLevelUpAfterEnemyDefeated() {
        // Set initial conditions
        gameWindow.getBeeCopter().setHp(0);  // Simulate bee copter defeat

        // Simulate the game loop
        gameWindow.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        // Check if the game level was incremented after bee copter was defeated
        assertEquals(2, gameWindow.getGameStatus().getCurrentLevel());
    }

    @Test
    public void testGameOver() {
        // Simulate game over scenario
        gameWindow.getMegaMan().setHealth(0);  // Set player health to 0
        gameWindow.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        // Check if game over state is set
        assertTrue(gameWindow.isGameOver());
    }

    @Test
    public void testResetGame() {
        // Simulate resetting the game
        gameWindow.resetGame();

        // Check that the player health is reset
        assertEquals(3, gameWindow.getMegaMan().getHealth());

        // Check that the enemy health is reset
        assertEquals(4, gameWindow.getBeeCopter().getHp());

        // Assert other reset conditions like projectiles being cleared
        assertTrue(gameWindow.getProjectiles().isEmpty());
    }

    @AfterEach
    public void tearDown() {
        gameWindow = null;
    }
}
