package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serializable;

public class Forfeit extends Message implements Serializable {
    private static final long serialVersionUID = 80L;

    @Override
    public void apply() {
        System.out.println("------------------------------ FORFEIT START");
        Player player = (Player) get("player");
        GameFragment.runLater((data1 -> {
            ScoreAdapter adapter = (ScoreAdapter) data1.get("adapter");
            GameModel.removePlayer(player, adapter);
        }));
    }
}
