package za.nmu.wrpv.qwirkle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.gridlayout.widget.GridLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public GameModel model;
    List<Fragment> fragments;
    private final String TAG = "game";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                model = new GameModel(Integer.parseInt(extras.getString("playerCount")), this);
                setupViewPager(model);
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Forfeit Game")
                .setMessage("Are you sure you want to forfeit this game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupViewPager(GameModel model) {
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        fragments = new ArrayList<>(Arrays.asList(new GameFragment(model), new MessagesFragment(model)));
        PagerAdapter adapter = new PagerAdapter(this, fragments);
        viewPager2.setAdapter(adapter);
    }
}