package com.frienders.main;

import androidx.annotation.NonNull;

import com.frienders.main.notification.NotificaitonHandler;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class LocalFirebaseMessagingService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage != null  && remoteMessage.getNotification() != null)
        {
            String title = remoteMessage.getNotification().getTitle();
            String text = remoteMessage.getNotification().getBody();
            NotificaitonHandler.displayNotification(getApplicationContext(), title, text);
        }
    }
}
