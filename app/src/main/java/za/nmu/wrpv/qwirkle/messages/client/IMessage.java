package za.nmu.wrpv.qwirkle.messages.client;

import static za.nmu.wrpv.qwirkle.Helper.vibrate;

import android.app.Activity;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.MainActivity;
import za.nmu.wrpv.qwirkle.MessagesAdapter;
import za.nmu.wrpv.qwirkle.MessagesFragment;
import za.nmu.wrpv.qwirkle.Notification;
import za.nmu.wrpv.qwirkle.PlayerMessage;
import za.nmu.wrpv.qwirkle.messages.Message;

public class IMessage extends Message implements Serializable {
    private static final long serialVersionUID = 5L;

    @Override
    public void apply() {
        PlayerMessage playerMessage = (PlayerMessage) data.get("message");
        MessagesFragment.runLater(data -> {
            MessagesAdapter adapter = (MessagesAdapter) data.get("adapter");
            adapter.add(playerMessage);
        });

        MainActivity.runLater(d -> {
            Activity context = (Activity) d.get("context");
            if (playerMessage.player.name != GameModel.clientPlayer.name) {
                Notification.displayNotification(context);
                vibrate(50, context);
                vibrate(50, context);
            }
        });
    }
}
