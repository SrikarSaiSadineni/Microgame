public class GameStatus {
    private int currentLevel;
    private int timerPerLevel; // in seconds
    private int playerLives;

    public GameStatus(int startingLevel, int startingLives) {
        this.currentLevel = startingLevel;
        this.playerLives = startingLives;
        updateTimer();
    }

    private void updateTimer() {
        int baseTime = 12;
        int reductionPerLevel = 1;
        timerPerLevel = baseTime - ((currentLevel - 1) * reductionPerLevel);
    }

    public void levelUp() {
        currentLevel++;
        updateTimer();
    }

    public void loseLife() {
        if (playerLives > 0) {
            playerLives--;
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getTimerPerLevel() {
        return timerPerLevel;
    }

    public int getPlayerLives() {
        return playerLives;
    }
}