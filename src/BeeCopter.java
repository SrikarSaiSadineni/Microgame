public class BeeCopter extends Enemy {

    public BeeCopter() {
        super(4, 10, 10);
    }

    @Override
    public void attack() {

    }

    @Override
    public void takeDamage() {
        hp--;
    }
}
