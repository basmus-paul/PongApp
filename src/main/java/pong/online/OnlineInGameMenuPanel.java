package pong.online;

import pong.util.GameConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Minimal semi-transparent overlay for online sessions.
 *
 * <p>Shown when the player presses {@code Esc} during an online game. Unlike the
 * full local {@code InGameMenuPanel} it only offers two actions: resume the
 * current game, or disconnect and return to the menu.</p>
 */
public final class OnlineInGameMenuPanel extends JPanel {

    /**
     * @param onResume     called when the player clicks "Resume" (closes the overlay)
     * @param onDisconnect called when the player clicks "Disconnect"
     */
    public OnlineInGameMenuPanel(Runnable onResume, Runnable onDisconnect) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        // ── card ─────────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setBackground(new Color(18, 18, 22, 230));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("ONLINE GAME");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(GameConstants.ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(20));

        JButton btnResume = makeButton("Resume", GameConstants.ACCENT, GameConstants.BG);
        btnResume.addActionListener(e -> onResume.run());
        card.add(btnResume);

        card.add(Box.createVerticalStrut(10));

        JButton btnDisconnect = makeButton("Disconnect", new Color(200, 60, 60), Color.WHITE);
        btnDisconnect.addActionListener(e -> onDisconnect.run());
        card.add(btnDisconnect);

        add(card);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // dim the game behind the overlay
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 38));
        btn.setMaximumSize(new Dimension(160, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}
