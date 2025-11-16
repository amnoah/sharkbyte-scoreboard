package sharkbyte.scoreboard.core.legacy;

import sharkbyte.scoreboard.core.SBScoreboardEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a line on a legacy scoreboard.
 * Legacy scoreboards are horribly inefficient and require some crazy logic to operate.
 *
 * @Author: am noah
 * @Since: 2.0.0
 * @Updated: 2.0.0
 */
public class LegacyScoreboardEntry implements SBScoreboardEntry {

    private final String color;
    private boolean dangerMode = false;

    private boolean nameChanged = false;
    private String identifyingName = null, leftDisplayName = null;

    /**
     * Initialize the LegacyScoreboardEntry object.
     */
    public LegacyScoreboardEntry(String color) {
        this.color = color;
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
     * This method will return null for legacy clients as it does nothing.
     */
    @Override
    public String getRightDisplayName() {
        return null;
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
     * Set the danger mode status.
     * If danger mode is true, headers will not be included - gaining 14 characters for usage. The problem arises by the
     * fact that our headers protect against duplicate lines being created, and if there are duplicate lines one will be
     * removed.
     */
    public void setDangerMode(boolean dangerMode) {
        this.dangerMode = dangerMode;
    }

    /**
     * Manually set the identifying name.
     */
    @Override
    public void setIdentifyingName(String identifyingName) {
        this.identifyingName = modifyString(identifyingName);
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
    @Override
    public void updateRightAlignedText(String text) {}

    /*
     * Other functions
     */

    /**
     * Modify the string to add a unique identifier to prevent duplicate lines as needed.
     */
    private String modifyString(String entry) {
        if (entry == null) return null;
        // Danger mode doesn't add an identifier.
        if (dangerMode) return entry;

        /*
         * To prevent duplicate lines from being removed, we add a unique identifier at the beginning of the main text
         * that ensures no 2 lines can be the same. To do this, we use a unique color code for each line. This alters
         * the formatting of the line, so we then reset the formatting to that of the previous text as needed. This
         * leaves us with a unique id as follows:
         * Unique color code - 2 characters
         * Previous text color code - 2 characters
         * Previous text formatting codes x5 - 10 characters
         *
         * So, we reserve 14 characters for this identifier.
         */

        StringBuilder finalString = new StringBuilder();
        String prefix = null, main;

        // Separate the prefix from the rest of our text. We add the unique id between the prefix and everything else.
        if (entry.length() <= 26) main = entry;
        else {
            prefix = entry.substring(0, 16);
            main = entry.substring(16);
        }

        String colorCode = "§f";
        List<String> formattingCodes = new ArrayList<>();

        // If there is a prefix:
        if (prefix != null) {
            char[] prefixChars = prefix.toCharArray();
            // Find what color and formatting codes are used in the prefix. We will need to re-set them later.
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

            // Find out if a color/formatting code is split over the prefix -> name gap.
            if (prefix.charAt(prefix.length() - 1) == '§') {
                String code = "§" + main.charAt(0);
                boolean shrink = true;

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

                // If there is a split code, remove it from each line and we'll handle it in our unique identifier.
                if (shrink) {
                    prefix = prefix.substring(0, 15);
                    if (main.length() == 1) main = "";
                    else main = main.substring(0, main.length() - 1);
                }
            }
        }

        // Finally, build our final string with our unique identifier and all text.

        // First, add the prefix.
        if (prefix != null) finalString.append(prefix);
        // Then our unique color.
        finalString.append(color);
        // Fill any unused formatting code spots with reset codes.
        for (int i = 0; i < 5 - formattingCodes.size(); i++) finalString.append(LegacyScoreboard.RESET_CODE);
        // Then our appropriate color code.
        finalString.append(colorCode);
        // Then all of our formatting codes.
        for (String format : formattingCodes) finalString.append(format);
        // Then the rest of the text.
        finalString.append(main);

        return finalString.toString();
    }
}