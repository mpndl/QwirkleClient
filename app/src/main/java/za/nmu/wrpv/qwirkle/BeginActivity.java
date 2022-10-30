package za.nmu.wrpv.qwirkle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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

public class BeginActivity extends AppCompatActivity {
    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //deleteFile("games.xml");

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
                viewPager2.setCurrentItem(1);
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
