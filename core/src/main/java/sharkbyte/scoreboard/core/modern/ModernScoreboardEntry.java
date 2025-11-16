package sharkbyte.scoreboard.core.modern;

import sharkbyte.scoreboard.core.SBScoreboardEntry;

/**
 * This class represents a line on a modern scoreboard.
 * Modern scoreboards work incredibly efficiently, so little logic is needed here.
 *
 * @Author: am noah
 * @Since: 2.0.0
 * @Updated: 2.0.0
 */
public class ModernScoreboardEntry implements SBScoreboardEntry {

    private boolean nameChanged = false;
    private String identifyingName, leftDisplayName = null, rightDisplayName = null;

    /**
     * Initialize the ModernScoreboardEntry object.
     */
    public ModernScoreboardEntry(String identifyingName) {
        this.identifyingName = identifyingName;
    }

    /*
     * Getters.
     */

    /**
     * Return the current assigned identifying text for this line.
     */
    @Override
    public String getIdentifyingName() {
        return identifyingName;
    }

    /**
     * Return the current assigned left-aligned display text for this line.
     */
    @Override
    public String getLeftDisplayName() {
        return leftDisplayName;
    }

    /**
     * Return the current assigned right-aligned display text for this line.
     */
    @Override
    public String getRightDisplayName() {
        return rightDisplayName;
    }

    /**
     * Return whether any text on this line has been changed.
     */
    @Override
    public boolean hasNameChanged() {
        return nameChanged;
    }

    /*
     * Setters.
     */

    /**
     * Manually set the identifying name.
     */
    @Override
    public void setIdentifyingName(String identifyingName) {
        this.identifyingName = identifyingName;
    }

    /**
     * Manually force a line update.
     */
    @Override
    public void setNameChanged(boolean nameChanged) {
        this.nameChanged = nameChanged;
    }

    /*
     * Board Updaters.
     */

    /**
     * Handle a scoreboard update.
     */
    @Override
    public void update() {
        nameChanged = false;
    }

    /**
     * This method update this line's left-aligned text to the given text.
     */
    @Override
    public void updateLeftAlignedText(String text) {
        if (leftDisplayName != null && leftDisplayName.equals(text)) return;
        leftDisplayName = text;
        nameChanged = true;
    }

    /**
     * This method update this line's right-aligned text to the given text.
     */
    @Override
    public void updateRightAlignedText(String text) {
        if (rightDisplayName != null && rightDisplayName.equals(text)) return;
        rightDisplayName = text;
        nameChanged = true;
    }
}
