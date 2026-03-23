package pong.online;

import pong.WindowPreset;
import pong.util.GameConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

/**
 * Menu frame for the LAN online multiplayer flow.
 *
 * <p>Uses a {@link CardLayout} to present three views:</p>
 * <ol>
 *   <li><b>Main</b> – "Host Game" / "Join Game" / "Back to Menu"</li>
 *   <li><b>Host</b> – shows local IP addresses and port, "Start Hosting" button,
 *       and a status label ("Waiting for player…")</li>
 *   <li><b>Join</b> – host-IP and port input fields, "Connect" button</li>
 * </ol>
 */
public final class OnlineMenuFrame extends JFrame {

    private static final String CARD_MAIN = "main";
    private static final String CARD_HOST = "host";
    private static final String CARD_JOIN = "join";

    private final CardLayout cards    = new CardLayout();
    private final JPanel     deck     = new JPanel(cards);
    private final WindowPreset preset;
    private final Runnable backToMenu;

    // host-card mutable state
    private final JLabel   hostStatusLabel = new JLabel(" ");
    private OnlineServer   pendingServer   = null;

    // join-card mutable state
    private final JTextField joinHostField = new JTextField(18);
    private final JTextField joinPortField = new JTextField(String.valueOf(Protocol.DEFAULT_PORT), 7);

    // ── constructor ───────────────────────────────────────────────────────────

