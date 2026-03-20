package pong.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pong.util.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

class PaddleTest {

    private Paddle paddle;

    @BeforeEach
    void setUp() {
        paddle = new Paddle(40, 100);
    }

    @Test
    void initialPositionIsCorrect() {
        assertEquals(40, paddle.getX());
        assertEquals(100, paddle.getY());
    }

    @Test
    void movesDownWhenPositiveVelocity() {
        paddle.setVelocityY(200);
        paddle.update(0.5);
        assertEquals(200, paddle.getY(), 0.001);
    }

    @Test
    void movesUpWhenNegativeVelocity() {
        paddle = new Paddle(40, 200);
        paddle.setVelocityY(-100);
        paddle.update(1.0);
        assertEquals(100, paddle.getY(), 0.001);
    }

    @Test
    void clampedAtTopWall() {
        paddle = new Paddle(40, 5);
        paddle.setVelocityY(-500);
        paddle.update(1.0);
        assertEquals(0, paddle.getY(), 0.001);
    }

    @Test
    void clampedAtBottomWall() {
        paddle = new Paddle(40, GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT - 5);
        paddle.setVelocityY(500);
        paddle.update(1.0);
        assertEquals(GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT, paddle.getY(), 0.001);
    }

    @Test
    void centerYIsMiddleOfPaddle() {
        double expected = paddle.getY() + GameConstants.PADDLE_HEIGHT / 2.0;
        assertEquals(expected, paddle.centerY(), 0.001);
    }

    @Test
    void resetSetsPositionAndZeroesVelocity() {
        paddle.setVelocityY(300);
        paddle.reset(250);
        assertEquals(250, paddle.getY(), 0.001);
        // after reset, one update tick should not move the paddle
        paddle.update(1.0);
        assertEquals(250, paddle.getY(), 0.001);
    }

    @Test
    void boundsMatchPosition() {
        var b = paddle.getBounds();
        assertEquals(paddle.getX(), b.getX(), 0.001);
        assertEquals(paddle.getY(), b.getY(), 0.001);
        assertEquals(GameConstants.PADDLE_WIDTH, b.getWidth(), 0.001);
        assertEquals(GameConstants.PADDLE_HEIGHT, b.getHeight(), 0.001);
    }
}
