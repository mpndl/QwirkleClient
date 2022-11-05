package za.nmu.wrpv.qwirkle;

import static za.nmu.wrpv.qwirkle.Helper.BOARD_TILE_SIZE;
import static za.nmu.wrpv.qwirkle.Helper.getColor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

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
        listItemView.getLayoutParams().height =  BOARD_TILE_SIZE;

        ImageView imageView = listItemView.findViewById(R.id.iv_player_avatar);

        Player player = getItem(position);
        TextView tvScore = listItemView.findViewById(R.id.tv_player_score);

        tvScore.setTextSize(BOARD_TILE_SIZE / 7f);

        imageView.setTag(context.getString(R.string.you) + "," + player.name.toString());

        listItemView.setBackgroundColor(getColor(player, context));
        tvScore.setText(getContext().getString(R.string.score, player.points + ""));
        return listItemView;
    }
}
