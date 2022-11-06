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
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Sync extends Message implements Serializable {
    private final static long serialVersionUID = 200L;

    @Override
    public void apply() {
        System.out.println("--------------------------- SYNCING ---------------");
        if (data.containsKey("currentPlayerIndex")) {
            int currentPlayerIndex = (int) get("currentPlayerIndex");
            List<Player> players = (List<Player>) get("players");
            System.out.println("----------------- CURRENT PLAYER INDEX = " + currentPlayerIndex + ", player count = " + players.size());

            GameModel.players = players;
            System.out.println("----------------- PLAYER COUNT AFTER SYNC = " + GameModel.players.size());
            GameModel.currentPlayer = GameModel.players.get(currentPlayerIndex);

            GameFragment.runLater(d -> {
                Activity context = (Activity) d.get("context");
                ScoreAdapter adapter = (ScoreAdapter) d.get("adapter");
                GameFragment fragment = (GameFragment) d.get("fragment");

                Objects.requireNonNull(adapter).players = GameModel.players;
                Objects.requireNonNull(context).runOnUiThread(adapter::notifyDataSetChanged);

                Objects.requireNonNull(context).runOnUiThread(Objects.requireNonNull(adapter)::notifyDataSetChanged);
                Button btnPlay = Objects.requireNonNull(fragment).requireView().findViewById(R.id.btn_play);
                Button btnDraw = fragment.requireView().findViewById(R.id.btn_draw);
                Button btnUndo = fragment.requireView().findViewById(R.id.btn_undo);
                context.runOnUiThread(() -> {
                    Objects.requireNonNull(fragment).setupCurrentPlayer(currentPlayerIndex);
                    Helper.enableIfTurn(btnPlay, btnDraw, btnUndo);
                });
            });
        }
    }
}
