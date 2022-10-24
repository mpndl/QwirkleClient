package za.nmu.wrpv.qwirkle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class ScoreAdapter extends ArrayAdapter<Player> implements Serializable {
    public List<Player> players;
    public Context context;
    public ScoreAdapter(@NonNull Context context, List<Player> players) {
        super(context, 0, players);
        this.players = players;
        this.context = context;
    }

    public void updatePlayerScore(Player player) {
        for (Player p: players) {
            if (p.name.toString().equals(player.name.toString())) {
                p.points = player.points;
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void remove(Player player) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.name == player.name) {
                players.remove(i);
                notifyDataSetChanged();
                return;
            }
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.game_status, parent, false);

        ImageView imageView = listItemView.findViewById(R.id.iv_player_avatar);
        Player player = getItem(position);
        TextView tvScore = listItemView.findViewById(R.id.tv_player_score);
        imageView.setTag(context.getString(R.string.you) + "," + player.name.toString());

        listItemView.setBackgroundColor(getColor(player, context));
        tvScore.setText(getContext().getString(R.string.score, player.points + ""));
        return listItemView;
    }

    public static int getColor(Player player, Context context) {
        switch (player.color) {
            case "red":
                return context.getColor(R.color.red);
            case "green":
                return context.getColor(R.color.green);
            case "blue":
                return context.getColor(R.color.blue);
            case "purple":
                return context.getColor(R.color.purple);
        }
        return 0;
    }
}
