package za.nmu.wrpv.qwirkle;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlayerMessageAdapter extends RecyclerView.Adapter<PlayerMessageAdapter.ViewHolder> {
    public List<PlayerMessage> playerMessages;
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
            SpannableString content = new SpannableString(playerMessage.player.name.toString());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            playerName.setText(content);
            message.setText(playerMessage.message);
            itemView.setBackgroundColor(playerMessage.player.color);
        }
    }
}
