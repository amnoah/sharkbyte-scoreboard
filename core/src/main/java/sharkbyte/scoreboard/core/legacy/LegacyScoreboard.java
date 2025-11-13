package sharkbyte.scoreboard.core.legacy;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents and handles a scoreboard for a user on a 1.8 - 1.20.2 server.
 * Due to the inefficiency of legacy versions, many packets are required to be sent for minimal
 *
 * @Author: am noah
 * @Since: 2.0.0
 * @Updated: 2.0.0
 */
public class LegacyScoreboard extends SBScoreboard {

    private final static String RESET_CODE = "§r";
    private final static List<String> COLOR_CODES = Arrays.asList(
            "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9",
            "§a", "§b", "§c", "§d", "§e", "§f"
    );
    private final static List<String> FORMATTING_CODES = Arrays.asList(
            "§k", "§l", "§m", "§n", "§o"
    );

    private final boolean thirteen = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13);
    private final boolean eighteen = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_18);
    private boolean dangerMode = false;

    /**
     * Initialize the LegacyScoreboard object.
     * The internalName can be up to 16 characters on [1.8, 1.18).
     */
    public LegacyScoreboard(User user, String internalName) {
        this(user, internalName, "");
    }

    /**
     * Initialize the LegacyScoreboard object.
     * The internalName can be up to 16 characters on [1.8, 1.18), and unlimited on [1.18, 1.20.2].
     * The title can be up to 32 characters on [1.8, 1.13), and unlimited on [1.13, 1.20.2].
     */
    public LegacyScoreboard(User user, String internalName, String title) {
        super(user, internalName, title);
        if (!eighteen) super.internalName = internalName.length() > 16 ? internalName.substring(0, 16) : internalName;
    }

    /*
     * Setters.
     */

    /**
     * In the versions [1.8, 1.13), the client has a theoretical maximum display length of 72 characters. 40 of these
     * characters refer to a player's name, which unfortunately has the limitation that only one of each player can
     * exist on the board. We get around this by hiding color codes in the player name to differentiate them, but this
     * reserves 16 characters - leaving 56 for actual usage. By setting danger mode to true, you disable this reserving
     * process - gaining you 16 extra characters per line but requiring you to make sure no lines have identical
     * content.
     */
    public void setDangerMode(boolean dangerMode) {
        this.dangerMode = dangerMode;
    }

    /**
     * This will only display up to 72 characters. All others will be removed.
     * Sets the left-aligned text for the given line to the given text.
     * Setting the value to null will remove the line from the scoreboard.
     * Valid indices: 0-14.
     */
    @Override
    public void setLeftAlignedText(int index, String text) {
        // Ensure we do not exceed the maximum legacy character support.
        if (text != null && text.length() > 72) text = text.substring(0, 72);
        super.setLeftAlignedText(index, text);
    }

    /**
     * This will only display up to 32 characters on [1.8, 1.13). All others will be removed.
     * Set the scoreboard's title.
     */
    @Override
    public void setTitle(@NotNull String title) {
        if (!thirteen && title.length() > 32) title = title.substring(0, 32);
        super.setTitle(title);
    }

    /*
     * Scoreboard Handlers.
     * TODO: Handle 1.18 unlimited characters
     * TODO: Reserve 4 (maybe 2?) characters on player names to make hidden color codes to differentiate them.
     * Notes:
     * - Internal Name became unlimited in 1.18
     * - Title became unlimited in 1.13
     * - Player names on score became unlimited in 1.18
     * - Team prefix/suffix became unlimited in 1.13
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
                            NamedTextColor.BLACK,
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
     * Some versions may require a line to be set to display.
     */
    @Override
    public void display() {
        if (!created) return;

        user.sendPacket(new WrapperPlayServerDisplayScoreboard(
                1,
                internalName
        ));
    }

    @Override
    public void update() {
        if (!created) return;

        if (dangerMode) updateDangerous();
        else updateSafe();
    }

    private void updateDangerous() {
        boolean updated = false;

        for (int i = 0; i < 15; i++) {
            SBScoreboardEntry entry = entries[i];

            // Here we support versions before the 1.20.3 UpdateScore rewrite.
            if (!entry.hasNameChanged()) continue;

            // If identifyingName isn't null then it is a line that has to be removed from the board.
            if (entry.getIdentifyingName() != null) {
                String main;
                if (entry.getIdentifyingName().length() <= 40) main = entry.getIdentifyingName();
                else if (entry.getIdentifyingName().length() <= 56) main = entry.getIdentifyingName().substring(entry.getIdentifyingName().length() - 40);
                else main = entry.getIdentifyingName().substring(16, 56);

                user.writePacket(new WrapperPlayServerUpdateScore(
                        main,
                        WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                        internalName,
                        15 - i,
                        null,
                        null
                ));

                if (entry.getIdentifyingName().length() <= 24) {
                    if (entry.getLeftDisplayName() == null || (entry.getLeftDisplayName() != null && entry.getLeftDisplayName().length() <= 40)) {
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
                                        NamedTextColor.BLACK,
                                        WrapperPlayServerTeams.OptionData.ALL
                                )
                        ));
                    }

                    user.writePacket(new WrapperPlayServerTeams(
                            (internalName + (15 - i)),
                            WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                            (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                            main
                    ));
                }

                updated = true;
            }

            // If displayName isn't null then it is a line that has to be added to the board.
            if (entry.getLeftDisplayName() != null) {
                String prefix = "", main, suffix = "";

                if (entry.getLeftDisplayName().length() <= 40) {
                    main = entry.getLeftDisplayName();
                } else if (entry.getLeftDisplayName().length() <= 56) {
                    prefix = entry.getLeftDisplayName().substring(0, 56 - entry.getLeftDisplayName().length());
                    main = entry.getLeftDisplayName().substring(56 - entry.getLeftDisplayName().length());
                } else {
                    prefix = entry.getLeftDisplayName().substring(0, 16);
                    main = entry.getLeftDisplayName().substring(16, 56);
                    suffix = entry.getLeftDisplayName().substring(56);
                }

                if (!prefix.isEmpty()) {
                    user.writePacket(new WrapperPlayServerTeams(
                            (internalName + (15 - i)),
                            WrapperPlayServerTeams.TeamMode.UPDATE,
                            new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                                    Component.text("Display" + (15 - i)),
                                    Component.text(prefix),
                                    Component.text(suffix),
                                    // Dummy data from here down.
                                    WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                                    WrapperPlayServerTeams.CollisionRule.ALWAYS,
                                    NamedTextColor.BLACK,
                                    WrapperPlayServerTeams.OptionData.ALL
                            )
                    ));

                    user.writePacket(new WrapperPlayServerTeams(
                            (internalName + (15 - i)),
                            WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                            (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                            main
                    ));
                }

                user.writePacket(new WrapperPlayServerUpdateScore(
                        main,
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

    private void updateSafe() {
        boolean updated = false;

        for (int i = 0; i < 15; i++) {
            SBScoreboardEntry entry = entries[i];

            // Here we support versions before the 1.20.3 UpdateScore rewrite.
            if (!entry.hasNameChanged()) continue;

            // If identifyingName isn't null then it is a line that has to be removed from the board.
            if (entry.getIdentifyingName() != null) {
                String[] strings = parseStrings(entry.getIdentifyingName(), COLOR_CODES.get(15 - i));

                user.writePacket(new WrapperPlayServerUpdateScore(
                        strings[1],
                        WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                        internalName,
                        15 - i,
                        null,
                        null
                ));

                if (strings[0] != null) {
                    if (entry.getLeftDisplayName() == null || (entry.getLeftDisplayName() != null && entry.getLeftDisplayName().length() <= 40)) {
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
                                        NamedTextColor.BLACK,
                                        WrapperPlayServerTeams.OptionData.ALL
                                )
                        ));
                    }

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
                String[] strings = parseStrings(entry.getLeftDisplayName(), COLOR_CODES.get(15 - i));

                if (strings[0] != null) {
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
                                    NamedTextColor.BLACK,
                                    WrapperPlayServerTeams.OptionData.ALL
                            )
                    ));

                    user.writePacket(new WrapperPlayServerTeams(
                            (internalName + (15 - i)),
                            WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                            (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                            strings[1]
                    ));
                }

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

    private String[] parseStrings(String entry, String unique) {
        String prefix = null, main, suffix = "";

        if (entry.length() <= 24) main = entry;
        else if (entry.length() <= 40) {
            prefix = entry.substring(0, 16);
            main = entry.substring(16);
        } else {
            prefix = entry.substring(0, 16);
            main = entry.substring(16, 40);
            suffix = entry.substring(40);
        }

        String colorCode = "§f";
        List<String> formattingCodes = new ArrayList<>();
        boolean shrink = false;

        if (prefix != null) {
            char[] prefixChars = prefix.toCharArray();
            for (int i = 0; i < prefixChars.length - 1; i++) {
                if (prefixChars[i] == '§') {
                    String code = "§" + prefixChars[i + 1];
                    if (code.equals(RESET_CODE)) {
                        formattingCodes.clear();
                        colorCode = "§f";
                    } else if (FORMATTING_CODES.contains(code)) {
                        formattingCodes.remove(code);
                        formattingCodes.add(code);
                    } else if (COLOR_CODES.contains(code)) {
                        colorCode = code;
                    }
                }
            }

            if (prefix.charAt(prefix.length() - 1) == '§') {
                String code = "§" + main.charAt(0);
                shrink = true;
                if (code.equals(RESET_CODE)) {
                    formattingCodes.clear();
                    colorCode = "§f";
                } else if (FORMATTING_CODES.contains(code)) {
                    formattingCodes.remove(code);
                    formattingCodes.add(code);
                } else if (COLOR_CODES.contains(code)) {
                    colorCode = code;
                } else shrink = false;
            }
        }

        if (shrink) {
            prefix = prefix.substring(0, 15);
            if (main.length() == 1) main = "";
            else main = main.substring(0, main.length() - 1);
        }

        // Format: unique -> &r -> formatting options (5) -> color.
        // If there are less than 5 formatting options, &r will be repeated.
        StringBuilder uniqueMain = new StringBuilder(unique + "§r");
        for (int i = 0; i < 5 - formattingCodes.size(); i++) uniqueMain.append("§r");
        for (String format : formattingCodes) uniqueMain.append(format);
        uniqueMain.append(colorCode);
        uniqueMain.append(main);

        return new String[]{prefix, uniqueMain.toString(), suffix};
    }
}