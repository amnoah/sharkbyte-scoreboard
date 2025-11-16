package sharkbyte.scoreboard.core.legacythirteen;

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
import sharkbyte.scoreboard.core.modern.ModernScoreboardEntry;

public class Legacy13Scoreboard extends SBScoreboard {

    private final boolean eighteen = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_18);

    // Yes, I'm aware that this isn't a full list of color codes. We only need 15.
    private final static String[] COLOR_CODES = {
            "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9",
            "§a", "§b", "§c", "§d", "§e"
    };

    /**
     * Initialize the Legacy13Scoreboard object.
     * The internalName can be up to 16 characters on [1.13, 1.18), and unlimited on [1.18, 1.20.2].
     */
    public Legacy13Scoreboard(User user, String internalName) {
        this(user, internalName, "");
    }

    /**
     * Initialize the LegacyScoreboard object.
     * The internalName can be up to 16 characters on [1.13, 1.18), and unlimited on [1.18, 1.20.2].
     * The title can be unlimited characters.
     */
    public Legacy13Scoreboard(User user, String internalName, String title) {
        super(user, internalName, title);
        if (!eighteen) super.internalName = internalName.length() > 16 ? internalName.substring(0, 16) : internalName;
        for (int i = 0; i < 15; i++) entries[i] = new ModernScoreboardEntry(null);
    }

    /*
     * Setters.
     */

    /**
     * Sets the left-aligned text for the given line to the given text.
     * Setting the value to null will remove the line from the scoreboard.
     * Valid indices: 0-14.
     */
    @Override
    public void setLeftAlignedText(int index, String text) {
        super.setLeftAlignedText(index, text);
    }

    /**
     * This will only display up to 32 characters on [1.8, 1.13). All others will be removed.
     * Set the scoreboard's title.
     */
    @Override
    public void setTitle(@NotNull String title) {
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
                            NamedTextColor.WHITE,
                            WrapperPlayServerTeams.OptionData.ALL
                    )
            ));

            user.writePacket(new WrapperPlayServerTeams(
                    (internalName + (15 - i)),
                    WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                    COLOR_CODES[i]
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

        boolean updated = false;

        for (int i = 0; i < 15; i++) {
            SBScoreboardEntry entry = entries[i];

            if (!entry.hasNameChanged()) continue;

            if (entry.getLeftDisplayName() != null) {
                if (entry.getIdentifyingName() == null) {
                    user.writePacket(new WrapperPlayServerUpdateScore(
                            COLOR_CODES[i],
                            WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                            internalName,
                            15 - i,
                            null,
                            null
                    ));

                    entry.setIdentifyingName("");
                }

                user.writePacket(new WrapperPlayServerTeams(
                        (internalName + (15 - i)),
                        WrapperPlayServerTeams.TeamMode.UPDATE,
                        new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                                Component.text("Display" + (15 - i)),
                                Component.text(entry.getLeftDisplayName()),
                                Component.empty(),
                                // Dummy data from here down.
                                WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                                NamedTextColor.WHITE,
                                WrapperPlayServerTeams.OptionData.ALL
                        )
                ));

            } else {
                if (entry.getIdentifyingName() != null) {
                    user.writePacket(new WrapperPlayServerUpdateScore(
                            COLOR_CODES[i],
                            WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                            internalName,
                            15 - i,
                            null,
                            null
                    ));
                    entry.setIdentifyingName(null);
                }
            }

            updated = true;
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
}