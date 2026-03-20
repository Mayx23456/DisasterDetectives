package model;

import controller.GameController;


public abstract class Tile {
    private final int index;

    protected Tile(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public abstract void onLand(Player player, GameController controller);
}
