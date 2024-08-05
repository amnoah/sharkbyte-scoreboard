package sharkbyte.scoreboard.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import net.kyori.adventure.text.Component;

/**
 * This class represents and handles a scoreboard for a user.
 * This implementation is meant to be basic, not ideal. In 1.8 - 1.20.2, it is likely better to use team packets for
 * scoreboards.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class Scoreboard {

    private final ScoreboardEntry[] entries = new ScoreboardEntry[15];
    private final String internalName;

    private final User user;
    private String title;
    private boolean changedTitle, changedOther, showNumbers;

    /**
     * Initialize the Scoreboard object.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     */
    public Scoreboard(User user, String internalName) {
        this(user, internalName, "", false);
    }

    /**
     * Initialize the Scoreboard object.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     * The showNumbers setting only has functionality in 1.20.3+.
     */
    public Scoreboard(User user, String internalName, String title, boolean showNumbers) {
        this.user = user;
        this.internalName = internalName;
        this.title = title;
        this.showNumbers = showNumbers;

        for (int i = 0; i < 15; i++) entries[i] = new ScoreboardEntry(String.valueOf(i));
    }

    /*
     * Setters.
     */

    /**
     * Sets the inputted line as the inputted text.
     * Setting the value to null will remove the line from the scoreboard.
     * Valid indices: 0-14.
     * Keep in mind, 1.8 - 1.17.2 will only display 40 characters.
     */
    public void setLine(int index, String line) {
        entries[index].updateDisplayName(line);
    }

    /**
     * Set whether the score numbers should be shown alongside text.
     * Only works on 1.20.3+.
     */
    public void setShowNumbers(boolean showNumbers) {
        if (this.showNumbers == showNumbers) return;
        this.showNumbers = showNumbers;
        changedOther = true;
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
     * Avoid calling this method multiple times without destroying the scoreboard as some versions will disconnect if
     * an objective is registered multiple times.
     */
    public void create() {
        user.sendPacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                Component.text(title),
                null
        ));
    }

    /**
     * Calling this method will unregister the scoreboard inside the client.
     * Avoid calling this unless the create void has already been called.
     */
    public void destroy() {
        user.sendPacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.text(title),
                null
        ));

        for (ScoreboardEntry entry : entries) {
            entry.setNameChanged(entry.getDisplayName() != null);
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
                if (!changedOther && !entry.hasNameChanged()) continue;

                /*
                 * If displayName is null, this line is intended to be removed from the scoreboard.
                 * If display is not null, this line is intended to be modified on the scoreboard.
                 */
                if (entry.getDisplayName() != null) {
                    user.writePacket(new WrapperPlayServerUpdateScore(
                            entry.getIdentifyingName(),
                            WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                            internalName,
                            15 - i,
                            Component.text(entry.getDisplayName()),
                            showNumbers ? null : ScoreFormat.fixedScore(Component.text(""))
                    ));
                // In 1.20.3 the ResetScore packet replaced the REMOVE action on the UpdateScore packet.
                } else if (entry.hasNameChanged()) {
                    user.writePacket(new WrapperPlayServerResetScore(
                            entry.getIdentifyingName(), internalName
                    ));
                }
            // Here we support versions before the 1.20.3 UpdateScore rewrite.
            } else {
                if (!entry.hasNameChanged()) continue;

                // If identifyingName isn't null then it is a line that has to be removed from the board.
                if (entry.getIdentifyingName() != null) {
                    user.writePacket(new WrapperPlayServerUpdateScore(
                            entry.getIdentifyingName(),
                            WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                            internalName,
                            15 - i,
                            null,
                            null
                    ));
                }

                // If displayName isn't null then it is a line that has to be added to the board.
                if (entry.getDisplayName() != null) {
                    user.writePacket(new WrapperPlayServerUpdateScore(
                            entry.getDisplayName(),
                            WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                            internalName,
                            15 - i,
                            null,
                            null
                    ));
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
        }

        // Finally, send all packets to the player!
        user.flushPackets();
    }
}
