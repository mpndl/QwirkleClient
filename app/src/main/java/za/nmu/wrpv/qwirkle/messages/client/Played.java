package za.nmu.wrpv.qwirkle.messages.client;

import static za.nmu.wrpv.qwirkle.Helper.calculatePoints;
import static za.nmu.wrpv.qwirkle.Helper.focusOnView;
import static za.nmu.wrpv.qwirkle.Helper.getDrawable;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.gridlayout.widget.GridLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.PlayerTilesAdapter;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
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
        List<Tile> visitedTiles = (List<Tile>) data.get("visitedTiles");
        int qwirkle = (int) get("qwirkle");
        int placedCount = (int) data.get("placedCount");
        GameFragment.runLater(d -> {
            Activity context = (Activity) d.get("context");
            ScoreAdapter adapter = (ScoreAdapter) d.get("adapter");
            GameFragment fragment = (GameFragment) d.get("fragment");

            HorizontalScrollView hsv = context.findViewById(R.id.horizontalScrollView);
            ScrollView sv = context.findViewById(R.id.scrollView2);

            Helper.sound(context, R.raw.play);
            if (player.name != GameModel.clientPlayer.name) {
                GameModel.updatePlayerTiles(player);
                GameModel.updatePlayerScore(player);
                context.runOnUiThread(() -> adapter.notifyDataSetChanged());
                GameModel.board = board;
                GameModel.bag = bag;

                GridLayout glBoard = context.findViewById(R.id.board);
                View v = null;
                for (int i = 0; i < places.size(); i++) {
                    Tile tile = places.get(i);
                    View view = glBoard.getChildAt(tile.index);
                    context.runOnUiThread(() -> view.setForeground(getDrawable(tile.toString(), context)));
                    context.runOnUiThread(() -> view.getForeground().setAlpha(128));
                    context.runOnUiThread(() -> Helper.AnimateTilePlacement.add(view));
                    v = view;
                }
                View finalV = v;
                context.runOnUiThread(() -> focusOnView(context, sv,hsv, finalV));
                context.runOnUiThread(Helper.AnimateTilePlacement::easeInTilePlacement);

                calculatePoints(context,visitedTiles, player, qwirkle, fragment);

                GameModel.turn();

                GameModel.placedCount = placedCount;
                GameModel.placing = false;
                context.runOnUiThread(() -> fragment.setupCurrentPlayer());
                context.runOnUiThread(() -> fragment.setupBagCount());
            }

            Button btnPlay = context.findViewById(R.id.btn_play);
            Button btnDraw = context.findViewById(R.id.btn_draw);
            Button btnUndo = context.findViewById(R.id.btn_undo);
            context.runOnUiThread(() -> Helper.enableIfTurn(btnPlay, btnDraw, btnUndo));
        });
    }
}
