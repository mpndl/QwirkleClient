package za.nmu.wrpv.qwirkle.messages.client;

import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;
import java.util.List;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.Tile;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Begin extends Message {
    private static final long serialVersionUID = 4L;

    @Override
    public void apply() {
        int currentPlayerIndex = (int) data.get("currentPlayerIndex");
        List<Tile> bag = (List<Tile>) data.get("bag");
        List<Player> players = (List<Player>)  data.get("players");

        BeginActivity.runLater(data -> {
            BeginActivity context = (BeginActivity) data.get("context");

            context.startGame = true;
            Waiting.interrupt();

            Intent intent = new Intent(context, MainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("currentPlayerIndex", currentPlayerIndex);
            bundle.putSerializable("bag", (Serializable) bag);
            bundle.putSerializable("players", (Serializable) players);

            intent.putExtra("bundle", bundle);
            context.startActivity(intent);
        });
    }
}
