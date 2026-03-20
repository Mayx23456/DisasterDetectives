package model;

import controller.GameController;

/**
 * Fixed-path board that composes tiles in a 1..100 layout.
 */
public class Board {
    public static final int SIZE = 100;

    private final Tile[] tiles = new Tile[SIZE + 1];

    public Board() {
        initializeTiles();
    }

    public int getSize() {
        return SIZE;
    }

    public Tile getTile(int position) {
        if (position < 1 || position > SIZE) {
            throw new IllegalArgumentException("Position out of bounds: " + position);
        }
        return tiles[position];
    }

    private void initializeTiles() {
        for (int i = 1; i <= SIZE; i++) {
            tiles[i] = new BasicTile(i);
        }

        tiles[22] = new SnakeTile(22, 3,
                "Bad news! Global CO₂ levels have exceeded 420 ppm — the highest in 3 million years.");
        tiles[33] = new SnakeTile(33, 12,
                "Bad news! The Arctic is warming nearly four times faster than the global average.");
        tiles[45] = new DisasterTile(45, 6, 6);
        tiles[54] = new SnakeTile(54, 47,
                "Bad news! Ocean plastic pollution kills over one million seabirds and 100,000 marine mammals every year.");
        tiles[76] = new SnakeTile(76, 65,
                "Bad news! Deforestation destroys around 10 million hectares of forest every year — an area the size of Iceland.");
        tiles[89] = new DisasterTile(89, 8, 8);
        tiles[99] = new SnakeTile(99, 84,
                "Bad news! The last decade (2011–2020) was the hottest on record since measurements began.");

        tiles[15] = new LadderTile(15, 35,
                "Good news! Renewable energy now accounts for over 30% of global electricity generation.");
        tiles[38] = new LadderTile(38, 58,
                "Good news! The ozone layer is on track to fully recover by 2066 thanks to the Montreal Protocol.");
        tiles[50] = new LadderTile(50, 70,
                "Good news! Over 17% of the world's land and 8% of its oceans are now protected conservation areas.");
        tiles[62] = new LadderTile(62, 82,
                "Good news! Electric vehicle sales have grown from under 1% to over 18% of global car sales in just a decade.");
        tiles[74] = new LadderTile(74, 94,
                "Good news! Costa Rica generated 99% of its electricity from renewable sources in 2023.");

        tiles[SIZE] = new FinalTile(SIZE, 3);
    }

    private static final class BasicTile extends Tile {
        private BasicTile(int index) {
            super(index);
        }

        @Override
        public void onLand(Player player, GameController controller) {
            System.out.println(player.getName() + " landed on a safe tile.");
        }
    }
}
