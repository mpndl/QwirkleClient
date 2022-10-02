package za.nmu.wrpv.qwirkle;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessagesFragment extends Fragment implements Serializable {
    private GameModel model;
    private PlayerMessageAdapter playerMessageAdapter;

    public static MessagesFragment newInstance(GameModel model) {
        MessagesFragment messagesFragment = new MessagesFragment();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable("model", model);
        messagesFragment.setArguments(bundle);
        return messagesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        model = (GameModel) getArguments().getSerializable("model");

        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecycleView();
        setupListeners();
    }

    private void setupListeners() {
        Button btnSendMessage = getView().findViewById(R.id.btn_send_message);
        EditText etMessage = getView().findViewById(R.id.et_message);
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btnSendMessage.setEnabled(!charSequence.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnSendMessage.setOnClickListener(view -> {
            PlayerMessage playerMessage = new PlayerMessage();
            playerMessage.player = model.cPlayer;
            playerMessage.message = etMessage.getText().toString();

            playerMessage.time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            model.insertPlayerMessage(playerMessage);
            playerMessageAdapter.notifyItemInserted(playerMessageAdapter.playerMessages.size() - 1);
            etMessage.setText("");
        });
    }

    private void setupRecycleView() {
        RecyclerView rvPlayerTilesView = getView().findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvPlayerTilesView.setLayoutManager(layoutManager);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

        playerMessageAdapter = new PlayerMessageAdapter(model.playerMessages, getContext());
        rvPlayerTilesView.setAdapter(playerMessageAdapter);
    }
}
