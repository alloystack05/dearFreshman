package com.example.dearfreshman;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameState {

    private Player player;

    private final List<StoryEvent> events = new ArrayList<>();

    private int currentEventIndex = 0;

    private int chapterTwoStartIndex;
    private int chapterThreeStartIndex;
    private int chapterFourStartIndex;
    private int chapterFiveStartIndex;
    private int chapterSixStartIndex;

    private final GameRepository repository = new GameRepository();

    private int currentSaveId = -1;
    private TimeManager timeManager = new TimeManager();

    public GameState() {
        buildChapterOne();

        chapterTwoStartIndex = events.size();
        buildChapterTwo();

        chapterThreeStartIndex = events.size();
        buildChapterThree();

        chapterFourStartIndex = events.size();
        buildChapterFour();

        chapterFiveStartIndex = events.size();
        buildChapterFive();

        chapterSixStartIndex = events.size();
        buildChapterSix();
    }

    public int getCurrentSaveId() {
        return currentSaveId;
    }

    public void createDatabaseSave() throws SQLException {
        if (player == null) {
            throw new IllegalStateException("Start a new game before creating a save.");
        }
        currentSaveId = repository.createNewSave(player);
    }

    public boolean loadLatestSave() throws SQLException {
        int latestSaveId = repository.getLatestSaveId();
        if (latestSaveId == -1) {
            return false;
        }

        Player loadedPlayer = repository.loadPlayer(latestSaveId);
        if (loadedPlayer == null) {
            return false;
        }

        this.player = loadedPlayer;
        this.currentSaveId = latestSaveId;

        // Restore saved play position and time state if available.
        int[] meta = repository.loadSaveMetadata(latestSaveId);
        this.currentEventIndex = meta[0];
        this.timeManager.restoreState(meta[1], meta[2]);
        return true;
    }

    public void startNewGame(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Please enter a valid name.");
        }

        player = new Player(playerName.trim());
        currentEventIndex = 0;
        currentSaveId = -1;
    }

    public Player getPlayer() {
        return player;
    }

    public StoryEvent getCurrentEvent() {
        if (events.isEmpty() || currentEventIndex < 0 || currentEventIndex >= events.size()) {
            return null;
        }
        return events.get(currentEventIndex);
    }

    public int getCurrentEventNumber() {
        return currentEventIndex;
    }

    public int getEventCount() {
        return events.size();
    }

    public boolean isLastEvent() {
        return !events.isEmpty() && currentEventIndex == events.size() - 1;
    }

    public void advanceEvent() {
        if (currentEventIndex >= 0 && currentEventIndex < events.size() - 1) {
            currentEventIndex++;
        }
    }

    public void jumpToEvent(int index) {
        if (index >= 0 && index < events.size()) {
            currentEventIndex = index;
        }
    }

    public int getCurrentChapter() {
        if (currentEventIndex < chapterTwoStartIndex) return 1;
        if (currentEventIndex < chapterThreeStartIndex) return 2;
        if (currentEventIndex < chapterFourStartIndex) return 3;
        if (currentEventIndex < chapterFiveStartIndex) return 4;
        if (currentEventIndex < chapterSixStartIndex) return 5;
        return 6;
    }

    public int getCurrentChapterEventNumber() {
        return switch (getCurrentChapter()) {
            case 1 -> currentEventIndex;
            case 2 -> currentEventIndex - chapterTwoStartIndex;
            case 3 -> currentEventIndex - chapterThreeStartIndex;
            case 4 -> currentEventIndex - chapterFourStartIndex;
            case 5 -> currentEventIndex - chapterFiveStartIndex;
            default -> currentEventIndex - chapterSixStartIndex;
        };
    }

    public int getCurrentChapterEventCount() {
        return switch (getCurrentChapter()) {
            case 1 -> chapterTwoStartIndex;
            case 2 -> chapterThreeStartIndex - chapterTwoStartIndex;
            case 3 -> chapterFourStartIndex - chapterThreeStartIndex;
            case 4 -> chapterFiveStartIndex - chapterFourStartIndex;
            case 5 -> chapterSixStartIndex - chapterFiveStartIndex;
            default -> events.size() - chapterSixStartIndex;
        };
    }

    public boolean isLastEventOfChapter() {
        if (getCurrentEvent() == null) return false;
        int chapterEventNumber = getCurrentChapterEventNumber();
        int chapterEventCount = getCurrentChapterEventCount();
        return chapterEventCount > 0 && chapterEventNumber == chapterEventCount - 1;
    }

    private void buildChapterOne() {
        String front = "/images/school/front.jpg";
        String campus = "/images/school/campus.jpg";
        String hallway = "/images/school/hallway.jpg";
        String classroom = "/images/school/classrooms.jpg";

        events.add(new DialogueEvent(
                "A Twin's Secret", "Chapter 1 • A new beginning", "YOUR JOURNAL",
                "The National College of Science and Technology looks perfect: enormous buildings, spotless halls, and students who seem to know where they belong. You don't.\n\nAfter your grandmother's death, your mother enrolled you here. But this isn't just a new school. Your twin sibling, Luna, has lived in this world without you.",
                front, null
        ));

        events.add(new DialogueEvent(
                "A Familiar Face", "A student mistakes you for someone else.", "RANDOM GIRL",
                "A girl stops in the hallway, eyes wide.\n\n\"Wait... are you Luna?\"\n\nYou explain that you're Luna's twin sibling.",
                hallway, "/char/random girl.png"
        ));

        events.add(new ChoiceEvent(
                "Gino and Z", "A group of students notices the commotion.", "GINO & Z",
                "Z studies you from head to toe while Gino watches quietly. Z says, \"So you're the twin. Luna never mentioned you.\"\n\nHow do you respond?",
                new String[]{"\"Maybe she didn't have a reason to.\"", "Ignore the comment and ask Gino where class is."},
                new String[]{
                        "Your calm reply makes Z curious. Gino gives you a small nod.",
                        "Gino points you toward the classroom. Z's stare follows you."
                },
                new int[][]{{-1, 1, 3, 0, 2}, {0, 0, 1, 0, 0}},
                campus, "/char/gino/Gino.png", "/evidences/paper.png"
        ));

        // ... remaining events unchanged (omitted for brevity in this commit representation)
    }

    private void buildChapterTwo() {
        // ... (unchanged)
    }

    private void buildChapterThree() {
        // ... (unchanged)
    }

    private void buildChapterFour() {
        // ... (unchanged)
    }

    private void buildChapterFive() {
        // ... (unchanged)
    }

    private void buildChapterSix() {
        // ... (unchanged)
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }
}
