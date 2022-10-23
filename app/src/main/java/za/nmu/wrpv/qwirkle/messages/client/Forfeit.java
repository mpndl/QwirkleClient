package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serializable;

public class Forfeit extends Message implements Serializable {
    private static final long serialVersionUID = 80L;

    @Override
    public void apply() {
        Player player = (Player) get("player");
        GameFragment.runLater((data1 -> {
            GameFragment fragment = (GameFragment) get("fragment");
            fragment.removePlayer(player);
        }));
    }
}
