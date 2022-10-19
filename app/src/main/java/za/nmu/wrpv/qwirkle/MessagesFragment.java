package za.nmu.wrpv.qwirkle;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.client.IMessage;

public class MessagesFragment extends Fragment implements Serializable {
    public static final BlockingDeque<Runnable> runs = new LinkedBlockingDeque<>();
    private Thread thread;
    @SuppressLint("StaticFieldLeak")
    public static MessagesAdapter adapter;

    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        thread = new Thread(() -> {
            try {
                do {
                    runs.take().run();
                }while (true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        setupRecycleView();
        setupListeners();
        setupScrollToBottom();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (thread != null && thread.isAlive())
            thread.interrupt();
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
            playerMessage.player = GameModel.player;
            playerMessage.message = etMessage.getText().toString();
            playerMessage.time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            IMessage msg = new IMessage();
            msg.put("message", playerMessage);

            ServerHandler.send(msg);

            etMessage.setText("");
        });
    }

    private void setupScrollToBottom() {
        RecyclerView rvPlayerTilesView = getView().findViewById(R.id.rv_messages);
        FloatingActionButton actionButton = getView().findViewById(R.id.fab_scroll_to_bottom);
        actionButton.setOnClickListener(view -> {
            rvPlayerTilesView.smoothScrollToPosition(adapter.getItemCount());
        });

        rvPlayerTilesView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                actionButton.setEnabled(recyclerView.canScrollVertically(1));
            }
        });
    }

    private void setupRecycleView() {
        RecyclerView rvPlayerTilesView = getView().findViewById(R.id.rv_messages);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvPlayerTilesView.setLayoutManager(layoutManager);
        rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        adapter = new MessagesAdapter(new ArrayList<>());
        GameModel.adapter = adapter;
        rvPlayerTilesView.setAdapter(adapter);
        rvPlayerTilesView.setLayoutManager(linearLayoutManager);
        rvPlayerTilesView.smoothScrollToPosition(adapter.getItemCount());
    }
}
