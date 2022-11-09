package za.nmu.wrpv.qwirkle;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.client.Countdown;
import za.nmu.wrpv.qwirkle.messages.server.Join;

public class BeginFragment extends Fragment {
    public static BeginFragment newInstance() {
        return new BeginFragment();
    }
    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_begin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        thread = new Thread(() -> {
            do {
                if (getActivity() == null || !isAdded() || getView() == null || !isVisible()) {
                    continue;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("context", requireContext());
                data.put("fragment", this);
                Run run = null;
                try {
                    run = runs.take();

                    if (getActivity() == null || !isAdded() || getView() == null) {
                        runs.add(run);
                        continue;
                    }

                    run.run(data);
                } catch (NullPointerException e) {
                    if (run != null) {
                        runs.add(run);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        });
        thread.start();

        SharedPreferences preferences = requireActivity().getPreferences(MODE_PRIVATE);
        String serverAddress = preferences.getString("server_address", null);

        EditText etPlayerCount = requireView().findViewById(R.id.et_server_address);
        etPlayerCount.setHint("X.X.X.X");
        etPlayerCount.setText(serverAddress);

        etPlayerCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Button btnStartGame = requireView().findViewById(R.id.btn_start_game);
                btnStartGame.setEnabled(!charSequence.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etPlayerCount.setEnabled(false);
        SwitchMaterial sLan = requireView().findViewById(R.id.s_lan);
        sLan.setOnCheckedChangeListener((compoundButton, b) -> {
            etPlayerCount.setEnabled(b);
        });

        Button btnStartGame = requireView().findViewById(R.id.btn_start_game);
        btnStartGame.setOnClickListener(this::onStartGame);

        if (!ServerHandler.running()) btnStartGame.setText(R.string.connect);
        else btnStartGame.setText(R.string.btn_start_game);
    }

    public static void runLater(Run run) {
        runs.add(run);
    }

    public void onStartGame(View view) {
        Button btnStartGame = (Button)view;

        EditText etServerAddress = requireView().findViewById(R.id.et_server_address);

        SwitchMaterial sLan = requireView().findViewById(R.id.s_lan);
        if (sLan.isChecked() && !ServerHandler.serverAddress.equals(etServerAddress.getText().toString())) {
            requireActivity().getPreferences(MODE_PRIVATE).edit().putString("server_address", etServerAddress.getText().toString()).apply();
            ServerHandler.serverAddress = etServerAddress.getText().toString();
            ServerHandler.restart();
        }

        SharedPreferences preferences  = requireActivity().getPreferences(MODE_PRIVATE);
        int curClientID = preferences.getInt("curClientID", -2);
        int prevClientID = preferences.getInt("prevClientID", -3);
        int gameID = preferences.getInt("gameID", -1);

        Join message = new Join();
        message.put("clientID", curClientID);
        message.put("prevClientID", prevClientID);
        message.put("gameID", gameID);
        ServerHandler.send(message);
    }

    @Override
    public void onResume() {
        super.onResume();
        Button btnStartGame = requireView().findViewById(R.id.btn_start_game);
        btnStartGame.setText(R.string.btn_start_game);
    }
}