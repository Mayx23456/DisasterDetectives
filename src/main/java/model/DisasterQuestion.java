package model;

import java.util.List;
import java.util.Random;

public class DisasterQuestion {
    private final String prompt;
    private final List<String> options;
    private final int correctIndex;

    public DisasterQuestion(String prompt, List<String> options, int correctIndex) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Question prompt must be non-empty");
        }
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("Question must have at least two options");
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("Correct index out of range");
        }
        this.prompt = prompt;
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public int getRandomIncorrectIndex(Random rng) {
        int index;
        do {
            index = rng.nextInt(options.size());
        } while (index == correctIndex);
        return index;
    }
}
