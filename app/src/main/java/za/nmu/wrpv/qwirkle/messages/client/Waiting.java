package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.R;

public class Waiting extends IMessage implements Serializable {
    private static final long serialVersionUID = 10L;
    private static Thread thread = null;

    @Override
    public void apply() {
        System.out.println("--------------------- WAITING START --------------------------------");
        AtomicReference<Integer> seconds = new AtomicReference<>((Integer) get("seconds"));
        BeginActivity.runLater(data1 -> {
            Activity context = (Activity) data1.get("context");
            Button button = context.findViewById(R.id.btn_start_game);
            System.out.println("------------------------ " + get("seconds"));
            interrupt();
            if (data.containsKey("seconds")) {
                button.setText(context.getResources().getString(R.string.waiting, "("+seconds.get() + ")"));
                thread = new Thread(() -> {
                    try {
                        do {
                            System.out.println("COUNTDOWN = " + seconds);
                            Thread.sleep(1000);
                            seconds.set(seconds.get() - 1);
                            context.runOnUiThread(() -> button.setText(context.getResources().getString(R.string.waiting, "("+seconds.get() + ")")));
                        } while (seconds.get() > 0);
                    }catch (InterruptedException ignored) {
                        System.out.println("-------------------------------- INTERRUPTED");
                        button.setText(context.getResources().getString(R.string.waiting, ""));
                    }
                });
                thread.start();
            }else {
                System.out.println("------------------------- NO SECONDS");
                if (data.containsKey("restart")) {
                    System.out.println("------------------------- RESTART");
                    if (thread != null && thread.isAlive()) {
                        thread.interrupt();
                    }
                }else {
                    System.out.println("-------------------------WAITING");
                    button.setText(context.getResources().getString(R.string.waiting, ""));
                }
            }
        });
    }

    public static void interrupt() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
        }
    }
}
