package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.ServerHandler;

public class Info extends IMessage implements Serializable {
    private static final long serialVersionUID = 50L;

    @Override
    public void apply() {
        if (data.containsKey("clientID")) {
            ServerHandler.clientID = (int) get("clientID");
            BeginActivity.runLater(d -> {
                Activity context = (Activity) d.get("context");
                SharedPreferences preferences = Objects.requireNonNull(context).getPreferences(Context.MODE_PRIVATE);
                if (BeginActivity.track % 2 == 0) preferences.edit().putInt("prevClientID", ServerHandler.clientID).apply();
                if (BeginActivity.track % 3 == 0) preferences.edit().putInt("prev2ClientID", ServerHandler.clientID).apply();
                BeginActivity.track++;
                System.out.println("PREV CLIENT ID" + preferences.getInt("prevClientID", -2));
                System.out.println("PREV2 CLIENT ID" + preferences.getInt("prev2ClientID", -3));
            });
        }
        if (get("gameID") != null) {
            String name = (String) get("name");
            System.out.println("ASSIGNING NAME = " + name);
            int gameID = (int) get("gameID");

            GameModel.playerName = name;
            GameModel.gameID = gameID;
        }
    }
}
