package za.nmu.wrpv.qwirkle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.gridlayout.widget.GridLayout;

import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public GameModel model;
    private ImageAdapter imageAdapter;
    private StatusAdapter statusAdapter;
    private ArrayList<Tile> selectedTiles = new ArrayList<>();
    private ArrayList<Tile> placedTiles = new ArrayList<>();
    private boolean multiSelect = false;
    private final String TAG = "game";
    private boolean multiSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                model = new GameModel(Integer.parseInt(extras.getString("playerCount")), this);

                setupPlayersStatus();
                setupGridLayout();
                setupRecyclerView();
                setupBagCount();

                new Thread(() -> {
                    int i = 0;
                    boolean set = false;
                    while (!set && i < 5) {
                        try {
                            Thread.sleep(1000);
                            setupCurrentPlayer();
                            set = true;
                        } catch (Exception ignored) {
                        }
                        i++;
                    }
                }).start();
            }
        }
    }



    private void resetWidthExcept(ImageView imageView) {
        RecyclerView rvPlayerTiles = findViewById(R.id.player_tiles);
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
        GridView gvPlayersStatus = findViewById(R.id.gv_players_status);

        for (int i = 0; i < gvPlayersStatus.getChildCount(); i++) {
            CardView cardView = (CardView) gvPlayersStatus.getChildAt(i);
            ImageView imageView = cardView.findViewById(R.id.iv_player_avatar);
            String playerName = imageView.getTag().toString();
            if (playerName.equals(model.cPlayer.name.toString())) {
                cardView.setCardBackgroundColor(getColor(R.color.purple_200));
            }
            else {
                cardView.setCardBackgroundColor(getColor(R.color.blue));
            }
        }
    }

    private void setupPlayersStatus() {
        GridView gvPlayersStatus = findViewById(R.id.gv_players_status);
        statusAdapter = new StatusAdapter(this, (ArrayList<Player>) model.players.clone());
        gvPlayersStatus.setAdapter(statusAdapter);
    }

    private void setupBagCount() {
        TextView tvTileCount = findViewById(R.id.tv_tileCount);
        tvTileCount.setText(model.geBagCount() + "");
    }

    private void updatePlayerScore() {
        statusAdapter.updatePlayerScore(model.cPlayer);
    }

    private void setupGridLayout() {
        GridLayout glBoard = findViewById(R.id.board);
        glBoard.setColumnCount(model.XLENGTH);
        glBoard.setRowCount(model.YLENGTH);
        populate(glBoard);
    }

    private void populate(GridLayout grid) {
        grid.removeAllViews();
        for (int i = 0; i < model.XLENGTH; i++) {
            for (int j = 0; j < model.YLENGTH; j++) {
                ImageButton button = new ImageButton(this);
                button.setMinimumWidth(90);
                button.setMinimumHeight(90);
                button.setTag(i + "_" + j);
                button.setPadding(0, 0, 0, 0);
                button.setOnClickListener(this::onTileClicked);
                grid.addView(button);
            }
        }
    }

    private void onTileClicked(View view) {
        if (selectedTiles.size() > 0) {
            ImageButton imageButton = (ImageButton) view;
            String[] rowCol = imageButton.getTag().toString().split("_");
            int row_no = Integer.parseInt(rowCol[1]);
            int col_no = Integer.parseInt(rowCol[0]);
            GameModel.Legality legality = model.place(row_no, col_no, selectedTiles.get(0));
            if (legality == GameModel.Legality.LEGAL) {
                imageAdapter.removeItem(selectedTiles.get(0));
                updateTags();
                // set image resource to the tile selected by a player
                imageButton.setImageResource(getDrawable(selectedTiles.get(0).toString()));
                // no further interaction allowed
                imageButton.setEnabled(false);
            }
        }

        resetWidthExcept(null);
        selectedTiles = new ArrayList<>();
        multiSelect = false;
    }

    private void updateTags() {
        RecyclerView rvPlayerTiles = findViewById(R.id.player_tiles);
        for (int i = 0; i < rvPlayerTiles.getChildCount(); i++) {
            ConstraintLayout constraintLayout = (ConstraintLayout) rvPlayerTiles.getChildAt(i);
            ImageView curImageView = (ImageView) constraintLayout.getChildAt(0);
            curImageView.setTag(i);
        }
    }

    private int getDrawable(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private void setupRecyclerView() {
        RecyclerView rvPlayerTilesView = findViewById(R.id.player_tiles);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPlayerTilesView.setLayoutManager(layoutManager);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

        imageAdapter = new ImageAdapter(model.cPlayer.tiles, getApplicationContext());
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

    public void onPlay(View view) {
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

    private void undoTiles(ArrayList<Tile> selectedTiles) {
        GridLayout glBoard = findViewById(R.id.board);
        for (Tile tile: selectedTiles) {
            int index = tile.yPos * model.XLENGTH + tile.xPos;
            ImageButton imageButton2 = (ImageButton) glBoard.getChildAt(index);

            ImageButton button = new ImageButton(this);
            button.setMinimumWidth(90);
            button.setMinimumHeight(90);
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
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
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