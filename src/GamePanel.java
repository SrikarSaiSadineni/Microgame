import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private GameWindow game;

    public GamePanel(GameWindow game) {
        this.game = game;
        setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.drawGame(g);
    }
}
