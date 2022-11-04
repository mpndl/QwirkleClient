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
import java.util.Objects;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.Tile;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Played extends Message implements Serializable {
    private final static long serialVersionUID = 50L;

    @Override
    public void apply() {
        Player player = (Player) data.get("player");

        List<Tile> bag = (List<Tile>) data.get("bag");
        Tile[][] board = (Tile[][]) data.get("board");
        List<Tile> places = (ArrayList<Tile>)data.get("places");
        List<Tile> visitedTiles = (List<Tile>) data.get("visitedTiles");
        List<Tile> placed = (List<Tile>) data.get("placed");
        int currentPlayerIndex = (int) get("currentPlayerIndex");
        int qwirkle = (int) get("qwirkle");
        GameFragment.runLater(d -> {
            Activity context = (Activity) d.get("context");
            ScoreAdapter adapter = (ScoreAdapter) d.get("adapter");
            GameFragment fragment = (GameFragment) d.get("fragment");

            HorizontalScrollView hsv = Objects.requireNonNull(context).findViewById(R.id.horizontalScrollView);
            ScrollView sv = context.findViewById(R.id.scrollView2);

            Helper.sound(context, R.raw.play);
            if (Objects.requireNonNull(player).name != GameModel.player.name) {
                GameModel.updatePlayerTiles(player);
                GameModel.updatePlayerScore(player);
                context.runOnUiThread(Objects.requireNonNull(adapter)::notifyDataSetChanged);
                GameModel.board = board;
                GameModel.bag = bag;
                GameModel.placed.addAll(Objects.requireNonNull(placed));

                GridLayout glBoard = context.findViewById(R.id.board);
                View v = null;
                for (int i = 0; i < Objects.requireNonNull(places).size(); i++) {
                    Tile tile = places.get(i);
                    View view = glBoard.getChildAt(tile.index);
                    context.runOnUiThread(() -> view.setForeground(getDrawable(tile.toString(), context)));
                    context.runOnUiThread(() -> view.getForeground().setAlpha(128));
                    context.runOnUiThread(() -> Helper.AnimateTilePlacement.add(view));
                    v = view;
                }
                View finalV = v;
                context.runOnUiThread(() -> focusOnView(context, sv,hsv, finalV));
                context.runOnUiThread(() -> Helper.AnimateTilePlacement.easeInTilePlacement(50));

                calculatePoints(context, Objects.requireNonNull(visitedTiles), player, qwirkle, fragment);

                //GameModel.turn();
                GameModel.tempBoard = null;
            }

            GameModel.setNewCurrentPlayer(currentPlayerIndex);
            context.runOnUiThread(Objects.requireNonNull(adapter)::notifyDataSetChanged);

            if (GameModel.gameEnded()) Objects.requireNonNull(fragment).gameEnded();

            GameModel.placing = false;
            context.runOnUiThread(() -> Objects.requireNonNull(fragment).setupCurrentPlayer(currentPlayerIndex));
            context.runOnUiThread(Objects.requireNonNull(fragment)::setupBagCount);

            Button btnPlay = context.findViewById(R.id.btn_play);
            Button btnDraw = context.findViewById(R.id.btn_draw);
            Button btnUndo = context.findViewById(R.id.btn_undo);
            context.runOnUiThread(() -> Helper.enableIfTurn(btnPlay, btnDraw, btnUndo));
        });
    }
}
