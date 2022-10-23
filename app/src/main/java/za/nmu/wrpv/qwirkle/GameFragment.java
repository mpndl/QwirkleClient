package za.nmu.wrpv.qwirkle;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.client.Drawn;
import za.nmu.wrpv.qwirkle.messages.client.Played;

public class GameFragment extends Fragment implements Serializable {
    private PlayerTilesAdapter playerTilesAdapter;
    private ScoreAdapter scoreAdapter;
    private List<Tile> selectedTiles = new ArrayList<>();
    private boolean multiSelect = false;
    private boolean multiSelected = false;

    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;

    public static GameFragment newInstance() {
        return new GameFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scoreAdapter = new ScoreAdapter(getActivity(), GameModel.players);
        playerTilesAdapter = new PlayerTilesAdapter(GameModel.clientPlayer.tiles, getActivity());

        Button btnPlay = getView().findViewById(R.id.btn_play);
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        btnDraw.setEnabled(false);
        btnPlay.setEnabled(false);
        if (GameModel.isTurn()) {
            btnDraw.setEnabled(true);
            btnPlay.setEnabled(true);
        }

        thread = new Thread(() -> {
            do {
                Map<String, Object> data = new HashMap<>();
                data.put("adapter", scoreAdapter);
                data.put("context", getContext());
                data.put("fragment", this);
                try {
                    Run run = runs.take();
                    getActivity().runOnUiThread(() -> run.run(data));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (true);
        });
        thread.start();

        setupGridLayout();
        setupRecyclerView();
        setupPlayersStatus();
        setupBagCount();
        setButtonListeners();
        center();
    }

    public static void runLater(Run run) {
        runs.add(run);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (thread.isAlive()) thread.interrupt();
    }

    private void center() {
        HorizontalScrollView hsv = getView().findViewById(R.id.horizontalScrollView);
        ScrollView sv = getView().findViewById(R.id.scrollView2);
        hsv.post(() -> {
            sv.smoothScrollBy((GameModel.XLENGTH * (GameModel.XLENGTH * 2)) /2, (GameModel.XLENGTH * (GameModel.XLENGTH * 2)) /2);
            hsv.smoothScrollBy((GameModel.XLENGTH * (GameModel.XLENGTH * 2)) /2, (GameModel.XLENGTH * (GameModel.XLENGTH * 2)) /2);
        });
    }

    public void focusOnView(final ScrollView scroll, final  HorizontalScrollView hScroll, final View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        int scrollTo = ((View) view.getParent().getParent()).getTop() + view.getTop() - (height/2);
        scroll.smoothScrollTo(0, scrollTo);

        hsvFocusOnView(hScroll, view);
    }

    private void hsvFocusOnView(final HorizontalScrollView scroll, final View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int scrollTo = ((View)view.getParent()).getLeft() + view.getLeft() - (width/2);
        scroll.scrollTo(scrollTo,0);
    }

    private void setButtonListeners() {
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        Button btnPlay = getView().findViewById(R.id.btn_play);
        btnDraw.setOnClickListener(this::setOnDraw);
        btnPlay.setOnClickListener(this:: setOnPlay);
    }

    private void setupPlayersStatus() {
        GridView gvPlayersStatus = getView().findViewById(R.id.gv_players_status);
        gvPlayersStatus.setAdapter(scoreAdapter);
    }

    private void resetWidthExcept(ImageView imageView) {
        RecyclerView rvPlayerTiles = getActivity().findViewById(R.id.player_tiles);
        for (int i = 0; i < rvPlayerTiles.getChildCount(); i++) {
            ConstraintLayout constraintLayout = (ConstraintLayout) rvPlayerTiles.getChildAt(i);
            ImageView curImageView = (ImageView) constraintLayout.getChildAt(0);
            if (imageView != curImageView) {
                ViewGroup.LayoutParams params = curImageView.getLayoutParams();
                params.width = 50;
                curImageView.setLayoutParams(params);
            }
        }
    }

    public void setupCurrentPlayer() {
        GridView gvPlayersStatus = getView().findViewById(R.id.gv_players_status);

        for (int i = 0; i < gvPlayersStatus.getChildCount(); i++) {
            CardView cardView = (CardView) gvPlayersStatus.getChildAt(i);
            TextView textView = cardView.findViewById(R.id.tv_player_name);
            ImageView imageView = cardView.findViewById(R.id.iv_player_avatar);
            String[] data = imageView.getTag().toString().split(",");
            String playerName = data[1];
            String you = data[0];
            if (playerName.equals(GameModel.currentPlayer.name.toString())) {
                if (playerName.equals(GameModel.clientPlayerName))
                    textView.setText("> " + you);
                else
                    textView.setText("> " + playerName);
                textView.setTextColor(Color.BLUE);
            }
            else {
                if (playerName.equals(GameModel.clientPlayerName))
                    textView.setText(you);
                else
                    textView.setText(playerName);
                textView.setTextColor(getActivity().getColor(R.color.white));
            }
        }

        ConstraintLayout constraintLayout = getView().findViewById(R.id.cl_fragment_game);
        if (GameModel.isTurn()) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setStroke(15, ScoreAdapter.getColor(GameModel.currentPlayer, getContext()));
            constraintLayout.setBackground(gradientDrawable);
        }
        else constraintLayout.setBackground(null);
    }

    public void setupBagCount() {
        TextView tvTileCount = getView().findViewById(R.id.tv_tileCount);
        tvTileCount.setText(GameModel.getBagCount() + "");
    }

    public void updatePlayerScore() {
        scoreAdapter.updatePlayerScore(GameModel.currentPlayer);
    }

    private void setupGridLayout() {
        GridLayout glBoard = getView().findViewById(R.id.board);
        glBoard.setColumnCount(GameModel.XLENGTH);
        glBoard.setRowCount(GameModel.YLENGTH);
        populate(glBoard);
    }

    private void populate(GridLayout grid) {
        grid.removeAllViews();

        int index = 0;
        for (int i = 0; i < GameModel.XLENGTH; i++) {
            for (int j = 0; j < GameModel.YLENGTH; j++) {
                ImageButton button = new ImageButton(getActivity());

                button.setMinimumWidth(100);
                button.setMinimumHeight(100);
                button.setTag(i + "_" + j + "_" + index);
                button.setPadding(0, 0, 0, 0);
                button.setOnClickListener(this::onTileClicked);
                grid.addView(button, index);

                index++;
            }
        }
    }

    private void onTileClicked(View view) {
        if (selectedTiles.size() > 0 && GameModel.isTurn()) {
            ImageButton button = (ImageButton) view;
            String[] rowColIndex = button.getTag().toString().split("_");
            int row_no = Integer.parseInt(rowColIndex[1]);
            int col_no = Integer.parseInt(rowColIndex[0]);
            selectedTiles.get(0).index = Integer.parseInt(rowColIndex[2]);
            GameModel.Legality legality = GameModel.place(row_no, col_no, selectedTiles.get(0), playerTilesAdapter);
            if (legality == GameModel.Legality.LEGAL) {
                updateTags();
                playerTilesAdapter.notifyDataSetChanged();

                // set image resource to the tile selected by a player
                button.setForeground(getDrawable(selectedTiles.get(0).toString()));

                // no further interaction allowed
                button.setEnabled(false);
            }
        }else {
            if (!GameModel.isTurn())
                System.out.println("NOT YOUR TURN "+ GameModel.clientPlayer.name +" BUT " + GameModel.currentPlayer.name + "'s");
            else {
                System.out.println("SELECT TILES TO PLACE " + GameModel.currentPlayer.name);
            }
        }

        resetWidthExcept(null);
        resetMultiSelect();
    }

    private void updateTags() {
        RecyclerView rvPlayerTiles = getView().findViewById(R.id.player_tiles);
        for (int i = 0; i < rvPlayerTiles.getChildCount(); i++) {
            ConstraintLayout constraintLayout = (ConstraintLayout) rvPlayerTiles.getChildAt(i);
            ImageView curImageView = (ImageView) constraintLayout.getChildAt(0);
            curImageView.setTag(i);
        }
    }

    private Drawable getDrawable(String name) {
        return getResources().getDrawable(getResources().getIdentifier(name, "drawable", getContext().getPackageName()));
    }

    private void setupRecyclerView() {
        RecyclerView rvPlayerTilesView = getView().findViewById(R.id.player_tiles);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

        rvPlayerTilesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rvPlayerTilesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupCurrentPlayer();
            }
        });

        playerTilesAdapter.setOnClickListener(this::iaOnclickListener);

        playerTilesAdapter.setOnLongClickListener(this::iaLongClickListener);

        rvPlayerTilesView.setAdapter(playerTilesAdapter);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(15, ScoreAdapter.getColor(GameModel.clientPlayer, getContext()));
        rvPlayerTilesView.setBackground(gradientDrawable);
    }

    public void iaOnclickListener(View view) {
        ImageView imageView = view.findViewById(R.id.iv_tile);
        updateTags();

        // multi-select feature
        if (selectedTiles.size() > 1)
            multiSelected = true;
        if (selectedTiles.size() < 2 && multiSelected) {
            multiSelect = false;
            multiSelected = false;
        }

        Tile selectedTile = playerTilesAdapter.get(Integer.parseInt(imageView.getTag().toString()));
        if (multiSelect) {
            if (!selectedTiles.contains(selectedTile))
                selectedTiles.add(selectedTile);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if (params.width == 50) {
                params.width = 60;
            } else {
                params.width = 50;
                selectedTiles.remove(selectedTile);
            }
            imageView.setLayoutParams(params);
        }
        else {
            selectedTiles = new ArrayList<>();
            selectedTiles.add(selectedTile);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if (params.width == 50) {
                params.width = 60;
            } else {
                params.width = 50;
                selectedTiles.remove(selectedTile);
            }
            imageView.setLayoutParams(params);
            resetWidthExcept(imageView);
        }
    }

    public boolean iaLongClickListener(View view) {
        multiSelect = !multiSelect;
        vibrate(50);
        resetWidthExcept(null);
        if (selectedTiles == null)
            selectedTiles = new ArrayList<>();
        ImageView imageView = view.findViewById(R.id.iv_tile);
        Tile selectedTile = GameModel.currentPlayer.tiles.get(Integer.parseInt(imageView.getTag().toString()));
        selectedTiles.add(selectedTile);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        if (params.width == 50) {
            params.width = 60;
        }
        else {
            params.width = 50;
            selectedTiles.remove(selectedTile);
        }
        imageView.setLayoutParams(params);
        return true;
    }

    public void setOnPlay(View view) {
        if(GameModel.places.size() > 0) {
            GameModel.recover();
            GameModel.play(playerTilesAdapter);
            updatePlayerScore();

            List<Tile> temp = new ArrayList<>();
            for (Tile tile: GameModel.places) {
                Tile tempTile = new Tile();
                tempTile.xPos = tile.xPos;
                tempTile.yPos = tile.yPos;
                tempTile.color = tile.color;
                tempTile.shape = tile.shape;
                tempTile.index = tile.index;
                temp.add(tempTile);
            }

            Played message = new Played();
            message.put("bag", GameModel.bag);
            message.put("player", GameModel.currentPlayer);
            message.put("places", temp);
            message.put("board", GameModel.board);
            message.put("placedCount", GameModel.placedCount);

            GameModel.turn();

            setupBagCount();
            updatePlayerTiles();
            setupCurrentPlayer();
            resetMultiSelect();

            if (GameModel.currentPlayer.tiles.size() == 0)
                gameFinished();

            GameModel.places = new ArrayList<>();
            ServerHandler.send(message);

            Button btnPlay = getView().findViewById(R.id.btn_play);
            Button btnDraw = getView().findViewById(R.id.btn_draw);
            btnDraw.setEnabled(false);
            btnPlay.setEnabled(false);
        }
    }

    public void gameFinished() {
        Intent intent = new Intent(getActivity(), EndActivity.class);
        intent.putExtra("winner", GameModel.getWinner());
        startActivity(intent);
    }

    public void setOnDraw(View view) {
        if (GameModel.getBagCount() > 0) {
            if (selectedTiles.size() > 0)
                GameModel.draw(false, selectedTiles, playerTilesAdapter);
            else
                GameModel.draw(false, null, playerTilesAdapter);

            Drawn message = new Drawn();
            message.put("bag", GameModel.bag);
            message.put("player", GameModel.currentPlayer);

            GameModel.turn();
            updatePlayerTiles();

            ServerHandler.send(message);

            setupBagCount();
            resetWidthExcept(null);
            setupCurrentPlayer();
            resetMultiSelect();
            undoPlacedTiles(GameModel.places);
            Button btnPlay = getView().findViewById(R.id.btn_play);
            Button btnDraw = getView().findViewById(R.id.btn_draw);
            btnDraw.setEnabled(false);
            btnPlay.setEnabled(false);
        }
    }

    public void removePlayer(Player player) {
        scoreAdapter.remove(player);
    }

    private void undoPlacedTiles(List<Tile> selectedTiles) {
        GridLayout glBoard = getView().findViewById(R.id.board);
        for (Tile tile: selectedTiles) {
            int index = tile.yPos * GameModel.XLENGTH + tile.xPos;
            ImageButton imageButton2 = (ImageButton) glBoard.getChildAt(index);

            ImageButton button = new ImageButton(getActivity());
            button.setMinimumWidth(imageButton2.getMinimumWidth());
            button.setMinimumHeight(imageButton2.getMinimumHeight());
            button.setTag(imageButton2.getTag());
            button.setPadding(0, 0, 0, 0);
            button.setOnClickListener(this::onTileClicked);

            glBoard.removeViewAt(index);
            glBoard.addView(button, index);
        }
    }

    private void resetMultiSelect() {
        multiSelect = false;
        selectedTiles = new ArrayList<>();
    }

    public void vibrate(int milliseconds) {
        Vibrator v = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(milliseconds);
        }
    }

    public void updatePlayerTiles() {
            playerTilesAdapter.updateTiles(GameModel.clientPlayer.tiles);
    }
}
