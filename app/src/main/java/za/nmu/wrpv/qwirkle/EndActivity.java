package za.nmu.wrpv.qwirkle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EndActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Player winner = (Player) bundle.getSerializable("winner");
                TextView textView = findViewById(R.id.tv_winner);
                textView.setTextColor(getResources().getIdentifier(winner.color, "color", getPackageName()));
                textView.setText(getResources().getString(R.string.congrats, winner.name, winner.points + ""));
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), BeginActivity.class);
        startActivity(intent);
    }
}