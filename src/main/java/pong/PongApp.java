package pong;

import pong.i18n.Lang;

import javax.swing.SwingUtilities;

public class PongApp {

    /** Last language chosen by the user; remembered when returning to the menu. */
    private static Lang currentLang = Lang.EN;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PongApp::startGame);
    }

    public static void startGame() {
        new MenuFrame(currentLang, result -> {
            currentLang = result.lang();
            new GameFrame(result.mode(), result.difficulty(), result.lang()).setVisible(true);
        }).setVisible(true);
    }
}
