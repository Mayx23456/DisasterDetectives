package util;

import java.util.Random;

public final class Dice {
    private static final Random RNG = new Random();

    private Dice() {
    }

    public static int roll() {
        return roll(6);
    }

    public static int roll(int sides) {
        if (sides <= 0) {
            throw new IllegalArgumentException("Dice must have at least one side");
        }
        return RNG.nextInt(sides) + 1;
    }
}
