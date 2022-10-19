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
import java.util.List;

public class GameFragment extends Fragment implements Serializable {
    private ImageAdapter imageAdapter;
    public StatusAdapter statusAdapter;
    private ArrayList<Tile> selectedTiles = new ArrayList<>();
    private boolean multiSelect = false;
    private boolean multiSelected = false;

    private final int SIZE = 100;

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
        setButtons();
        setupGridLayout();
        setupRecyclerView();
        setupPlayersStatus();
        setupBagCount();
        setButtonListeners();
        center();
    }

    private void center() {
        HorizontalScrollView hsv = getView().findViewById(R.id.horizontalScrollView);
        ScrollView sv = getView().findViewById(R.id.scrollView2);

        hsv.post(() -> {
            sv.smoothScrollBy((SIZE * GameModel.XLENGTH) /2, (SIZE * GameModel.XLENGTH) /2);
            hsv.smoothScrollBy((SIZE * GameModel.XLENGTH) /2, (SIZE * GameModel.XLENGTH) /2);
        });
    }

    private void setButtonListeners() {
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        Button btnPlay = getView().findViewById(R.id.btn_play);
        btnDraw.setOnClickListener(this::setOnDraw);
        btnPlay.setOnClickListener(this:: setOnPlay);
    }

    private void setupPlayersStatus() {
        GridView gvPlayersStatus = getView().findViewById(R.id.gv_players_status);
        statusAdapter = new StatusAdapter(getActivity(), GameModel.players);
        gvPlayersStatus.setAdapter(statusAdapter);
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

    private void setupCurrentPlayer() {
        GridView gvPlayersStatus = getActivity().findViewById(R.id.gv_players_status);
        
        for (int i = 0; i < gvPlayersStatus.getChildCount(); i++) {
            CardView cardView = (CardView) gvPlayersStatus.getChildAt(i);
            TextView textView = cardView.findViewById(R.id.tv_player_name);
            ImageView imageView = cardView.findViewById(R.id.iv_player_avatar);
            String playerName = imageView.getTag().toString();
            if (playerName.equals(GameModel.currentPlayer.name.toString())) {
                textView.setText("> " + playerName);
                textView.setTextColor(Color.BLUE);
            }
            else {
                textView.setText(playerName);
                textView.setTextColor(getActivity().getColor(R.color.white));
            }
        }

        if (GameModel.isTurn()) {
            ConstraintLayout constraintLayout = getView().findViewById(R.id.cl_fragment_game);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setStroke(4, getActivity().getResources().getIdentifier(GameModel.currentPlayer.color, "color", getActivity().getPackageName()));
            constraintLayout.setBackground(gradientDrawable);
        }
    }

    private void setupBagCount() {
        TextView tvTileCount = getView().findViewById(R.id.tv_tileCount);
        tvTileCount.setText(GameModel.geBagCount() + "");
    }

    private void updatePlayerScore() {
        statusAdapter.updatePlayerScore(GameModel.currentPlayer);
    }

    private void setupGridLayout() {
        GridLayout glBoard = getView().findViewById(R.id.board);
        glBoard.setColumnCount(GameModel.XLENGTH);
        glBoard.setRowCount(GameModel.YLENGTH);
        populate(glBoard);
    }

    private void populate(GridLayout grid) {
        grid.removeAllViews();

        for (int i = 0; i < GameModel.XLENGTH; i++) {
            for (int j = 0; j < GameModel.YLENGTH; j++) {
                ImageButton button = new ImageButton(getActivity());

                button.setMinimumWidth(SIZE);
                button.setMinimumHeight(SIZE);
                button.setTag(i + "_" + j);
                button.setPadding(0, 0, 0, 0);
                button.setOnClickListener(this::onTileClicked);
                grid.addView(button);
            }
        }
    }

    private void onTileClicked(View view) {
        if (selectedTiles.size() > 0) {
            ImageButton button = (ImageButton) view;
            String[] rowCol = button.getTag().toString().split("_");
            int row_no = Integer.parseInt(rowCol[1]);
            int col_no = Integer.parseInt(rowCol[0]);
            GameModel.Legality legality = GameModel.place(row_no, col_no, selectedTiles.get(0));
            if (legality == GameModel.Legality.LEGAL) {
                updateTags();
                imageAdapter.notifyDataSetChanged();

                // set image resource to the tile selected by a player
                button.setForeground(getDrawable(selectedTiles.get(0).toString()));

                // no further interaction allowed
                button.setEnabled(false);
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

    public void setButtons() {
        Button btnSend = getView().findViewById(R.id.btn_play);
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        if (GameModel.player.name != GameModel.currentPlayer.name) {
            btnSend.setEnabled(false);
            btnDraw.setEnabled(false);
        }
        else {
            btnSend.setEnabled(true);
            btnDraw.setEnabled(true);
        }
    }

    private void setupRecyclerView() {
        RecyclerView rvPlayerTilesView = getView().findViewById(R.id.player_tiles);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvPlayerTilesView.setLayoutManager(layoutManager);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));


        class SerializableViewTreeObserver implements ViewTreeObserver.OnGlobalLayoutListener, Serializable {
            @Override
            public void onGlobalLayout() {
                rvPlayerTilesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupCurrentPlayer();
            }
        }

        rvPlayerTilesView.getViewTreeObserver().addOnGlobalLayoutListener(new SerializableViewTreeObserver());

        imageAdapter = new ImageAdapter(GameModel.player.tiles, getActivity());
        imageAdapter.setOnClickListener((IaOnClickListener) this::iaOnclickListener);

        imageAdapter.setOnLongClickListener((IaOnLongClickListener) this::iaLongClickListener);

        rvPlayerTilesView.setAdapter(imageAdapter);
    }

    public interface IaOnClickListener extends Serializable, View.OnClickListener {

    }

    public interface IaOnLongClickListener extends Serializable, View.OnLongClickListener {

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

        Tile selectedTile = imageAdapter.tiles.get(Integer.parseInt(imageView.getTag().toString()));
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
            GameModel.play();
            updatePlayerScore();
            GameModel.turn();

            setupBagCount();
            updatePlayerTiles(GameModel.currentPlayer.tiles);
            setupCurrentPlayer();
            resetMultiSelect();

            if (GameModel.currentPlayer.tiles.size() == 0)
                gameFinished();
        }
    }

    public void gameFinished() {
        Intent intent = new Intent(getActivity(), EndActivity.class);
        intent.putExtra("winner", GameModel.getWinner());
        startActivity(intent);
    }

    public void setOnDraw(View view) {
        if (GameModel.geBagCount() > 0) {
            if (selectedTiles.size() > 0)
                GameModel.draw(false, selectedTiles);
            else
                GameModel.draw(false, null);
            GameModel.turn();
            updatePlayerTiles(GameModel.currentPlayer.tiles);

            setupBagCount();
            resetWidthExcept(null);
            setupCurrentPlayer();
            resetMultiSelect();
            undoPlacedTiles(GameModel.places);
        }
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

    public void updatePlayerTiles(List<Tile> selectedTiles) {
            imageAdapter.updateTiles(GameModel.currentPlayer.tiles);
    }
}
