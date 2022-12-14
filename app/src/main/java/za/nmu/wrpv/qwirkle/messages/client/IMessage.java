package za.nmu.wrpv.qwirkle.messages.client;

import static za.nmu.wrpv.qwirkle.Helper.vibrate;

import android.app.Activity;

import java.io.Serializable;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.MessagesAdapter;
import za.nmu.wrpv.qwirkle.MessagesFragment;
import za.nmu.wrpv.qwirkle.Notification;
import za.nmu.wrpv.qwirkle.PlayerMessage;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.messages.Message;

public class IMessage extends Message implements Serializable {
    private static final long serialVersionUID = 5L;

    @Override
    public void apply() {
        PlayerMessage playerMessage = (PlayerMessage) data.get("message");
        MessagesFragment.runLater(data -> {
            MessagesAdapter adapter = (MessagesAdapter) data.get("adapter");
            Activity context = (Activity) data.get("context");
            Objects.requireNonNull(context).runOnUiThread(() -> adapter.add(playerMessage));
        });

        MainActivity.runLater(d -> {
            Activity context = (Activity) d.get("context");
            if (Objects.requireNonNull(playerMessage).player.name != GameModel.player.name) {
                Notification.displayNotification(Objects.requireNonNull(context), playerMessage.message);
                vibrate(50, context);
                vibrate(50, context);
                Helper.sound(context, R.raw.imessage);
            }
        });
    }
}
