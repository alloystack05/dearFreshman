# Dear Freshman — A4 Quick Reference (Top 10 Snippets)

This file is ready to be converted to PDF by printing it from VS Code, browser, or Markdown-to-PDF tools. It includes the source file path where each code snippet appears.

---

## 1) Loading the scene background
File: `src/main/java/com/example/dearfreshman/HelloController.java`

```java
// load scene background
loadImageInto(sceneBackground, story.getBackgroundPath());
```

What it does: Loads the current StoryEvent's background image into the ImageView via a helper that uses getResource() → Image.
Why it matters: Visuals establish the scene; missing resource → blank background.
One-line takeaway: UI gets visuals from the event model via a resource-loading helper.

---

## 2) Choosing evidence image or character image
File: `src/main/java/com/example/dearfreshman/HelloController.java`

```java
// choose evidence (dialogue) or character image
String characterOrEvidence = null;
if (story instanceof DialogueEvent) {
    characterOrEvidence = ((DialogueEvent) story).getEvidencePath();
}
if (characterOrEvidence == null || characterOrEvidence.isBlank()) {
    characterOrEvidence = story.getCharacterPath();
}
```

What it does: Prefers DialogueEvent evidence image, otherwise falls back to character art.
Why it matters: Lets certain events show evidence (like a photo) instead of a character portrait.
Takeaway: The controller uses runtime type-checking plus fallback logic to choose the displayed image.

---

## 3) Building dynamic choice buttons
File: `src/main/java/com/example/dearfreshman/HelloController.java`

```java
// dynamic choice button creation
choicesBox.getChildren().clear();
String[] choices = story.getChoices();
for (int i = 0; i < choices.length; i++) {
    final int choiceIndex = i;
    Button button = createButton(String.format("%02d   %s", i + 1, personalizeText(choices[i])));
    button.setOnAction(event -> handleChoice(choiceIndex));
    choicesBox.getChildren().add(button);
}
```

What it does: Clears old choices, creates a button for each available choice, and binds each one to the correct choice index.
Why it matters: Supports variable choice counts and preserves a reliable choice callback.
Takeaway: Data-driven UI mapping — each runtime choice gets its own button and index-bound handler.

---

## 4) Handling choice flow versus dialogue flow
File: `src/main/java/com/example/dearfreshman/HelloController.java`

```java
// difference between dialogue and choice events
StoryEvent story = gameState.getCurrentEvent();
if (story == null) return;

if (story instanceof DialogueEvent) {
    story.resolveChoice(gameState.getPlayer(), choiceIndex);
    advanceStoryTime(15);
    advanceToNextStoryEvent();
    return;
}
String result = story.resolveChoice(gameState.getPlayer(), choiceIndex);
```

What it does: Distinguishes DialogueEvent (simple continue) from other events, which return an outcome to display.
Why it matters: The controller decides how to handle the result and time progression depending on the event type.
Takeaway: The controller delegates the action but controls the subsequent UI and time behavior.

---

## 5) Hard-coded database connection (pre-update)
File: `src/main/java/com/example/dearfreshman/DatabaseConnection.java`

```java
// hard-coded DB connection (pre-update)
private static final String URL = "jdbc:mysql://localhost:3306/dear_freshman";
private static final String USER = "root";
private static final String PASSWORD = "";

public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}
```

What it does: Returns a JDBC Connection using a fixed MySQL URL and credentials.
Why it matters: Centralizes DB access but is insecure and inflexible because the credentials are hard-coded.
Takeaway: Pre-update configuration is static MySQL; prefer environment variables or external config.

---

## 6) Transactional save flow in GameRepository
File: `src/main/java/com/example/dearfreshman/GameRepository.java`

```java
// createNewSave - begin transaction and insert player
try (Connection con = DatabaseConnection.getConnection()) {
    con.setAutoCommit(false);

    try (PreparedStatement ps = con.prepareStatement(playerSql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, player.getName());
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) throw new SQLException("Could not create player.");
            playerId = rs.getInt(1);
        }
    }
    // ... insert game_saves and player_stats, then:
    con.commit();
} catch (SQLException | RuntimeException e) {
    con.rollback();
    throw e;
} finally {
    con.setAutoCommit(true);
}
```

What it does: Starts a database transaction, inserts the player, reads the generated player_id, then inserts the save and stats before committing.
Why it matters: Ensures atomic save behavior — all related rows are inserted together or none are.
Takeaway: Proper JDBC transaction + generated-key pattern is essential for consistent save data.

