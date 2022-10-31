package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.PlayerTilesAdapter;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.Tile;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Drawn extends Message implements Serializable {
    private static final long serialVersionUID = 70L;

    @Override
    public void apply() {
        Player player = (Player) data.get("player");
        List<Tile> bag = (List<Tile>) data.get("bag");
        GameFragment.runLater(data1 -> {
            Activity context = (Activity) data1.get("context");
            GameFragment fragment = (GameFragment) data1.get("fragment");
            PlayerTilesAdapter adapter = (PlayerTilesAdapter) data1.get("playerTilesAdapter");

            Helper.sound(context, R.raw.draw);
            if ((player != null ? player.name : null) != GameModel.clientPlayer.name) {
                GameModel.updatePlayerTiles(player);
                Objects.requireNonNull(context).runOnUiThread(() -> adapter.notifyDataSetChanged());
                GameModel.bag = bag;

                GameModel.turn();
                GameModel.placing = false;

                context.runOnUiThread(() -> fragment.setupCurrentPlayer());
            }

            Button btnPlay = Objects.requireNonNull(context).findViewById(R.id.btn_play);
            Button btnDraw = context.findViewById(R.id.btn_draw);
            Button btnUndo = context.findViewById(R.id.btn_undo);
            context.runOnUiThread(() -> Helper.enableIfTurn(btnPlay, btnDraw, btnUndo));
        });
    }
}
