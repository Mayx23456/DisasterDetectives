package controller;

import model.DisasterQuestion;
import model.Player;

import java.util.List;
import java.util.Random;

public class DisasterController {
    private final List<DisasterQuestion> questions;
    private final Random rng = new Random();
    private HumanAnswerProvider humanAnswerProvider = question -> rng.nextInt(question.getOptions().size());

    public DisasterController() {
        this.questions = List.of(
                new DisasterQuestion(
                        "What should you do first during an earthquake?",
                        List.of("Drop, cover, and hold on", "Run outside immediately", "Stand in a doorway", "Head to an elevator"),
                        0
                ),
                new DisasterQuestion(
                        "Which item is most essential in a disaster kit?",
                        List.of("Water", "Board games", "Perfume", "Extra pillows"),
                        0
                ),
                new DisasterQuestion(
                        "During a flood, the safest action is to:",
                        List.of("Walk through moving water", "Move to higher ground", "Drive into water", "Stay in a low-lying area"),
                        1
                ),
                new DisasterQuestion(
                        "Before a wildfire approaches, you should:",
                        List.of("Clear dry vegetation", "Leave windows open", "Store extra gasoline", "Park in dry grass"),
                        0
                )
        );
    }

    public DisasterQuestion drawQuestion() {
        return questions.get(rng.nextInt(questions.size()));
    }

    public boolean isCorrect(DisasterQuestion question, int chosenIndex) {
        return question.getCorrectIndex() == chosenIndex;
    }

    public int chooseAnswer(Player player, DisasterQuestion question) {
        return humanAnswerProvider.ask(question);
    }

    public void setHumanAnswerProvider(HumanAnswerProvider humanAnswerProvider) {
        if (humanAnswerProvider == null) {
            throw new IllegalArgumentException("Human answer provider cannot be null");
        }
        this.humanAnswerProvider = humanAnswerProvider;
    }

    public interface HumanAnswerProvider {
        int ask(DisasterQuestion question);
    }
}
