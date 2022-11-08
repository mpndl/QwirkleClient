package za.nmu.wrpv.qwirkle.messages.client;

import android.app.Activity;
import android.widget.Button;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import za.nmu.wrpv.qwirkle.BeginActivity;
import za.nmu.wrpv.qwirkle.R;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Countdown extends Message implements Serializable {
    private static final long serialVersionUID = 71L;
    private static Thread thread = null;
    public static AtomicReference<Integer> seconds = null;

    @Override
    public void apply() {
        System.out.println("--------------- COUNTDOWN START");
        seconds = new AtomicReference<>((Integer) get("seconds"));
        BeginActivity.runLater(data1 -> {
            Activity context = (Activity) data1.get("context");
            Button button = Objects.requireNonNull(context).findViewById(R.id.btn_start_game);
            if (button != null) {
                System.out.println("------------------------ " + get("seconds"));
                context.runOnUiThread(() -> button.setText(context.getResources().getString(R.string.waiting_countdown, seconds.get())));
                thread = new Thread(() -> {
                    try {
                        do {
                            System.out.println("COUNTDOWN = " + seconds);
                            Thread.sleep(1000);
                            seconds.set(seconds.get() - 1);
                            context.runOnUiThread(() -> button.setText(context.getResources().getString(R.string.waiting_countdown, seconds.get())));
                        } while (seconds.get() > 0);
                    } catch (InterruptedException ignored) {
                        System.out.println("-------------------------------- COUNTDOWN INTERRUPTED");
                    }
                });
                thread.start();
            }
        });
        System.out.println("--------------- COUNTDOWN END");
    }

    public static void interrupt() {
        if (thread != null && thread.isAlive()) {
            System.out.println("CALLING INTERRUPT");
            thread.interrupt();
            thread = null;
        }else {
            if (thread == null) System.out.println("THREAD IS NULL");
            else if (!thread.isAlive()) System.out.println("THREAD IS NOT ALIVE");
            else System.out.println("TEST");
        }
    }
}
