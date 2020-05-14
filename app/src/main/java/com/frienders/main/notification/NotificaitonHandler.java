package com.frienders.main.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.frienders.main.config.Configuration;
import com.frienders.main.notification.NotificaitonActivity;

public class NotificaitonHandler {
    public static void displayNotification(Context context, String title, String body){

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, Configuration.default_channel_id)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, mBuilder.build());
    }
}
