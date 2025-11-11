package sharkbyte.scoreboard.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This class represents and handles a scoreboard for a user.
 * This implementation is meant to be basic, not ideal. In 1.8 - 1.20.2, it is likely better to use team packets for
 * scoreboards.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.1.0
 */
public class Scoreboard {

    private final ScoreboardEntry[] entries = new ScoreboardEntry[15];
    private final String internalName;

    private final User user;
    private String title;
    private boolean created, changedTitle;

    /**
     * Initialize the Scoreboard object.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     */
    public Scoreboard(User user, String internalName) {
        this(user, internalName, "");
    }

    /**
     * Initialize the Scoreboard object.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     * The showNumbers setting only has functionality in 1.20.3+.
     */
    public Scoreboard(User user, String internalName, String title) {
        this.user = user;
        this.internalName = internalName;
        this.title = title;
        created = false;

        // We can have a maximum of 15 lines. Even if we don't actively use each line, we keep its object.
        for (int i = 0; i < 15; i++) entries[i] = new ScoreboardEntry(String.valueOf(i));
    }

    /*
     * Setters.
     */

    /**
     * Sets the left-aligned text for the given line to the given text.
     * Setting the value to null will remove the line from the scoreboard.
     * Valid indices: 0-14.
     * Keep in mind, 1.8 - 1.17.2 will only display 40 characters.
     */
    public void setLeftAlignedText(int index, String text) {
        entries[index].updateLeftAlignedText(text);
    }

    /**
     * Sets the right-aligned text for the given line to the given text.
     * Setting the value to null will remove the line from the scoreboard.
     * Valid indices: 0-14.
     */
    public void setRightAlignedText(int index, String text) {
        entries[index].updateRightAlignedText(text);
    }

    /**
     * Set the scoreboard's title.
     */
    public void setTitle(String title) {
        if (this.title.equals(title)) return;
        this.title = title;
        changedTitle = true;
    }

    /*
     * Scoreboard Handlers.
     */

    /**
     * Calling this method will register the scoreboard inside the client.
     */
    public void create() {
        if (created) return;

        user.writePacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                Component.text(title),
                null
        ));

        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_20_3)) {
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
        }

        user.flushPackets();

        changedTitle = false; // We just set the title while creating the scoreboard.
        created = true;
    }

    /**
     * Calling this method will unregister the scoreboard inside the client.
     */
    public void destroy() {
        if (!created) return;

        user.writePacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.text(title),
                null
        ));

        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_20_3)) {
            for (int i = 0; i < 15; i++) {
                user.writePacket(new WrapperPlayServerTeams(
                        (internalName + (15 - i)),
                        WrapperPlayServerTeams.TeamMode.REMOVE,
                        (WrapperPlayServerTeams.ScoreBoardTeamInfo) null
                ));
            }
        }

        user.flushPackets();

        created = false;

        for (ScoreboardEntry entry : entries) {
            entry.setNameChanged(entry.getLeftDisplayName() != null || entry.getRightDisplayName() != null);
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_20_3)) {
                entry.setIdentifyingName(null);
            }
        }
    }

    /**
     * Calling this method will set this scoreboard as the client's active scoreboard.
     * Some versions may require a line to be set to display.
     */
    public void display() {
        if (!created) return;

        user.sendPacket(new WrapperPlayServerDisplayScoreboard(
                1,
                internalName
        ));
    }

    /**
     * Calling this method will send out all appropriate packets to update the client's scoreboard.
     * Don't be afraid to call this often, it will only send packets when required.
     */
    public void update() {
        if (!created) return;

        boolean updated = false;

        /*
         * Important note:
         * We do not send any packets until the update has completed.
         * We write packets to the channel, flushing only once all packets have been constructed!
         */

        for (int i = 0; i < 15; i++) {
            ScoreboardEntry entry = entries[i];

            // Because the UpdateScore packet was rewritten in 1.20.3, we have separate functionality for it.
            if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_3)) {
                // We continue if the line has been modified or if the scoreboard is being modified.
                if (!entry.hasNameChanged()) continue;

                /*
                 * If displayName is null, this line is intended to be removed from the scoreboard.
                 * If display is not null, this line is intended to be modified on the scoreboard.
                 */
                if (entry.getLeftDisplayName() != null || entry.getRightDisplayName() != null) {
                    String left = entry.getLeftDisplayName(), right = entry.getRightDisplayName();

                    user.writePacket(new WrapperPlayServerUpdateScore(
                            entry.getIdentifyingName(),
                            WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                            internalName,
                            15 - i,
                            left == null ? Component.empty() : Component.text(left),
                            ScoreFormat.fixedScore(right == null ? Component.empty() : Component.text(right))
                    ));

                    updated = true;
                // In 1.20.3 the ResetScore packet replaced the REMOVE action on the UpdateScore packet.
                } else if (entry.hasNameChanged()) {
                    user.writePacket(new WrapperPlayServerResetScore(
                            entry.getIdentifyingName(),
                            internalName
                    ));

                    updated = true;
                }
            } else {
                // Here we support versions before the 1.20.3 UpdateScore rewrite.
                if (!entry.hasNameChanged()) continue;

                // If identifyingName isn't null then it is a line that has to be removed from the board.
                if (entry.getIdentifyingName() != null) {
                    boolean usedTeam = entry.getIdentifyingName().length() > 40;
                    String main;

                    if (!usedTeam) main = entry.getIdentifyingName();
                    else if (entry.getIdentifyingName().length() < 56) {
                        main = entry.getIdentifyingName().substring(56 - entry.getIdentifyingName().length());
                    } else {
                        main = entry.getIdentifyingName().substring(16, 56);
                    }

                    user.writePacket(new WrapperPlayServerUpdateScore(
                            main,
                            WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                            internalName,
                            15 - i,
                            null,
                            null
                    ));

                    if (usedTeam) {
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

                    System.out.println(prefix.length() + " prefix: " + prefix);
                    System.out.println(main.length() + " main: " + main);
                    System.out.println(suffix.length() + " suffix: " + suffix);

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
}
