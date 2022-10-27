package za.nmu.wrpv.qwirkle;

import static za.nmu.wrpv.qwirkle.Helper.AnimateTilePlacement.easeInTilePlacement;
import static za.nmu.wrpv.qwirkle.Helper.BOARD_TILE_SIZE;
import static za.nmu.wrpv.qwirkle.Helper.PLAYER_TILE_OPACITY;
import static za.nmu.wrpv.qwirkle.Helper.PLAYER_TILE_SIZE_50;
import static za.nmu.wrpv.qwirkle.Helper.PLAYER_TILE_SIZE_60;
import static za.nmu.wrpv.qwirkle.Helper.enableIfTurn;
import static za.nmu.wrpv.qwirkle.Helper.getColor;
import static za.nmu.wrpv.qwirkle.Helper.getDrawable;
import static za.nmu.wrpv.qwirkle.Helper.qwirkleAnimate;
import static za.nmu.wrpv.qwirkle.Helper.setBackgroundBorder;
import static za.nmu.wrpv.qwirkle.Helper.setBackgroundColor;
import static za.nmu.wrpv.qwirkle.Helper.setTurnBackgroundBorder;
import static za.nmu.wrpv.qwirkle.Helper.setTurnBackgroundColor;
import static za.nmu.wrpv.qwirkle.Helper.vibrate;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
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
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.client.Drawn;
import za.nmu.wrpv.qwirkle.messages.client.GameEnded;
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
        playerTilesAdapter = new PlayerTilesAdapter(getActivity(), GameModel.clientPlayer.tiles);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x; // int screenWidth = display.getWidth(); on API < 13
        int screenHeight = size.y; // int screenHeight = display.getHeight(); on API <13

        BOARD_TILE_SIZE = screenWidth/6;
        PLAYER_TILE_SIZE_60 = screenWidth/8;
        PLAYER_TILE_SIZE_50 = screenWidth/9;

        Button btnPlay = getView().findViewById(R.id.btn_play);
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        Button btnUndo = getView().findViewById(R.id.btn_undo);
        enableIfTurn(btnPlay, btnDraw, btnUndo);

        btnPlay.getLayoutParams().width = (BOARD_TILE_SIZE);
        btnPlay.getLayoutParams().height = (BOARD_TILE_SIZE);

        btnDraw.getLayoutParams().width = (BOARD_TILE_SIZE);
        btnDraw.getLayoutParams().height = (BOARD_TILE_SIZE);

        btnUndo.getLayoutParams().width = (BOARD_TILE_SIZE);
        btnUndo.getLayoutParams().height = (BOARD_TILE_SIZE);

        ImageView ivBag = getView().findViewById(R.id.iv_bag);
        ivBag.getLayoutParams().width = (BOARD_TILE_SIZE);
        ivBag.getLayoutParams().height = (BOARD_TILE_SIZE);

        TextView tvQwirkle = getView().findViewById(R.id.tv_qwirkle);
        tvQwirkle.setTextSize(30);
        tvQwirkle.setText(R.string.app_name);


        thread = new Thread(() -> {
            do {
                Map<String, Object> data = new HashMap<>();
                data.put("adapter", scoreAdapter);
                data.put("playerTilesAdapter",playerTilesAdapter);
                data.put("context", getContext());
                data.put("fragment", this);
                Run run = null;
                try {
                    run = runs.take();
                    Run finalRun = run;
                    getActivity().runOnUiThread(() -> finalRun.run(data));
                }catch (NullPointerException e) {
                    if (run != null) {
                        runs.add(run);
                    }
                }
                catch (InterruptedException e) {
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

    private void setButtonListeners() {
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        Button btnPlay = getView().findViewById(R.id.btn_play);
        Button btnUndo = getView().findViewById(R.id.btn_undo);
        btnDraw.setOnClickListener(this::setOnDraw);
        btnPlay.setOnClickListener(this:: setOnPlay);
        btnUndo.setOnClickListener(this::setOnUndo);
    }

    private void setupGridLayout() {
        GridLayout glBoard = getView().findViewById(R.id.board);
        glBoard.setColumnCount(GameModel.XLENGTH);
        glBoard.setRowCount(GameModel.YLENGTH);
        populate(glBoard);
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
                params.width = PLAYER_TILE_SIZE_50;
                //params.height = PLAYER_TILE_SIZE_50;
                curImageView.setLayoutParams(params);
            }
        }
    }

    private void setupRecyclerView() {
        RecyclerView rvPlayerTilesView = getView().findViewById(R.id.player_tiles);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

        rvPlayerTilesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rvPlayerTilesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupCurrentPlayer();
                Helper.sound(getActivity(), R.raw.begin);
            }
        });

        playerTilesAdapter.setOnClickListener(this::iaOnclickListener);

        playerTilesAdapter.setOnLongClickListener(this::iaLongClickListener);

        rvPlayerTilesView.setAdapter(playerTilesAdapter);
    }


    public void setupCurrentPlayer() {
        GridView gvPlayersStatus = getView().findViewById(R.id.gv_players_status);

        for (int i = 0; i < gvPlayersStatus.getChildCount(); i++) {
            CardView cardView = (CardView) gvPlayersStatus.getChildAt(i);
            TextView textView = cardView.findViewById(R.id.tv_player_name);
            textView.setTextSize(BOARD_TILE_SIZE /9f);

            ImageView imageView = cardView.findViewById(R.id.iv_player_avatar);
            String[] data = imageView.getTag().toString().split(",");
            String playerName = data[1];
            String you = data[0];
            if (playerName.equals(GameModel.currentPlayer.name.toString())) {
                if (playerName.equals(GameModel.clientPlayerName))
                    textView.setText(">" + you);
                else
                    textView.setText(">" + playerName);
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

        RecyclerView recyclerView = getView().findViewById(R.id.player_tiles);

        setTurnBackgroundColor(gvPlayersStatus, GameModel.clientPlayer);
        setBackgroundColor(recyclerView, GameModel.clientPlayer);
    }

    public void setupBagCount() {
        TextView tvTileCount = getView().findViewById(R.id.tv_tileCount);
        tvTileCount.setText(GameModel.getBagCount() + "");
    }

    private void populate(GridLayout grid) {
        grid.removeAllViews();

        int index = 0;
        for (int i = 0; i < GameModel.XLENGTH; i++) {
            for (int j = 0; j < GameModel.YLENGTH; j++) {
                ImageButton button = new ImageButton(getActivity());

                button.setBackground(Helper.getDrawable("shadow", getActivity()));
                button.setMinimumWidth(BOARD_TILE_SIZE);
                button.setMinimumHeight(BOARD_TILE_SIZE);
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
                Helper.sound(getActivity(), R.raw.placed);
                updateTags();
                playerTilesAdapter.notifyDataSetChanged();

                // set image resource to the tile selected by a player
                button.setForeground(getDrawable(selectedTiles.get(0).toString(), getActivity()));
                button.getForeground().setAlpha(PLAYER_TILE_OPACITY);
                Helper.AnimateTilePlacement.add(button);
            }else {
                Helper.sound(getActivity(), R.raw.invalid);
            }
        }else {
            if (!GameModel.isTurn())
                System.out.println("NOT YOUR TURN "+ GameModel.clientPlayer.name +" BUT " + GameModel.currentPlayer.name + "'s");
            else {
                System.out.println("SELECT TILES TO PLACE " + GameModel.clientPlayer.name);
            }
            Helper.sound(getActivity(), R.raw.invalid);
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
            if (params.width == PLAYER_TILE_SIZE_50) {
                params.width = PLAYER_TILE_SIZE_60;
                //params.height = PLAYER_TILE_SIZE_60;
            } else {
                params.width = PLAYER_TILE_SIZE_50;
                //params.height = PLAYER_TILE_SIZE_50;
                selectedTiles.remove(selectedTile);
            }
            imageView.setLayoutParams(params);
        }
        else {
            selectedTiles = new ArrayList<>();
            selectedTiles.add(selectedTile);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if (params.width == PLAYER_TILE_SIZE_50) {
                params.width = PLAYER_TILE_SIZE_60;
                //params.height = PLAYER_TILE_SIZE_60;
            } else {
                params.width = PLAYER_TILE_SIZE_50;
                //params.height = PLAYER_TILE_SIZE_50;
                selectedTiles.remove(selectedTile);
            }
            imageView.setLayoutParams(params);
            resetWidthExcept(imageView);
        }
    }

    public boolean iaLongClickListener(View view) {
        multiSelect = !multiSelect;
        vibrate(50, getActivity());
        resetWidthExcept(null);
        if (selectedTiles == null)
            selectedTiles = new ArrayList<>();
        ImageView imageView = view.findViewById(R.id.iv_tile);
        Tile selectedTile = GameModel.clientPlayer.tiles.get(Integer.parseInt(imageView.getTag().toString()));
        selectedTiles.add(selectedTile);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        if (params.width == PLAYER_TILE_SIZE_50) {
            params.width = PLAYER_TILE_SIZE_60;
            //params.height = PLAYER_TILE_SIZE_60;
        }
        else {
            params.width = PLAYER_TILE_SIZE_50;
            //params.height = PLAYER_TILE_SIZE_50;
            selectedTiles.remove(selectedTile);
        }
        imageView.setLayoutParams(params);
        return true;
    }

    public void setOnPlay(View view) {
        if(GameModel.places.size() > 0) {
            System.out.println("-------------------------- PLAY START -------------------------");
            GameModel.recover();
            GameModel.play(playerTilesAdapter);
            GameModel.updatePlayerScore(GameModel.clientPlayer, scoreAdapter);

            Played message = new Played();
            message.put("bag", GameModel.bag);
            message.put("player", GameModel.clonePlayer(GameModel.clientPlayer));
            message.put("places", GameModel.cloneTiles(GameModel.places));
            message.put("board", GameModel.board);
            message.put("placedCount", GameModel.placedCount);
            message.put("qwirkle",GameModel.qwirkle);

            System.out.println(GameModel.clientPlayer.name + " POINTS = " + GameModel.clientPlayer.points);
            System.out.println(GameModel.clientPlayer.name + " POINTS = " + GameModel.currentPlayer.points);

            if (GameModel.qwirkle) {
                GridLayout gridLayout = getView().findViewById(R.id.board);
                qwirkleAnimate(getActivity(), GameModel.clientPlayer, gridLayout);
                Helper.vibrate(500,getActivity());
            }

            GameModel.turn();

            setupBagCount();
            setupCurrentPlayer();
            resetMultiSelect();
            easeInTilePlacement();

            GameModel.places = new ArrayList<>();
            GameModel.qwirkle = false;
            ServerHandler.send(message);
            System.out.println(GameModel.clientPlayer.name + " POINTS = " + GameModel.clientPlayer.points);

            Button btnPlay = getView().findViewById(R.id.btn_play);
            Button btnDraw = getView().findViewById(R.id.btn_draw);
            Button btnUndo = getView().findViewById(R.id.btn_undo);
            enableIfTurn(btnPlay, btnDraw, btnUndo);
            System.out.println("-------------------------- PLAY END -------------------------");
        }else Helper.sound(getActivity(), R.raw.invalid);
    }

    public void gameEnded() {
        GameEnded message = new GameEnded();
        //message.put("players", GameModel.players);
        ServerHandler.send(message);

        Button btnPlay = getView().findViewById(R.id.btn_play);
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        Button btnUndo = getView().findViewById(R.id.btn_undo);
        enableIfTurn(btnPlay, btnDraw, btnUndo);
    }

    public void setOnUndo(View view) {
        if (GameModel.places.size() > 0) {
            Helper.sound(getActivity(), R.raw.undo);
            GameModel.undo(GameModel.places, playerTilesAdapter);
            resetWidthExcept(null);
            resetMultiSelect();
            undoPlacedTiles(GameModel.places);
            easeInTilePlacement();
            GameModel.places = new ArrayList<>();
        }else Helper.sound(getActivity(), R.raw.invalid);
    }

    public void setOnDraw(View view) {
        if (GameModel.getBagCount() > 0) {
            if (selectedTiles.size() > 0)
                GameModel.draw(false, selectedTiles, playerTilesAdapter);
            else
                GameModel.draw(false, null, playerTilesAdapter);

            Drawn message = new Drawn();
            message.put("bag", GameModel.bag);
            message.put("player", GameModel.clientPlayer);

            GameModel.turn();
            GameModel.updatePlayerTiles(GameModel.clientPlayer, playerTilesAdapter);

            ServerHandler.send(message);

            setupBagCount();
            resetWidthExcept(null);
            setupCurrentPlayer();
            resetMultiSelect();
            undoPlacedTiles(GameModel.places);

            Button btnPlay = getView().findViewById(R.id.btn_play);
            Button btnDraw = getView().findViewById(R.id.btn_draw);
            Button btnUndo = getView().findViewById(R.id.btn_undo);
            enableIfTurn(btnPlay, btnDraw, btnUndo);
        }else Helper.sound(getActivity(), R.raw.invalid);
    }

    private void undoPlacedTiles(List<Tile> selectedTiles) {
        GridLayout glBoard = getView().findViewById(R.id.board);
        for (Tile tile: selectedTiles) {
            int index = tile.yPos * GameModel.XLENGTH + tile.xPos;
            ImageButton imageButton2 = (ImageButton) glBoard.getChildAt(index);

            ImageButton button = new ImageButton(getActivity());
            button.setBackground(Helper.getDrawable("shadow", getActivity()));
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
}
