package pong.model;

import org.junit.jupiter.api.Test;
import pong.util.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    @Test
    void initialScoreIsZero() {
        Score score = new Score();
        assertEquals(0, score.getLeft());
        assertEquals(0, score.getRight());
    }

    @Test
    void leftScoresIncrementsLeft() {
        Score score = new Score();
        score.leftScores();
        assertEquals(1, score.getLeft());
        assertEquals(0, score.getRight());
    }

    @Test
    void rightScoresIncrementsRight() {
        Score score = new Score();
        score.rightScores();
        assertEquals(0, score.getLeft());
        assertEquals(1, score.getRight());
    }

    @Test
    void notGameOverBelowMaxScore() {
        Score score = new Score();
        for (int i = 0; i < GameConstants.MAX_SCORE - 1; i++) {
            score.leftScores();
        }
        assertFalse(score.isGameOver());
    }

    @Test
    void gameOverWhenLeftReachesMaxScore() {
        Score score = new Score();
        for (int i = 0; i < GameConstants.MAX_SCORE; i++) {
            score.leftScores();
        }
        assertTrue(score.isGameOver());
    }

    @Test
    void gameOverWhenRightReachesMaxScore() {
        Score score = new Score();
        for (int i = 0; i < GameConstants.MAX_SCORE; i++) {
            score.rightScores();
        }
        assertTrue(score.isGameOver());
    }

    @Test
    void winnerTextLeftWins() {
        Score score = new Score();
        for (int i = 0; i < GameConstants.MAX_SCORE; i++) {
            score.leftScores();
        }
        assertEquals("LEFT WINS", score.winnerText());
    }

    @Test
    void winnerTextRightWins() {
        Score score = new Score();
        for (int i = 0; i < GameConstants.MAX_SCORE; i++) {
            score.rightScores();
        }
        assertEquals("RIGHT WINS", score.winnerText());
    }

    @Test
    void winnerTextEmptyWhenNotOver() {
        Score score = new Score();
        assertEquals("", score.winnerText());
    }

    @Test
    void resetClearsScore() {
        Score score = new Score();
        for (int i = 0; i < 5; i++) score.leftScores();
        for (int i = 0; i < 3; i++) score.rightScores();
        score.reset();
        assertEquals(0, score.getLeft());
        assertEquals(0, score.getRight());
        assertFalse(score.isGameOver());
    }
}
