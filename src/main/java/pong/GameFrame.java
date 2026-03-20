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

        // ── game panel ────────────────────────────────────────────────────────
        GamePanel panel = new GamePanel(mode, difficulty, lang, () -> {
            if (gd.getFullScreenWindow() == this) gd.setFullScreenWindow(null);
            dispose();
            SwingUtilities.invokeLater(PongApp::startGame);
        });
        setContentPane(panel);
        pack();

        // ── in-game overlay menu (glass pane) ─────────────────────────────────
        InGameMenuPanel overlay = new InGameMenuPanel(
                mode, difficulty, lang, fullscreen,
                // onResume
                () -> {
                    setGlassPane(new javax.swing.JPanel());  // clear glass pane
                    getGlassPane().setVisible(false);
                    panel.resume();
                    panel.requestFocusInWindow();
                },
                // onNewGame
                result -> {
                    PongApp.updatePreferences(result.lang(), result.fullscreen());
                    if (gd.getFullScreenWindow() == this) gd.setFullScreenWindow(null);
                    dispose();
                    SwingUtilities.invokeLater(() ->
                            new GameFrame(result.mode(), result.difficulty(),
                                    result.lang(), result.fullscreen()).setVisible(true));
                },
                // onExit
                () -> {
                    if (gd.getFullScreenWindow() == this) gd.setFullScreenWindow(null);
                    dispose();
                    SwingUtilities.invokeLater(PongApp::startGame);
                }
        );

        // ── ESC key: pause + show / hide overlay ──────────────────────────────
        panel.setOnEscPressed(() -> {
            boolean overlayVisible = getGlassPane().isVisible()
                    && getGlassPane() instanceof InGameMenuPanel;
            if (overlayVisible) {
                // ESC pressed again → resume
                setGlassPane(new javax.swing.JPanel());
                getGlassPane().setVisible(false);
                panel.resume();
                panel.requestFocusInWindow();
            } else {
                panel.pause();
                setGlassPane(overlay);
                overlay.setVisible(true);
            }
        });

        // ── fullscreen ────────────────────────────────────────────────────────
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
