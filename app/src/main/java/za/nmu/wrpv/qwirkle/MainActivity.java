package za.nmu.wrpv.qwirkle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public GameModel model;
    private ImageAdapter imageAdapter;
    private Tile[][] board;
    private ArrayList<Tile> tiles;
    private Tile clickedTile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = new GameModel(4, this);
        board = model.board;
        tiles = model.cPlayer.tiles;

        setupGridView();
        setupRecyclerView();
        setupBagCount();
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

    private void setupBagCount() {
        TextView tvTileCount = findViewById(R.id.tv_tileCount);
        tvTileCount.setText(model.geBagCount() + "");
    }

    private void setupGridView() {

    }

    private void setupRecyclerView() {
        RecyclerView rvPlayerTilesView = findViewById(R.id.player_tiles);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPlayerTilesView.setLayoutManager(layoutManager);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

        imageAdapter = new ImageAdapter(tiles, getApplicationContext());
        imageAdapter.setOnClickListener(view -> {
            ImageView imageView = view.findViewById(R.id.iv_tile);
            clickedTile = tiles.get(Integer.parseInt(imageView.getTag().toString()));
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if (params.width == 50)
                params.width = 60;
            else
                params.width = 50;
            resetWidthExcept(imageView);
            imageView.setLayoutParams(params);
        });

        rvPlayerTilesView.setAdapter(imageAdapter);
    }
}