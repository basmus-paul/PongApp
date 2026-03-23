package pong.online;

import pong.model.Ball;
import pong.model.Paddle;
import pong.model.Score;
import pong.util.GameConstants;

/**
 * Authoritative game state used exclusively by {@link OnlineServer}.
 *
 * <p>Physics are identical to the local {@code GameState}: paddle clamping,
 * ball wall bounce, paddle-ball collision with spin, and score detection.</p>
 */
final class OnlineGameState {

    private static final double COUNTDOWN_START = 3.0;

    final Paddle leftPaddle;
    final Paddle rightPaddle;
    final Ball   ball;
    final Score  score;

    private double countdown = COUNTDOWN_START;
    private boolean paused   = false;

    OnlineGameState() {
        double paddleY = (GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0;
        leftPaddle  = new Paddle(40, paddleY);
        rightPaddle = new Paddle(GameConstants.WIDTH - 40 - GameConstants.PADDLE_WIDTH, paddleY);
        ball  = new Ball(GameConstants.WIDTH / 2.0, GameConstants.HEIGHT / 2.0);
        score = new Score();
    }

    /**
     * Advances the simulation by one fixed timestep.
     *
     * @param dt      timestep in seconds
     * @param leftVy  desired velocity for the left paddle (px/s)
     * @param rightVy desired velocity for the right paddle (px/s)
     */
    void update(double dt, double leftVy, double rightVy) {
        if (countdown > 0) {
            countdown = Math.max(0, countdown - dt);
            return;
        }
        if (paused || score.isGameOver()) return;

        leftPaddle.setVelocityY(leftVy);
        leftPaddle.update(dt);

        rightPaddle.setVelocityY(rightVy);
        rightPaddle.update(dt);

        ball.update(dt);

        // paddle collision
        if (ball.getBounds().intersects(leftPaddle.getBounds())) {
            ball.bounceFromPaddle(leftPaddle);
        } else if (ball.getBounds().intersects(rightPaddle.getBounds())) {
            ball.bounceFromPaddle(rightPaddle);
        }

        // scoring
        if (ball.getX() + ball.getSize() < 0) {
            score.rightScores();
            ball.randomServe(-1);
            resetPaddles();
            countdown = COUNTDOWN_START;
        } else if (ball.getX() > GameConstants.WIDTH) {
            score.leftScores();
            ball.randomServe(1);
            resetPaddles();
            countdown = COUNTDOWN_START;
        }
    }

    void togglePause() { paused = !paused; }

    boolean isPaused() { return paused; }

    /** Returns the current countdown value rounded up (3, 2, 1, or 0). */
    int countdownSeconds() { return (int) Math.ceil(countdown); }

    /** Builds a snapshot from the current state. */
    GameSnapshot toSnapshot(long tick) {
        return new GameSnapshot(
                tick,
                leftPaddle.getY(),
                rightPaddle.getY(),
                ball.getX(),
                ball.getY(),
                ball.getVx(),
                ball.getVy(),
                score.getLeft(),
                score.getRight(),
                paused,
                score.isGameOver(),
                countdownSeconds());
    }

    private void resetPaddles() {
        double y = (GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0;
        leftPaddle.reset(y);
        rightPaddle.reset(y);
    }
}
