package za.nmu.wrpv.qwirkle;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameHistoryFragment extends Fragment {
    public GameHistoryAdapter adapter;
    public static GameHistoryFragment newInstance() {
        return new GameHistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

            adapter = new GameHistoryAdapter(new ArrayList<>(), getActivity());

            RecyclerView rvGameHistory = requireView().findViewById(R.id.rv_game_history);
            rvGameHistory.setAdapter(adapter);
            rvGameHistory.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL));

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true);
            layoutManager.setStackFromEnd(true);

            rvGameHistory.setLayoutManager(layoutManager);

            XMLHandler.loadFromXML(data -> {
                Game game = (Game) data.get("game");
                adapter.add(game);
            }, getActivity());
    }
}