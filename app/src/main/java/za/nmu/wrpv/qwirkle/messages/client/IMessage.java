package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.MessagesAdapter;
import za.nmu.wrpv.qwirkle.MessagesFragment;
import za.nmu.wrpv.qwirkle.PlayerMessage;
import za.nmu.wrpv.qwirkle.messages.Message;

public class IMessage extends Message implements Serializable {
    private static final long serialVersionUID = 5L;

    @Override
    public void apply() {
        PlayerMessage playerMessage = (PlayerMessage) data.get("message");
        MessagesFragment.runLater(data -> {
            MessagesAdapter adapter = (MessagesAdapter) data.get("adapter");
            adapter.add(playerMessage);
        });
    }
}