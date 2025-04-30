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
        System.out.println("Charging started.");
    }

    public void stopCharging() {
        isCharging = false;
        System.out.println("Charging stopped.");
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

    public abstract void attack();

    public abstract void takeDamage(int amount);
}