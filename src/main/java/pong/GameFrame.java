package pong;

import pong.i18n.Lang;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class GameFrame extends JFrame {
    public GameFrame(GameMode mode, Difficulty difficulty, Lang lang, boolean fullscreen) {
        setTitle("Pong (OOP) - " + mode);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        if (fullscreen) setUndecorated(true);

        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();

        GamePanel panel = new GamePanel(mode, difficulty, lang, () -> {
            if (gd.getFullScreenWindow() == this) gd.setFullScreenWindow(null);
            dispose();
            SwingUtilities.invokeLater(PongApp::startGame);
        });
        setContentPane(panel);
        pack();

        if (fullscreen) {
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setLocationRelativeTo(null);
            }
        } else {
            setLocationRelativeTo(null);
        }

        // ensure key focus
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }
}
