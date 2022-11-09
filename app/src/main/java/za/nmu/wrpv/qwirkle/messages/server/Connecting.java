package za.nmu.wrpv.qwirkle.messages.server;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.Serializable;
import java.util.Objects;

import za.nmu.wrpv.qwirkle.BeginFragment;
import za.nmu.wrpv.qwirkle.Helper;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.ServerHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Connecting extends Message implements Serializable {
    private static final long serialVersionUID = 501L;

    @Override
    public void apply() {
        BeginFragment.runLater(d-> {
            Activity context = (Activity) d.get("context");
            Button btnStartGame = Objects.requireNonNull(context).findViewById(R.id.btn_start_game);
            Objects.requireNonNull(context).runOnUiThread(() -> {
                btnStartGame.setText(R.string.connecting);
                EditText etPlayerCount = context.findViewById(R.id.et_server_address);
                etPlayerCount.setEnabled(false);
                SwitchMaterial sLan = context.findViewById(R.id.s_lan);
                sLan.setEnabled(false);
                btnStartGame.setEnabled(false);
            });
        });
    }
}
