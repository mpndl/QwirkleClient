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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import za.nmu.wrpv.qwirkle.messages.client.IMessage;

public class MessagesFragment extends Fragment implements Serializable {
    private static final BlockingDeque<Run> runs = new LinkedBlockingDeque<>();
    private Thread thread;
    public MessagesAdapter adapter;

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
            Notification.cancel(requireContext(), Notification.NOTIFICATION_ID);

            adapter = new MessagesAdapter(GameModel.messages, GameModel.player);

            thread = new Thread(() -> {
                do {
                    if (getActivity() == null || !isAdded() || getView() == null) continue;

                    Map<String, Object> data = new HashMap<>();
                    data.put("adapter", adapter);
                    data.put("context", getActivity());
                    Run run = null;
                    try {
                        run = runs.take();
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
            setupRecycleView();
            setupListeners();
            setupScrollToBottom();
    }

    private void setupListeners() {
            Button btnSendMessage = requireView().findViewById(R.id.btn_send_message);
            EditText etMessage = requireView().findViewById(R.id.et_message);
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
            RecyclerView rvPlayerTilesView = requireView().findViewById(R.id.rv_messages);
            FloatingActionButton actionButton = requireView().findViewById(R.id.fab_scroll_to_bottom);
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
            RecyclerView rvPlayerTilesView = requireView().findViewById(R.id.rv_messages);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            rvPlayerTilesView.setLayoutManager(layoutManager);
            rvPlayerTilesView.addItemDecoration(new EqualSpaceItemDecoration(5));

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setStackFromEnd(true);

            rvPlayerTilesView.setAdapter(adapter);
            rvPlayerTilesView.setLayoutManager(linearLayoutManager);
            rvPlayerTilesView.smoothScrollToPosition(adapter.getItemCount());
    }

    public static void runLater(Run run) {
        runs.add(run);
    }
}
