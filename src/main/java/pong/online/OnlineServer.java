package pong.online;

import pong.util.GameConstants;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * TCP server for the LAN online multiplayer mode.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Binds to TCP port {@link Protocol#DEFAULT_PORT}</li>
 *   <li>Accepts exactly one remote client (the joiner)</li>
 *   <li>Owns the authoritative {@link OnlineGameState}</li>
 *   <li>Runs a fixed-timestep game loop ({@code 1/120 s})</li>
 *   <li>Reads the remote client's {@code INPUT} messages on a dedicated thread</li>
 *   <li>After each simulation step, broadcasts a {@link GameSnapshot} to the host's
 *       local renderer (via the {@code onSnapshot} callback) and to the remote client
 *       (via TCP)</li>
 * </ul>
 */
public final class OnlineServer {

    private static final double STEP                = 1.0 / GameConstants.FPS;
    private static final double MAX_FRAME_TIME      = 0.25;
    private static final int    MAX_UPDATES_PER_FRAME = 5;

    // ── game state ────────────────────────────────────────────────────────────
    private final OnlineGameState state = new OnlineGameState();

    // ── network ───────────────────────────────────────────────────────────────
    private ServerSocket     serverSocket;
    private Socket           clientSocket;
    private DataInputStream  clientIn;
    private DataOutputStream clientOut;

    // ── input state (set from different threads, read by game loop) ───────────
    private record InputState(boolean up, boolean down) {}
    private volatile InputState localInput  = new InputState(false, false);
    private volatile InputState remoteInput = new InputState(false, false);

    // ── lifecycle ─────────────────────────────────────────────────────────────
    private final AtomicBoolean gameRunning = new AtomicBoolean(false);
    private Thread gameThread;

    private Consumer<GameSnapshot> onSnapshot;
    private volatile Runnable      onDisconnect;

    private long tick = 0;
    private final AtomicInteger lastRemoteSeq = new AtomicInteger(-1);

    // ── public API ─────────────────────────────────────────────────────────────

    /**
     * Binds to the default port.
     *
     * @throws IOException if the port is already in use or binding fails
     */
    public void startListening() throws IOException {
        serverSocket = new ServerSocket(Protocol.DEFAULT_PORT);
    }

    /**
     * Waits for the one expected client connection on a background thread.
     * Sends the protocol handshake and then calls {@code onConnected} on the EDT.
     */
    public void waitForClientAsync(Runnable onConnected) {
        Thread t = new Thread(() -> {
            try {
                clientSocket = serverSocket.accept();
                clientSocket.setTcpNoDelay(true);
                clientIn  = new DataInputStream(
                        new BufferedInputStream(clientSocket.getInputStream()));
                clientOut = new DataOutputStream(
                        new BufferedOutputStream(clientSocket.getOutputStream()));

                // handshake: version byte + role byte (joiner is RIGHT)
                clientOut.writeByte(Protocol.VERSION);
                clientOut.writeByte(Protocol.ROLE_RIGHT);
                clientOut.flush();

                SwingUtilities.invokeLater(onConnected);
            } catch (IOException e) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    SwingUtilities.invokeLater(() -> {
                        if (onDisconnect != null) onDisconnect.run();
                    });
                }
            }
        }, "OnlineServer-Accept");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Updates the host's (left paddle) input state.
     * Safe to call from the EDT key listener.
     */
    public void setLocalInput(boolean up, boolean down) {
        localInput = new InputState(up, down);
    }

    /**
     * Sets the callback invoked on the EDT when the connection is lost unexpectedly.
     */
    public void setOnDisconnect(Runnable callback) {
        this.onDisconnect = callback;
    }

    /**
     * Starts the game loop and the client-input read thread.
     *
     * @param onSnapshot consumer called with each new snapshot (runs on the game loop
     *                   thread; use {@link SwingUtilities#invokeLater} in the consumer
     *                   if Swing updates are needed)
     */
    public void startGameLoop(Consumer<GameSnapshot> onSnapshot) {
        this.onSnapshot = onSnapshot;
        if (!gameRunning.compareAndSet(false, true)) return;

        Thread readThread = new Thread(this::readClientLoop, "OnlineServer-Read");
        readThread.setDaemon(true);
        readThread.start();

        gameThread = new Thread(this::gameLoop, "OnlineServer-GameLoop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /** Stops all threads and closes all sockets cleanly. */
    public void stop() {
        gameRunning.set(false);
        if (gameThread != null) gameThread.interrupt();
        closeQuietly(clientSocket);
        closeQuietly(serverSocket);
    }

    // ── internal loops ─────────────────────────────────────────────────────────

    private void readClientLoop() {
        try {
            while (gameRunning.get()) {
                byte msgType = clientIn.readByte();
                if (msgType == Protocol.MSG_INPUT) {
                    int     seq  = clientIn.readInt();
                    boolean up   = clientIn.readBoolean();
                    boolean down = clientIn.readBoolean();
                    // ignore out-of-order messages
                    if (seq > lastRemoteSeq.get()) {
                        lastRemoteSeq.set(seq);
                        remoteInput = new InputState(up, down);
                    }
                }
            }
        } catch (IOException e) {
            if (gameRunning.getAndSet(false)) {
                SwingUtilities.invokeLater(() -> {
                    if (onDisconnect != null) onDisconnect.run();
                });
            }
        }
    }

    private void gameLoop() {
        long   previous    = System.nanoTime();
        double accumulator = 0.0;

        while (gameRunning.get()) {
            long   now       = System.nanoTime();
            double frameTime = (now - previous) / 1_000_000_000.0;
            previous = now;

            if (frameTime > MAX_FRAME_TIME) frameTime = MAX_FRAME_TIME;
            accumulator += frameTime;

            int steps = 0;
            while (accumulator >= STEP && steps < MAX_UPDATES_PER_FRAME) {
                InputState li = localInput;
                InputState ri = remoteInput;
                state.update(STEP,
                        velocity(li.up(), li.down()),
                        velocity(ri.up(), ri.down()));
                accumulator -= STEP;
                steps++;
            }
            if (steps >= MAX_UPDATES_PER_FRAME) accumulator = 0.0;

            if (steps > 0) {
                GameSnapshot snap = state.toSnapshot(tick++);

                // notify host renderer on every simulation step
                if (onSnapshot != null) onSnapshot.accept(snap);

                // send to remote client at 60 Hz (every other tick)
                if (snap.tick() % 2 == 0 && clientOut != null) {
                    try {
                        snap.writeTo(clientOut);
                        clientOut.flush();
                    } catch (IOException e) {
                        if (gameRunning.getAndSet(false)) {
                            SwingUtilities.invokeLater(() -> {
                                if (onDisconnect != null) onDisconnect.run();
                            });
                        }
                        return;
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static double velocity(boolean up, boolean down) {
        double vy = 0;
        if (up)   vy -= GameConstants.PADDLE_SPEED;
        if (down) vy += GameConstants.PADDLE_SPEED;
        return vy;
    }

    private static void closeQuietly(Closeable c) {
        if (c == null) return;
        try { c.close(); } catch (IOException ignored) {}
    }
}
