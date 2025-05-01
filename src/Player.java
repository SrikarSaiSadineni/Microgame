public abstract class Player {
    int health;
    int chargeTime;
    int invincibilityFrames;
    boolean isCharging;

    public Player(int health, int chargeTime, int invincibilityFrames) {
        this.health = health;
        this.chargeTime = chargeTime;
        this.invincibilityFrames = invincibilityFrames;
        this.isCharging = false;
    }

    public void startCharging() {
        isCharging = true;
    }

    public void stopCharging() {
        isCharging = false;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public int getHealth() {
        return health;
    }

    public int getChargeTime() {
        return chargeTime;
    }

    public int getInvincibilityFrames() {
        return invincibilityFrames;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public abstract void takeDamage();
}