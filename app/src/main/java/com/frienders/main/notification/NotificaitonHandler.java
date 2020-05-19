package com.frienders.main.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

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


        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
        contentView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));

        contentView.setImageViewResource(R.id.big_icon, R.drawable.group);
        contentView.setTextViewText(R.id.title, title);
        contentView.setTextViewText(R.id.text, body);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, Configuration.default_channel_id)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContent(contentView)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, mBuilder.build());
    }
}
