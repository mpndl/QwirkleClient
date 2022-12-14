package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import java.io.Serializable;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.BeginFragment;
import za.nmu.wrpv.qwirkle.R;

public class Waiting extends IMessage implements Serializable {
    private static final long serialVersionUID = 10L;

    @Override
    public void apply() {
        System.out.println("--------------------- WAITING --------------------------------");
        Countdown.interrupt();
        BeginActivity.runLater(d -> {
            Activity context = (Activity) d.get("context");
            Button button = Objects.requireNonNull(context).findViewById(R.id.btn_start_game);
            if (button != null) {
                context.runOnUiThread(() -> button.setText(context.getResources().getString(R.string.waiting)));
            }
        });
    }
}
