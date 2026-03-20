package controller;

import model.GameState;

public class TurnController {
    public void advanceTurn(GameState gameState) {
        int nextIndex = (gameState.getCurrentPlayerIndex() + 1) % gameState.getPlayers().size();
        gameState.setCurrentPlayerIndex(nextIndex);
    }
}
