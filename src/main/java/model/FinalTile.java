package model;

import controller.DisasterController;
import controller.GameController;

public class FinalTile extends Tile {
    private final int setbackSteps;

    public FinalTile(int index, int setbackSteps) {
        super(index);
        if (setbackSteps < 0) {
            throw new IllegalArgumentException("Setback steps must be non-negative");
        }
        this.setbackSteps = setbackSteps;
    }

    @Override
    public void onLand(Player player, GameController controller) {
        DisasterController disasterController = controller.getDisasterController();
        if (disasterController == null) {
            controller.finishGame(player);
            return;
        }

        DisasterQuestion question = disasterController.drawQuestion();
        int chosenIndex = disasterController.chooseAnswer(player, question);
        boolean correct = disasterController.isCorrect(question, chosenIndex);

        System.out.println(player.getName() + " faces the final quiz: " + question.getPrompt());
        System.out.println("Answer chosen: " + question.getOptions().get(chosenIndex) + (correct ? " (correct)" : " (incorrect)"));
        controller.recordDisaster(getIndex(), question.getPrompt(), chosenIndex, correct);

        if (correct) {
            controller.finishGame(player);
        } else if (setbackSteps > 0) {
            System.out.println("Final quiz failed. Move back " + setbackSteps + " spaces.");
            controller.movePlayerBy(player, -setbackSteps, false);
        } else {
            System.out.println("Final quiz failed. Stay on final tile.");
        }
    }
}
