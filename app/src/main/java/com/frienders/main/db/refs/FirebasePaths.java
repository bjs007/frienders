package com.frienders.main.db.refs;

import com.frienders.main.config.Configuration;
import com.frienders.main.config.GroupFirebaseFields;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebasePaths
{
    public static final String GroupsPath = "Groups";
    public static final String SubscribedPath = "Subscribed";
    public static final String UsersPath = "Users";
    public static final String MessagesPath = "Messages";
    public static final String groupLeafsPath = "leafs";
    public static final String lastNotificationTime = "UsersNotification";
    public static final String messageLike = "MessageLikes";
    public static final String userMessageLikesCount  = "UserMessageLikesCount";


    public static DatabaseReference firebaseDbRawRef()
    {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference firebaseGroupsRootDbRef()
    {
        return FirebasePaths.firebaseDbRawRef().child("level - 0");
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

    public static DatabaseReference firebaseUsersNotificationTimeDbRef()
    {
        return FirebaseDatabase.getInstance().getReference(lastNotificationTime);
    }

    public static DatabaseReference firebaseMessageLikeDbRef()
    {
        return FirebaseDatabase.getInstance().getReference(messageLike);
    }

    public static final DatabaseReference fireabaseUserMessageLikesCount(){
        return firebaseDbRawRef().child(userMessageLikesCount);
    }

    public static StorageReference firestoragebaseRef()
    {
        return FirebaseStorage.getInstance().getReference();
    }
    public static StorageReference firestorageGroupImageReference(String groupId)
    {
        StorageReference str = FirebaseStorage.getInstance().getReferenceFromUrl(Configuration.DEFAULT_BUCKET + Configuration.GROUP_IMAGE + "/" + groupId + Configuration.DEFAULT_GROUP_IMAGE_EXT);
        return str;
    }
    public static String messagePath()
    {
        return MessagesPath + "/";
    }

}
