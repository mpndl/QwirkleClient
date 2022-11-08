package za.nmu.wrpv.qwirkle.messages.client;

import static za.nmu.wrpv.qwirkle.ServerHandler.connectErrCount;

import android.app.Activity;
import android.widget.Toast;

import java.io.Serializable;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class ConnectionError extends Message implements Serializable {
    private final static long serialVersionUID = 300L;

    @Override
    public void apply() {
        ConnectionError message = new ConnectionError();
        message.put("connectErrCount", 0);
        message.put("gameID", GameModel.gameID);
        ServerHandler.send(message);
        BeginActivity.runLater(data -> Toast.makeText((Activity) data.get("context"), R.string.connection_error, Toast.LENGTH_LONG));
        connectErrCount = 0;
    }
}
