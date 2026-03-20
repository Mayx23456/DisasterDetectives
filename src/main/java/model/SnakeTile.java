package model;

import controller.GameController;

public class SnakeTile extends Tile {
    private final int tailPosition;
    private final String clue;

    public SnakeTile(int index, int tailPosition, String clue) {
        super(index);
        if (tailPosition >= index) {
            throw new IllegalArgumentException("Snake tail must be below head position");
        }
        this.tailPosition = tailPosition;
        this.clue = clue;
    }

    public int getTailPosition() {
        return tailPosition;
    }

    public String getClue() {
        return clue;
    }

    @Override
    public void onLand(Player player, GameController controller) {
        controller.showClue(clue);
        System.out.println(player.getName() + " hit a snake! Sliding down to " + tailPosition + ".");
        controller.movePlayerTo(player, tailPosition, true);
    }
}
