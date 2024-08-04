package sharkbyte.scoreboard.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

/**
 * This class represents an individual line on a scoreboard.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class ScoreboardEntry {

    private boolean nameChanged = false;
    private String identifyingName, displayName = null;

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
     * Return the current assigned display text for this line.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Return the current assigned identifying text for this line.
     */
    public String getIdentifyingName() {
        return identifyingName;
    }

    /**
     * Return whether the text on this line has been changed.
     */
    public boolean hasNameChanged() {
        return nameChanged;
    }

    /*
     * Setters.
     */

    /**
     * Manually force a line update.
     */
    public void setNameChanged(boolean nameChanged) {
        this.nameChanged = nameChanged;
    }

    /*
     * Update the text in the board.
     */

    /**
     * Handle a scoreboard update.
     */
    public void update() {
        nameChanged = false;
    }

    /**
     * This method update this line's display text to the given text.
     */
    public void updateDisplayName(String text) {
        if (displayName != null && displayName.equals(text)) return;

        /*
         * In 1.20.3+, the identifyingName string permanently directs us to this line.
         * In older versions, the previous display text directs us to this line.
         */
        if (!PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_3)) {
            if (!nameChanged) identifyingName = displayName;
        }

        displayName = text;
        nameChanged = true;
    }
}
