package za.nmu.wrpv.qwirkle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.viewpager2.widget.ViewPager2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity implements Serializable {
    private List<Fragment> fragments;
    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        thread = new Thread(() -> {
            do {
                Map<String, Object> data = new HashMap<>();
                data.put("context", this);
                try {
                    Run run = runs.take();
                    runOnUiThread(() -> run.run(data));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (true);
        });
        thread.start();

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey("bundle")) {
                    Bundle bundle = extras.getBundle("bundle");
                    Player currentPlayer = (Player) bundle.get("currentPlayer");
                    List<Tile> bag = (List<Tile>) bundle.get("bag");
                    List<Player> players = (List<Player>)  bundle.get("players");

                    GameModel.currentPlayer = currentPlayer;
                    GameModel.bag = bag;
                    GameModel.players = players;
                    GameModel.clientPlayer = getPlayer(GameModel.clientPlayerName, players);
                    setupViewPager();
                }
            }
        }
    }

    private static Player getPlayer(String name, List<Player> players) {
        return (Player) players.stream().filter(player -> player.name.toString().equals(name)).toArray()[0];
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(thread.isAlive()) thread.interrupt();
    }

    @Override
    public void onBackPressed() {
        /*String titleForfeit = getResources().getString(R.string.title_forfeit);
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
                        ((GameFragment)fragments.get(0)).scoreAdapter.players.remove(oldPlayer);
                        ((GameFragment)fragments.get(0)).scoreAdapter.notifyDataSetChanged();
                    }
                    else finish();
                })
                .setNegativeButton(no, null)
                .show();*/
    }

    private void setupViewPager() {
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        fragments = new ArrayList<>(Arrays.asList(GameFragment.newInstance(), MessagesFragment.newInstance()));
        PagerAdapter adapter = new PagerAdapter(this, fragments);
        viewPager2.setAdapter(adapter);
    }

    public static void runLater(Run run) {
        runs.add(run);
    }
}