package pong;

import pong.i18n.Lang;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;

/**
 * Swing window that hosts the {@link MenuPanel}.
 *
 * <p>When the user clicks "Start Game" the frame disposes itself and hands the
 * selection to the provided callback.</p>
 */
public class MenuFrame extends JFrame {

    public MenuFrame(Lang initialLang, WindowPreset initialPreset,
                     Consumer<MenuPanel.MenuResult> onStart,
                     Runnable onOnline) {
        setTitle("Pong");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        MenuPanel panel = new MenuPanel(initialLang, initialPreset, getGraphicsConfiguration(),
                result -> {
                    dispose();
                    SwingUtilities.invokeLater(() -> onStart.accept(result));
                },
                () -> {
                    dispose();
                    SwingUtilities.invokeLater(onOnline);
                });

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }
}
