package pong.util;

import java.awt.Color;

public final class GameConstants {
    private GameConstants() {}

    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;

    public static final int FPS = 120;
    public static final double DT = 1.0 / FPS;

    public static final int PADDLE_WIDTH = 14;
    public static final int PADDLE_HEIGHT = 110;
    public static final double PADDLE_SPEED = 460; // px/s

    public static final int BALL_SIZE = 14;
    public static final double BALL_SPEED = 420; // basis px/s
    public static final double BALL_SPEEDUP_FACTOR = 1.04; // nach Paddle-Hit

    public static final int MAX_SCORE = 10;

    public static final Color BG = new Color(18, 18, 22);
    public static final Color FG = new Color(235, 235, 245);
    public static final Color ACCENT = new Color(120, 210, 255);
}
