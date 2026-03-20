package model;

import controller.GameController;

public class LadderTile extends Tile {
    private final int topPosition;
    private final String clue;

    public LadderTile(int index, int topPosition, String clue) {
        super(index);
        if (topPosition <= index) {
            throw new IllegalArgumentException("Ladder top must be above base position");
        }
        this.topPosition = topPosition;
        this.clue = clue;
    }

    public int getTopPosition() {
        return topPosition;
    }

    public String getClue() {
        return clue;
    }

    @Override
    public void onLand(Player player, GameController controller) {
        controller.showClue(clue);
        System.out.println(player.getName() + " climbed a ladder to " + topPosition + ".");
        controller.movePlayerTo(player, topPosition, true);
    }
}
