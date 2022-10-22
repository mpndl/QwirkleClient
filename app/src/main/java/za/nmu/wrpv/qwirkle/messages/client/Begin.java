package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import java.io.Serializable;
import java.util.List;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.Tile;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Begin extends Message {
    private static final long serialVersionUID = 4L;

    @Override
    public void apply() {
        Player currentPlayer = (Player) data.get("currentPlayer");
        List<Tile> bag = (List<Tile>) data.get("bag");
        List<Player> players = (List<Player>)  data.get("players");

        BeginActivity.runLater(data -> {
            Activity context = (Activity) data.get("context");

            Intent intent = new Intent(context, MainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("currentPlayer", currentPlayer);
            bundle.putSerializable("bag", (Serializable) bag);
            bundle.putSerializable("players", (Serializable) players);

            intent.putExtra("bundle", bundle);
            context.startActivity(intent);
        });
    }
}
