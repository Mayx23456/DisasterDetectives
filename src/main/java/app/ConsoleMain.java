package app;

import controller.DisasterController;
import controller.GameController;
import controller.TurnController;
import model.Board;
import model.GameState;
import model.Player;

import java.util.List;

public class ConsoleMain {
    public static void main(String[] args) {
        List<Player> players = List.of(
                new Player("Jack"),
                new Player("Jordan")
        );

        Board board = new Board();
        GameState gameState = new GameState(players, board);
        GameController gameController = new GameController(
                gameState,
                new TurnController(),
                new DisasterController()
        );

        int turn = 1;
        int turnLimit = 200;

        while (!gameState.isFinished() && turn <= turnLimit) {
            System.out.println("\nTurn " + turn + ": " + gameState.getCurrentPlayer().getName() + "'s move");
            gameController.playTurn();
            turn++;
        }

        if (!gameState.isFinished()) {
            System.out.println("Reached turn limit without a winner.");
        } else if (gameController.getWinner() != null) {
            System.out.println("Winner: " + gameController.getWinner().getName());
        }

    }
}
