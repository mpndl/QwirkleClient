package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.BeginFragment;
import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Stop extends Message {
    private static final long serialVersionUID = 1L;

    @Override
    public void apply() {
        if (!data.containsKey("connectionError")) {
            System.out.println("---------------------------------------------------------- GAME ENDED");
            Countdown.interrupt();
            GameFragment.runLater(data1 -> {
                Activity context = (Activity) data1.get("context");
                Objects.requireNonNull(context).finish();
            });

            BeginFragment.runLater(d -> {
                Activity context = (Activity) d.get("context");
                Button btnStartGame = Objects.requireNonNull(context).findViewById(R.id.btn_start_game);
                context.runOnUiThread(() -> {
                    if (!ServerHandler.running()) btnStartGame.setText(R.string.connect);
                    else btnStartGame.setText(R.string.btn_start_game);
                });
            });
        }else {
            BeginFragment.runLater( d-> {
                Activity context = (Activity) d.get("context");
                Button btnStartGame = Objects.requireNonNull(context).findViewById(R.id.btn_start_game);
                System.out.println("CONNECTION ERROR");
                Objects.requireNonNull(context).runOnUiThread(() -> {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    if (!ServerHandler.running()) btnStartGame.setText(R.string.connect);
                    else btnStartGame.setText(R.string.btn_start_game);
                });
            });
        }
    }
}
