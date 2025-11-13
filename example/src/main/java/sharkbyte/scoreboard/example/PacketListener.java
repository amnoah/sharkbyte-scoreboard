package sharkbyte.scoreboard.example;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import org.bukkit.ChatColor;
import sharkbyte.scoreboard.core.SBScoreboard;

/**
 * This class shows basic usage of the Scoreboard.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.1.0
 */
public class PacketListener extends SimplePacketListenerAbstract {

    private SBScoreboard scoreboard;

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Client.CHAT_MESSAGE)) return;

        if (scoreboard == null) {
            scoreboard = SBScoreboard.createScoreboard(event.getUser(), "scoreboard", "test");
        }

        WrapperPlayClientChatMessage message = new WrapperPlayClientChatMessage(event);

        String[] elements = message.getMessage().split(" ", 3);
        int index;

        switch (elements[0]) {
            case "create":
                scoreboard.create();
                scoreboard.display();
                scoreboard.setLeftAlignedText(0, "hello world!");
                scoreboard.update();
                break;
            case "title":
                scoreboard.setTitle(elements.length == 2 ? ChatColor.translateAlternateColorCodes('&', elements[1]) : ChatColor.translateAlternateColorCodes('&', elements[1] + " " + elements[2]));
                scoreboard.update();
                break;
            case "destroy":
                scoreboard.destroy();
                break;
            case "left":
                try {
                    index = Integer.parseInt(elements[1]);
                } catch (Exception e) {
                    index = 0;
                }

                if (elements[2].equals("null")) scoreboard.setLeftAlignedText(index, null);
                else scoreboard.setLeftAlignedText(index, ChatColor.translateAlternateColorCodes('&', elements[2]));
                scoreboard.update();

                break;
            case "right":
                try {
                    index = Integer.parseInt(elements[1]);
                } catch (Exception e) {
                    index = 0;
                }

                if (elements[2].equals("null")) scoreboard.setRightAlignedText(index, null);
                else scoreboard.setRightAlignedText(index, ChatColor.translateAlternateColorCodes('&', elements[2]));
                scoreboard.update();

                break;
        }
    }
}
