package pong;

import pong.i18n.Lang;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameFrame extends JFrame {
    public GameFrame(GameMode mode, Difficulty difficulty, Lang lang) {
        setTitle("Pong (OOP) - " + mode);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel(mode, difficulty, lang, () -> {
            dispose();
            SwingUtilities.invokeLater(PongApp::startGame);
        });
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);

        // ensure key focus
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }
}
