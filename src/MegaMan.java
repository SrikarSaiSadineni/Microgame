public class MegaMan extends Player {

    public MegaMan() {
        super(3, 60, 10);
    }

    @Override
    public void takeDamage() {
        health--;
    }
}
