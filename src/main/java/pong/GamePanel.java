package pong;

import pong.i18n.Lang;
import pong.input.InputController;
import pong.util.GameConstants;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    private static final double STEP = 1.0 / GameConstants.FPS;
    private static final double MAX_FRAME_TIME = 0.25;
    private static final int MAX_UPDATES_PER_FRAME = 5;

    private final GameState state;
    private final InputController input = new InputController();
    private final Runnable onReturnToMenu;
    private Lang lang;
    private Runnable onEscPressed;

    private volatile boolean running = false;
    private volatile boolean repaintPending = false;
    private Thread gameThread;

    public GamePanel(GameMode mode, Difficulty difficulty, Lang lang, Runnable onReturnToMenu) {
        this.state = new GameState(mode, difficulty);
        this.lang = lang;
        this.onReturnToMenu = onReturnToMenu;

        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setBackground(GameConstants.BG);
        setFocusable(true);
        addKeyListener(input);

        // extra hotkeys (pause/reset/menu)
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) state.togglePause();
                if (e.getKeyCode() == KeyEvent.VK_R) state.resetMatch();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (onEscPressed != null) {
                        onEscPressed.run();
                    }
                }
            }
        });
    }

    private void startGameLoop() {
        running = true;
        gameThread = new Thread(() -> {
            long previous = System.nanoTime();
            double accumulator = 0.0;

            while (running) {
                long now = System.nanoTime();
                double frameTime = (now - previous) / 1_000_000_000.0;
                previous = now;

                if (frameTime > MAX_FRAME_TIME) {
                    frameTime = MAX_FRAME_TIME;
                }
                accumulator += frameTime;

                int steps = 0;
                while (accumulator >= STEP && steps < MAX_UPDATES_PER_FRAME) {
                    state.update(STEP, input);
                    accumulator -= STEP;
                    steps++;
                }
                if (steps >= MAX_UPDATES_PER_FRAME) {
                    accumulator = 0.0;
                }

                if (!repaintPending) {
                    repaintPending = true;
                    SwingUtilities.invokeLater(() -> {
                        repaintPending = false;
                        repaint();
                    });
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "GameLoop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public synchronized void addNotify() {
        super.addNotify();
        if (!running) {
            startGameLoop();
        }
    }

    @Override
    public synchronized void removeNotify() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
            try {
                gameThread.join(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        super.removeNotify();
    }

    /** Sets the callback invoked when the player presses {@code Esc}. */
    public void setOnEscPressed(Runnable callback) {
        this.onEscPressed = callback;
    }

    /** Updates the display language (in-game overlay text). */
    public void setLang(Lang lang) {
        this.lang = lang;
    }

    /** Pauses the game loop (no-op if already paused). */
    public void pause() {
        if (!state.isPaused()) state.togglePause();
    }

    /** Resumes the game loop (no-op if already running). */
    public void resume() {
        if (state.isPaused()) state.togglePause();
    }

    /** Stops the game loop and returns to the menu. */
    public void stopAndReturnToMenu() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
        SwingUtilities.invokeLater(onReturnToMenu);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Scale game to fill actual component size (fixes fullscreen rendering)
        int cw = getWidth();
        int ch = getHeight();
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
        g2.fillRect((int) state.getLeftPaddle().getX(), (int) state.getLeftPaddle().getY(),
                state.getLeftPaddle().getWidth(), state.getLeftPaddle().getHeight());
        g2.fillRect((int) state.getRightPaddle().getX(), (int) state.getRightPaddle().getY(),
                state.getRightPaddle().getWidth(), state.getRightPaddle().getHeight());

        // ball
        g2.setColor(GameConstants.ACCENT);
        g2.fillOval((int) state.getBall().getX(), (int) state.getBall().getY(),
                state.getBall().getSize(), state.getBall().getSize());

        // score
        g2.setColor(GameConstants.FG);
        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        String left = String.valueOf(state.getScore().getLeft());
        String right = String.valueOf(state.getScore().getRight());
        FontMetrics fm = g2.getFontMetrics();

        int pad = 18;
        g2.drawString(left, GameConstants.WIDTH / 2 - pad - fm.stringWidth(left), 60);
        g2.drawString(right, GameConstants.WIDTH / 2 + pad, 60);

        // mode & help
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(new Color(255, 255, 255, 160));
        g2.drawString(lang.statusBar(state.getMode(), state.getDifficulty()),
                16, GameConstants.HEIGHT - 16);

        if (state.isPaused()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 54));
            String t = lang.pauseTitle();
            int w = g2.getFontMetrics().stringWidth(t);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(t, (GameConstants.WIDTH - w) / 2, GameConstants.HEIGHT / 2);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String hint = lang.pauseHint();
            int hw = g2.getFontMetrics().stringWidth(hint);
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawString(hint, (GameConstants.WIDTH - hw) / 2, GameConstants.HEIGHT / 2 + 34);
        }

        if (state.getScore().isGameOver()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 54));
            String t = lang.winnerText(state.getScore());
            int w = g2.getFontMetrics().stringWidth(t);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.drawString(t, (GameConstants.WIDTH - w) / 2, GameConstants.HEIGHT / 2);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String hint = lang.gameOverHint();
            int hw = g2.getFontMetrics().stringWidth(hint);
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawString(hint, (GameConstants.WIDTH - hw) / 2, GameConstants.HEIGHT / 2 + 34);
        }

        g2.dispose();
    }
}
