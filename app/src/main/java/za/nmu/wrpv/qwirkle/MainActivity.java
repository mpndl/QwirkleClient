package za.nmu.wrpv.qwirkle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.client.Stop;

public class MainActivity extends AppCompatActivity implements Serializable {
    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;
    private Thread internetTestThread;
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
                    run.run(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (true);
        });
        thread.start();

        internetTestThread = new Thread(() -> {
            try {
                do {
                    Thread.sleep(1000);
                    if (!Helper.isOnline()) {
                        BeginActivity.runLater(d -> {
                            Activity context = (Activity) d.get("context");
                            Objects.requireNonNull(context).runOnUiThread(() -> Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show());
                        });
                        new Stop().apply();
                    }
                } while (true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        //internetTestThread.start();

        setupViewPager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(thread.isAlive()) thread.interrupt();
        if (internetTestThread.isAlive()) thread.interrupt();
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
                    Intent intent = new Intent(getApplicationContext(), BeginActivity.class);
                    intent.putExtra("clear", true);
                    ServerHandler.stop();
                    startActivity(intent);
                })
                .setNegativeButton(no, null)
                .show();
    }

    private void setupViewPager() {
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        List<Fragment> fragments = new ArrayList<>(Arrays.asList(GameFragment.newInstance(), MessagesFragment.newInstance()));
        PagerAdapter adapter = new PagerAdapter(this, fragments);
        viewPager2.setAdapter(adapter);
    }

    public <F extends Fragment> void switchFragment(Class<F> clazz) {
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        if (clazz.equals(GameFragment.class))
            viewPager2.setCurrentItem(0);
        else viewPager2.setCurrentItem(1);
    }

    public static void runLater(Run run) {
        runs.add(run);
    }
}