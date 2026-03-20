package controller;

import java.util.function.Consumer;

import model.Board;
import model.GameState;
import model.Player;
import model.Tile;
import util.Dice;

public class GameController {
    private final GameState gameState;
    private final TurnController turnController;
    private final DisasterController disasterController;
    private SaveLoadController saveLoadController;
    private Integer activeGameId;
    private Player winner;
    private int turnCounter;

    private Consumer<String> clueProvider;

    public GameController(GameState gameState, TurnController turnController, DisasterController disasterController) {
        if (gameState == null || turnController == null || disasterController == null) {
            throw new IllegalArgumentException("Controllers and game state are required");
        }
        this.gameState = gameState;
        this.turnController = turnController;
        this.disasterController = disasterController;
    }

    public GameState getGameState() {
        return gameState;
    }

    public DisasterController getDisasterController() {
        return disasterController;
    }

    public void setClueProvider(Consumer<String> clueProvider) {
        this.clueProvider = clueProvider;
    }

    public void showClue(String clue) {
        if (clueProvider != null) {
            clueProvider.accept(clue);
        } else {
            System.out.println("Clue: " + clue);
        }
    }

    public void attachPersistence(SaveLoadController saveLoadController, Integer gameId) {
        this.saveLoadController = saveLoadController;
        this.activeGameId = gameId;
        this.turnCounter = 0;
    }

    public Player getWinner() {
        return winner;
    }

    public int playTurn() {
        if (gameState.isFinished()) {
            return 0;
        }

        Player player = gameState.getCurrentPlayer();
        int roll = Dice.roll();
        int start = player.getPosition();
        int target = start + roll;
        Board board = gameState.getBoard();

        if (target > board.getSize()) {
            target = board.getSize();
        }

        System.out.println(player.getName() + " rolls " + roll + " and moves from " + start + " to " + target + ".");
        movePlayerTo(player, target);

        if (!gameState.isFinished()) {
            Tile tile = board.getTile(player.getPosition());
            tile.onLand(player, this);
        }

        recordTurn(player, roll, start, player.getPosition());

        if (!gameState.isFinished()) {
            turnController.advanceTurn(gameState);
        }

        autoSaveIfEnabled();
        return roll;
    }

    public void playTurnWithRoll(int roll) {
        if (gameState.isFinished()) {
            return;
        }

        Player player = gameState.getCurrentPlayer();
        int start = player.getPosition();
        int target = start + roll;
        Board board = gameState.getBoard();

        if (target > board.getSize()) {
            target = board.getSize();
        }

        System.out.println(player.getName() + " rolls " + roll + " and moves from " + start + " to " + target + ".");
        movePlayerTo(player, target);

        if (!gameState.isFinished()) {
            Tile tile = board.getTile(player.getPosition());
            tile.onLand(player, this);
        }

        recordTurn(player, roll, start, player.getPosition());

        if (!gameState.isFinished()) {
            turnController.advanceTurn(gameState);
        }

        autoSaveIfEnabled();
    }

    public void movePlayerTo(Player player, int position) {
        movePlayerTo(player, position, false);
    }

    public void movePlayerTo(Player player, int position, boolean checkFinalTile) {
        int bounded = clampPosition(position);
        player.setPosition(bounded);
        if (checkFinalTile && bounded == gameState.getBoard().getSize() && !gameState.isFinished()) {
            Tile tile = gameState.getBoard().getTile(bounded);
            tile.onLand(player, this);
        }
    }

    public void movePlayerBy(Player player, int delta) {
        movePlayerBy(player, delta, false);
    }

    public void movePlayerBy(Player player, int delta, boolean checkFinalTile) {
        movePlayerTo(player, player.getPosition() + delta, checkFinalTile);
    }

    public void finishGame(Player player) {
        if (!gameState.isFinished()) {
            gameState.setFinished(true);
            winner = player;
            System.out.println("Game over! " + player.getName() + " wins.");
        }
    }

    public void recordDisaster(int tileIndex, String question, int chosenIndex, boolean correct) {
        if (saveLoadController == null || activeGameId == null) {
            return;
        }
        try {
            saveLoadController.recordDisaster(activeGameId, tileIndex, question, chosenIndex, correct);
        } catch (Exception ex) {
            System.err.println("Failed to record disaster event: " + ex.getMessage());
        }
    }

    private void recordTurn(Player player, int roll, int startPos, int endPos) {
        if (saveLoadController == null || activeGameId == null) {
            return;
        }
        try {
            turnCounter++;
            saveLoadController.recordTurn(activeGameId, turnCounter, player.getName(), roll, startPos, endPos);
        } catch (Exception ex) {
            System.err.println("Failed to record turn: " + ex.getMessage());
        }
    }

    private void autoSaveIfEnabled() {
        if (saveLoadController == null || activeGameId == null) {
            return;
        }
        try {
            saveLoadController.updateGame(activeGameId, gameState, winner);
        } catch (Exception ex) {
            System.err.println("Auto-save failed: " + ex.getMessage());
        }
    }

    private int clampPosition(int position) {
        if (position < 0) {
            return 0;
        }
        int max = gameState.getBoard().getSize();
        if (position > max) {
            return max;
        }
        return position;
    }
}
