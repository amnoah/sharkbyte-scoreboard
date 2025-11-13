package sharkbyte.scoreboard.core.legacy;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import sharkbyte.scoreboard.core.SBScoreboardEntry;

public class LegacyScoreboardEntry implements SBScoreboardEntry {

    private boolean nameChanged = false;
    private String identifyingName = null, leftDisplayName = null;

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
     * This method will return null for legacy clients as it does nothing.
     */
    public String getRightDisplayName() {
        return null;
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
    public void setIdentifyingName(String identifyingName) {
        this.identifyingName = identifyingName;
    }

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
    @Override
    public void updateLeftAlignedText(String text) {
        if (leftDisplayName != null && leftDisplayName.equals(text)) return;

        /*
         * Given the major differences in how legacy and modern clients handle scoreboards, this is our fix that is
         * surprisingly versatile.
         *
         * In modern clients, scoreboards work like a map. The identifyingName can stay a constant name and will always
         * refer to the specific display names that need to be changed. This means to alter a scoreboard entry we just
         * need to tell the client "hey, the line under the name identifyingName needs its text updated to be this".
         *
         * In legacy clients, this feature doesn't exist. The client only knows the text it is displaying as the text it
         * is displaying. So, for legacy clients we need to know both the previous text that we will need to remove and
         * the new text. We then tell the client "you need to remove identifyingName, and in its place you need to put
         * displayName".
         *
         * This process recreates the same behavior on legacy clients.
         */
        if (!nameChanged) identifyingName = leftDisplayName;

        leftDisplayName = text;
        nameChanged = true;
    }

    /**
     * This method will do nothing for legacy clients.
     */
    public void updateRightAlignedText(String text) {}
}
