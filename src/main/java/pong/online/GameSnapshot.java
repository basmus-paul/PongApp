package pong.online;

import pong.util.GameConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Immutable snapshot of the authoritative game state, sent from the server to
 * both players after every simulation step.
 *
 * <p>Wire layout (preceded by one {@code MSG_SNAPSHOT} byte):</p>
 * <pre>
 *   long   tick         (8)
 *   double leftPaddleY  (8)
 *   double rightPaddleY (8)
 *   double ballX        (8)
 *   double ballY        (8)
 *   double ballVelX     (8)
 *   double ballVelY     (8)
 *   int    scoreLeft    (4)
 *   int    scoreRight   (4)
 *   bool   paused       (1)
 *   bool   gameOver     (1)
 *   int    countdown    (4)  seconds remaining before play starts (0 = live)
 * </pre>
 * Total payload: 78 bytes + 1 type byte = 79 bytes per snapshot.
 */
public record GameSnapshot(
        long    tick,
        double  leftPaddleY,
        double  rightPaddleY,
        double  ballX,
        double  ballY,
        double  ballVelX,
        double  ballVelY,
        int     scoreLeft,
        int     scoreRight,
        boolean paused,
        boolean gameOver,
        int     countdown) {

    /** Returns a snapshot representing the initial board state. */
    public static GameSnapshot empty() {
        double paddleY = (GameConstants.HEIGHT - GameConstants.PADDLE_HEIGHT) / 2.0;
        return new GameSnapshot(
                0,
                paddleY, paddleY,
                GameConstants.WIDTH  / 2.0,
                GameConstants.HEIGHT / 2.0,
                0, 0,
                0, 0,
                false, false,
                3);
    }

    /**
     * Writes this snapshot to {@code out}, including the leading message-type byte.
     * Does not flush; the caller is responsible for flushing at the appropriate time.
     */
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeByte(Protocol.MSG_SNAPSHOT);
        out.writeLong(tick);
        out.writeDouble(leftPaddleY);
        out.writeDouble(rightPaddleY);
        out.writeDouble(ballX);
        out.writeDouble(ballY);
        out.writeDouble(ballVelX);
        out.writeDouble(ballVelY);
        out.writeInt(scoreLeft);
        out.writeInt(scoreRight);
        out.writeBoolean(paused);
        out.writeBoolean(gameOver);
        out.writeInt(countdown);
    }

    /**
     * Reads a snapshot from {@code in}.
     * The leading message-type byte must already have been consumed.
     */
    public static GameSnapshot readFrom(DataInputStream in) throws IOException {
        return new GameSnapshot(
                in.readLong(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readInt(),
                in.readInt(),
                in.readBoolean(),
                in.readBoolean(),
                in.readInt());
    }
}
