package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import java.io.Serializable;
import java.util.List;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
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

            if (player.name != GameModel.clientPlayer.name) {
                GameModel.updatePlayerTiles(player, adapter);
                GameModel.bag = bag;

                GameModel.turn();
                GameModel.placing = false;

                fragment.setupCurrentPlayer();
            }

            Button btnPlay = context.findViewById(R.id.btn_play);
            Button btnDraw = context.findViewById(R.id.btn_draw);
            Button btnUndo = context.findViewById(R.id.btn_undo);
            btnDraw.setEnabled(false);
            btnPlay.setEnabled(false);
            btnUndo.setEnabled(false);
            if (GameModel.isTurn()) {
                btnDraw.setEnabled(true);
                btnPlay.setEnabled(true);
                btnUndo.setEnabled(true);
            }
        });
    }
}
