package za.nmu.wrpv.qwirkle.messages.client;

import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;
import java.util.List;

import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.Tile;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Begin extends Message {
    private static final long serialVersionUID = 4L;

    @Override
    public void apply() {
        Intent intent = new Intent(ServerHandler.activity, MainActivity.class);

        Player currentPlayer = (Player) data.get("currentPlayer");
        List<Tile> bag = (List<Tile>) data.get("bag");
        List<Player> players = (List<Player>)  data.get("players");

        Bundle bundle = new Bundle();
        bundle.putSerializable("currentPlayer", currentPlayer);
        bundle.putSerializable("bag", (Serializable) bag);
        bundle.putSerializable("players", (Serializable) players);

        intent.putExtra("bundle", bundle);

        ServerHandler.activity.startActivity(intent);
    }
}
