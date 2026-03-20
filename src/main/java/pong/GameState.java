package pong;

import pong.ai.AiController;
import pong.input.InputController;
import pong.model.Ball;
import pong.model.Paddle;
import pong.model.Score;
import pong.util.GameConstants;

import java.awt.event.KeyEvent;

public class GameState {
    private final Paddle leftPaddle;
    private final Paddle rightPaddle;
    private final Ball ball;
    private final Score score;

    private final GameMode mode;
    private final Difficulty difficulty;
    private final AiController ai; // only used in VS_COMPUTER

    private boolean paused = false;

    public GameState(GameMode mode, Difficulty difficulty) {
        this.mode = mode;
        this.difficulty = difficulty;

        leftPaddle = new Paddle(40, (GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0);
        rightPaddle = new Paddle(GameConstants.WIDTH - 40 - GameConstants.PADDLE_WIDTH,
                (GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0);
        ball = new Ball(GameConstants.WIDTH / 2.0, GameConstants.HEIGHT / 2.0);
        score = new Score();

        if (mode == GameMode.VS_COMPUTER) {
            ai = createAi(difficulty);
        } else {
            ai = null;
        }
    }

    private static AiController createAi(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY   -> new AiController(GameConstants.PADDLE_SPEED * 0.50, 30, 0.05);
            case MEDIUM -> new AiController(GameConstants.PADDLE_SPEED * 0.72, 18, 0.11);
            case HARD   -> new AiController(GameConstants.PADDLE_SPEED * 0.92, 10, 0.18);
        };
    }

    public void update(double dt, InputController input) {
        if (score.isGameOver()) return;
        if (paused) return;

        // left: W/S
        double leftVy = 0;
        if (input.isDown(KeyEvent.VK_W)) leftVy -= GameConstants.PADDLE_SPEED;
        if (input.isDown(KeyEvent.VK_S)) leftVy += GameConstants.PADDLE_SPEED;
        leftPaddle.setVelocityY(leftVy);
        leftPaddle.update(dt);

        // right: either player or AI
        if (mode == GameMode.TWO_PLAYERS) {
            double rightVy = 0;
            if (input.isDown(KeyEvent.VK_UP)) rightVy -= GameConstants.PADDLE_SPEED;
            if (input.isDown(KeyEvent.VK_DOWN)) rightVy += GameConstants.PADDLE_SPEED;
            rightPaddle.setVelocityY(rightVy);
            rightPaddle.update(dt);
        } else {
            ai.update(rightPaddle, ball, dt);
        }

        ball.update(dt);

        // collisions with paddles
        if (ball.getBounds().intersects(leftPaddle.getBounds())) {
            ball.bounceFromPaddle(leftPaddle);
        } else if (ball.getBounds().intersects(rightPaddle.getBounds())) {
            ball.bounceFromPaddle(rightPaddle);
        }

        // scoring: left/right wall
        if (ball.getX() + ball.getSize() < 0) {
            score.rightScores();
            ball.randomServe(-1);
            resetPaddles();
        } else if (ball.getX() > GameConstants.WIDTH) {
            score.leftScores();
            ball.randomServe(1);
            resetPaddles();
        }
    }

    private void resetPaddles() {
        leftPaddle.reset((GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0);
        rightPaddle.reset((GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0);
    }

    public void togglePause() { paused = !paused; }
    public void resetMatch() {
        score.reset();
        ball.randomServe(1);
        resetPaddles();
        paused = false;
    }

    public Paddle getLeftPaddle() { return leftPaddle; }
    public Paddle getRightPaddle() { return rightPaddle; }
    public Ball getBall() { return ball; }
    public Score getScore() { return score; }
    public GameMode getMode() { return mode; }
    public Difficulty getDifficulty() { return difficulty; }
    public boolean isPaused() { return paused; }
}
