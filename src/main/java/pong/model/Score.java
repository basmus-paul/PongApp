package pong.model;

import pong.util.GameConstants;

public class Score {
    private int left;
    private int right;

    public void leftScores() { left++; }
    public void rightScores() { right++; }

    public int getLeft() { return left; }
    public int getRight() { return right; }

    public boolean isGameOver() {
        return left >= GameConstants.MAX_SCORE || right >= GameConstants.MAX_SCORE;
    }

    public String winnerText() {
        if (!isGameOver()) return "";
        return left > right ? "LEFT WINS" : "RIGHT WINS";
    }

    public void reset() {
        left = 0;
        right = 0;
    }
}
