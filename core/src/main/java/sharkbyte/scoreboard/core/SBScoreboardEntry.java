package sharkbyte.scoreboard.core;

/**
 * This represents a line on a scoreboard.
 *
 * @Author: am noah
 * @Since: 2.0.0
 * @Updated: 2.0.0
 */
public interface SBScoreboardEntry {

    /*
     * Getters.
     */

    /**
     * Return the current assigned identifying text for this line.
     */
    String getIdentifyingName();

    /**
     * Return the current assigned left-aligned display text for this line.
     */
    String getLeftDisplayName();

    /**
     * Return the current assigned right-aligned display text for this line.
     */
    String getRightDisplayName();

    /*
     * Setters.
     */

    /**
     * Return whether any text on this line has been changed.
     */
    boolean hasNameChanged();

    /**
     * Manually set the identifying name.
     */
    void setIdentifyingName(String identifyingName);

    /**
     * Manually force a line update.
     */
    void setNameChanged(boolean nameChanged);

    /*
     * Board Updaters.
     */

    /**
     * Handle a scoreboard update.
     */
    void update();

    /**
     * This method update this line's left-aligned text to the given text.
     */
    void updateLeftAlignedText(String text);

    /**
     * This method update this line's right-aligned text to the given text.
     */
    void updateRightAlignedText(String text);
}
