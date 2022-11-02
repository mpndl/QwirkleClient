package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;

import java.io.Serializable;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Joined extends Message implements Serializable {
    private static final long serialVersionUID = 102L;

    @Override
    public void apply() {
        Player player = (Player) get("player");
        if (GameModel.player != null) {
            GameModel.addPlayerSorted(player);
            GameFragment.runLater(d -> {
                Activity context = (Activity) d.get("context");
                ScoreAdapter adapter = (ScoreAdapter) d.get("adapter");
                GameFragment fragment = (GameFragment) d.get("fragment");

                Objects.requireNonNull(context).runOnUiThread(Objects.requireNonNull(adapter)::notifyDataSetChanged);
            });
        }
    }
}