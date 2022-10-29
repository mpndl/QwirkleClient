package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.GameModel;

public class Name extends IMessage implements Serializable {
    private static final long serialVersionUID = 50L;

    @Override
    public void apply() {
        GameModel.clientPlayerName = (String) data.get("name");
        GameModel.gameID = (int) get("gameID");
    }
}
