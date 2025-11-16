package sharkbyte.scoreboard.core.legacy;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import sharkbyte.scoreboard.core.SBScoreboard;
import sharkbyte.scoreboard.core.SBScoreboardEntry;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents and handles a scoreboard for a user on a 1.8 - 1.12.2 server.
 * Due to the inefficiency of legacy versions, many packets are required to be sent for minimal changes.
 *
 * @Author: am noah
 * @Since: 2.0.0
 * @Updated: 2.0.0
 */
public class LegacyScoreboard extends SBScoreboard {

    public final static String RESET_CODE = "§r";
    public final static List<String> COLOR_CODES = Arrays.asList(
            "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9",
            "§a", "§b", "§c", "§d", "§e", "§f"
    );
    public final static List<String> FORMATTING_CODES = Arrays.asList(
            "§k", "§l", "§m", "§n", "§o"
    );

    private boolean dangerMode = false;

    /**
     * Initialize the LegacyScoreboard object.
     * The internalName can be up to 16 characters.
     */
    public LegacyScoreboard(User user, String internalName) {
        this(user, internalName, "");
    }

    /**
     * Initialize the LegacyScoreboard object.
     * The internalName can be up to 16 characters.
     * The title can be up to 32 characters.
     */
    public LegacyScoreboard(User user, String internalName, String title) {
        super(user, internalName, title);
        super.internalName = internalName.length() > 16 ? internalName.substring(0, 16) : internalName;

        // We can have a maximum of 15 lines. Even if we don't actively use each line, we keep its object.
        for (int i = 0; i < 15; i++) entries[i] = new LegacyScoreboardEntry(COLOR_CODES.get(i));
    }

    /*
     * Setters.
     */

    /**
     * Set the danger mode status.
     * If danger mode is false, the maximum character count will be 58 but sharkbyte-scoreboard will prevent duplicate
     * lines from occurring. When a duplicate line occurs, one will be removed.
     * If danger mode is true, the maximum character count will be 72 but there will be no duplicate line protection. If
     * you enable danger mode and lines disappear from the board, YOU WILL RECEIVE NO SUPPORT.
     */
    public void setDangerMode(boolean dangerMode) {
        this.dangerMode = dangerMode;
        for (SBScoreboardEntry entry : entries) ((LegacyScoreboardEntry) entry).setDangerMode(dangerMode);
    }

    /**
     * This will only display up to 58 characters (72 on danger mode). All others will be removed.
     * Sets the left-aligned text for the given line to the given text.
     * Setting the value to null will remove the line from the scoreboard. Note that if all lines are set to null the
     * board will disappear.
     * Valid indices: 0-14.
     */
    @Override
    public void setLeftAlignedText(int index, String text) {
        // Ensure we do not exceed the maximum legacy character support.
        if (text != null && text.length() > (dangerMode ? 72 : 58)) text = text.substring(0, (dangerMode ? 72 : 58));
        super.setLeftAlignedText(index, text);
    }

    /**
     * This will only display up to 32 characters, all others will be removed.
     * Set the scoreboard's title.
     */
    @Override
    public void setTitle(@NotNull String title) {
        if (title.length() > 32) title = title.substring(0, 32);
        super.setTitle(title);
    }

    /*
     * Scoreboard Handlers.
     */

