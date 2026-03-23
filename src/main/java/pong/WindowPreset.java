package pong;

import java.awt.Dimension;

/**
 * Available window-size presets for the game window.
 *
 * <p>The logical resolution stays fixed at
 * {@link pong.util.GameConstants#WIDTH} × {@link pong.util.GameConstants#HEIGHT}
 * (900 × 600). The game panel scales its rendering to fill the chosen preset
 * size.</p>
 */
public enum WindowPreset {

    P1080("1080p", 1350, 900),
    P1440("1440p", 1800, 1200),
    P4K  ("4K",    3000, 2000);

    /** Short human-readable label shown in the UI. */
    public final String label;

    /** Target width of the game-panel (client area). */
    public final int width;

    /** Target height of the game-panel (client area). */
    public final int height;

    WindowPreset(String label, int width, int height) {
        this.label  = label;
        this.width  = width;
        this.height = height;
    }

    /** Returns the preset dimensions as a {@link Dimension}. */
    public Dimension toDimension() {
        return new Dimension(width, height);
    }
}
