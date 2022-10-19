package za.nmu.wrpv.qwirkle;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> implements Serializable {
    public List<PlayerMessage> playerMessages;

    public MessagesAdapter(List<PlayerMessage> playerMessages) {
        this.playerMessages = playerMessages;
    }

    public void add(PlayerMessage playerMessage) {
        playerMessages.add(playerMessage);
        notifyItemInserted(getItemCount() - 1);
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
        private ConstraintLayout clLeft;
        public TextView playerNameLeft;
        public TextView messageLeft;
        public TextView timeLeft;

        private ConstraintLayout clRight;
        public TextView playerNameRight;
        public TextView messageRight;
        public TextView timeRight;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            playerNameLeft = itemView.findViewById(R.id.tv_left_message_player_name);
            messageLeft = itemView.findViewById(R.id.tv_left_player_message);
            timeLeft = itemView.findViewById(R.id.tv_left_send_time);
            clLeft = (ConstraintLayout) timeLeft.getParent();

            playerNameRight = itemView.findViewById(R.id.tv_right_message_player_name);
            messageRight = itemView.findViewById(R.id.tv_right_player_message);
            timeRight = itemView.findViewById(R.id.tv_right_send_time);
            clRight = (ConstraintLayout) timeRight.getParent();
        }

        public void set(PlayerMessage playerMessage) {

            if (GameModel.player.name.equals(playerMessage.player.name)) {
                SpannableString content = new SpannableString(playerMessage.player.name.toString());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

                clLeft.setBackgroundColor(StatusAdapter.getColor(playerMessage.player));
                playerNameLeft.setText(R.string.you);
                messageLeft.setText(playerMessage.message);
                timeLeft.setText(playerMessage.time);
                timeLeft.setTextColor(Color.GREEN);
                clRight.setVisibility(View.GONE);
            } else {
                SpannableString content = new SpannableString(playerMessage.player.name.toString());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

                clRight.setBackgroundColor(StatusAdapter.getColor(playerMessage.player));
                playerNameRight.setText(content);
                messageRight.setText(playerMessage.message);
                timeRight.setText(playerMessage.time);
                timeRight.setTextColor(Color.GREEN);

                clLeft.setVisibility(View.GONE);
            }
        }
    }
}
