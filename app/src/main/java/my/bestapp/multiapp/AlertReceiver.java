package my.bestapp.multiapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
                nb.setContentText(intent.getStringExtra("Text"));
        notificationHelper.getManager().notify(intent.getIntExtra("Id", 1), nb.build());

        context.getSharedPreferences("Notifications", Context.MODE_PRIVATE).edit().remove(intent.getStringExtra("Key")).apply();
    }
}
