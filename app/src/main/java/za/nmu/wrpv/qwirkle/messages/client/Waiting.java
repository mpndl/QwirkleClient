package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.R;

public class Waiting extends IMessage implements Serializable {
    private static final long serialVersionUID = 10L;

    @Override
    public void apply() {
        BeginActivity.runLater(data1 -> {
            Activity context = (Activity) data1.get("context");
            Button button = context.findViewById(R.id.btn_start_game);
            button.setText(R.string.waiting);
        });
    }
}
