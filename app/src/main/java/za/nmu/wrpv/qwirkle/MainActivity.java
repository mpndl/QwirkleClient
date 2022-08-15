package za.nmu.wrpv.qwirkle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public GameModel gameModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);gameModel = new GameModel(4, findViewById(R.id.tiles), this);

    }

    public void setOnPlay(View view) {
        if(gameModel.places.size() > 0) {
            TextView tvLegal = findViewById(R.id.legal);
            gameModel.recover();
            ArrayList<Tile> tiles = gameModel.play();
            if (tiles != null)
                for (Tile tile : tiles)
                    tvLegal.setText(tvLegal.getText().toString() + "(" + tile.color + "," + tile.shape + ")(" + tile.xPos + "," + tile.yPos + "), ");
            gameModel.turn();
            gameModel.showCPlayerTiles();
        }
    }

    public void setOnDraw(View view) {
        gameModel.draw(addAll());
        if (gameModel.places.size() == 0)
            gameModel.recover();
        gameModel.turn();
        gameModel.showCPlayerTiles();
    }

    private ArrayList<Tile> addAll() {
        ArrayList<Tile> ts = new ArrayList<>();
        ts.addAll(gameModel.cPlayer.tiles);
        return ts;
    }

    public void setOnPlace(View view) {
        EditText etXPos = findViewById(R.id.xPos);
        EditText etYPos = findViewById(R.id.yPos);
        EditText etIndex = findViewById(R.id.index);
        int xPos = Integer.parseInt(etXPos.getText().toString());
        int yPos = Integer.parseInt(etYPos.getText().toString());
        int index = Integer.parseInt(etIndex.getText().toString());
        if(!gameModel.backedup) {
            gameModel.backedup = true;
            gameModel.backup();
        }
        gameModel.place(xPos, yPos, get(index));
        gameModel.showCPlayerTiles();
    }

    private Tile get(int i) {
        if (i >= 0 && i < gameModel.cPlayer.tiles.size())
            return gameModel.cPlayer.tiles.get(i);
        return null;
    }
}