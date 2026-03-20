package pong;

import pong.i18n.Lang;

import javax.swing.SwingUtilities;

public class PongApp {

    /** Last language chosen by the user; remembered when returning to the menu. */
    private static Lang currentLang = Lang.EN;

    /** Last fullscreen preference chosen by the user; remembered when returning to the menu. */
    private static boolean currentFullscreen = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PongApp::startGame);
    }

    public static void startGame() {
        new MenuFrame(currentLang, currentFullscreen, result -> {
            currentLang = result.lang();
            currentFullscreen = result.fullscreen();
            new GameFrame(result.mode(), result.difficulty(), result.lang(), result.fullscreen()).setVisible(true);
        }).setVisible(true);
    }
}
