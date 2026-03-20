package pong;

import pong.input.InputController;
import pong.util.GameConstants;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    private final GameState state;
    private final InputController input = new InputController();
    private final Timer timer;

    public GamePanel(GameMode mode) {
        this.state = new GameState(mode);

        setPreferredSize(new Dimension(GameConstants.WIDTH, GameConstants.HEIGHT));
        setBackground(GameConstants.BG);
        setFocusable(true);
        addKeyListener(input);

        // extra hotkeys (pause/reset)
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) state.togglePause();
                if (e.getKeyCode() == KeyEvent.VK_R) state.resetMatch();
            }
        });

        int delayMs = (int) Math.round(1000.0 / GameConstants.FPS);
        timer = new Timer(delayMs, e -> {
            state.update(GameConstants.DT, input);
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
        String modeText = "Mode: " + state.getMode() + " | Left: W/S | Right: " +
                (state.getMode() == GameMode.TWO_PLAYERS ? "Up/Down" : "Computer") +
                " | P: Pause | R: Reset";
        g2.setColor(new Color(255, 255, 255, 160));
        g2.drawString(modeText, 16, GameConstants.HEIGHT - 16);

        if (state.isPaused()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 54));
            String t = "PAUSED";
            int w = g2.getFontMetrics().stringWidth(t);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(t, (GameConstants.WIDTH - w) / 2, GameConstants.HEIGHT / 2);
        }

        if (state.getScore().isGameOver()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 54));
            String t = state.getScore().winnerText();
            int w = g2.getFontMetrics().stringWidth(t);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.drawString(t, (GameConstants.WIDTH - w) / 2, GameConstants.HEIGHT / 2);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String hint = "Press R to restart";
            int hw = g2.getFontMetrics().stringWidth(hint);
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawString(hint, (GameConstants.WIDTH - hw) / 2, GameConstants.HEIGHT / 2 + 34);
        }

        g2.dispose();
    }
}
