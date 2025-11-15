package sharkbyte.scoreboard.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;
import sharkbyte.scoreboard.core.legacy.LegacyScoreboard;
import sharkbyte.scoreboard.core.legacythirteen.Legacy13Scoreboard;
import sharkbyte.scoreboard.core.modern.ModernScoreboard;

import java.security.InvalidParameterException;

public abstract class SBScoreboard {

    protected final SBScoreboardEntry[] entries = new SBScoreboardEntry[15];
    protected String internalName; // Only modify this variable if you know what you're doing.

    protected final User user;
    protected String title = "";
    protected boolean created = false, changedTitle = false;

    /**
     * Create a version appropriate SBScoreboard.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     */
    public static SBScoreboard createScoreboard(User user, String internalName) {
        return createScoreboard(user, internalName, "");
    }

    /**
     * Create a version appropriate SBScoreboard.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     *
     */
    public static SBScoreboard createScoreboard(User user, String internalName, String title) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_3)) {
            return new ModernScoreboard(user, internalName, title);
        } else if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
            return new Legacy13Scoreboard(user, internalName, title);
        } else return new LegacyScoreboard(user, internalName, title);
    }

    /**
     * Initialize the Scoreboard object.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     */
    public SBScoreboard(User user, String internalName) {
        this(user, internalName, "");
    }

    /**
     * Initialize the Scoreboard object.
     * The internalName should be up to 16 characters in 1.8-1.17.2, or unlimited length in 1.18+.
     */
    public SBScoreboard(User user, String internalName, String title) {
        this.user = user;
        this.internalName = internalName;
        setTitle(title);
        created = false;
    }

    /*
     * Setters.
     */

    /**
     * This is a feature on all versions, but 1.8-1.20.2 will only display 72 characters.
     * Sets the left-aligned text for the given line to the given text.
     * Setting the value to null will remove the line from the scoreboard.
     * Valid indices: 0-14.
     */
    public void setLeftAlignedText(int index, String text) {
        entries[index].updateLeftAlignedText(text);
    }

    /**
     * This only a feature on 1.20.3+ servers.
     * Sets the right-aligned text for the given line to the given text.
     * Setting the value to null will remove the line from the scoreboard.
     * Valid indices: 0-14.
     */
    public void setRightAlignedText(int index, String text) {
        entries[index].updateRightAlignedText(text);
    }

    /**
     * This is a feature on all versions, but 1.8-1.20.2 will only display 32 characters.
     * Set the scoreboard's title.
     */
    public void setTitle(@NotNull String title) {
        if (this.title.equals(title)) return;
        this.title = title;
        changedTitle = true;
    }

    /*
     * Scoreboard Handlers.
     */

    /**
     * Calling this method will register the scoreboard inside the client.
     * This does NOT display a scoreboard, for that you must call SBScoreboard#display().
     */
    public abstract void create();

    /**
     * Calling this method will unregister the scoreboard inside the client.
     * If you are done with the scoreboard, CALL THIS METHOD. It will both hide this scoreboard from the client and
     * remove everything associated with the scoreboard from the client's memory.
     */
    public abstract void destroy();

    /**
     * Calling this method will set this scoreboard as the client's active scoreboard.
     * Some versions may require a line to be set to display on the client.
     */
    public abstract void display();

    /**
     * Calling this method will send out all appropriate packets to update the client's scoreboard.
     * Don't be afraid to call this often, it is highly optimized to only send packets when necessary.
     */
    public abstract void update();
}