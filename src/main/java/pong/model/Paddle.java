package pong.model;

import pong.util.GameConstants;

import java.awt.geom.Rectangle2D;

public class Paddle {
    private double x;
    private double y;
    private final int width;
    private final int height;

    private double velocityY; // px/s

    public Paddle(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = GameConstants.PADDLE_WIDTH;
        this.height = GameConstants.PADDLE_HEIGHT;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public void update(double dt) {
        y += velocityY * dt;

        // clamp
        if (y < 0) y = 0;
        if (y + height > GameConstants.HEIGHT) y = GameConstants.HEIGHT - height;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public double centerY() {
        return y + height / 2.0;
    }

    public void reset(double y) {
        this.y = y;
        this.velocityY = 0;
    }
}
