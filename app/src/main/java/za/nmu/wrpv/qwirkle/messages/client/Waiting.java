package za.nmu.wrpv.qwirkle.messages.client;

import android.widget.Button;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ServerHandler;

public class Waiting extends IMessage implements Serializable {
    private static final long serialVersionUID = 10L;

    @Override
    public void apply() {
        ServerHandler.activity.runOnUiThread(() -> {
            Button button = ServerHandler.activity.findViewById(R.id.btn_start_game);
            button.setText(R.string.waiting);
        });
    }
}
