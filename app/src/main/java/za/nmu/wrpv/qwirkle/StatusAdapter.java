package za.nmu.wrpv.qwirkle;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatusAdapter extends ArrayAdapter<Player> implements Serializable {
    public final List<Player> players;
    public StatusAdapter(@NonNull Context context, List<Player> players) {
        super(context, 0, players);
        this.players = players;
    }

    public void updatePlayerScore(Player player) {
        int i = 0;
        for (Player p: players) {
            if (p.name.toString().equals(player.name.toString())) {
                p.points = player.points;
                players.set(i, p);
                notifyDataSetChanged();
            }
            i++;
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
        TextView tvPlayerName = listItemView.findViewById(R.id.tv_player_name);

        tvPlayerName.setText(player.name.toString());

        imageView.setTag(player.name);

        listItemView.setBackgroundColor(getColor(player));
        tvScore.setText(getContext().getString(R.string.score, player.points + ""));
        return listItemView;
    }

    public static int getColor(Player player) {
        Activity activity = ServerHandler.activity;
        switch (player.color) {
            case "red":
                return activity.getColor(R.color.red);
            case "green":
                return activity.getColor(R.color.green);
            case "blue":
                return activity.getColor(R.color.blue);
            case "purple":
                return activity.getColor(R.color.purple);
        }
        return 0;
    }
}
