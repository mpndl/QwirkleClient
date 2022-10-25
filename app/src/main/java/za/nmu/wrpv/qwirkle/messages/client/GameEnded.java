package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;
import java.util.List;

import za.nmu.wrpv.qwirkle.EndActivity;
import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.messages.Message;

public class GameEnded extends Message implements Serializable {
    private static final long serialVersionUID = 75L;

    @Override
    public void apply() {
        List<Player> players = (List<Player>) get("players");
        GameFragment.runLater(d -> {
            Activity context = (Activity) d.get("context");

            Intent intent = new Intent(context, EndActivity.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("players", (Serializable) players);

            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }
}
