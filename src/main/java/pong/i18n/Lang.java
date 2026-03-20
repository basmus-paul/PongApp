package pong.i18n;

import pong.Difficulty;
import pong.GameMode;
import pong.model.Score;

/**
 * All user-visible strings in both supported languages.
 * Pass the selected {@code Lang} instance wherever UI text is needed so that
 * the whole application speaks the same language at runtime.
 */
public enum Lang {

    EN("English"),
    DE("Deutsch");

    /** Display name shown in the language selector. */
    public final String displayName;

    Lang(String displayName) {
        this.displayName = displayName;
    }

    // ── Menu labels ──────────────────────────────────────────────────────────

    public String menuTitle()       { return "Pong"; }
    public String labelLanguage()   { return this == EN ? "Language"           : "Sprache"; }
    public String labelGameMode()   { return this == EN ? "Game Mode"          : "Spielmodus"; }
    public String mode2Players()    { return this == EN ? "2 Players"          : "2 Spieler"; }
    public String modeVsComputer()  { return this == EN ? "vs. Computer"       : "Gegen Computer"; }
    public String labelDifficulty() { return this == EN ? "Difficulty"         : "Schwierigkeit"; }
    public String diffNote()        { return this == EN ? "(vs. Computer only)": "(nur im Computer-Modus)"; }
    public String diffEasy()        { return this == EN ? "Easy"               : "Einfach"; }
    public String diffMedium()      { return this == EN ? "Medium"             : "Mittel"; }
    public String diffHard()        { return this == EN ? "Hard"               : "Schwer"; }
    public String btnStart()        { return this == EN ? "Start Game"         : "Spiel starten"; }
    public String labelFullscreen() { return this == EN ? "Fullscreen"         : "Vollbild"; }

    // ── In-game strings ───────────────────────────────────────────────────────

    /** Bottom status-bar text shown during gameplay. */
    public String statusBar(GameMode mode, Difficulty diff) {
        String right = (mode == GameMode.TWO_PLAYERS)
                ? (this == EN ? "↑/↓" : "↑/↓")
                : (this == EN ? "Computer" : "Computer") + " (" + diffLabel(diff) + ")";
        return "Left: W/S  |  Right: " + right
                + "   |   P: " + (this == EN ? "Pause"   : "Pause")
                + "  |  R: "   + (this == EN ? "Reset"   : "Neustart")
                + "  |  M: "   + (this == EN ? "Menu"    : "Menü");
    }

    /** Short label for a difficulty value (used in status bar). */
    public String diffLabel(Difficulty diff) {
        return switch (diff) {
            case EASY   -> diffEasy();
            case MEDIUM -> diffMedium();
            case HARD   -> diffHard();
        };
    }

    public String pauseTitle()    { return this == EN ? "PAUSED"        : "PAUSE"; }
    public String pauseHint()     { return this == EN
            ? "Press M to go back to menu"
            : "M drücken um zum Menü zurückzukehren"; }

    /** Winner banner derived from the final score. */
    public String winnerText(Score score) {
        if (!score.isGameOver()) return "";
        return score.getLeft() > score.getRight()
                ? (this == EN ? "LEFT WINS"  : "LINKS GEWINNT")
                : (this == EN ? "RIGHT WINS" : "RECHTS GEWINNT");
    }

    public String gameOverHint()  { return this == EN
            ? "Press R to restart  |  Press M to go back to menu"
            : "R: Neustart  |  M: Menü"; }
}
