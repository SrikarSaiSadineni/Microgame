import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class GameWindow extends JFrame implements KeyListener, ActionListener {


    private Timer timer;
    private int delay = 8;


    private int playerX = 100;
    private int playerY = 100;


    private int score = -1;



    public GameWindow()
    {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(true);
        timer = new Timer(delay,this);
        timer.start();
    }

    public void paint(Graphics g)
    {

        g.setColor(Color.cyan);
        g.fillRect(0, 0, 695, 592);


        g.setColor(Color.red);




        g.setColor(Color.white);
        g.setFont(new Font("serif",Font.BOLD,25));
        g.drawString(""+score, 300, 30);


    }
    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();







        repaint();

    }
    @Override
    public void keyPressed(KeyEvent e) {


    }
    @Override
    public void keyReleased(KeyEvent e) {



    }
    @Override
    public void keyTyped(KeyEvent arg0) {


    }
}