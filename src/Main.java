import javax.swing.*;

public class Main {

    public static void main (String[]args) {

        GameWindow gamewindow = new GameWindow();
        gamewindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gamewindow.setBounds(10, 10, 700, 550);
        gamewindow.setVisible(true);
    }
}
