package pong.online;

import pong.util.GameConstants;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Game panel for online sessions.
 *
 * <p>Renders the most recent {@link GameSnapshot} received from the server.
 * Key presses are translated into input messages and forwarded to the server:</p>
 * <ul>
 *   <li><b>Host (LEFT role)</b> – input is delivered directly to
 *       {@link OnlineServer#setLocalInput}.</li>
 *   <li><b>Joiner (RIGHT role)</b> – input is sent over TCP via
 *       {@link OnlineClient#sendInput}.</li>
 * </ul>
 *
 * <p>Both {@code W}/{@code S} and {@code ↑}/{@code ↓} are accepted so either
 * player can use whichever key pair they prefer.</p>
 */
public final class OnlineGamePanel extends JPanel {

    private volatile GameSnapshot latestSnapshot = GameSnapshot.empty();
    private volatile boolean      repaintPending = false;

    private final OnlineServer server; // non-null for host
    private final OnlineClient client; // non-null for joiner
    private final byte         role;

    private volatile boolean upDown   = false;
    private volatile boolean downDown = false;

    private Runnable onEscPressed;

    private final AtomicBoolean started = new AtomicBoolean(false);

    /** Creates a panel for the host (renders from server's game state). */
    public OnlineGamePanel(OnlineServer server) {
        this.server = server;
        this.client = null;
        this.role   = Protocol.ROLE_LEFT;
        init();
    }

    /** Creates a panel for the joiner (renders from received snapshots). */
    public OnlineGamePanel(OnlineClient client) {
        this.client = client;
        this.server = null;
        this.role   = Protocol.ROLE_RIGHT;
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setBackground(GameConstants.BG);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                boolean changed = false;
                if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)   { upDown   = true;  changed = true; }
                if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  { downDown = true;  changed = true; }
                if (changed) dispatchInput();
                if (k == KeyEvent.VK_ESCAPE && onEscPressed != null) onEscPressed.run();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int k = e.getKeyCode();
                boolean changed = false;
                if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)   { upDown   = false; changed = true; }
                if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  { downDown = false; changed = true; }
                if (changed) dispatchInput();
            }
        });
    }

    private void dispatchInput() {
        if (server != null) {
            server.setLocalInput(upDown, downDown);
        } else if (client != null) {
            client.sendInput(upDown, downDown);
        }
    }

    /** Called when a new snapshot arrives; may be called from any thread. */
    public void onSnapshot(GameSnapshot snap) {
        latestSnapshot = snap;
        if (!repaintPending) {
            repaintPending = true;
            repaint();
        }
    }

    /** Sets the callback invoked when the player presses {@code Esc}. */
    public void setOnEscPressed(Runnable callback) {
        this.onEscPressed = callback;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (started.compareAndSet(false, true)) {
            if (server != null) {
                server.startGameLoop(this::onSnapshot);
            } else if (client != null) {
                client.setOnSnapshot(this::onSnapshot);
                client.startReadLoop();
            }
        }
    }

    // ── rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameSnapshot snap = latestSnapshot;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // scale logical resolution to fill window
        int cw = getWidth(), ch = getHeight();
        if (cw != GameConstants.WIDTH || ch != GameConstants.HEIGHT) {
            g2.scale((double) cw / GameConstants.WIDTH, (double) ch / GameConstants.HEIGHT);
        }

        // middle dashed line
        g2.setColor(new Color(255, 255, 255, 40));
        for (int y = 0; y < GameConstants.HEIGHT; y += 26) {
            g2.fillRect(GameConstants.WIDTH / 2 - 2, y, 4, 16);
        }

        // paddles
        g2.setColor(GameConstants.FG);
        g2.fillRect(40,
                (int) snap.leftPaddleY(),
                GameConstants.PADDLE_WIDTH, GameConstants.PADDLE_HEIGHT);
        g2.fillRect(GameConstants.WIDTH - 40 - GameConstants.PADDLE_WIDTH,
                (int) snap.rightPaddleY(),
                GameConstants.PADDLE_WIDTH, GameConstants.PADDLE_HEIGHT);

        // ball
        g2.setColor(GameConstants.ACCENT);
        g2.fillOval((int) snap.ballX(), (int) snap.ballY(),
                GameConstants.BALL_SIZE, GameConstants.BALL_SIZE);

        // scores
        g2.setColor(GameConstants.FG);
        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        FontMetrics fm = g2.getFontMetrics();
        String sLeft  = String.valueOf(snap.scoreLeft());
        String sRight = String.valueOf(snap.scoreRight());
        int pad = 18;
        g2.drawString(sLeft,  GameConstants.WIDTH / 2 - pad - fm.stringWidth(sLeft),  60);
        g2.drawString(sRight, GameConstants.WIDTH / 2 + pad, 60);

        // status bar
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(new Color(255, 255, 255, 160));
        String roleTip = (role == Protocol.ROLE_LEFT) ? "You: LEFT  W/S" : "You: RIGHT  W/S or ↑/↓";
        g2.drawString("Online Multiplayer  |  " + roleTip + "  |  Esc: menu",
                16, GameConstants.HEIGHT - 16);

        // paused overlay
        if (snap.paused()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 54));
            String t = "PAUSED";
            int w = g2.getFontMetrics().stringWidth(t);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(t, (GameConstants.WIDTH - w) / 2, GameConstants.HEIGHT / 2);
        }

        // game-over overlay
        if (snap.gameOver()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 54));
            String winner = snap.scoreLeft() > snap.scoreRight() ? "LEFT WINS" : "RIGHT WINS";
            int w = g2.getFontMetrics().stringWidth(winner);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.drawString(winner, (GameConstants.WIDTH - w) / 2, GameConstants.HEIGHT / 2);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String hint = "Press Esc to open menu";
            int hw = g2.getFontMetrics().stringWidth(hint);
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawString(hint, (GameConstants.WIDTH - hw) / 2, GameConstants.HEIGHT / 2 + 34);
        }

        // countdown overlay
        int cd = snap.countdown();
        if (cd > 0) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 120));
            String num = String.valueOf(cd);
            FontMetrics cfm = g2.getFontMetrics();
            int tw = cfm.stringWidth(num);
            g2.setColor(new Color(255, 255, 255, 210));
            g2.drawString(num,
                    (GameConstants.WIDTH - tw) / 2,
                    GameConstants.HEIGHT / 2 + cfm.getAscent() / 2 - cfm.getDescent());
        }

        repaintPending = false;
        g2.dispose();
    }
}
