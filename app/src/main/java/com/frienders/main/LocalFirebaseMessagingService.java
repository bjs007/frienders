package com.frienders.main;

import androidx.annotation.NonNull;

import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.notification.NotificaitonHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Iterator;

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
//            NotificaitonHandler.displayNotification(getApplicationContext(), title, text);
        }
    }

    @Override
    public void onNewToken(final String token)
    {
        final String currentUserId = FirebaseAuthProvider.getCurrentUserId();
        if(currentUserId != null)
        {
            final DatabaseReference userDatabaseReference = FirebasePaths.firebaseUserRef(currentUserId);
            userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {

                        userDatabaseReference.child(UsersFirebaseFields.device_token).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                userDatabaseReference.child(UsersFirebaseFields.subscribed).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(dataSnapshot.exists() && dataSnapshot.hasChildren())
                                        {
                                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                            while (iterator.hasNext())
                                            {
                                                final DataSnapshot ds = iterator.next();
                                                final String groupId = ds.getKey();
                                                FirebasePaths.firebaseSubscribedRef().child(groupId).child(currentUserId).child(token);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }

    }

}
