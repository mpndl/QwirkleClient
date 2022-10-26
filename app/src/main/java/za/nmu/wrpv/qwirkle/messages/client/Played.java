package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.gridlayout.widget.GridLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.PlayerTilesAdapter;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.Tile;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Played extends Message implements Serializable {
    private final static long serialVersionUID = 50L;

    @Override
    public void apply() {
        System.out.println("------------------------------- PLAYED START -----------------------------");
        Player player = (Player) data.get("player");
        System.out.println(player.name + " POINTS = " + player.points);

        List<Tile> bag = (List<Tile>) data.get("bag");
        Tile[][] board = (Tile[][]) data.get("board");
        List<Tile> places = (ArrayList<Tile>)data.get("places");
        int placedCount = (int) data.get("placedCount");
        GameFragment.runLater(data -> {
            Activity context = (Activity) data.get("context");
            ScoreAdapter adapter = (ScoreAdapter) data.get("adapter");
            PlayerTilesAdapter playerTileAdapter = (PlayerTilesAdapter) data.get("playerTilesAdapter");
            GameFragment fragment = (GameFragment) data.get("fragment");

            HorizontalScrollView hsv = context.findViewById(R.id.horizontalScrollView);
            ScrollView sv = context.findViewById(R.id.scrollView2);

            if (player.name != GameModel.clientPlayer.name) {
                GameModel.updatePlayerTiles(player, playerTileAdapter);
                GameModel.updatePlayerScore(player, adapter);
                GameModel.board = board;
                GameModel.bag = bag;

                GridLayout glBoard = context.findViewById(R.id.board);
                View view = null;
                for (int i = 0; i < places.size(); i++) {
                    Tile tile = places.get(i);
                    view = glBoard.getChildAt(tile.index);
                    view.setForeground(getDrawable(tile.toString(), context));
                    view.setEnabled(false);
                }

                fragment.focusOnView(sv,hsv, view);

                GameModel.turn();

                if (GameModel.gameEnded(playerTileAdapter))
                    fragment.gameEnded();

                GameModel.placedCount = placedCount;
                GameModel.placing = false;
                fragment.setupCurrentPlayer();
                fragment.setupBagCount();
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

    private Drawable getDrawable(String name, Activity activity) {
        return activity.getResources().getDrawable(activity.getResources().getIdentifier(name, "drawable", activity.getPackageName()));
    }
}
