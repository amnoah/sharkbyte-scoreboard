package sharkbyte.scoreboard.example;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import sharkbyte.scoreboard.core.Scoreboard;

/**
 * This class shows basic usage of the Scoreboard.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class PacketListener extends SimplePacketListenerAbstract {

    private Scoreboard scoreboard;

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Client.CHAT_MESSAGE)) return;

        if (scoreboard == null) {
            scoreboard = new Scoreboard(event.getUser(), "scoreboard", "test", true);
        }

        WrapperPlayClientChatMessage message = new WrapperPlayClientChatMessage(event);

        String[] elements = message.getMessage().split(" ", 2);
        int index;

        switch (elements[0]) {
            case "create":
                scoreboard.create();
                scoreboard.display();
                scoreboard.setLine(0, "hello world!");
                scoreboard.update();
                break;
            case "title":
                scoreboard.setTitle(elements[1]);
                scoreboard.update();
                break;
            case "destroy":
                scoreboard.destroy();
                break;
            default:
                try {
                    index = Integer.parseInt(elements[0]);
                } catch (Exception e) {
                    index = 0;
                }

                if (elements[1].equals("null")) scoreboard.setLine(index, null);
                else scoreboard.setLine(index, elements[1]);
                scoreboard.update();
        }
    }
}
