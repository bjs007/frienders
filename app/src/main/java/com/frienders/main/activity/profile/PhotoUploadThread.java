package com.frienders.main.activity.profile;

import android.app.ProgressDialog;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public  class PhotoUploadThread implements Runnable
{
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private String currentUserId;
    private Uri uri;

    public PhotoUploadThread(DatabaseReference databaseReference,
                             String currentUserId, Uri uri)
    {
        this.databaseReference = databaseReference;
        this.currentUserId = currentUserId;
        this.uri = uri;
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");

    }

    @Override
    public void run()
    {
        final StorageReference filePath = storageReference.child(currentUserId + ".jpg");

        filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                {

                    final Task<Uri> firebaseUri = task.getResult().getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            final String downloadUrl = uri.toString();
                            // complete the rest of your code

                            databaseReference.child(currentUserId).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {

                                            }

                                        }
                                    });
                        }
                    });
                }

            }
        });
    }
}
