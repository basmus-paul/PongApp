package pong.online;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * TCP client for the LAN online multiplayer mode.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Connects to the host and reads the protocol handshake</li>
 *   <li>Sends {@code INPUT} messages whenever key state changes</li>
 *   <li>Reads incoming {@link GameSnapshot}s on a background thread and
 *       forwards them to the registered {@code onSnapshot} consumer</li>
 * </ul>
 */
public final class OnlineClient {

    private Socket           socket;
    private DataInputStream  in;
    private DataOutputStream out;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread readThread;

    private final AtomicInteger seq = new AtomicInteger(0);

    private volatile Consumer<GameSnapshot> onSnapshot;
    private volatile Runnable               onDisconnect;

    /** Most recent snapshot (volatile so the renderer can read it without locking). */
    private volatile GameSnapshot latestSnapshot = GameSnapshot.empty();

    /** Role assigned by the server ({@link Protocol#ROLE_LEFT} or {@link Protocol#ROLE_RIGHT}). */
    private byte role = Protocol.ROLE_RIGHT;

    // ── public API ─────────────────────────────────────────────────────────────

    /**
     * Connects to the server and completes the handshake.
     * Blocks until the connection is established or an error is thrown.
     *
     * @param host hostname or IP address of the server
     * @param port TCP port of the server
     * @throws IOException on connection or protocol failure
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        byte version = in.readByte();
        if (version != Protocol.VERSION) {
            throw new IOException("Protocol version mismatch: expected "
                    + Protocol.VERSION + ", got " + version);
        }
        role = in.readByte();
    }

    /** @return the role assigned by the server during the handshake. */
    public byte getRole() { return role; }

    /**
     * Sets the consumer called (on the background read thread) whenever a new
     * snapshot arrives. Use {@link SwingUtilities#invokeLater} inside the consumer
     * if you need to update Swing components.
     */
    public void setOnSnapshot(Consumer<GameSnapshot> callback) {
        this.onSnapshot = callback;
    }

    /** Sets the callback invoked on the EDT when the connection is lost. */
    public void setOnDisconnect(Runnable callback) {
        this.onDisconnect = callback;
    }

    /** @return the most recent snapshot received from the server. */
    public GameSnapshot getLatestSnapshot() { return latestSnapshot; }

    /**
     * Starts the background snapshot-read loop.
     * Must be called after {@link #connect} and after setting callbacks.
     */
    public void startReadLoop() {
        if (!running.compareAndSet(false, true)) return;
        readThread = new Thread(this::readLoop, "OnlineClient-Read");
        readThread.setDaemon(true);
        readThread.start();
    }

    /**
     * Sends an {@code INPUT} message to the server.
     * Thread-safe; safe to call from the EDT.
     */
    public void sendInput(boolean up, boolean down) {
        if (out == null || !running.get()) return;
        int s = seq.incrementAndGet();
        try {
            out.writeByte(Protocol.MSG_INPUT);
            out.writeInt(s);
            out.writeBoolean(up);
            out.writeBoolean(down);
            out.flush();
        } catch (IOException ignored) {
            // the read loop will detect the dropped connection and fire onDisconnect
        }
    }

    /** Stops the read loop and closes the connection. */
    public void stop() {
        running.set(false);
        if (readThread != null) readThread.interrupt();
        if (socket != null) try { socket.close(); } catch (IOException ignored) {}
    }

    // ── internal ──────────────────────────────────────────────────────────────

    private void readLoop() {
        try {
            while (running.get()) {
                byte msgType = in.readByte();
                if (msgType == Protocol.MSG_SNAPSHOT) {
                    GameSnapshot snap = GameSnapshot.readFrom(in);
                    latestSnapshot = snap;
                    Consumer<GameSnapshot> cb = onSnapshot;
                    if (cb != null) cb.accept(snap);
                }
            }
        } catch (IOException e) {
            if (running.getAndSet(false)) {
                SwingUtilities.invokeLater(() -> {
                    if (onDisconnect != null) onDisconnect.run();
                });
            }
        }
    }
}