    /**
     * Calling this method will register the scoreboard inside the client.
     */
    @Override
    public void create() {
        if (created) return;

        user.writePacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                Component.text(title),
                null
        ));

        // Register teams for usage in displays.
        // We use the prefix/suffix options to display extra content.
        for (int i = 0; i < 15; i++) {
            user.writePacket(new WrapperPlayServerTeams(
                    (internalName + (15 - i)),
                    WrapperPlayServerTeams.TeamMode.CREATE,
                    new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                            Component.text("Display" + (15 - i)),
                            Component.text(""),
                            Component.text(""),
                            // Dummy data from here down.
                            WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                            WrapperPlayServerTeams.CollisionRule.ALWAYS,
                            NamedTextColor.WHITE,
                            WrapperPlayServerTeams.OptionData.ALL
                    )
            ));
        }

        user.flushPackets();

        changedTitle = false; // We just set the title while creating the scoreboard.
        created = true;
    }

    /**
     * Calling this method will unregister the scoreboard inside the client.
     */
    @Override
    public void destroy() {
        if (!created) return;

        user.writePacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.text(title),
                null
        ));

        // Also remove all the teams we created.
        for (int i = 0; i < 15; i++) {
            user.writePacket(new WrapperPlayServerTeams(
                    (internalName + (15 - i)),
                    WrapperPlayServerTeams.TeamMode.REMOVE,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null
            ));
        }

        user.flushPackets();
        created = false;

        /*
         * If we don't do this, if the client tries to recreate the scoreboard it will think it had already updated the
         * text on the board and will not send it again.
         */
        for (SBScoreboardEntry entry : entries) {
            entry.setNameChanged(entry.getLeftDisplayName() != null);
            entry.setIdentifyingName(null);
        }
    }

    /**
     * Calling this method will set this scoreboard as the client's active scoreboard.
     * You must have a line set on the board for this to work.
     */
    @Override
    public void display() {
        if (!created) return;

        user.sendPacket(new WrapperPlayServerDisplayScoreboard(
                1,
                internalName
        ));
    }

    /**
     * Calling this method will send out all appropriate packets to update the client's scoreboard.
     * Don't be afraid to call this often, it is highly optimized to only send packets when necessary.
     */
    @Override
    public void update() {
        if (!created) return;

        /*
         * 1.8-1.12.2 has some really interesting scoreboard mechanics.
         *
         * On a display, for each line there are 3 noteworthy things:
         * Team Prefix: 16 characters, placed before a player's name.
         * Player Name: 40 characters, cannot have duplicates.
         * Team Suffix: 16 characters, placed after a player's name.
         *
         * If two player names are identical, the previously added one will be removed.
         *
         * Theoretically, we could have 72 characters by manipulating a combinations of these - but to ensure we
         * automate things as much as possible, sharkbyte-scoreboard automatically adds unique identifiers to ensure
         * no duplicate lines can happen. These utilize color codes, which are invisible to the end client.
         *
         * These identifiers work like this:
         * Unique color code (2): Each line has a separate color code that only it can have. No 2 lines can be the same.
         * Color code (2): Preserve the previously set color of the line
         * Formatting codes (10): Preserve the previously set formatting of the line
         *
         * This reserves 14 characters, but ensures no duplicate player names are added onto the board. All logic is
         * handled in the LegacyScoreboardEntry class.
         */

        boolean updated = false;

        for (int i = 0; i < 15; i++) {
            SBScoreboardEntry entry = entries[i];

            // Here we support versions before the 1.20.3 UpdateScore rewrite.
            if (!entry.hasNameChanged()) continue;

            // If identifyingName isn't null then it is a line that has to be removed from the board.
            if (entry.getIdentifyingName() != null) {
                // Layout: (0, prefix), (1, name), (2, suffix)
                String[] strings = separateStrings(entry.getIdentifyingName());

                user.writePacket(new WrapperPlayServerUpdateScore(
                        strings[1],
                        WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                        internalName,
                        15 - i,
                        null,
                        null
                ));

                // If the prefix isn't null, this line used team packets.
                if (strings[0] != null) {
                    // If the next section of this update method isn't going to modify the team:
                    if (entry.getLeftDisplayName() == null || (entry.getLeftDisplayName() != null && entry.getLeftDisplayName().length() <= 40)) {
                        // Reset prefix and suffix to prevent text from being kept on future changes.
                        user.writePacket(new WrapperPlayServerTeams(
                                (internalName + (15 - i)),
                                WrapperPlayServerTeams.TeamMode.UPDATE,
                                new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                                        Component.text("Display" + (15 - i)),
                                        Component.text(""),
                                        Component.text(""),
                                        // Dummy data from here down.
                                        WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                                        WrapperPlayServerTeams.CollisionRule.ALWAYS,
                                        NamedTextColor.WHITE,
                                        WrapperPlayServerTeams.OptionData.ALL
                                )
                        ));
                    }

                    // And remove this "player" from the team.
                    user.writePacket(new WrapperPlayServerTeams(
                            (internalName + (15 - i)),
                            WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                            (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                            strings[1]
                    ));
                }

                updated = true;
            }

            // If displayName isn't null then it is a line that has to be added to the board.
            if (entry.getLeftDisplayName() != null) {
                // Layout: (0, prefix), (1, name), (2, suffix)
                String[] strings = separateStrings(entry.getLeftDisplayName());

                // If the line is long enough to warrant using a team
                if (strings[0] != null) {
                    // Write line info to the team
                    user.writePacket(new WrapperPlayServerTeams(
                            (internalName + (15 - i)),
                            WrapperPlayServerTeams.TeamMode.UPDATE,
                            new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                                    Component.text("Display" + (15 - i)),
                                    Component.text(strings[0]),
                                    Component.text(strings[2]),
                                    // Dummy data from here down.
                                    WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                                    WrapperPlayServerTeams.CollisionRule.ALWAYS,
                                    NamedTextColor.WHITE,
                                    WrapperPlayServerTeams.OptionData.ALL
                            )
                    ));

                    // Write the "player" to the team.
                    user.writePacket(new WrapperPlayServerTeams(
                            (internalName + (15 - i)),
                            WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                            (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                            strings[1]
                    ));
                }

                // Write the "player" to the scoreboard.
                user.writePacket(new WrapperPlayServerUpdateScore(
                        strings[1],
                        WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                        internalName,
                        15 - i,
                        null,
                        null
                ));

                updated = true;
            }

            entry.update();
        }

        // If the title has been changed then update the scoreboard itself.
        if (changedTitle) {
            user.writePacket(new WrapperPlayServerScoreboardObjective(
                    internalName,
                    WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE,
                    Component.text(title),
                    null
            ));

            updated = true;
        }

        // Finally, send all packets to the player!
        if (updated) user.flushPackets();
    }

    /**
     * This function rips apart the line into three strings; prefix, main, and suffix.
     * Prefix can be null, suffix will either be empty or have content, and main will always have content.
     */
    private String[] separateStrings(String entry) {

        /*
         * Prefix - 16 characters
         * Name - 40 characters
         * Suffix - 16 characters
         *
         * Prioritize using name, and then add on prefix and suffix as needed.
         */

        String prefix = null, main, suffix = "";
        if (entry.length() <= 40) main = entry;
        else if (entry.length() <= 56) {
            prefix = entry.substring(0, 16);
            main = entry.substring(16);
        } else {
            prefix = entry.substring(0, 16);
            main = entry.substring(16, 56);
            suffix = entry.substring(56);
        }

        return new String[]{prefix, main, suffix};
    }
}