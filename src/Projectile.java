public class Projectile {
    private int x, y;
    private boolean active;
    private int damage;
    private int size;

    public Projectile(int startX, int startY, int damage, int size) {
        this.x = startX;
        this.y = startY;
        this.damage = damage;
        this.size = size;
        this.active = true;
    }

    public void move() {
        x += 10; // Speed of projectile
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public int getSize() {
        return size;
    }
}