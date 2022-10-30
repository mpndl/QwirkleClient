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
import android.widget.EditText;

import za.nmu.wrpv.qwirkle.messages.client.Countdown;

public class BeginFragment extends Fragment {
    public static boolean startGame = true;
    public static BeginFragment newInstance() {
        return new BeginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_begin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = getActivity().getPreferences(MODE_PRIVATE);
        String serverAddress = preferences.getString("server_address", "");

        EditText etPlayerCount = getView().findViewById(R.id.et_server_address);
        etPlayerCount.setHint("X.X.X.X");
        etPlayerCount.setText(serverAddress);

        etPlayerCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Button btnStartGame = getView().findViewById(R.id.btn_start_game);
                btnStartGame.setEnabled(!charSequence.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button btnStartGame = getView().findViewById(R.id.btn_start_game);
        btnStartGame.setOnClickListener(this::onStartGame);
    }

    public void onStartGame(View view) {
        Button btnStartGame = (Button)view;
        if (startGame) {
            EditText etServerAddress = getView().findViewById(R.id.et_server_address);

            getActivity().getPreferences(MODE_PRIVATE).edit().putString("server_address", etServerAddress.getText().toString()).apply();
            ServerHandler.serverAddress = etServerAddress.getText().toString();
            ServerHandler.start();
            startGame = false;
        }
        else {
            ServerHandler.interrupt();
            Countdown.interrupt();
            btnStartGame.setText(R.string.btn_start_game);
            startGame = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Button btnStartGame = getView().findViewById(R.id.btn_start_game);
        btnStartGame.setText(R.string.btn_start_game);
    }
}