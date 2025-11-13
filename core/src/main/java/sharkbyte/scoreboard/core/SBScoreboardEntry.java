package sharkbyte.scoreboard.core;

public interface SBScoreboardEntry {

    String getIdentifyingName();

    String getLeftDisplayName();

    String getRightDisplayName();

    boolean hasNameChanged();

    void setIdentifyingName(String identifyingName);

    void setNameChanged(boolean nameChanged);

    void update();

    void updateLeftAlignedText(String text);

    void updateRightAlignedText(String text);
}
