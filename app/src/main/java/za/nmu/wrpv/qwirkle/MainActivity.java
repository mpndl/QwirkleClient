package za.nmu.wrpv.qwirkle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    GameModel gameModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameModel = new GameModel(4);

    }

    public void setOnAdd(View view) {
        EditText etXpos = findViewById(R.id.xPos);
        EditText etYPos = findViewById(R.id.yPos);
        TextView tvLegal = findViewById(R.id.legal);

        GameModel.Legality legality = gameModel.play(Integer.parseInt(etXpos.getText().toString()), Integer.parseInt(etYPos.getText().toString()), gameModel.cPlayer.tiles.get(0));
        tvLegal.setText(legality.toString());
    }
}