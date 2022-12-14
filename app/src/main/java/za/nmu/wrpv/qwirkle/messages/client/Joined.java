package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Joined extends Message implements Serializable {
    private static final long serialVersionUID = 102L;

    @Override
    public void apply() {
        System.out.println("------------------------------- JOINED START --------------------------");
        Player player = (Player) get("player");
        int currentPlayerIndex = (int) get("currentPlayerIndex");
        GameFragment.runLater(d -> {
            Activity context = (Activity) d.get("context");
            ScoreAdapter adapter = (ScoreAdapter) d.get("adapter");
            GameFragment fragment = (GameFragment) d.get("fragment");
            GameModel.addPlayerSorted(player);
            if (GameModel.gameEnded()) Objects.requireNonNull(fragment).gameEnded();
            Objects.requireNonNull(context).runOnUiThread(Objects.requireNonNull(adapter)::notifyDataSetChanged);
            Button btnPlay = Objects.requireNonNull(context).findViewById(R.id.btn_play);
            Button btnDraw = context.findViewById(R.id.btn_draw);
            Button btnUndo = context.findViewById(R.id.btn_undo);
            context.runOnUiThread(() -> {
                Objects.requireNonNull(fragment).setupCurrentPlayer(currentPlayerIndex);
                Helper.enableIfTurn(btnPlay, btnDraw, btnUndo);
                Sync message = new Sync();
                message.put("currentPlayerIndex", GameModel.getPlayerIndex(GameModel.currentPlayer));
                ServerHandler.send(message);
            });
        });

        System.out.println("---------------------------- JOINED END ------------------------");
    }
}
