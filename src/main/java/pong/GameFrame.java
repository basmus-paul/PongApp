package pong;

import pong.i18n.Lang;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameFrame extends JFrame {
    public GameFrame(GameMode mode, Difficulty difficulty, Lang lang, WindowPreset preset) {
        setTitle("Pong (OOP) - " + mode);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ── game panel ────────────────────────────────────────────────────────
        GamePanel panel = new GamePanel(mode, difficulty, lang, () -> {
            dispose();
            SwingUtilities.invokeLater(PongApp::startGame);
        });
        panel.setPreferredSize(preset.toDimension());
        setContentPane(panel);
        pack();

        // ── in-game overlay menu (glass pane) ─────────────────────────────────
        InGameMenuPanel overlay = new InGameMenuPanel(
                mode, difficulty, lang, preset, getGraphicsConfiguration(),
                // onResume
                () -> {
                    setGlassPane(new javax.swing.JPanel());  // clear glass pane
                    getGlassPane().setVisible(false);
                    panel.resume();
                    panel.requestFocusInWindow();
                },
                // onNewGame
                result -> {
                    PongApp.updatePreferences(result.lang(), result.preset());
                    dispose();
                    SwingUtilities.invokeLater(() ->
                            new GameFrame(result.mode(), result.difficulty(),
                                    result.lang(), result.preset()).setVisible(true));
                },
                // onExit
                () -> {
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

        setLocationRelativeTo(null);

        // ensure key focus
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }
}
