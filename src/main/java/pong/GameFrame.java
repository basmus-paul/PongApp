package pong;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameFrame extends JFrame {
    public GameFrame(GameMode mode) {
        setTitle("Pong (OOP) - " + mode);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel(mode);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);

        // ensure key focus
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }
}
