package com.frienders.main.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;


import androidx.annotation.NonNull;

import com.frienders.main.config.GroupFirebaseFields;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Utility
{
    public static Calendar calendar = Calendar.getInstance();

    public static String getGroupDisplayNameFromDbGroupName(String groupNameFromDb) {
        final String[] groupWithParentNameWithoutAsterisk =  groupNameFromDb.split("\\*");
        String groupDisplayNameMayContainRootName = null;

        if (groupWithParentNameWithoutAsterisk != null && groupWithParentNameWithoutAsterisk.length == 2)
        {
            groupDisplayNameMayContainRootName = groupWithParentNameWithoutAsterisk[0] + " - " + groupWithParentNameWithoutAsterisk[1];
        }
        else if(groupWithParentNameWithoutAsterisk != null && groupWithParentNameWithoutAsterisk.length == 1)
        {
            groupDisplayNameMayContainRootName = groupWithParentNameWithoutAsterisk[0];
        }

        return groupDisplayNameMayContainRootName;
    }

    public static Bitmap decodeFile(String filePath) {

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

// The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

// Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

// Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap image = BitmapFactory.decodeFile(filePath, o2);

        ExifInterface exif;
        try
        {
            exif = new ExifInterface(filePath);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            if (rotate != 0) {
                int w = image.getWidth();
                int h = image.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap & convert to ARGB_8888, required by tess
                image = Bitmap.createBitmap(image, 0, 0, w, h, mtx, false);

            }
        } catch (IOException e) {
            return null;
        }
        return image.copy(Bitmap.Config.ARGB_8888, true);
    }


    public static String getLevelPathAtLevel(int level)
    {
        return GroupFirebaseFields.level + " - " + level;
    }

    public static String getLevelPathAtLevel(int level, String levelI)
    {
        return GroupFirebaseFields.level + " - " + level;
    }

    public static String getCurrentDate()
    {
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        return currentDate.format(calendar.getTime());
    }

    public static String getCurrentTime()
    {
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        return currentTime.format(calendar.getTime());
    }

    public static void createDeviceToken()
    {
        final String currentUserId =  FirebaseAuthProvider.getCurrentUserId();

        FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists() && !dataSnapshot.hasChild(UsersFirebaseFields.device_token))
                {
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        final String deviceToken = task.getResult().getToken();
                                        FirebasePaths.firebaseUserRef(currentUserId).child(UsersFirebaseFields.device_token)
                                                .setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {

                                                        }
                                                    }
                                                });
                                    }
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
