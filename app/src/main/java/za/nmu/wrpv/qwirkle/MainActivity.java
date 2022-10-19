package za.nmu.wrpv.qwirkle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.viewpager2.widget.ViewPager2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Serializable {
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
                if (extras.containsKey("bundle")) {
                    Bundle bundle = extras.getBundle("bundle");
                    Player currentPlayer = (Player) bundle.get("currentPlayer");
                    List<Tile> bag = (List<Tile>) bundle.get("bag");
                    List<Player> players = (List<Player>)  bundle.get("players");

                    ServerHandler.activity = this;
                    GameModel.currentPlayer = currentPlayer;
                    GameModel.bag = bag;
                    GameModel.players = players;
                    GameModel.player = getPlayer(ServerHandler.playerName, players);

                    setupViewPager();
                }
            }
        }
    }

    private Player getPlayer(String name, List<Player> players) {
        return (Player) players.stream().filter(player -> player.name.toString().equals(name)).toArray()[0];
    }

    @Override
    public void onBackPressed() {
        String titleForfeit = getResources().getString(R.string.title_forfeit);
        String confForfeit = getResources().getString(R.string.conf_forfeit);
        String yes = getResources().getString(R.string.yes);
        String no = getResources().getString(R.string.no);

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(titleForfeit)
                .setMessage(confForfeit)
                .setPositiveButton(yes, (dialog, which) -> {
                    if (GameModel.playerCount() > 2) {
                        Player oldPlayer = GameModel.player;
                        ((GameFragment)fragments.get(0)).setOnDraw(null);
                        ((GameFragment)fragments.get(0)).statusAdapter.players.remove(oldPlayer);
                        ((GameFragment)fragments.get(0)).statusAdapter.notifyDataSetChanged();
                    }
                    else finish();
                })
                .setNegativeButton(no, null)
                .show();
    }

    private void setupViewPager() {
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        fragments = new ArrayList<>(Arrays.asList(GameFragment.newInstance(), MessagesFragment.newInstance()));
        PagerAdapter adapter = new PagerAdapter(this, fragments);
        viewPager2.setAdapter(adapter);
    }
}