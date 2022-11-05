package za.nmu.wrpv.qwirkle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.server.Join;
import za.nmu.wrpv.qwirkle.messages.server.Rejoin;

public class BeginActivity extends AppCompatActivity {
    public static int track = 0;
    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        System.out.println("------------------------- RETRIEVING PREVIOUS GAME INFORMATION");
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        int clientID = preferences.getInt("clientID", -1);
        int gameID = preferences.getInt("gameID", -1);
        System.out.println("clientID = "+ clientID + ", gameID = " + gameID);
        System.out.println("----------------------------------------------------------------------------");

        ServerHandler.serverAddress = getPreferences(MODE_PRIVATE).getString("server_address", null);

        ServerHandler.start();
        if (clientID != -1 && gameID != -1) {
            Rejoin message = new Rejoin();
            message.put("clientID", clientID);
            message.put("gameID", gameID);
            ServerHandler.send(message);
        }

        thread = new Thread(() -> {
            do {
                Map<String, Object> data = new HashMap<>();
                data.put("context", this);
                try {
                     Run run = runs.take();
                     run.run(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (true);
        });
        thread.start();

        ViewPager2 viewPager2 = findViewById(R.id.main_viewpager);
        List<Fragment> fragments = new ArrayList<>(Arrays.asList(BeginFragment.newInstance(), GameHistoryFragment.newInstance()));
        BeginPagerAdapter adapter = new BeginPagerAdapter(this, fragments);
        viewPager2.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey("clear")) {
                    System.out.println("------------------------- CLEARING PREVIOUS GAME INFORMATION");
                    System.out.println("clientID = "+ preferences.getInt("clientID", -1) + ", gameID = " + preferences.getInt("gameID", -1));
                    System.out.println("----------------------------------------------------------------------------");

                    preferences.edit().remove("clientID").apply();
                    preferences.edit().remove("gameID").apply();
                }

                if (bundle.containsKey("history")) viewPager2.setCurrentItem(1);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread.isAlive()) thread.interrupt();
    }

    public static void runLater(Run run) {
        runs.add(run);
    }
}
