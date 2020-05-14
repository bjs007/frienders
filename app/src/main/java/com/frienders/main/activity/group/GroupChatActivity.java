package com.frienders.main.activity.group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.frienders.main.activity.MainActivity;
import com.frienders.main.config.ActivityParameters;
import com.frienders.main.config.Configuration;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.MsgType;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.db.model.Group;
import com.frienders.main.db.model.GroupMessage;
import com.frienders.main.adapter.GroupMessageAdapter;
import com.frienders.main.R;
import com.frienders.main.db.refs.FirestorePath;
import com.frienders.main.utility.Utility;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.frienders.main.activity.profile.SettingActivity.calculateInSampleSize;
import static com.frienders.main.config.Configuration.RequestCodeForDocPick;
import static com.frienders.main.config.Configuration.RequestCodeForImagePick;
import static com.frienders.main.config.Configuration.RequestCodeForVideoPick;

public class GroupChatActivity extends AppCompatActivity
{
    private TextView groupDisplayName, groupDescription;
    private CircleImageView groupProfileImage;
    private String messageSenderUserId;

    private ImageButton groupSendMessageButton, groupSendFileButton;
    private EditText groupMessageInputText;
    private androidx.appcompat.widget.Toolbar groupChatToolBar;

    private final List<GroupMessage> groupMessageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter groupMessageAdapter;
    private RecyclerView groupMessagesListView;
    private String groupId;
    private ProgressDialog loadingBar;
    private String language = "eng";
    private String checker = "", myUrl = "";
    private UploadTask uploadTask;
    private String currentUserDisplayName = "Unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        groupId = getIntent().getExtras().get(ActivityParameters.groupId).toString();

