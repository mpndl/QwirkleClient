package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;

import java.io.Serializable;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.BeginFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.R;
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

                int curClientID = preferences.getInt("curClientID", ServerHandler.clientID);
                preferences.edit().putInt("prevClientID", curClientID).apply();
                preferences.edit().putInt("curClientID", ServerHandler.clientID).apply();
                System.out.println("CURRENT CLIENT ID = " + preferences.getInt("curClientID", -2));
                System.out.println("PREV CLIENT ID = " + preferences.getInt("prevClientID", -3));
            });
        }
        if (get("gameID") != null) {
            String name = (String) get("name");
            System.out.println("ASSIGNING NAME = " + name);
            int gameID = (int) get("gameID");

            GameModel.playerName = name;
            GameModel.gameID = gameID;
        }

        BeginFragment.runLater(d -> {
            Activity context = (Activity) d.get("context");
            Button btnStartGame = Objects.requireNonNull(context).findViewById(R.id.btn_start_game);
            if (btnStartGame == null) throw new NullPointerException();
            context.runOnUiThread(() -> {
                        btnStartGame.setText(R.string.btn_start_game);
                    });
            System.out.println("------------ BUTTON START GAME TEXT SET");
        });
    }
}
