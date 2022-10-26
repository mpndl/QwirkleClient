package za.nmu.wrpv.qwirkle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.client.Countdown;

public class BeginActivity extends AppCompatActivity {
    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;
    public static boolean startGame = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

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

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String serverAddress = preferences.getString("server_address", "");

        EditText etPlayerCount = findViewById(R.id.et_server_address);
        etPlayerCount.setHint("X.X.X.X");
        etPlayerCount.setText(serverAddress);

        etPlayerCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Button btnStartGame = findViewById(R.id.btn_start_game);
                btnStartGame.setEnabled(!charSequence.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button btnStartGame = findViewById(R.id.btn_start_game);
        btnStartGame.setText(R.string.btn_start_game);
    }

    public void onStartGame(View view) {
        Button btnStartGame = (Button)view;
        if (startGame) {
            EditText etServerAddress = findViewById(R.id.et_server_address);

            getPreferences(MODE_PRIVATE).edit().putString("server_address", etServerAddress.getText().toString()).apply();
            ServerHandler.serverAddress = etServerAddress.getText().toString();
            ServerHandler.start();
            startGame = false;
        }
        else {
            ServerHandler.stop();
            Countdown.interrupt();
            btnStartGame.setText(R.string.btn_start_game);
            startGame = true;
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
