package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;

import za.nmu.wrpv.qwirkle.MainActivity;
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
            context.finish();
        });
    }
}
