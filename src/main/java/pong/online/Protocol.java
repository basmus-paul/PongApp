package pong.online;

/** Network protocol constants for the LAN online multiplayer mode. */
public final class Protocol {
    private Protocol() {}

    /** Default TCP port for game sessions. */
    public static final int DEFAULT_PORT = 7777;

    /** Protocol version byte sent during handshake. */
    public static final byte VERSION = 1;

    /** Message type: input from client to server. */
    public static final byte MSG_INPUT = 0x01;

    /** Message type: game snapshot from server to both sides. */
    public static final byte MSG_SNAPSHOT = 0x02;

    /** Role constant – host controls the left paddle. */
    public static final byte ROLE_LEFT = 0;

    /** Role constant – joiner controls the right paddle. */
    public static final byte ROLE_RIGHT = 1;
}
