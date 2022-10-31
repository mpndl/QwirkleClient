package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import za.nmu.wrpv.qwirkle.EndActivity;
import za.nmu.wrpv.qwirkle.Game;
import za.nmu.wrpv.qwirkle.GameFragment;
import za.nmu.wrpv.qwirkle.GameModel;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.Player;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ScoreAdapter;
import za.nmu.wrpv.qwirkle.XMLHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class GameEnded extends Message implements Serializable {
    private static final long serialVersionUID = 75L;

    @Override
    public void apply() {
        Game game = new Game();
        game.gameID = GameModel.gameID;
        game.player = GameModel.clientPlayer;
        game.date = new Date();
        game.players = GameModel.players;
        game.messages = GameModel.messages;

        GameFragment.runLater(d -> {
            Activity context = (Activity) d.get("context");

            try {
                XMLHandler.appendToXML(game, context);
            } catch (TransformerException | ParserConfigurationException e) {
                e.printStackTrace();
            }

            Helper.sound(context, R.raw.end);

            Intent intent = new Intent(context, EndActivity.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("players", (Serializable) GameModel.players);

            intent.putExtras(bundle);
            Objects.requireNonNull(context).startActivity(intent);
        });
    }
}
