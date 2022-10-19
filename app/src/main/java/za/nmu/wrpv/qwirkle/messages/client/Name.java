package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.ServerHandler;

public class Name extends IMessage implements Serializable {
    private static final long serialVersionUID = 50L;

    @Override
    public void apply() {
        ServerHandler.playerName = (String) data.get("name");
    }
}