    public OnlineMenuFrame(WindowPreset preset, Runnable backToMenu) {
        this.preset     = preset;
        this.backToMenu = backToMenu;

        setTitle("Pong – Online Multiplayer");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                goBack();
            }
        });
        setResizable(false);

        deck.setBackground(GameConstants.BG);
        deck.add(buildMainCard(), CARD_MAIN);
        deck.add(buildHostCard(), CARD_HOST);
        deck.add(buildJoinCard(), CARD_JOIN);

        setContentPane(deck);
        pack();
        setLocationRelativeTo(null);
    }

    // ── card builders ─────────────────────────────────────────────────────────

    private JPanel buildMainCard() {
        JPanel p = basePanel();

        p.add(makeTitle("Online Multiplayer"), gbc(0, 0, new Insets(0, 0, 24, 0)));

        JButton btnHost = accentButton("Host Game", 220, 44);
        btnHost.addActionListener(e -> cards.show(deck, CARD_HOST));
        p.add(btnHost, gbc(0, 1, new Insets(6, 0, 6, 0)));

        JButton btnJoin = accentButton("Join Game", 220, 44);
        btnJoin.addActionListener(e -> cards.show(deck, CARD_JOIN));
        p.add(btnJoin, gbc(0, 2, new Insets(6, 0, 6, 0)));

        JButton btnBack = plainButton("← Back to Main Menu");
        btnBack.addActionListener(e -> goBack());
        p.add(btnBack, gbc(0, 3, new Insets(20, 0, 0, 0)));

        return p;
    }

    private JPanel buildHostCard() {
        JPanel p = basePanel();

        p.add(makeTitle("Host Game"), gbc(0, 0, new Insets(0, 0, 16, 0)));

        // build address list from all viable LAN interfaces
        List<String> ips = NetUtil.getLocalIpv4Addresses();
        StringBuilder sb = new StringBuilder("<html><b>Share this address with the other player:</b>");
        for (String ip : ips) {
            sb.append("<br>&nbsp;&nbsp;")
              .append(ip).append(':').append(Protocol.DEFAULT_PORT);
        }
        sb.append("</html>");
        JLabel ipLabel = new JLabel(sb.toString());
        ipLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ipLabel.setForeground(GameConstants.FG);
        p.add(ipLabel, gbc(0, 1, new Insets(0, 4, 16, 4)));

        hostStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        hostStatusLabel.setForeground(GameConstants.ACCENT);
        p.add(hostStatusLabel, gbc(0, 2, new Insets(0, 4, 12, 4)));

        JButton btnStart = accentButton("Start Hosting", 200, 40);
        btnStart.addActionListener(e -> startHosting(btnStart));
        p.add(btnStart, gbc(0, 3, new Insets(4, 0, 8, 0)));

        JButton btnBack = plainButton("← Back");
        btnBack.addActionListener(e -> {
            cancelPendingServer();
            btnStart.setEnabled(true);
            hostStatusLabel.setText(" ");
            cards.show(deck, CARD_MAIN);
        });
        p.add(btnBack, gbc(0, 4, new Insets(12, 0, 0, 0)));

        return p;
    }

    private JPanel buildJoinCard() {
        JPanel p = basePanel();

        p.add(makeTitle("Join Game"), gbc(0, 0, new Insets(0, 0, 16, 0)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.anchor = GridBagConstraints.WEST;

        c.gridy = 1; c.insets = new Insets(4, 4, 2, 4);
        p.add(sectionLabel("Host IP / Address:"), c);

        styleTextField(joinHostField);
        c.gridy = 2; c.insets = new Insets(0, 4, 10, 4); c.fill = GridBagConstraints.HORIZONTAL;
        p.add(joinHostField, c);
        c.fill = GridBagConstraints.NONE;

        c.gridy = 3; c.insets = new Insets(4, 4, 2, 4);
        p.add(sectionLabel("Port:"), c);

        styleTextField(joinPortField);
        c.gridy = 4; c.insets = new Insets(0, 4, 14, 4); c.fill = GridBagConstraints.HORIZONTAL;
        p.add(joinPortField, c);
        c.fill = GridBagConstraints.NONE;

        JButton btnConnect = accentButton("Connect", 200, 40);
        btnConnect.addActionListener(e -> joinGame(btnConnect));
        c.gridy = 5; c.anchor = GridBagConstraints.CENTER; c.insets = new Insets(4, 0, 8, 0);
        p.add(btnConnect, c);

        JButton btnBack = plainButton("← Back");
        btnBack.addActionListener(e -> cards.show(deck, CARD_MAIN));
        c.gridy = 6; c.insets = new Insets(12, 0, 0, 0);
        p.add(btnBack, c);

        return p;
    }

    // ── actions ───────────────────────────────────────────────────────────────

    private void startHosting(JButton btnStart) {
        btnStart.setEnabled(false);
        hostStatusLabel.setText("Starting…");

        OnlineServer srv = new OnlineServer();
        try {
            srv.startListening();
        } catch (IOException ex) {
            btnStart.setEnabled(true);
            hostStatusLabel.setText(" ");
            JOptionPane.showMessageDialog(
                    this,
                    "Could not bind port " + Protocol.DEFAULT_PORT + ":\n" + ex.getMessage()
                            + "\n\nCheck that no other process is using this port.",
                    "Port Unavailable",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        pendingServer = srv;
        hostStatusLabel.setText("Waiting for player to join…");

        srv.waitForClientAsync(() -> {
            pendingServer = null;
            dispose();
            new OnlineGameFrame(srv, preset, backToMenu).setVisible(true);
        });
    }

    private void joinGame(JButton btnConnect) {
        String host = joinHostField.getText().trim();
        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please enter the host IP address.", "Missing Host",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(joinPortField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this, "Invalid port number.", "Invalid Port",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnConnect.setEnabled(false);
        final int finalPort = port;

        Thread t = new Thread(() -> {
            OnlineClient cli = new OnlineClient();
            try {
                cli.connect(host, finalPort);
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new OnlineGameFrame(cli, preset, backToMenu).setVisible(true);
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    btnConnect.setEnabled(true);
                    JOptionPane.showMessageDialog(
                            OnlineMenuFrame.this,
                            "Could not connect to " + host + ":" + finalPort + "\n" + ex.getMessage()
                                    + "\n\nMake sure:\n"
                                    + "  • Both devices are on the same network\n"
                                    + "  • The host IP address is correct\n"
                                    + "  • The host's firewall allows TCP port " + Protocol.DEFAULT_PORT,
                            "Connection Failed",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "OnlineConnect");
        t.setDaemon(true);
        t.start();
    }

    private void goBack() {
        cancelPendingServer();
        dispose();
        SwingUtilities.invokeLater(backToMenu);
    }

    private void cancelPendingServer() {
        if (pendingServer != null) {
            pendingServer.stop();
            pendingServer = null;
        }
    }

    // ── layout helpers ────────────────────────────────────────────────────────

    private static JPanel basePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(GameConstants.BG);
        p.setBorder(new EmptyBorder(40, 60, 40, 60));
        return p;
    }

    private static JLabel makeTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 30));
        lbl.setForeground(GameConstants.ACCENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(GameConstants.FG);
        return lbl;
    }

    private static JButton accentButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(GameConstants.ACCENT);
        btn.setForeground(GameConstants.BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(w, h));
        return btn;
    }

    private static JButton plainButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(new Color(180, 180, 190));
        btn.setBackground(GameConstants.BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void styleTextField(JTextField field) {
        field.setBackground(new Color(35, 35, 42));
        field.setForeground(GameConstants.FG);
        field.setCaretColor(GameConstants.FG);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 75)));
    }

    /** Returns a centered {@link GridBagConstraints} for the given row. */
    private static GridBagConstraints gbc(int col, int row, Insets insets) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = col;
        c.gridy = row;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = insets;
        return c;
    }
}
