package com.frienders.main.db.refs;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirestorePath
{
    private static String ProfileImagesPath = "ProfileImages";
    private static String videoMessagePath = "VideoMessages";

    public static StorageReference firestoreprofileImagesDbRef()
    {
        return FirebaseStorage.getInstance().getReference(ProfileImagesPath);
    }

    public static StorageReference firestoreVideoMessagesDbRef()
    {
        return FirebaseStorage.getInstance().getReference(videoMessagePath);
    }
}
