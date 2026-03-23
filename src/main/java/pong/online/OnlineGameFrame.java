package pong.online;

import pong.WindowPreset;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Game window for online sessions.
 *
 * <p>Hosts an {@link OnlineGamePanel} as the content pane and an
 * {@link OnlineInGameMenuPanel} as the glass pane (shown on {@code Esc}).</p>
 *
 * <p>Two constructors are provided – one for the host and one for the joiner.
 * Both configure the same frame; only the panel flavour differs.</p>
 */
public final class OnlineGameFrame extends JFrame {

    private final OnlineGamePanel  panel;
    private final OnlineServer     server; // null for joiner
    private final OnlineClient     client; // null for host
    private final AtomicBoolean    disconnecting = new AtomicBoolean(false);

    // ── constructors ──────────────────────────────────────────────────────────

    /** Creates the game frame for the host. */
    public OnlineGameFrame(OnlineServer server, WindowPreset preset, Runnable onReturnToMenu) {
        this.server = server;
        this.client = null;
        this.panel  = new OnlineGamePanel(server);
        server.setOnDisconnect(() -> handleRemoteDisconnect(onReturnToMenu));
        setup(preset, onReturnToMenu);
    }

    /** Creates the game frame for the joiner. */
    public OnlineGameFrame(OnlineClient client, WindowPreset preset, Runnable onReturnToMenu) {
        this.client = client;
        this.server = null;
        this.panel  = new OnlineGamePanel(client);
        client.setOnDisconnect(() -> handleRemoteDisconnect(onReturnToMenu));
        setup(preset, onReturnToMenu);
    }

    // ── setup ─────────────────────────────────────────────────────────────────

    private void setup(WindowPreset preset, Runnable onReturnToMenu) {
        setTitle("Pong – Online");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect(onReturnToMenu);
            }
        });
        setResizable(false);

        panel.setPreferredSize(preset.toDimension());
        setContentPane(panel);
        pack();

        // overlay shown when Esc is pressed
        OnlineInGameMenuPanel overlay = new OnlineInGameMenuPanel(
                // onResume: close overlay, return focus
                () -> {
                    setGlassPane(new JPanel());
                    getGlassPane().setVisible(false);
                    panel.requestFocusInWindow();
                },
                // onDisconnect
                () -> disconnect(onReturnToMenu));

        panel.setOnEscPressed(() -> {
            boolean overlayVisible = getGlassPane().isVisible()
                    && getGlassPane() instanceof OnlineInGameMenuPanel;
            if (overlayVisible) {
                setGlassPane(new JPanel());
                getGlassPane().setVisible(false);
                panel.requestFocusInWindow();
            } else {
                setGlassPane(overlay);
                overlay.setVisible(true);
            }
        });

        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }

    // ── disconnect helpers ────────────────────────────────────────────────────

    /** Clean disconnect initiated by the local player (Disconnect button or window close). */
    private void disconnect(Runnable onReturnToMenu) {
        if (!disconnecting.compareAndSet(false, true)) return;
        stopSession();
        dispose();
        SwingUtilities.invokeLater(onReturnToMenu);
    }

    /** Unexpected disconnect detected on a background thread; show a dialog. */
    private void handleRemoteDisconnect(Runnable onReturnToMenu) {
        if (!disconnecting.compareAndSet(false, true)) return;
        stopSession();
        JOptionPane.showMessageDialog(
                this,
                "The connection was lost.",
                "Disconnected",
                JOptionPane.WARNING_MESSAGE);
        dispose();
        SwingUtilities.invokeLater(onReturnToMenu);
    }

    private void stopSession() {
        if (server != null) server.stop();
        if (client != null) client.stop();
    }
}
