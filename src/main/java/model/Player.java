package model;

public class Player {
    private final String name;
    private int position;
    private javafx.scene.paint.Color color;

    public Player(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name must be non-empty");
        }
        this.name = name;
        this.position = 0;
    }

    public javafx.scene.paint.Color getColor() {
        return color;
    }

    public void setColor(javafx.scene.paint.Color color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return name + "@" + position;
    }
}
