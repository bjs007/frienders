package com.frienders.main.db.refs;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebasePaths
{
    public static final String GroupsPath = "Groups";
    public static final String SubscribedPath = "Subscribed";
    public static final String UsersPath = "Users";
    public static final String MessagesPath = "Messages";
    public static final String groupLeafsPath = "leafs";

    public static DatabaseReference firebaseDbRawRef()
    {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference firebaseGroupDbRef()
    {
        return FirebaseDatabase.getInstance().getReference(GroupsPath);
    }

    public static DatabaseReference firebaseGroupsAtLevelDBRef(int level)
    {
        return firebaseGroupDbRef().child("level - " + level);
    }

    public static DatabaseReference firebaseGroupNodeAtLevelRef(int level, String nodeId)
    {
        return firebaseGroupsAtLevelDBRef(level).child(nodeId);
    }

    public static DatabaseReference firebaseUsersDbRef()
    {
        return FirebaseDatabase.getInstance().getReference(UsersPath);
    }

    public static DatabaseReference firebaseGroupsLeafsRef()
    {
        return firebaseGroupDbRef().child(groupLeafsPath);
    }

    public static DatabaseReference firebaseUserRef(String userId)
    {
        return firebaseUsersDbRef().child(userId);
    }

    public static DatabaseReference firebaseMessageRef()
    {
        return FirebaseDatabase.getInstance().getReference(MessagesPath);
    }

    public static DatabaseReference firebaseSubscribedRef() {
        return FirebaseDatabase.getInstance().getReference(SubscribedPath);
    }

    public static String messagePath()
    {
        return MessagesPath + "/";
    }

}
