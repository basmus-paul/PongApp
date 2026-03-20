package pong.ai;

import org.junit.jupiter.api.Test;
import pong.model.Ball;
import pong.model.Paddle;
import pong.util.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

class AiControllerTest {

    @Test
    void aiMovesPaddleTowardBall() {
        // Place AI paddle near the top, ball near the bottom
        Paddle paddle = new Paddle(
                GameConstants.WIDTH - 40 - GameConstants.PADDLE_WIDTH,
                10 // near top
        );
        Ball ball = new Ball(GameConstants.WIDTH / 2.0, GameConstants.HEIGHT - 50); // near bottom

        AiController ai = new AiController(
                GameConstants.PADDLE_SPEED * 0.92,
                5,
                0.5 // fast reaction for test predictability
        );

        double yBefore = paddle.getY();
        // Run several ticks so the AI has time to react
        for (int i = 0; i < 30; i++) {
            ai.update(paddle, ball, GameConstants.DT);
        }

        // Paddle should have moved downward (toward the ball)
        assertTrue(paddle.getY() > yBefore, "AI paddle should move down toward ball below it");
    }

    @Test
    void aiDoesNotMoveWhenAlignedWithBall() {
        // Place paddle exactly at the ball's Y center
        double ballY = 250;
        Ball ball = new Ball(GameConstants.WIDTH / 2.0, ballY);
        ball.randomServe(1);

        double paddleY = ballY - GameConstants.PADDLE_HEIGHT / 2.0;
        Paddle paddle = new Paddle(
                GameConstants.WIDTH - 40 - GameConstants.PADDLE_WIDTH,
                paddleY
        );

        // deadZone large enough that perfectly aligned paddle won't move
        AiController ai = new AiController(GameConstants.PADDLE_SPEED, 50, 1.0);

        // Update a few ticks – with full reactionBlend=1, targetY == ballCenter instantly
        for (int i = 0; i < 5; i++) {
            ai.update(paddle, ball, GameConstants.DT);
        }

        // Paddle should be close to its original Y (within deadZone)
        assertEquals(paddleY, paddle.getY(), 50,
                "Paddle should not move significantly when already aligned with the ball");
    }
}
