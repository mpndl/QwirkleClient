package za.nmu.wrpv.qwirkle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public GameModel gameModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameModel = new GameModel(4);

    }

    public void setOnPlay(View view) {

        EditText etXPos = findViewById(R.id.xPos);
        EditText etYPos = findViewById(R.id.yPos);
        TextView tvLegal = findViewById(R.id.legal);

        int xPos = Integer.parseInt(etXPos.getText().toString());
        int yPos = Integer.parseInt(etYPos.getText().toString());

        Tile cPlayerTile = gameModel.cPlayer.tiles.get(0);
        GameModel.Legality legality = gameModel.play(xPos, yPos, cPlayerTile);
        if(legality == GameModel.Legality.LEGAL)
            tvLegal.setText(tvLegal.getText().toString() + "(" + gameModel.board[xPos][yPos].color + "," + gameModel.board[xPos][yPos].shape + "), ");
        else
            Toast.makeText(this, "ILLEGAL", Toast.LENGTH_LONG).show();
    }

    public void setOnDraw(View view) {
        gameModel.draw(gameModel.cPlayer.tiles.get(0), gameModel.cPlayer.tiles.get(1), gameModel.cPlayer.tiles.get(2), gameModel.cPlayer.tiles.get(3), gameModel.cPlayer.tiles.get(4), gameModel.cPlayer.tiles.get(5));
        gameModel.turn();
    }
}