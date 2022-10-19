package za.nmu.wrpv.qwirkle;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BeginActivity extends AppCompatActivity {
    private boolean waiting = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        EditText etPlayerCount = findViewById(R.id.et_server_address);
        etPlayerCount.setHint("X.X.X.X");
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

    public void onStartGame(View view) {
        Button btnStartGame = (Button)view;
        ServerHandler.activity = this;
        if (btnStartGame.getText().toString().equals(getString(R.string.btn_start_game))) {
            EditText etServerAddress = findViewById(R.id.et_server_address);
            ServerHandler.serverAddress = etServerAddress.getText().toString();
            ServerHandler.start();
        }
        else {
            ServerHandler.stop();
            btnStartGame.setText(R.string.btn_start_game);
        }
    }
}
