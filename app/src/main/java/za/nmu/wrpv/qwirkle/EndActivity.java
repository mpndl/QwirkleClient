package za.nmu.wrpv.qwirkle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import static za.nmu.wrpv.qwirkle.Helper.getColor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class EndActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                List<Player> players = (List<Player>) bundle.getSerializable("players");
                players.sort((player, t1) -> -Integer.compare(player.points, t1.points));

                TableLayout scoreBoard = findViewById(R.id.tl_score_board);
                for (int i = 0; i < players.size(); i++) {
                    TableRow row = (TableRow) scoreBoard.getChildAt(i+1);
                    TextView player = (TextView) row.getChildAt(0);
                    TextView score = (TextView) row.getChildAt(1);
                    player.setText(players.get(i).name + "");
                    score.setText(players.get(i).points + "");

                    row.setBackgroundColor(Helper.getColor(players.get(i), this));
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), BeginActivity.class);
        startActivity(intent);
    }
}
