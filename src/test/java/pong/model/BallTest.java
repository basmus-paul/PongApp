package pong.model;

import org.junit.jupiter.api.Test;
import pong.util.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

class BallTest {

    @Test
    void initialPositionIsCenter() {
        Ball ball = new Ball(GameConstants.WIDTH / 2.0, GameConstants.HEIGHT / 2.0);
        // randomServe() is called during construction and recenters using (WIDTH - size) / 2
        assertEquals((GameConstants.WIDTH - GameConstants.BALL_SIZE) / 2.0, ball.getX(), 0.001);
        assertEquals((GameConstants.HEIGHT - GameConstants.BALL_SIZE) / 2.0, ball.getY(), 0.001);
    }

    @Test
    void sizeMatchesConstant() {
        Ball ball = new Ball(100, 100);
        assertEquals(GameConstants.BALL_SIZE, ball.getSize());
    }

    @Test
    void hasNonZeroVelocityAfterConstruction() {
        Ball ball = new Ball(100, 100);
        // Ball should be moving after construction (randomServe is called)
        assertTrue(Math.abs(ball.getVx()) > 0 || Math.abs(ball.getVy()) > 0);
    }

    @Test
    void boundsMatchPosition() {
        Ball ball = new Ball(200, 300);
        var b = ball.getBounds();
        assertEquals(ball.getX(), b.getX(), 0.001);
        assertEquals(ball.getY(), b.getY(), 0.001);
        assertEquals(GameConstants.BALL_SIZE, b.getWidth(), 0.001);
        assertEquals(GameConstants.BALL_SIZE, b.getHeight(), 0.001);
    }

    @Test
    void setPositionUpdatesCoordinates() {
        Ball ball = new Ball(100, 100);
        ball.setPosition(400, 300);
        assertEquals(400, ball.getX(), 0.001);
        assertEquals(300, ball.getY(), 0.001);
    }

    @Test
    void bouncesOffTopWall() {
        Ball ball = new Ball(100, 100);
        ball.randomServe(1);
        // Place ball near top with upward velocity
        ball.setPosition(100, 1);
        // Manually trigger a tick that would push it past top
        // We check the ball stays >= 0 after update
        ball.update(1.0 / GameConstants.FPS);
        assertTrue(ball.getY() >= 0);
    }

    @Test
    void bouncesOffBottomWall() {
        Ball ball = new Ball(100, 100);
        ball.randomServe(1);
        ball.setPosition(100, GameConstants.HEIGHT - GameConstants.BALL_SIZE - 1);
        ball.update(1.0 / GameConstants.FPS);
        assertTrue(ball.getY() + GameConstants.BALL_SIZE <= GameConstants.HEIGHT);
    }

    @Test
    void randomServeLeftMovesLeft() {
        Ball ball = new Ball(100, 100);
        ball.randomServe(-1);
        assertTrue(ball.getVx() < 0, "Ball should move left when direction=-1");
    }

    @Test
    void randomServeRightMovesRight() {
        Ball ball = new Ball(100, 100);
        ball.randomServe(1);
        assertTrue(ball.getVx() > 0, "Ball should move right when direction=1");
    }

    @Test
    void randomServeCentersBall() {
        Ball ball = new Ball(50, 50);
        ball.randomServe(1);
        assertEquals((GameConstants.WIDTH - GameConstants.BALL_SIZE) / 2.0, ball.getX(), 0.001);
        assertEquals((GameConstants.HEIGHT - GameConstants.BALL_SIZE) / 2.0, ball.getY(), 0.001);
    }

    @Test
    void bounceFromPaddleReversesXVelocity() {
        Ball ball = new Ball(100, 100);
        ball.randomServe(1); // moving right
        Paddle paddle = new Paddle(
                GameConstants.WIDTH - 40 - GameConstants.PADDLE_WIDTH,
                (GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0
        );
        double vxBefore = ball.getVx();
        ball.bounceFromPaddle(paddle);
        // x velocity should now be negative (reflected)
        assertTrue(ball.getVx() * vxBefore < 0, "vx should reverse sign after paddle bounce");
    }
}
