package pong.ai;

import pong.model.Ball;
import pong.model.Paddle;
import pong.util.GameConstants;

public class AiController {
    // "Schwierigkeitsgrad" über Reaktion & Toleranz
    private final double maxSpeed;      // px/s
    private final double deadZone;      // px
    private final double reactionBlend; // 0..1 (näher an 1 => reagiert schneller)

    private double targetY; // internal smoothed target

    public AiController(double maxSpeed, double deadZone, double reactionBlend) {
        this.maxSpeed = maxSpeed;
        this.deadZone = deadZone;
        this.reactionBlend = reactionBlend;
        this.targetY = GameConstants.HEIGHT / 2.0;
    }

    public void update(Paddle aiPaddle, Ball ball, double dt) {
        // ball center y
        double ballCenter = ball.getY() + ball.getSize() / 2.0;

        // Smooth target to simulate reaction delay
        targetY = targetY + (ballCenter - targetY) * reactionBlend;

        double diff = targetY - aiPaddle.centerY();
        if (Math.abs(diff) <= deadZone) {
            aiPaddle.setVelocityY(0);
            return;
        }

        double dir = Math.signum(diff);
        aiPaddle.setVelocityY(dir * maxSpeed);
        aiPaddle.update(dt);
    }
}
