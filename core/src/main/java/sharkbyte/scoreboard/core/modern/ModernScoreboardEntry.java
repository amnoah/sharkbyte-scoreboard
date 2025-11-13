package sharkbyte.scoreboard.core.modern;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import sharkbyte.scoreboard.core.SBScoreboardEntry;

public class ModernScoreboardEntry implements SBScoreboardEntry {

    private boolean nameChanged = false;
    private String identifyingName, leftDisplayName = null, rightDisplayName = null;

    /**
     * Initialize the ScoreBoardEntry object.
     * The identifyingName string only matters on 1.20.3+. It can be any value on other versions.
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
    public String getIdentifyingName() {
        return identifyingName;
    }

    /**
     * Return the current assigned left-aligned display text for this line.
     */
    public String getLeftDisplayName() {
        return leftDisplayName;
    }

    /**
     * Return the current assigned right-aligned display text for this line.
     */
    public String getRightDisplayName() {
        return rightDisplayName;
    }

    /**
     * Return whether any text on this line has been changed.
     */
    public boolean hasNameChanged() {
        return nameChanged;
    }

    @Override
    public void setIdentifyingName(String identifyingName) {

    }

    /*
     * Setters.
     */

    /**
     * Manually force a line update.
     * Currently package-private, may become public in the future.
     */
    public void setNameChanged(boolean nameChanged) {
        this.nameChanged = nameChanged;
    }

    /*
     * Board Updaters.
     */

    /**
     * Handle a scoreboard update.
     */
    public void update() {
        nameChanged = false;
    }

    /**
     * This method update this line's left-aligned text to the given text.
     */
    public void updateLeftAlignedText(String text) {
        if (leftDisplayName != null && leftDisplayName.equals(text)) return;
        leftDisplayName = text;
        nameChanged = true;
    }

    /**
     * This method update this line's right-aligned text to the given text.
     */
    public void updateRightAlignedText(String text) {
        if (rightDisplayName != null && rightDisplayName.equals(text)) return;
        rightDisplayName = text;
        nameChanged = true;
    }
}
