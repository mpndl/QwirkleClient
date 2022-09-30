package za.nmu.wrpv.qwirkle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlayerMessageAdapter extends RecyclerView.Adapter<PlayerMessageAdapter.ViewHolder> {
    private List<PlayerMessage> playerMessages = new ArrayList<>();
    private Context context;

    public PlayerMessageAdapter(List<PlayerMessage> playerMessages, Context context) {
        this.playerMessages = playerMessages;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlayerMessage playerMessage = playerMessages.get(position);
        holder.set(playerMessage);
    }

    @Override
    public int getItemCount() {
        return playerMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView playerName;
        public TextView message;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            playerName = itemView.findViewById(R.id.tv_message_player_name);
            message = itemView.findViewById(R.id.tv_player_message);
        }

        public void set(PlayerMessage playerMessage) {
            playerName.setText(playerMessage.player.name.toString());
            message.setText(playerMessage.message);
        }
    }
}
