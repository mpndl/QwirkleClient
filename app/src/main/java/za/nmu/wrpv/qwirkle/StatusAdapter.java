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

import java.util.ArrayList;

public class StatusAdapter extends ArrayAdapter<Player> {
    public StatusAdapter(@NonNull Context context, ArrayList<Player> players) {
        super(context, 0, players);
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

        tvScore.setText(getContext().getString(R.string.score, player.points + ""));
        return listItemView;
    }
}
