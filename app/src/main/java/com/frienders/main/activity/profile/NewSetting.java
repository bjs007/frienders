package com.frienders.main.activity.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.frienders.main.config.Configuration;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.db.refs.FirestorePath;
import com.frienders.main.utility.FileUtil;
import com.frienders.main.utility.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.frienders.main.config.Configuration.RequestCodeForImagePick;
import static com.frienders.main.config.Configuration.imageMaxHeight;
import static com.frienders.main.config.Configuration.imageMaxWidth;

public class NewSetting extends AppCompatActivity {

    private ImageButton updateAccountSettings, cancelAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private androidx.appcompat.widget.Toolbar settingsToolBar;
    private String deviceLanguage = Utility.getDeviceLanguage();
    private TextView deleteProfileImage, userPhoneNumber, remove_profile_image_new;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private static final int RC_PHOTO_PICKER_PERM = 555;
    private TextView groupsSubscribed, queriesAsked, answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_setting);
        initializeUi();
    }

    private void initializeUi() {

        updateAccountSettings = findViewById(R.id.update_settings_button_new);
        cancelAccountSettings = findViewById(R.id.cancel_settings_button_new);
        remove_profile_image_new = findViewById(R.id.remove_profile_image_new);
        userName = findViewById(R.id.set_user_name_new);
        progressBar = findViewById(R.id.progressbar_profile_setting);
        progressBar.setVisibility(View.VISIBLE);
        userStatus = findViewById(R.id.set_profile_status_new);
        userProfileImage = findViewById(R.id.set_profile_image_new);
//        languaeRadioGroup = findViewById(R.id.radioGroup_new);
//        engLanguageRadioButton = findViewById(R.id.english);
//        hinLanuguageRadioButton = findViewById(R.id.hindi);
        deleteProfileImage = findViewById(R.id.delete_profile_image_new);
        userName.setVisibility(View.VISIBLE);
        groupsSubscribed = findViewById(R.id.numberOfgroupsSubscribed);
        queriesAsked = findViewById(R.id.numberOfQuestionsAsked);
        answers = findViewById(R.id.numberOfAnswers);
        userPhoneNumber = findViewById(R.id.user_phone_number);
        userPhoneNumber.setText(FirebaseAuthProvider.getCurrentUser().getPhoneNumber());

        updateAccountSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateSettings();
            }
        });

        retrieveUserInfo();

        cancelAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId()).child(UsersFirebaseFields.name)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    SendUserToMainActivity();
                                } else {
                                    Toast.makeText(NewSetting.this, getString(R.string.update_your_name), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });

        userProfileImage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
//                        Intent galleryIntent = new Intent();
//                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                        galleryIntent.setType("image/*");
//                        startActivityForResult(galleryIntent, Configuration.RequestCodeForImagePick);
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
                                            Utility.displayImage(NewSetting.this, dataSnapshot.getValue().toString());
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

        deleteProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Configuration.RequestCodeForImagePick);

            }
        });

        remove_profile_image_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
