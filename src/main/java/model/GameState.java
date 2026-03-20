package model;

import java.util.List;


public class GameState {
    public static final int MAX_PLAYERS = 7;

    private final List<Player> players;
    private final Board board;
    private int currentPlayerIndex;
    private boolean finished;

    public GameState(List<Player> players, Board board) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required");
        }
        if (players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("Maximum number of players is " + MAX_PLAYERS);
        }
        if (board == null) {
            throw new IllegalArgumentException("Board is required");
        }
        this.players = List.copyOf(players);
        this.board = board;
        this.currentPlayerIndex = 0;
        this.finished = false;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            throw new IllegalArgumentException("Current player index out of range");
        }
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
