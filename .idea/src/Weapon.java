public abstract class Weapon {
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    int damage;
    int direction;

    public Weapon(int damage, int direction) {
        if (!isValidDirection(direction)) {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        this.damage = damage;
        this.direction = direction;
    }

    public int getDamage() {
        return damage;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        if (!isValidDirection(direction)) {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        this.direction = direction;
    }

    private boolean isValidDirection(int direction) {
        return direction == UP || direction == DOWN || direction == LEFT || direction == RIGHT;
    }

    public abstract void use();
}