package com.frienders.main.activity.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.frienders.main.config.Configuration;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.db.refs.FirestorePath;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private androidx.appcompat.widget.Toolbar settingsToolBar;
    RadioButton languageRadioButton, engLanguageRadioButton, hinLanuguageRadioButton;
    RadioGroup languaeRadioGroup;
    private String userLanguage = "eng";
    private TextView deleteProfileImage;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initializeUi();
    }


    private void initializeUi() {
        progressDialog = new ProgressDialog(this);
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        progressBar = findViewById(R.id.progressbar);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        settingsToolBar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(settingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");
        languaeRadioGroup = findViewById(R.id.radioGroup);
        engLanguageRadioButton = findViewById(R.id.english);
        hinLanuguageRadioButton = findViewById(R.id.hindi);
        deleteProfileImage = findViewById(R.id.delete_profile_image);
        userName.setVisibility(View.VISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateSettings();
            }
        });

        retrieveUserInfo();

        userProfileImage.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Configuration.RequestCodeForImagePick);
            }
        });

        deleteProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                        .child(UsersFirebaseFields.profileImageLink).addListenerForSingleValueEvent(
                        new ValueEventListener()
                        {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    userProfileImage.setImageDrawable(getDrawable(R.drawable.profile_image));
                                    FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                                            .child(UsersFirebaseFields.profileImageLink).removeValue();
                                    deleteProfileImage.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        }
                );

            }
        });

        FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                .addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild(UsersFirebaseFields.language))
                    {
                        userLanguage = dataSnapshot.child(UsersFirebaseFields.language).getValue().toString();
                    }

                    if(userLanguage.equals("eng"))
                    {
                       int id = engLanguageRadioButton.getId();
                       languaeRadioGroup.check(id);
                    }
                    else
                    {
                        int id = hinLanuguageRadioButton.getId();
                        languaeRadioGroup.check(id);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        settingsToolBar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendUserToMainActivity();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Configuration.RequestCodeForImagePick && resultCode ==  RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                Bitmap bitmap = decodeFile(resultUri.getPath());
                Uri bitMapuri = bitmapToUriConverter(bitmap);


                final StorageReference filePath = FirestorePath.firestoreprofileImagesDbRef()
                        .child(FirebaseAuthProvider.getCurrentUserId() + ".jpg");

                progressBar.setVisibility(View.VISIBLE);
                progressBar.getProgressDrawable().setColorFilter(getColor(R.color.colorBlue), android.graphics.PorterDuff.Mode.SRC_IN);


                filePath.putFile(bitMapuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {

                            final Task<Uri> firebaseUri = task.getResult().getStorage().getDownloadUrl();
                            firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    final String downloadUrl = uri.toString();
                                    // complete the rest of your code

                                    FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                                            .child(UsersFirebaseFields.profileImageLink)
                                            .setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        progressBar.setVisibility(View.GONE);
                                                        deleteProfileImage.setVisibility(View.VISIBLE);
                                                    }
                                                    else
                                                    {
                                                        String message = task.getException().toString();
                                                        Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }

                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                    {
                        float progress = (float) (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        int currentprogress = (int) progress;

                        if(currentprogress > 20 && currentprogress <= 30)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(20);
                        }

                        if(currentprogress > 40 && currentprogress <= 50)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(40);
                        }

                        if(currentprogress > 50 && currentprogress <= 60)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(50);
                        }

                        if(currentprogress > 60 && currentprogress <= 70)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(60);
                        }

                        if(currentprogress > 70 && currentprogress <= 80)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(70);
                        }

                        if(currentprogress > 80 && currentprogress <= 90)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(80);
                        }

                        if(currentprogress > 90 && currentprogress < 100)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(90);
                        }

                        if(currentprogress == 100)
                        {
                            progressBar.setMax(100);
                            progressBar.setProgress(100);
                        }
                    }
                });
            }
        }
    }


    public Uri bitmapToUriConverter(Bitmap mBitmap) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 200, 200,
                    true);
            File file = new File(getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out = openFileOutput(file.getName(),
                    Context.MODE_PRIVATE);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Bitmap decodeFile(String filePath) {

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

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, getString(R.string.writeyourname), Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(setUserStatus))
        {
            Toast.makeText(this, getString(R.string.writeastatus) , Toast.LENGTH_SHORT).show();
        }

//        else if(languaeRadioGroup.getCheckedRadioButtonId() == -1)
//        {
//            Toast.makeText(this, getString(R.string.selectpreferredlanguage), Toast.LENGTH_SHORT).show();
//        }

        else
        {
            progressDialog.setMessage("Updating profile...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            int languageKey = languaeRadioGroup.getCheckedRadioButtonId();

            languageRadioButton = (RadioButton) findViewById(languageKey);

            String language = "hin";

            String inputLang = languageRadioButton.getText().toString().trim().toLowerCase();

            if(inputLang.equals("english"))
            {
                language = "eng";
            }

            HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put(UsersFirebaseFields.uid, FirebaseAuthProvider.getCurrentUserId());
                profileMap.put(UsersFirebaseFields.name, setUserName);
                profileMap.put(UsersFirebaseFields.status, setUserStatus);
                profileMap.put(UsersFirebaseFields.language, language);


            FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                    .updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                progressDialog.setMessage("Profile updated...");
                                SendUserToMainActivity();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            }

                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.setMessage("Profile failed...");
                    progressDialog.dismiss();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    
                }
            });


        }
    }



    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
    }

    private void retrieveUserInfo()
    {

        FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild(UsersFirebaseFields.name) && (dataSnapshot.hasChild(UsersFirebaseFields.profileImageLink))))
                        {
                           String retrieveUserName = dataSnapshot.child(UsersFirebaseFields.name).getValue().toString();
                           String retrieveStatus = dataSnapshot.child(UsersFirebaseFields.status).getValue().toString();
                           String retrieveProfileImage = dataSnapshot.child(UsersFirebaseFields.profileImageLink).getValue().toString();
                           userName.setText(retrieveUserName);
                           userStatus.setText(retrieveStatus);
                           Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild(UsersFirebaseFields.name)))
                        {
                            String retrieveUserName = dataSnapshot.child(UsersFirebaseFields.name).getValue().toString();
                            String retrieveStatus = dataSnapshot.child(UsersFirebaseFields.status).getValue().toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            deleteProfileImage.setVisibility(View.GONE);

                        }
                        else
                        {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
