package com.example.dearfreshman;

public class ChoiceEvent extends StoryEvent {

    private final String prompt;
    private final String[] choices;
    private final String[] outcomes;
    private final int[][] statChanges;

    public ChoiceEvent(
            String title,
            String description,
            String speaker,
            String prompt,
            String[] choices,
            String[] outcomes,
            int[][] statChanges,
            String backgroundPath,
            String characterPath, String s) {

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

        player.applyChanges(
                changes[0],
                changes[1],
                changes[2],
                changes[3],
                changes[4]
        );

        // If a choice specifically restores energy (e.g. taking a nap with positive energy value)
        if (changes[0] > 0) {
            player.restoreEnergy(changes[0]);
        }

        return outcomes[choiceIndex];
    }

    @Override
    public boolean isChoiceEvent() {
        return true;
    }
}