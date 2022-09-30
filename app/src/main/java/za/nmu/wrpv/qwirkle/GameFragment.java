package za.nmu.wrpv.qwirkle;

import static android.content.Context.VIBRATOR_SERVICE;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GameFragment extends Fragment {
    public GameModel model;
    private ImageAdapter imageAdapter;
    public StatusAdapter statusAdapter;
    private ArrayList<Tile> selectedTiles = new ArrayList<>();
    private ArrayList<Tile> placedTiles = new ArrayList<>();
    private boolean multiSelect = false;
    private final String TAG = "game";
    private boolean multiSelected = false;

    private final int SIZE = 100;

    public GameFragment(GameModel model) {
        this.model = model;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupGridLayout();
        setupRecyclerView();
        setupPlayersStatus();
        setupBagCount();
        setButtonListeners();
    }

    private void setButtonListeners() {
        Button btnDraw = getView().findViewById(R.id.btn_draw);
        Button btnPlay = getView().findViewById(R.id.btn_play);
        btnDraw.setOnClickListener(this::setOnDraw);
        btnPlay.setOnClickListener(this:: setOnPlay);
    }

    private void setupPlayersStatus() {
        GridView gvPlayersStatus = getView().findViewById(R.id.gv_players_status);
        statusAdapter = new StatusAdapter(getActivity(), model.players);
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
            if (playerName.equals(model.cPlayer.name.toString())) {
                textView.setTextColor(Color.BLUE);
            }
            else {
                textView.setTextColor(getActivity().getColor(R.color.white));
            }
        }

        ConstraintLayout constraintLayout = getView().findViewById(R.id.cl_fragment_game);
        GradientDrawable gradientDrawable=new GradientDrawable();
        gradientDrawable.setStroke(4,model.cPlayer.color);
        constraintLayout.setBackground(gradientDrawable);
    }

    private void setupBagCount() {
        TextView tvTileCount = getView().findViewById(R.id.tv_tileCount);
        tvTileCount.setText(model.geBagCount() + "");
    }

    private void updatePlayerScore() {
        statusAdapter.updatePlayerScore(model.cPlayer);
    }

    private void setupGridLayout() {
        GridLayout glBoard = getView().findViewById(R.id.board);
        glBoard.setColumnCount(model.XLENGTH);
        glBoard.setRowCount(model.YLENGTH);
        populate(glBoard);
    }

    private void populate(GridLayout grid) {
        grid.removeAllViews();
        for (int i = 0; i < model.XLENGTH; i++) {
            for (int j = 0; j < model.YLENGTH; j++) {
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
            GameModel.Legality legality = model.place(row_no, col_no, selectedTiles.get(0));
            if (legality == GameModel.Legality.LEGAL) {
                imageAdapter.removeItem(selectedTiles.get(0));
                updateTags();

                /*Drawable drawable = getResources().getDrawable(getDrawable(selectedTiles.get(0).toString()));
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, button.getWidth()/3, button.getHeight()/3, true));
*/
                // set image resource to the tile selected by a player
                button.setImageResource(getDrawable(selectedTiles.get(0).toString()));

                // no further interaction allowed
                button.setEnabled(false);
            }
        }

        resetWidthExcept(null);
        selectedTiles = new ArrayList<>();
        multiSelect = false;
    }

    private void updateTags() {
        RecyclerView rvPlayerTiles = getView().findViewById(R.id.player_tiles);
        for (int i = 0; i < rvPlayerTiles.getChildCount(); i++) {
            ConstraintLayout constraintLayout = (ConstraintLayout) rvPlayerTiles.getChildAt(i);
            ImageView curImageView = (ImageView) constraintLayout.getChildAt(0);
            curImageView.setTag(i);
        }
    }

    private int getDrawable(String name) {
        return getResources().getIdentifier(name, "drawable", getContext().getPackageName());
    }

    private void setupRecyclerView() {
        RecyclerView rvPlayerTilesView = getView().findViewById(R.id.player_tiles);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvPlayerTilesView.setLayoutManager(layoutManager);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

        rvPlayerTilesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rvPlayerTilesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupCurrentPlayer();
            }
        });

        imageAdapter = new ImageAdapter(model.cPlayer.tiles, getActivity());
        imageAdapter.setOnClickListener(view -> {
            ImageView imageView = view.findViewById(R.id.iv_tile);
            updateTags();

            // multi-select feature
            if (selectedTiles.size() > 1)
                multiSelected = true;
            if (selectedTiles.size() < 2 && multiSelected) {
                multiSelect = false;
                multiSelected = false;
            }

            Tile selectedTile = model.cPlayer.tiles.get(Integer.parseInt(imageView.getTag().toString()));
            placedTiles.add(selectedTile);
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

        });

        imageAdapter.setOnLongClickListener(view -> {
            multiSelect = !multiSelect;
            vibrate(50);
            resetWidthExcept(null);
            if (selectedTiles == null)
                selectedTiles = new ArrayList<>();
            ImageView imageView = view.findViewById(R.id.iv_tile);
            Tile selectedTile = model.cPlayer.tiles.get(Integer.parseInt(imageView.getTag().toString()));
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
        });

        rvPlayerTilesView.setAdapter(imageAdapter);
    }

    public void setOnPlay(View view) {
        if(model.places.size() > 0) {
            model.recover();
            model.play();
            updatePlayerScore();
            model.turn();
            setupBagCount();
            selectedTiles = (ArrayList<Tile>) model.cPlayer.tiles.clone();
            updatePlayerTiles(selectedTiles);
            setupCurrentPlayer();
            resetMultiSelect();
            placedTiles = new ArrayList<>();
        }
    }

    public void setOnDraw(View view) {
        if (view != null) {
            imageAdapter.tiles.addAll(placedTiles);
            model.cPlayer.tiles.addAll(placedTiles);
            imageAdapter.notifyDataSetChanged();
            model.turn();
            selectedTiles = (ArrayList<Tile>) model.cPlayer.tiles.clone();
            model.draw(selectedTiles);
            updatePlayerTiles(selectedTiles);
            setupBagCount();
            resetWidthExcept(null);
            setupCurrentPlayer();
            resetMultiSelect();
            placedTiles = new ArrayList<>();
            undoTiles(model.places);
            model.tempBoard = null;
        }
    }

    private void undoTiles(ArrayList<Tile> selectedTiles) {
        GridLayout glBoard = getView().findViewById(R.id.board);
        for (Tile tile: selectedTiles) {
            int index = tile.yPos * model.XLENGTH + tile.xPos;
            ImageButton imageButton2 = (ImageButton) glBoard.getChildAt(index);

            ImageButton button = new ImageButton(getActivity());
            button.setMinimumWidth(SIZE);
            button.setMinimumHeight(SIZE);
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

    public void updatePlayerTiles(ArrayList<Tile> selectedTiles) {
        if (selectedTiles.size() == 0 || selectedTiles.size() == 6)
            imageAdapter.updateTiles(model.cPlayer.tiles);
    }
}
