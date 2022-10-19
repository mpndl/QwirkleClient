package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Stop extends Message {
    private static final long serialVersionUID = 1L;

    @Override
    public void apply() {
        ServerHandler.interrupt();
    }
}
