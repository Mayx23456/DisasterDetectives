import controller.DisasterController;
import controller.GameController;
import controller.TurnController;
import model.*;
import util.Dice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DisasterDetectorTest {

    private Player playerA;
    private Player playerB;
    private GameState gameState;
    private GameController gameController;
    private DisasterController disasterController;

    // this runs before each test to set up the basic game objects we need
    @BeforeEach
    void setUp() {
        playerA = new Player("Alice");
        playerB = new Player("Bob");
        Board board = new Board();
        gameState = new GameState(List.of(playerA, playerB), board);
        disasterController = new DisasterController();
        gameController = new GameController(gameState, new TurnController(), disasterController);
    }

    // a player with a blank name should not be allowed
    @Test
    void creatingAPlayerWithAnEmptyNameShouldThrowAnError() {
        assertThrows(IllegalArgumentException.class, () -> new Player(""));
    }

    // a new player should start at position 0 before any moves
    @Test
    void newPlayerShouldStartAtPositionZero() {
        assertEquals(0, playerA.getPosition());
    }

    // you need at least one player to start a game
    @Test
    void gameStateShouldNotAllowZeroPlayers() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameState(List.of(), new Board()));
    }

    // the game has a max player limit so we test that it rejects too many players
    @Test
    void gameStateShouldNotAllowMoreThanSevenPlayers() {
        List<Player> tooMany = List.of(
                new Player("P1"), new Player("P2"), new Player("P3"),
                new Player("P4"), new Player("P5"), new Player("P6"),
                new Player("P7"), new Player("P8")
        );
        assertThrows(IllegalArgumentException.class,
                () -> new GameState(tooMany, new Board()));
    }

    // after both players take a turn it should go back to the first player
    @Test
    void turnsShouldCycleBackToFirstPlayerAfterEveryoneHasGone() {
        TurnController tc = new TurnController();
        tc.advanceTurn(gameState);
        tc.advanceTurn(gameState);
        assertEquals(playerA, gameState.getCurrentPlayer());
    }

    // we want to make sure position 22 on the board is a snake tile
    @Test
    void boardShouldHaveASnakeTileAtPosition22() {
        assertInstanceOf(SnakeTile.class, new Board().getTile(22));
    }

    // we want to make sure position 15 on the board is a ladder tile
    @Test
    void boardShouldHaveALadderTileAtPosition15() {
        assertInstanceOf(LadderTile.class, new Board().getTile(15));
    }

    // tile 100 is the last tile so it should be a final tile
    @Test
    void boardShouldHaveAFinalTileAtPosition100() {
        assertInstanceOf(FinalTile.class, new Board().getTile(100));
    }

    // if a player moves past 100 they should stop at 100 not go over
    @Test
    void playerPositionShouldBeClampedTo100IfTheyMoveOverTheEnd() {
        gameController.movePlayerTo(playerA, 150);
        assertEquals(100, playerA.getPosition());
    }

    // check that the disaster controller can tell if an answer is right or wrong
    @Test
    void disasterControllerShouldReturnTrueForCorrectAnswerAndFalseForWrongAnswer() {
        DisasterQuestion q = new DisasterQuestion(
                "Test?", List.of("Wrong", "Right"), 1);
        assertTrue(disasterController.isCorrect(q, 1));
        assertFalse(disasterController.isCorrect(q, 0));
    }
}