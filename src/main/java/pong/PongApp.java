package pong;

import pong.i18n.Lang;
import pong.online.OnlineMenuFrame;

import javax.swing.SwingUtilities;

public class PongApp {

    /** Last language chosen by the user; remembered when returning to the menu. */
    private static Lang currentLang = Lang.EN;

    /** Last window-size preset chosen by the user; remembered when returning to the menu. */
    private static WindowPreset currentPreset = WindowPreset.P1080;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PongApp::startGame);
    }

    public static void startGame() {
        new MenuFrame(currentLang, currentPreset,
                result -> {
                    currentLang   = result.lang();
                    currentPreset = result.preset();
                    new GameFrame(result.mode(), result.difficulty(), result.lang(), result.preset()).setVisible(true);
                },
                PongApp::startOnline
        ).setVisible(true);
    }

    /** Opens the LAN online multiplayer menu. */
    public static void startOnline() {
        new OnlineMenuFrame(currentPreset, PongApp::startGame).setVisible(true);
    }

    /** Called from the in-game menu to update stored language and window-size preferences. */
    public static void updatePreferences(Lang lang, WindowPreset preset) {
        currentLang   = lang;
        currentPreset = preset;
    }
}
