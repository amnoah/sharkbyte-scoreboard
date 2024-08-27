package sharkbyte.scoreboard.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

/**
 * This class represents an individual line on a scoreboard.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.1.0
 */
public class ScoreboardEntry {

    private boolean nameChanged = false;
    private String identifyingName, leftDisplayName = null, rightDisplayName = null;

    /**
     * Initialize the ScoreBoardEntry object.
     * The identifyingName string only matters on 1.20.3+. It can be any value on other versions.
     */
    public ScoreboardEntry(String identifyingName) {
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

    /*
     * Setters.
     */

    /**
     * Manually set the identifying name.
     * Currently package-private, may become public in the future.
     */
    void setIdentifyingName(String identifyingName) {
        this.identifyingName = identifyingName;
    }

    /**
     * Manually force a line update.
     * Currently package-private, may become public in the future.
     */
    void setNameChanged(boolean nameChanged) {
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

        /*
         * In 1.20.3+, the identifyingName string permanently directs us to this line.
         * In older versions, the previous display text directs us to this line.
         */
        if (!PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_3)) {
            if (!nameChanged) identifyingName = leftDisplayName;
        }

        leftDisplayName = text;
        nameChanged = true;
    }

    /**
     * This method update this line's right-aligned text to the given text.
     * This is only available in 1.20.3+.
     */
    public void updateRightAlignedText(String text) {
        /*
         * Because this is a 1.20.3+ feature we don't need all the extra legacy code.
         */
        if (rightDisplayName != null && rightDisplayName.equals(text)) return;

        rightDisplayName = text;
        nameChanged = true;
    }
}
