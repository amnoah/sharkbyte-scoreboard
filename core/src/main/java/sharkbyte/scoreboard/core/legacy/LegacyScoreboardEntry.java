package sharkbyte.scoreboard.core.legacy;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import sharkbyte.scoreboard.core.SBScoreboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LegacyScoreboardEntry implements SBScoreboardEntry {

    private final String color;

    private boolean nameChanged = false;
    private String identifyingName = null, leftDisplayName = null;

    public LegacyScoreboardEntry(String color) {
        this.color = color;
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
        this.identifyingName = modifyString(identifyingName);
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

        leftDisplayName = modifyString(text);
        nameChanged = true;
    }

    /**
     * This method will do nothing for legacy clients.
     */
    public void updateRightAlignedText(String text) {}

    /*
     * Other functions
     */

    private String modifyString(String entry) {
        if (entry == null) return null;

        StringBuilder finalString = new StringBuilder();
        String prefix = null, main;

        if (entry.length() <= 26) main = entry;
        else {
            prefix = entry.substring(0, 16);
            main = entry.substring(16);
        }

        String colorCode = "§f";
        List<String> formattingCodes = new ArrayList<>();
        boolean shrink = false;

        if (prefix != null) {
            char[] prefixChars = prefix.toCharArray();
            for (int i = 0; i < prefixChars.length - 1; i++) {
                if (prefixChars[i] == '§') {
                    String code = "§" + prefixChars[i + 1];
                    if (code.equals(LegacyScoreboard.RESET_CODE)) {
                        formattingCodes.clear();
                        colorCode = "§f";
                    } else if (LegacyScoreboard.FORMATTING_CODES.contains(code)) {
                        formattingCodes.remove(code);
                        formattingCodes.add(code);
                    } else if (LegacyScoreboard.COLOR_CODES.contains(code)) {
                        colorCode = code;
                        formattingCodes.clear();
                    }
                }
            }

            if (prefix.charAt(prefix.length() - 1) == '§') {
                String code = "§" + main.charAt(0);
                shrink = true;
                if (code.equals(LegacyScoreboard.RESET_CODE)) {
                    formattingCodes.clear();
                    colorCode = "§f";
                } else if (LegacyScoreboard.FORMATTING_CODES.contains(code)) {
                    formattingCodes.remove(code);
                    formattingCodes.add(code);
                } else if (LegacyScoreboard.COLOR_CODES.contains(code)) {
                    colorCode = code;
                    formattingCodes.clear();
                } else shrink = false;
            }
        }

        if (shrink) {
            prefix = prefix.substring(0, 15);
            if (main.length() == 1) main = "";
            else main = main.substring(0, main.length() - 1);
        }

        if (prefix != null) finalString.append(prefix);
        finalString.append(color);//.append(LegacyScoreboard.RESET_CODE);
        for (int i = 0; i < 5 - formattingCodes.size(); i++) finalString.append(LegacyScoreboard.RESET_CODE);
        finalString.append(colorCode);
        for (String format : formattingCodes) finalString.append(format);
        finalString.append(main);

        return finalString.toString();
    }
}