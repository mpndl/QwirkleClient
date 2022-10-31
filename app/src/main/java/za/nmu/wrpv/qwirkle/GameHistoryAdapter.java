package za.nmu.wrpv.qwirkle;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.GameItemHolder> {
    private final List<Game> games;
    private Activity context;

    public GameHistoryAdapter(List<Game> games, Activity context) {
        this.games = games;
        this.context = context;

        Helper.initializeTileSizes(context);
    }

    @NonNull
    @Override
    public GameItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_item, parent, false);
        return new GameItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameItemHolder holder, int position) {
        Game game = games.get(position);
        holder.set(game);

        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.messages.getContext(), LinearLayoutManager.VERTICAL, true);

        MessagesAdapter adapter = new MessagesAdapter(game.messages, game.player);
        holder.messages.setAdapter(adapter);

        holder.messages.setLayoutManager(layoutManager);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public void add(Game game) {
        games.add(game);
    }

    public class GameItemHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final RecyclerView messages;
        private final TableLayout players;
        private final TextView imsgTitle;
        public GameItemHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tv_game_date);
            messages = itemView.findViewById(R.id.rv_game_history_imessages);
            players = itemView.findViewById(R.id.tl_game_history_score_board);
            imsgTitle = itemView.findViewById(R.id.tv_imessages);
        }
        protected void set(Game game) {
            String time = new SimpleDateFormat("HH:mm").format(game.date);
            String date = new SimpleDateFormat("YYYY/MM/dd").format(game.date);
            this.date.setText(date + " " + time);
            this.date.setTextSize(Helper.PLAYER_TILE_SIZE_50/4);
            imsgTitle.setTextSize(Helper.PLAYER_TILE_SIZE_50/4);

                game.players.sort((player, t1) -> -Integer.compare(player.points, t1.points));

            for (int i = 0; i < game.players.size(); i++) {
                TableRow row = (TableRow) players.getChildAt(i+1);
                TextView player = (TextView) row.getChildAt(0);
                TextView score = (TextView) row.getChildAt(1);
                if (game.players.get(i).name == game.player.name)
                    player.setText(R.string.you);
                else player.setText(game.players.get(i).name + "");
                score.setText(game.players.get(i).points + "");

                row.setBackgroundColor(Helper.getColor(game.players.get(i), itemView.getContext()));
            }
        }
    }
}
