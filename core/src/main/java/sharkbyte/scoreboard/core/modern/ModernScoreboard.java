package sharkbyte.scoreboard.core.modern;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.kyori.adventure.text.Component;
import sharkbyte.scoreboard.core.SBScoreboard;
import sharkbyte.scoreboard.core.SBScoreboardEntry;

/**
 * This class represents and handles a scoreboard for a user on a 1.20.3+ server.
 * It is highly optimized and should not cause any performance issues.
 *
 * @Author: am noah
 * @Since: 2.0.0
 * @Updated: 2.0.0
 */
public class ModernScoreboard extends SBScoreboard {

    /**
     * Initialize the ModernScoreboard object.
     * The internalName can be unlimited characters.
     */
    public ModernScoreboard(User user, String internalName) {
        this(user, internalName, "");
    }

    /**
     * Initialize the ModernScoreboard object.
     * The internalName and title can be unlimited characters.
     */
    public ModernScoreboard(User user, String internalName, String title) {
        super(user, internalName, title);

        // We can have a maximum of 15 lines. Even if we don't actively use each line, we keep its object.
        for (int i = 0; i < 15; i++) entries[i] = new ModernScoreboardEntry(String.valueOf(i));
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

        user.sendPacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                Component.text(title),
                null
        ));

        changedTitle = false; // We just set the title while creating the scoreboard.
        created = true;
    }

    /**
     * Calling this method will unregister the scoreboard inside the client.
     */
    @Override
    public void destroy() {
        if (!created) return;

        user.sendPacket(new WrapperPlayServerScoreboardObjective(
                internalName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.text(title),
                null
        ));

        created = false;

        /*
         * If we don't do this, if the client tries to recreate the scoreboard it will think it had already updated the
         * text on the board and will not send it again.
         */
        for (SBScoreboardEntry entry : entries) {
            entry.setNameChanged(entry.getLeftDisplayName() != null || entry.getRightDisplayName() != null);
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

    /**
     * Calling this method will send out all appropriate packets to update the client's scoreboard.
     * Don't be afraid to call this often, it will only send packets when required.
     */
    @Override
    public void update() {
        if (!created) return;

        boolean updated = false;

        /*
         * Important note:
         * We do not send any packets until the update has completed. We write packets to the channel, flushing only
         * once all packets have been constructed! Flushing is an expensive operation and should only be done when
         * absolutely necessary.
         */

        for (int i = 0; i < 15; i++) {
            SBScoreboardEntry entry = entries[i];

            // We continue if the line has been modified or if the scoreboard is being modified.
            if (!entry.hasNameChanged()) continue;

            /*
             * If either display is not null, this line is intended to be modified on the scoreboard.
             * If both display names are null, this line is intended to be removed from the scoreboard.
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
            } else if (entry.hasNameChanged()) {
                // In 1.20.3 the ResetScore packet replaced the REMOVE action on the UpdateScore packet.
                user.writePacket(new WrapperPlayServerResetScore(
                        entry.getIdentifyingName(),
                        internalName
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
}