        initializeUi();
        initializeUiButtons();
        populateGroupName();
    }

    private void initializeUiButtons()
    {
        groupSendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessageInGroup();
            }
        });

        groupChatToolBar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendUserToMainActivity();
            }
        });

        groupSendFileButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final CharSequence options[] = new CharSequence[]
                    {
                        getString(R.string.image),
                        getString(R.string.video),
                        getString(R.string.Docs),
                            "PDF"
                    };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle(getString(R.string.selectfiletype));
                builder.setItems(options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which == 0)
                        {
                            checker = Configuration.IMAGEFILE;
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "select image"), RequestCodeForImagePick);
                        }
                        if(which == 1)
                        {
                            checker = Configuration.VIDEOFILE;
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
                            intent.setType("video/*");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivityForResult(intent, RequestCodeForVideoPick);

//                            startActivityForResult(Intent.createChooser(intent, "select video"), RequestCodeForVideoPick);
                        }
                        if(which == 2)
                        {
                            checker = Configuration.DOCFILE;
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("Application/msword");
                            startActivityForResult(Intent.createChooser(intent, "select video"), RequestCodeForDocPick);
                        }

                        if(which == 3)
                        {
                            checker = Configuration.PDFFILE;
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("Application/pdf");
                            startActivityForResult(Intent.createChooser(intent, "select video"), RequestCodeForDocPick);
                        }
                    }
                });
                builder.show();
            }
        });

        groupChatToolBar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent groupDetailDisplayActivity = new Intent(GroupChatActivity.this, GroupDetailDisplayActivity.class);
                groupDetailDisplayActivity.putExtra(ActivityParameters.groupId, groupId);
                groupDetailDisplayActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(groupDetailDisplayActivity);
            }
        });
    }

    private void initializeUi()
    {
        groupChatToolBar = findViewById(R.id.setting_toolbar);
        groupSendFileButton = findViewById(R.id.group_send_file_btn);
        groupChatToolBar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(groupChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater  = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_group_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        groupDisplayName = findViewById(R.id.custom_group_name);
        groupDescription = findViewById(R.id.custom_group_desc);
        groupProfileImage = findViewById(R.id.custom_group_profile_image);

        groupSendMessageButton = findViewById(R.id.group_send_message_btn);
        groupSendFileButton = findViewById(R.id.group_send_file_btn);
        groupMessageInputText = findViewById(R.id.group_input_message);


        groupMessagesListView = findViewById(R.id.group_message_list_of_users);
        groupMessageAdapter = new GroupMessageAdapter(groupMessageList, this, groupMessagesListView, groupId);
        linearLayoutManager = new LinearLayoutManager(this);
        groupMessagesListView.setLayoutManager(linearLayoutManager);
        groupMessagesListView.setAdapter(groupMessageAdapter);
        loadingBar = new ProgressDialog(GroupChatActivity.this);

        FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            currentUserDisplayName = dataSnapshot.child(UsersFirebaseFields.name).getValue().toString();
                            messageSenderUserId = FirebaseAuthProvider.getCurrentUserId();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
//        loadingBar.setMessage("Uploading " + (requestCode == RequestCodeForImagePick ? "Image" : "Video"));
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        if(requestCode == RequestCodeForImagePick && resultCode ==  RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

       if (checker.equals(Configuration.IMAGEFILE))
        {
            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {

                final Uri resultUri = result.getUri();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Image file").child(groupId);
                final String messageSenderRef = FirebasePaths.MessagesPath + "/";

                DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                        .child(groupId).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                Bitmap bitmap = Utility.decodeFile(resultUri.getPath());
                Uri bitMapuri = bitmapToUriConverter(bitmap);

                uploadTask = filePath.putFile(bitMapuri);
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        loadingBar.setMessage("Uploading " + (int)p + "% complete.");
                    }
                }).continueWithTask(new Continuation() {

                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            GroupMessage messages = new GroupMessage();
                            messages.setMessage(myUrl);
                            messages.setFileName(resultUri.getLastPathSegment());
                            messages.setType(MsgType.IMAGE.getMsgTypeId());
                            messages.setFrom(messageSenderUserId);
                            messages.setGroupId(groupId);
                            messages.setTime(Utility.getCurrentTime());
                            messages.setTime(Utility.getCurrentDate());
                            messages.setMessageId(messagePushID);
                            messages.setSenderDisplayName(currentUserDisplayName);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + groupId + "/" + messagePushID, messages);

                            FirebasePaths.firebaseDbRawRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    loadingBar.dismiss();
                                    groupMessageInputText.setText("");
                                }
                            });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        loadingBar.setMessage("Upload failed");
                        loadingBar.dismiss();
                    }
                });

            }
            }

       }

        else if(requestCode == RequestCodeForVideoPick && data !=null && data.getData() !=null)
        {

            final Uri resultUri = data.getData();

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Video file").child(groupId);
            final String messageSenderRef = FirebasePaths.MessagesPath + "/";

            DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                    .child(groupId).push();

            final String messagePushID = userMessageKeyRef.getKey();
            final StorageReference filePath = storageReference.child(messagePushID + "." + "3gp");

            uploadTask = filePath.putFile(resultUri);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                {
                    double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    loadingBar.setMessage("Uploading " + (int)p + "% complete.");
                }
            }).continueWithTask(new Continuation()
            {

                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();

                        myUrl = downloadUrl.toString();

                        GroupMessage messages = new GroupMessage();
                        messages.setMessage(myUrl);
                        messages.setFileName(resultUri.getLastPathSegment());
                        messages.setType(MsgType.VIDEO.getMsgTypeId());
                        messages.setFrom(messageSenderUserId);
                        messages.setGroupId(groupId);
                        messages.setTime(Utility.getCurrentTime());
                        messages.setTime(Utility.getCurrentDate());
                        messages.setMessageId(messagePushID);
                        messages.setSenderDisplayName(currentUserDisplayName);


                        final Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + groupId + "/" + messagePushID, messages);

                        FirebasePaths.firebaseDbRawRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }

                                groupMessageInputText.setText("");
                            }
                        });
                    }

                    loadingBar.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                loadingBar.setMessage("Upload failed");
                loadingBar.dismiss();
            }
        });

        }
        else if(requestCode == RequestCodeForDocPick && data != null && data.getData() != null)
        {
            final Uri resultUri = data.getData();

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Doc file").child(groupId);
            final String messageSenderRef = FirebasePaths.MessagesPath + "/";

            DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                    .child(groupId).push();

            final String messagePushID = userMessageKeyRef.getKey();
            final StorageReference filePath = storageReference.child(messagePushID + "." + checker == Configuration.DOCFILE ?
                    "docx" : "pdf");

            uploadTask = filePath.putFile(resultUri);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                {
                    double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    loadingBar.setMessage("Uploading " + (int)p + "% complete.");
                }
            }).continueWithTask(new Continuation()
            {

                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();

                        myUrl = downloadUrl.toString();

                        GroupMessage messages = new GroupMessage();
                        messages.setMessage(myUrl);
                        messages.setFileName(resultUri.getLastPathSegment());
                        messages.setType(MsgType.DOC.getMsgTypeId());
                        messages.setFrom(messageSenderUserId);
                        messages.setGroupId(groupId);
                        messages.setTime(Utility.getCurrentTime());
                        messages.setTime(Utility.getCurrentDate());
                        messages.setMessageId(messagePushID);
                        messages.setSenderDisplayName(currentUserDisplayName);


                        final Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + groupId + "/" + messagePushID, messages);

                        FirebasePaths.firebaseDbRawRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }

                                groupMessageInputText.setText("");
                            }
                        });
                    }

                    loadingBar.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    loadingBar.setMessage("Upload failed");
                    loadingBar.dismiss();
                }
            });
        }
    }

    private void populateGroupName()
    {
         final DatabaseReference groupLeafsDbRef = FirebasePaths.firebaseGroupsLeafsRef();
         final DatabaseReference userlanguage = FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                 .child(UsersFirebaseFields.language);

        userlanguage.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    language = dataSnapshot.getValue().toString();
                }

                groupLeafsDbRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            Group grp = dataSnapshot.getValue(Group.class);
                            if(grp != null)
                            {
                                if(language.equals("eng"))
                                {
                                    groupDisplayName.setText(grp.getEngName());
                                    groupDescription.setText(grp.getEngDesc());
                                }
                                else
                                {
                                    groupDisplayName.setText(grp.getHinName());
                                    groupDescription.setText(grp.getHinDesc());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private void sendMessageInGroup()
    {
        String messageText = groupMessageInputText.getText().toString();


        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, getString(R.string.writefirstmessage), Toast.LENGTH_SHORT).show();
        }
        else
        {
            final DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                    .child(groupId).push();
            String messagePushID = userMessageKeyRef.getKey();

            GroupMessage messages = new GroupMessage();
            messages.setMessage(messageText);
            messages.setType(MsgType.TEXT.getMsgTypeId());
            messages.setFrom(messageSenderUserId);
            messages.setGroupId(groupId);
            messages.setTime(Utility.getCurrentTime());
            messages.setTime(Utility.getCurrentDate());
            messages.setMessageId(messagePushID);
            messages.setSenderDisplayName(currentUserDisplayName);


            final Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(groupId + "/" + messagePushID, messages);

            FirebasePaths.firebaseMessageRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (!task.isSuccessful())
                    {
                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    loadingBar.dismiss();
                    groupMessageInputText.setText("");
                }
            });
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(GroupChatActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
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
}
