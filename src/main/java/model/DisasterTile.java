package model;

import controller.DisasterController;
import controller.GameController;

public class DisasterTile extends Tile {
    private final int rewardSteps;
    private final int penaltySteps;

    public DisasterTile(int index, int rewardSteps, int penaltySteps) {
        super(index);
        this.rewardSteps = rewardSteps;
        this.penaltySteps = penaltySteps;
    }

    @Override
    public void onLand(Player player, GameController controller) {
        DisasterController disasterController = controller.getDisasterController();
        DisasterQuestion question = disasterController.drawQuestion();
        int chosenIndex = disasterController.chooseAnswer(player, question);
        boolean correct = disasterController.isCorrect(question, chosenIndex);

        System.out.println(player.getName() + " faces a disaster quiz: " + question.getPrompt());
        System.out.println("Answer chosen: " + question.getOptions().get(chosenIndex) + (correct ? " (correct)" : " (incorrect)"));
        controller.recordDisaster(getIndex(), question.getPrompt(), chosenIndex, correct);

        if (correct && rewardSteps > 0) {
            System.out.println("Correct! Move forward " + rewardSteps + " spaces.");
            controller.movePlayerBy(player, rewardSteps, true);
        } else if (!correct && penaltySteps > 0) {
            System.out.println("Incorrect. Move back " + penaltySteps + " spaces.");
            controller.movePlayerBy(player, -penaltySteps, true);
        } else {
            System.out.println("No movement change.");
        }
    }
}
