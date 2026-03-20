package pong.model;

import pong.util.GameConstants;

import java.awt.geom.Rectangle2D;
import java.util.Random;

public class Ball {
    private double x, y;
    private final int size;

    private double vx, vy; // px/s
    private final Random rnd = new Random();

    public Ball(double x, double y) {
        this.x = x;
        this.y = y;
        this.size = GameConstants.BALL_SIZE;
        randomServe(rnd.nextBoolean() ? 1 : -1);
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;

        // wall bounce (top/bottom)
        if (y <= 0) {
            y = 0;
            vy = -vy;
        } else if (y + size >= GameConstants.HEIGHT) {
            y = GameConstants.HEIGHT - size;
            vy = -vy;
        }
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, y, size, size);
    }

    public void bounceFromPaddle(Paddle p) {
        // Reflect x
        vx = -vx;

        // Add "spin" based on hit position
        double offset = (y + size / 2.0) - p.centerY(); // negative => above center
        double norm = offset / (p.getHeight() / 2.0);    // -1..1

        // Adjust vy (keep game controllable)
        vy += norm * 260;

        // Speed up slightly after each paddle hit
        vx *= GameConstants.BALL_SPEEDUP_FACTOR;
        vy *= GameConstants.BALL_SPEEDUP_FACTOR;

        // Prevent too vertical
        double minAbsVx = 160;
        if (Math.abs(vx) < minAbsVx) vx = Math.signum(vx) * minAbsVx;

        // Nudge out of paddle to avoid sticking
        if (vx > 0) x = p.getX() + p.getWidth() + 0.5;
        else x = p.getX() - size - 0.5;
    }

    public void randomServe(int direction) {
        x = (GameConstants.WIDTH - size) / 2.0;
        y = (GameConstants.HEIGHT - size) / 2.0;

        double speed = GameConstants.BALL_SPEED;
        vx = direction * speed;

        // vy random but not too small
        vy = (rnd.nextDouble() * 2 - 1) * (speed * 0.65);
        if (Math.abs(vy) < 60) vy = Math.signum(vy == 0 ? 1 : vy) * 60;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }

    public void setPosition(double x, double y) {
        this.x = x; this.y = y;
    }
}
