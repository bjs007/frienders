package com.frienders.main.utility;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.frienders.main.SplashActivity;
import com.frienders.main.activity.ImageViwer;
import com.frienders.main.config.Configuration;
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
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


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


    public static String getGroupDisplayFirstNameFromDbGroupName(String groupNameFromDb) {
        final String[] groupWithParentNameWithoutAsterisk =  groupNameFromDb.split("\\*");
        String groupDisplayNameMayContainRootName = null;

        if (groupWithParentNameWithoutAsterisk != null && groupWithParentNameWithoutAsterisk.length == 2)
        {
            return groupWithParentNameWithoutAsterisk[0];
        }


        return groupNameFromDb;
    }

    public static void displayImage(Context context, String imageLink)
    {
        Intent imageDisplayIntent = new Intent(context, ImageViwer.class);
        imageDisplayIntent.putExtra(UsersFirebaseFields.imagelink, imageLink);
        context.startActivity(imageDisplayIntent);
    }

    public static void displayImageByGroupId(Context context, String groupId)
    {
        Intent imageDisplayIntent = new Intent(context, ImageViwer.class);
        imageDisplayIntent.putExtra(GroupFirebaseFields.groupId, groupId);
        context.startActivity(imageDisplayIntent);
    }

    public static void playVideo(Context context, String imageLink)
    {
        Intent playMusi = new Intent(context, SplashActivity.class);
        playMusi.putExtra("videoLink", imageLink != null ? imageLink : "");
        context.startActivity(playMusi);
    }

    public static String getDeviceLanguage()
    {
        String deviceLanguage = Locale.getDefault().getLanguage();
        if(Configuration.languageSupported.contains(deviceLanguage))
        {
            return deviceLanguage;
        }

        return Configuration.APP_DEFAULT_LANGUAGE;
    }

    public static void downloadDoc(Context context, String docLink)
    {
        try
        {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(docLink));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                    DownloadManager.Request.NETWORK_MOBILE);

            // set title and description
            request.setTitle("Data Download");
            request.setDescription("Android Data download using DownloadManager.");

            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //set the local destination for download file to a path within the application's external files directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloadfileName");
            request.setMimeType("*/*");
            downloadManager.enqueue(request);
            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex)
        {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
        }
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
