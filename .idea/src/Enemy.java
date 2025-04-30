public abstract class Enemy {
    int hp;
    int invincibilityTimer;

    public Enemy(int hp, int invincibilityTimer) {
        this.hp = hp;
        this.invincibilityTimer = invincibilityTimer;
    }

    public int getHp() {
        return hp;
    }

    public int getInvincibilityTimer() {
        return invincibilityTimer;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setInvincibilityTimer(int timer) {
        this.invincibilityTimer = timer;
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    public void tickInvincibility() {
        if (invincibilityTimer > 0) {
            invincibilityTimer--;
        }
    }

    public abstract void attack();

    public abstract void takeDamage(int amount);
}