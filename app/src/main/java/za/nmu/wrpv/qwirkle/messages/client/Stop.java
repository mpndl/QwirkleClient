package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Stop extends Message {
    private static final long serialVersionUID = 1L;

    @Override
    public void apply() {
        System.out.println("---------------------------------------------------------- GAME ENDED");
        ServerHandler.interrupt();
        MainActivity.runLater(data1 -> {
            Activity context = (Activity) data1.get("context");
            Objects.requireNonNull(context).finish();
        });

        BeginActivity.runLater(d -> {
            Activity context = (Activity) d.get("context");
            Button btnStart = Objects.requireNonNull(context).findViewById(R.id.btn_start_game);
            btnStart.setText(R.string.btn_start_game);
        });
        ServerHandler.interrupt();
    }
}
