package za.nmu.wrpv.qwirkle.messages.client;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.BeginFragment;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.PlayerMessage;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.Tile;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Begin extends Message {
    private static final long serialVersionUID = 4L;

    @Override
    public void apply() {
        int currentPlayerIndex = (int) data.get("currentPlayerIndex");
        List<Tile> bag = (List<Tile>) data.get("bag");
        List<Player> players = (List<Player>)  data.get("players");
        List<PlayerMessage> messages = (List<PlayerMessage>)  data.get("messages");
        Tile[][] board = (Tile[][]) data.get("board");

        BeginActivity.runLater(d -> {
            BeginActivity context = (BeginActivity) d.get("context");

            SharedPreferences preferences = Objects.requireNonNull(context).getPreferences(MODE_PRIVATE);
            if (GameModel.gameID != -1) {
                System.out.println("----------------- SAVING GAME INFORMATION");
                preferences.edit().putInt("gameID", GameModel.gameID).apply();

                int gameID = preferences.getInt("gameID", -1);
                System.out.println("gameID = " + gameID);

                System.out.println("------------------------- SAVED GAME INFORMATION");
                System.out.println("----------------------------------------------------------------------------");
            }

            if (ServerHandler.clientID != -1) {
                System.out.println("----------------- SAVING GAME INFORMATION");
                preferences.edit().putInt("clientID", ServerHandler.clientID).apply();

                int clientID = preferences.getInt("clientID", -1);
                System.out.println("clientID = "+ clientID);

                System.out.println("------------------------- SAVED GAME INFORMATION");
                System.out.println("----------------------------------------------------------------------------");
            }

            Countdown.interrupt();

            Intent intent = new Intent(context, MainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("currentPlayerIndex", currentPlayerIndex);
            bundle.putSerializable("bag", (Serializable) bag);
            bundle.putSerializable("players", (Serializable) players);
            bundle.putSerializable("board", board);
            bundle.putSerializable("messages", (Serializable) messages);

            if (data.containsKey("player")) {
                Player player = (Player) data.get("player");

                bundle.putSerializable("player", player);
                GameModel.playerName = (String) data.get("name");
                GameModel.placed.addAll((List<Tile>) Objects.requireNonNull(data.get("placed")));
            }

            intent.putExtra("bundle", bundle);
            Objects.requireNonNull(context).startActivity(intent);
        });
    }
}