---

## 7) Saving player stats into player_stats
File: `src/main/java/com/example/dearfreshman/GameRepository.java`

```java
// bind and insert player stats
try (PreparedStatement ps = con.prepareStatement(statsSql)) {
    ps.setInt(1, saveId);
    ps.setInt(2, player.getEnergy());
    ps.setInt(3, player.getFriendship());
    ps.setInt(4, player.getReputation());
    ps.setInt(5, player.getAcademics());
    ps.setInt(6, player.getSuspicion());
    ps.setInt(7, player.getGinoTrust());
    ps.setInt(8, player.getZTrust());
    ps.setInt(9, player.getObetTrust());
    ps.setInt(10, player.getWilliamSuspicion());
    ps.setInt(11, player.getProgrammingScore());
    ps.setInt(12, player.getDatabaseScore());
    ps.setInt(13, player.getHistoryScore());
    ps.setInt(14, player.getQuizAnswered());
    ps.executeUpdate();
}
```

What it does: Writes the Player's full state into the player_stats table using a parameterized insert.
Why it matters: Persists full gameplay state and prevents SQL injection through PreparedStatement parameter binding.
Takeaway: All player fields are saved in a consistent, ordered binding sequence.

---

## 8) Selecting the latest save
File: `src/main/java/com/example/dearfreshman/GameRepository.java`

```java
// get most recent save_id
String sql = "SELECT save_id FROM game_saves ORDER BY save_id DESC LIMIT 1";
try (Connection con = DatabaseConnection.getConnection();
     PreparedStatement ps = con.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    if (rs.next()) return rs.getInt("save_id");
}
return -1;
```

What it does: Returns the highest save_id (most recently created save) or -1 if no save exists.
Why it matters: Continue uses this to select which save to load.
Takeaway: Continue loads the latest created save via save_id DESC.

---

## 9) Loading the player and restoring state
File: `src/main/java/com/example/dearfreshman/GameRepository.java`

```java
// load player and restore progress
Player player = new Player(rs.getString("player_name"));
player.restoreProgress(
    rs.getInt("energy"), rs.getInt("friendship"),
    rs.getInt("reputation"), rs.getInt("academics"),
    rs.getInt("suspicion"), rs.getInt("programming_score"),
    rs.getInt("quiz_answered")
);
int ginoDelta = rs.getInt("gino_trust") - 50;
player.adjustNpcTrust(ginoDelta, zDelta, obetDelta, williamDelta);
```

What it does: Recreates the Player with name, restores major stats and quiz counters, and applies NPC trust deltas.
Why it matters: Rehydrates the in-memory Player state for Continue; pre-update limitation: saved event/time were not restored here.
Takeaway: loadPlayer restores stats and trust but not the full in-game position/time (pre-update).

---

## 10) Duplicate energy bug in ChoiceEvent
File: `src/main/java/com/example/dearfreshman/ChoiceEvent.java`

```java
// choice resolution applies changes then conditionally restores energy
int[] changes = statChanges[choiceIndex];
player.applyChanges(changes[0], changes[1], changes[2], changes[3], changes[4]);
if (changes[0] > 0) { // positive energy
    player.restoreEnergy(changes[0]);
}
```

What it does: Applies all stat deltas, then, if the energy delta is positive, calls restoreEnergy again with the same amount.
Why it matters: applyChanges already adds energyChange; this duplicates the positive energy increase — a pre-update bug.
Takeaway: Fix by removing the extra restoreEnergy call or changing applyChanges semantics.

---

## Final memory line
Use this in defense:
"The app is layered: JavaFX UI shows the story, GameState manages the flow and player data, and GameRepository saves and loads progress using JDBC. In the original version, there were a few important weaknesses: the DB connection was hard-coded, some positive-energy choices could be applied twice, and the saved event/time state was not fully restored on Continue."

---

## Quick file map for the 10 core snippets
- `src/main/java/com/example/dearfreshman/HelloController.java`
- `src/main/java/com/example/dearfreshman/DatabaseConnection.java`
- `src/main/java/com/example/dearfreshman/GameRepository.java`
- `src/main/java/com/example/dearfreshman/ChoiceEvent.java`

If you want, I can next create a second version specifically formatted for printing in a PDF layout (broader margins, shorter text, larger headings) or generate a companion one-page “oral defense version” with only the script and model answers.
