package pong;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * Available window-size presets for the game window.
 *
 * <p>The logical resolution stays fixed at
 * {@link pong.util.GameConstants#WIDTH} × {@link pong.util.GameConstants#HEIGHT}
 * (900 × 600). The game panel scales its rendering to fill the chosen preset
 * size.</p>
 *
 * <p>{@link #P_SMALL} is a fallback that matches the logical resolution (1:1
 * scale). It is always available regardless of screen size.</p>
 */
public enum WindowPreset {

    /**
     * Fallback preset that matches the logical resolution 1:1
     * ({@link pong.util.GameConstants#WIDTH} × {@link pong.util.GameConstants#HEIGHT}).
     * Always available regardless of screen size.
     */
    P_SMALL("900×600",  900,  600),
    P1080  ("1080p",   1350,  900),
    P1440  ("1440p",   1800, 1200),
    P4K    ("4K",      3000, 2000);

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

    /** Returns {@code true} if this preset fits within {@code usable}. */
    public boolean fitsIn(Dimension usable) {
        return width <= usable.width && height <= usable.height;
    }

    /**
     * Returns the usable screen area for the given graphics configuration,
     * accounting for taskbar/dock insets. Falls back to the default screen
     * device if {@code gc} is {@code null}.
     */
    public static Dimension usableSize(GraphicsConfiguration gc) {
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
        }
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        return new Dimension(
                bounds.width  - insets.left - insets.right,
                bounds.height - insets.top  - insets.bottom);
    }

    /**
     * Returns the largest preset that fits within {@code usable}.
     * Falls back to {@link #P_SMALL} if none of the scaled presets fit.
     */
    public static WindowPreset largestFitting(Dimension usable) {
        for (WindowPreset p : new WindowPreset[]{P4K, P1440, P1080}) {
            if (p.fitsIn(usable)) return p;
        }
        return P_SMALL;
    }
}
