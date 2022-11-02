package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.ServerHandler;

public class Info extends IMessage implements Serializable {
    private static final long serialVersionUID = 50L;

    @Override
    public void apply() {
        if (data.containsKey("clientID")) ServerHandler.clientID = (int) get("clientID");
        if (get("gameID") != null) {
            String name = (String) get("name");
            System.out.println("ASSIGNING NAME = " + name);
            int gameID = (int) get("gameID");

            GameModel.playerName = name;
            GameModel.gameID = gameID;
        }
    }
}
