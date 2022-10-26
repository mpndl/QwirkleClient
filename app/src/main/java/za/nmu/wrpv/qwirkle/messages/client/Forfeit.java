package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Forfeit extends Message implements Serializable {
    private static final long serialVersionUID = 80L;

    @Override
    public void apply() {
        System.out.println("------------------------------ FORFEIT START");
        Player player = (Player) get("player");
        GameFragment.runLater((data1 -> {
            ScoreAdapter adapter = (ScoreAdapter) data1.get("adapter");
            Activity context = (Activity) data1.get("context");
            GameFragment fragment= (GameFragment) data1.get("fragment");

            GameModel.removePlayer(player, adapter);
            if (player.name == GameModel.currentPlayer.name) {
                GameModel.setNewCurrentPlayer();
                fragment.setupCurrentPlayer();
            }
            Button btnPlay = context.findViewById(R.id.btn_play);
            Button btnDraw = context.findViewById(R.id.btn_draw);
            Button btnUndo = context.findViewById(R.id.btn_undo);
            Helper.enableIfTurn(btnPlay, btnDraw, btnUndo);
        }));
    }
}
