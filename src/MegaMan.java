public class MegaMan extends Player {

    public MegaMan() {
        super(3, 1, 10, 15);
    }

    @Override
    public void takeDamage() {
        health--;
    }
}
