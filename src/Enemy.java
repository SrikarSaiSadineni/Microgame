public abstract class Enemy {
    int hp;
    int invincibilityTimer;
    int attackSpeed;

    public Enemy(int hp, int invincibilityTimer, int attackSpeed) {
        this.hp = hp;
        this.invincibilityTimer = invincibilityTimer;
        this.attackSpeed = attackSpeed;
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

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed = attackSpeed;
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

    public abstract void takeDamage();
}