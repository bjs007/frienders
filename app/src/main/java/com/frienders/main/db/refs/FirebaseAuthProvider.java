package com.frienders.main.db.refs;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthProvider
{
    public static com.google.firebase.auth.FirebaseAuth getFirebaseAuth()
    {
        return com.google.firebase.auth.FirebaseAuth.getInstance();
    }

    public static String getCurrentUserId()
    {
        return getFirebaseAuth().getCurrentUser().getUid();
    }

    public static FirebaseUser getCurrentUser()
    {
        return getFirebaseAuth().getCurrentUser();
    }

    public static void logout()
    {
        getFirebaseAuth().signOut();
    }
}