////                                    deleteProfileImage.setText(null);
                                } else {
                                    Toast.makeText(NewSetting.this, "No profile pic!", Toast.LENGTH_SHORT).show();
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
                File actualImage = null;
                File compressedFile = null;


                final StorageReference filePath = FirestorePath.firestoreprofileImagesDbRef()
                        .child(FirebaseAuthProvider.getCurrentUserId() + ".jpg");

                try {
                    actualImage = FileUtil.from(NewSetting.this, resultUri);
                    compressedFile = new Compressor(this)
                            .setMaxHeight(Math.min(bitmap.getHeight(), imageMaxHeight))
                            .setMaxWidth(Math.min(bitmap.getWidth(), imageMaxWidth))
                            .setQuality(75)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .compressToFile(actualImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final File compressed = compressedFile;

                progressBar.setVisibility(View.VISIBLE);
//                progressBar.getProgressDrawable().setColorFilter(getColor(R.color.colorBlue), android.graphics.PorterDuff.Mode.SRC_IN);

                userProfileImage.setVisibility(View.INVISIBLE);
                filePath.putFile(Uri.fromFile(compressedFile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
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
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        deleteProfileImage.setVisibility(View.VISIBLE);
                                                        if(compressed != null)
                                                        Glide.with(getApplicationContext())
                                                                .asBitmap()
                                                                .load(Uri.fromFile(compressed)) // or URI/path
                                                                .into(userProfileImage); //imageview to set thumb
                                                        userProfileImage.setVisibility(View.VISIBLE);
                                                    }
                                                    else
                                                    {
                                                        String message = task.getException().toString();
                                                        Toast.makeText(NewSetting.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(NewSetting.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userProfileImage.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
    public void pickPhotoClicked() {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
            FilePickerBuilder.getInstance()
                    .setActivityTitle("Please select photos")
                    .enableVideoPicker(false)
                    .enableCameraSupport(true)
                    .showGifs(true)
                    .setMaxCount(1)
                    .showFolderView(true)
                    .enableImagePicker(true)
                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .pickPhoto(this, RequestCodeForImagePick);

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
                    RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
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
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
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
            Toast.makeText(this, getString(R.string.profile_updated) , Toast.LENGTH_SHORT).show();
//            progressDialog = new ProgressDialog(NewSetting.this);
//            progressDialog.setMessage(getString(R.string.updating_profile));
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();
//            int languageKey = languaeRadioGroup.getCheckedRadioButtonId();
//
//            languageRadioButton = (RadioButton) findViewById(languageKey);

//            String language = "hin";
//
//            String inputLang = languageRadioButton.getText().toString().trim().toLowerCase();
//
//            if(inputLang.equals("english"))
//            {
//                language = "eng";
//            }

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(UsersFirebaseFields.uid, FirebaseAuthProvider.getCurrentUserId());
            profileMap.put(UsersFirebaseFields.name, setUserName);
            profileMap.put(UsersFirebaseFields.status, setUserStatus);
            profileMap.put(UsersFirebaseFields.language, Configuration.APP_DEFAULT_LANGUAGE);

            final String userId = FirebaseAuthProvider.getCurrentUserId();

            try
            {

                FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                        .updateChildren(profileMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
//                                    progressDialog.setMessage("Profile updated...");
//                                    progressDialog.dismiss();
                                    FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                                            .child(UsersFirebaseFields.subscribed)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.exists() && dataSnapshot.hasChildren())
                                                    {
                                                        SendUserToMainActivity();
                                                    }
                                                    else
                                                    {

                                                        SendUserToMainActivity();
//                                                        Fragment mFragment = null;
//                                                        mFragment = new GinfoxGroupsFragment();
//                                                        FragmentManager fragmentManager = getSupportFragmentManager();
//
//                                                        fragmentManager.beginTransaction().replace(R.id.main_tabs, mFragment).commit();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    String message = task.getException().toString();
                                    Toast.makeText(NewSetting.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.setMessage("Profile failed...");
                        progressDialog.dismiss();
                    }
                });
            }catch (Exception ex){
                SendUserToMainActivity();
            }

        }
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(NewSetting.this, MainActivity.class);
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

                        Glide.with(getApplicationContext())
                                .asBitmap()
//                                .placeholder(R.drawable.video_preview_icon)
                                .load(retrieveProfileImage) // or URI/path
                                .into(userProfileImage); //imageview to set thumb
                        userProfileImage.setVisibility(View.VISIBLE);
//                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                    }
                    else if((dataSnapshot.exists()) && (dataSnapshot.hasChild(UsersFirebaseFields.name)))
                    {
                        String retrieveUserName = dataSnapshot.child(UsersFirebaseFields.name).getValue().toString();
                        String retrieveStatus = dataSnapshot.child(UsersFirebaseFields.status).getValue().toString();
                        userName.setText(retrieveUserName);
                        userStatus.setText(retrieveStatus);
//                        deleteProfileImage.setVisibility(View.INVISIBLE);

                    }
                    else
                    {

                    }

                    Long[] numberofGroupsSubscribed = new Long[1];

                    if(dataSnapshot.hasChild(UsersFirebaseFields.subscribed))
                    {
                        numberofGroupsSubscribed[0] = dataSnapshot.child(UsersFirebaseFields.subscribed).getChildrenCount();
                    }
                    groupsSubscribed.setText(String.valueOf(numberofGroupsSubscribed[0] == null ? 0 : numberofGroupsSubscribed[0]));
                    if(dataSnapshot.exists())
                    {
                        final Long[] questionasked = new Long[1];
                        FirebasePaths.firebaseUsersNotificationTimeDbRef()
                            .child(FirebaseAuthProvider.getCurrentUserId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        questionasked[0] = dataSnapshot.getChildrenCount();
                                    }
                                    queriesAsked.setText(String.valueOf(questionasked[0] == null ? 0 : questionasked[0]));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                        FirebasePaths.fireabaseUserMessageLikesCount()
                                .child(FirebaseAuthProvider.getCurrentUserId())
                                .child("likesCount")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists())
                                        {
                                            String likesCount = dataSnapshot.getValue().toString();
                                            if(likesCount != null)
                                            {
                                                answers.setText(String.valueOf(likesCount));
                                            }
                                            else
                                            {
                                                answers.setText(String.valueOf(0));
                                            }

                                        }
                                        else

                                        {
                                            answers.setText(String.valueOf(0));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

}
