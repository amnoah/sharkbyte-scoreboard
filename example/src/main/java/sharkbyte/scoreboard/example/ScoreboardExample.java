package sharkbyte.scoreboard.example;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Just a simple JavaPlugin class.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class ScoreboardExample extends JavaPlugin {

    @Override
    public void onEnable() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
    }
}
