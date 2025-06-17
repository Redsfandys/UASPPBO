package model;

public class HighScoreEntry implements Comparable<HighScoreEntry> {
    private final String name;
    private final int level;
    private final int kills;
    private final int time;
    private final boolean won;

    public HighScoreEntry(String name, int level, int kills, int time, boolean won) {
        this.name = name;
        this.level = level;
        this.kills = kills;
        this.time = time;
        this.won = won;
    }

    // For loading from file
    public HighScoreEntry(String name, int level, int kills, int time) {
        this(name, level, kills, time, false); // Default to not won
    }

    public int getScore() {
        return level * 100 + kills * 50 + time * 1;
    }

    @Override
    public int compareTo(HighScoreEntry other) {
        return Integer.compare(other.getScore(), this.getScore());
    }

    @Override
    public String toString() {
        return name + "," + level + "," + kills + "," + time + "," + (won ? "1" : "0");
    }

    public String getDisplayString() {
        return String.format("%-4s | Level: %2d | Kills: %2d | Time: %4d | Result: %s",
                name, level, kills, time, won ? "Won" : "Lost");
    }
}