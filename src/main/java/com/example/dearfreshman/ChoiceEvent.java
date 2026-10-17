package com.example.dearfreshman;

public class ChoiceEvent extends StoryEvent {

    private final String prompt;
    private final String[] choices;
    private final String[] outcomes;
    private final int[][] statChanges;
    private final String evidencePath;

    public ChoiceEvent(
            String title,
            String description,
            String speaker,
            String prompt,
            String[] choices,
            String[] outcomes,
            int[][] statChanges,
            String backgroundPath,
            String characterPath,
            String evidencePath) {

        super(
                title,
                description,
                speaker,
                backgroundPath,
                characterPath
        );

        if (choices == null || choices.length != 2
                || outcomes == null || outcomes.length != 2
                || statChanges == null || statChanges.length != 2) {

            throw new IllegalArgumentException(
                    "A choice event requires two choices, "
                            + "two outcomes, and two stat-change arrays.");
        }

        for (int[] changes : statChanges) {
            if (changes == null || changes.length != 5) {
                throw new IllegalArgumentException(
                        "Each choice must contain five stat changes.");
            }
        }

        this.prompt = prompt;
        this.choices = choices.clone();
        this.outcomes = outcomes.clone();
        this.statChanges = new int[][]{
                statChanges[0].clone(),
                statChanges[1].clone()
        };
        this.evidencePath = evidencePath;
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    @Override
    public String[] getChoices() {
        return choices.clone();
    }

    @Override
    public String resolveChoice(
            Player player, int choiceIndex) {

        if (player == null) {
            throw new IllegalStateException(
                    "Start a new game before making a choice.");
        }

        if (choiceIndex < 0 || choiceIndex >= choices.length) {
            throw new IllegalArgumentException(
                    "Invalid choice index.");
        }

        int[] changes = statChanges[choiceIndex];

        // Apply stat changes once. Do not double-apply energy.
        player.applyChanges(
                changes[0],
                changes[1],
                changes[2],
                changes[3],
                changes[4]
        );

        return outcomes[choiceIndex];
    }

    @Override
    public boolean isChoiceEvent() {
        return true;
    }

    @Override
    public String getEvidencePath() {
        return evidencePath;
    }
}
