package za.nmu.wrpv.qwirkle;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.DebugUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notification {
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_1 = "action_1";
    public static void displayNotification(Activity context) {
        String channelID = "newMessage";
        String channelName = context.getString(R.string.notification_title);
        String channelDescription = context.getResources().getString(R.string.notification_description);

        Intent action1Intent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_1);

        PendingIntent action1PendingIntent = PendingIntent.getService(context, 0,
                action1Intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.blue_circle)
                .setContentTitle(context.getResources().getString(R.string.notification_title))
                .setContentText(context.getResources().getString(R.string.notification_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(new NotificationCompat.Action(R.drawable.blue_circle,
                        "View", action1PendingIntent));

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID, builder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            NotificationManager manager1 = context.getSystemService(NotificationManager.class);
            manager1.createNotificationChannel(channel);
        }
    }


    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String action = intent.getAction();
            if (ACTION_1.equals(action)) {
                // If you want to cancel the notification: NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                MainActivity.runLater(d -> {
                    MainActivity context = (MainActivity) d.get("context");
                    context.switchFragment(MessagesFragment.class);
                    NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                });
            }
        }
    }

    public static void cancel(Context context, int id) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);
        nm.cancel(id);
    }
}
