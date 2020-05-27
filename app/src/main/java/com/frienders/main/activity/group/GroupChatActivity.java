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
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frienders.main.activity.MainActivity;
import com.frienders.main.config.ActivityParameters;
import com.frienders.main.config.Configuration;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.MsgType;
import com.frienders.main.db.model.Messages;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.db.model.Group;
import com.frienders.main.db.model.GroupMessage;
import com.frienders.main.adapter.GroupMessageAdapter;
import com.frienders.main.R;
import com.frienders.main.utility.FileUtil;
import com.frienders.main.utility.Utility;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.frienders.main.activity.profile.SettingActivity.calculateInSampleSize;
import static com.frienders.main.config.Configuration.RequestCodeForDocPick;
import static com.frienders.main.config.Configuration.RequestCodeForImagePick;
import static com.frienders.main.config.Configuration.RequestCodeForVideoPick;
import static com.frienders.main.config.Configuration.imageMaxHeight;
import static com.frienders.main.config.Configuration.imageMaxWidth;


public class GroupChatActivity extends AppCompatActivity
{
    private TextView groupDisplayName, groupDescription;
    private CircleImageView groupProfileImage;
    private String messageSenderUserId;

    private ImageButton groupSendMessageButton, groupSendFileButton;
    private EditText groupMessageInputText;
    private Button groupChatSubscribeButton;
    private androidx.appcompat.widget.Toolbar groupChatToolBar;

    private final List<GroupMessage> groupMessageList = new LinkedList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter groupMessageAdapter;
    private RecyclerView recyclerView;
    private String groupId;
    private ProgressDialog loadingBar;
    private String language = "eng";
    private String checker = "", myUrl = "";
    private UploadTask uploadTask;
    private String currentUserDisplayName = "Unknown";
    private static final int RC_PHOTO_PICKER_PERM = 555;
    public static int imageCount = 0;
    private ProgressBar progressBar, groupMessageProgressBar;
    private Group group;
    private List<GroupMessage> moreMessages = new LinkedList<>();
    boolean reachedEnd = false;
    boolean isLoading = false;
    private RecyclerView.OnScrollListener onScrollListener;
    private Set<String> messageDownloaded = new HashSet<>();
    private List<GroupMessage> buffer = new LinkedList<>();
    private TextView newmessagenotification;
    boolean istheLatestMessageTheLastVisibleItem = true;
    final int[] notvisiblenewmessagecount = new int[]{0};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        groupId = getIntent().getExtras().get(ActivityParameters.groupId).toString();

        if(getIntent().getExtras().get(ActivityParameters.Group) != null)
        {
            group = (Group) getIntent().getExtras().get(ActivityParameters.Group);
            groupId = group.getId();
        }

        initializeUi();
        initializeUiButtons();
        populateGroupName();
//        initScrollListener();
//        initScrollListener();
    }

    private void initializeUi()
    {
        groupMessageProgressBar = findViewById(R.id.groupMessageProgressBar);
        groupChatToolBar = findViewById(R.id.setting_toolbar);
        groupSendFileButton = findViewById(R.id.group_send_file_btn);
        groupChatToolBar = findViewById(R.id.group_chat_toolbar);
        groupChatSubscribeButton = findViewById(R.id.group_display_message);
        progressBar = findViewById(R.id.progressbar_group_chat);
        newmessagenotification = findViewById(R.id.new_message_notification);
        newmessagenotification.setVisibility(View.GONE);
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


        /// GroupMessageAdapter starts

        recyclerView = findViewById(R.id.group_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        groupMessageAdapter = new GroupMessageAdapter(groupMessageList, this, recyclerView, groupId,
                linearLayoutManager);
        recyclerView.setLayoutManager(linearLayoutManager);
        initScrollListener();

        /// GroupMessageAdapter Ends


        loadingBar = new ProgressDialog(GroupChatActivity.this);
        loadingBar.setCanceledOnTouchOutside(false);

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

        newmessagenotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                notvisiblenewmessagecount[0] = 0;
                newmessagenotification.setVisibility(View.GONE);
            }
        });

        final DatabaseReference ref = FirebasePaths.firebaseMessageRef().child(groupId);
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists() && dataSnapshot.hasChildren())
                {
                    int childrenCount = (int)dataSnapshot.getChildrenCount();

                    ref.endAt(childrenCount).limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if(dataSnapshot.exists())
                            {
                                GroupMessage groupMessage = dataSnapshot.getValue(GroupMessage.class);
                                if(!messageDownloaded.contains(groupMessage.getMessageId()))
                                {
                                    groupMessageList.add(groupMessage);
                                    groupMessageAdapter.notifyItemInserted(groupMessageList.size() - 1);
                                }

                                if(istheLatestMessageTheLastVisibleItem)
                                {
                                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                                }
                                else
                                {
                                    notvisiblenewmessagecount[0]++;
                                    newmessagenotification.setVisibility(View.VISIBLE);
                                    newmessagenotification.setText("Check new messages: "+ String.valueOf(notvisiblenewmessagecount[0]));
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void getMessages(final String nodeId)
    {
        Query query;
        moreMessages.clear();

        if(nodeId == null && groupMessageList.size() == 0)
        {
            groupMessageList.add(0, null);
            groupMessageAdapter.notifyItemInserted(0);
        }


        if(nodeId == null)
        {
            query = FirebasePaths
                    .firebaseMessageRef()
                    .child(groupId)
                    .orderByKey()
                    .limitToLast(12);
        }
        else
        {
            query = FirebasePaths
                    .firebaseMessageRef()
                    .child(groupId)
                    .endAt(nodeId)
                    .orderByKey()
                    .limitToLast(8);
        }


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot != null && dataSnapshot.exists() && dataSnapshot.hasChildren())
                {
                    if(dataSnapshot.getChildrenCount() == 1)
                    {
                        reachedEnd = false;
                    }
                    else
                    {
                        for(DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            reachedEnd = false;
                            GroupMessage message = ds.getValue(GroupMessage.class);


                            if(message != null && message.getMessageId() != null)
                            {
                                if(nodeId == null)
                                {
                                    if(!messageDownloaded.contains(message.getMessageId()))
                                    {
                                        messageDownloaded.add(message.getMessageId());
                                        moreMessages.add(message);
                                    }
                                    else if (message.getMessageId().equals(nodeId))
                                    {
                                        continue;
                                    }
                                    else
                                    {
                                        reachedEnd = true;
                                        break;
                                    }
                                }

                                else
                                {
                                    if(!messageDownloaded.contains(message.getMessageId()))
                                    {
                                        messageDownloaded.add(message.getMessageId());
                                        moreMessages.add(message);
                                    }
                                    else if (message.getMessageId().equals(nodeId))
                                    {
                                        continue;
                                    }
                                    else
                                    {
                                        reachedEnd = true;
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }
                else
                {
                    while (groupMessageList != null && groupMessageList.size()> 0 && groupMessageList.get(0) == null)
                    {
                        groupMessageList.remove(0);
                        groupMessageAdapter.notifyItemRemoved(0);
                    }
                    Toast.makeText(GroupChatActivity.this, "Be the first one to chat", Toast.LENGTH_SHORT).show();
                }


                if(reachedEnd == false)
                {
                    while (groupMessageList != null && groupMessageList.size()> 0 && groupMessageList.get(0) == null)
                    {
                        groupMessageList.remove(0);
                        groupMessageAdapter.notifyItemRemoved(0);
                    }


                    if(moreMessages.size() > 0)
                    {
                        groupMessageList.addAll(0, moreMessages);
                        groupMessageAdapter.notifyDataSetChanged();
                        moreMessages.clear();
                    }

                    if(nodeId == null)
                    {
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                    }

                }
                else
                {
                    Toast.makeText(GroupChatActivity.this, "No more chats", Toast.LENGTH_SHORT).show();
                }


                isLoading = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GroupChatActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initScrollListener() {

        getMessages(null);

        recyclerView.setAdapter(groupMessageAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == ListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                {
                    isLoading = true;

                    int lastCompletellyVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                    if(lastCompletellyVisibleItemPosition >= groupMessageList.size() -  4)
                    {
                        istheLatestMessageTheLastVisibleItem = true;
                    }
                    else
                    {
                        istheLatestMessageTheLastVisibleItem = false;
                        newmessagenotification.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int posx = linearLayoutManager.findFirstVisibleItemPosition();

                if(isLoading)
                {
                    if(dy < 0 && dx == 0)
                    {
                        groupMessageList.add(0, null);
                        groupMessageAdapter.notifyItemInserted(0);

                        if (linearLayoutManager != null)
                        {
                            //bottom of list!
                            if(groupMessageList.size() > 0)
                            {
                                final String messageId =  (groupMessageList.get(1) != null ? groupMessageList.get(1).getMessageId() : null);
                                if(messageId != null)
                                {
                                    getMessages(messageId);
                                }
                                else
                                {
                                    groupMessageList.remove(0);
                                    groupMessageAdapter.notifyItemRemoved(0);
                                }
                            }
                        }
                    }
                    else
                    {
                        isLoading = false;
                    }

                }
            }
        });


    }

    private void initializeUiButtons()
    {

        if(messageSenderUserId == null)
        {
            messageSenderUserId = FirebaseAuthProvider.getCurrentUserId();
        }

        final DatabaseReference firebaseRef = FirebasePaths.firebaseUsersDbRef()
                .child(messageSenderUserId)
                .child("subscribed").child(groupId);

        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())
                {
                    groupSendMessageButton.setVisibility(View.GONE);
                    groupSendFileButton.setVisibility(View.GONE);
                    groupMessageInputText.setVisibility(View.GONE);
                    groupChatSubscribeButton.setVisibility(View.VISIBLE);
                    groupChatSubscribeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            FirebasePaths.firebaseUserRef(messageSenderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(UsersFirebaseFields.device_token))
                                    {

                                        final String token = dataSnapshot.child(UsersFirebaseFields.device_token).getValue().toString();
                                        final DatabaseReference leaveRef = FirebasePaths.firebaseSubscribedRef()
                                                .child(groupId)
                                                .child(messageSenderUserId)
                                                .child(UsersFirebaseFields.device_token);
                                        leaveRef.setValue(token);

                                        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if(group == null)
                                                {
                                                    FirebasePaths.firebaseGroupsLeafsRef()
                                                            .child(groupId)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if(dataSnapshot.exists())
                                                                    {
                                                                        group = dataSnapshot.getValue(Group.class);
                                                                        firebaseRef.setValue(group);
                                                                        makeUserAbleToChat();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                }
                                                else
                                                {
                                                    firebaseRef.setValue(group);
                                                    makeUserAbleToChat();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {

                                            }
                                        });

                                    }
                                    else
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
                                                            FirebasePaths.firebaseUserRef(messageSenderUserId).child(UsersFirebaseFields.device_token)
                                                                    .setValue(deviceToken)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                final DatabaseReference leaveRef = FirebasePaths.firebaseSubscribedRef()
                                                                                        .child(groupId)
                                                                                        .child(messageSenderUserId)
                                                                                        .child(UsersFirebaseFields.device_token);
                                                                                leaveRef.setValue(deviceToken);
                                                                                firebaseRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                                                    {
                                                                                        firebaseRef.setValue(group);
                                                                                        makeUserAbleToChat();
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                                                                    {

                                                                                    }
                                                                                });
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
                    });
                }
                else
                {
                    makeUserAbleToChat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
                                getString(R.string.video) + " 20MB MAX",
                                "DOCS",
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
                            pickPhotoClicked();
//                            Intent intent = new Intent();
//                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                            intent.setType("image/*");
//                            intent.putExtra("crop", true);
//                            startActivityForResult(Intent.createChooser(intent, "select image"), RequestCodeForImagePick);
                        }
                        if(which == 1)
                        {
                            checker = Configuration.VIDEOFILE;

                            if (true)
                            {
                                pickVideoClicked();
                            }
                            else
                            {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("video/*");
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                                startActivityForResult(intent, RequestCodeForVideoPick);
                                startActivityForResult(Intent.createChooser(intent, "select video"), RequestCodeForVideoPick);
                            }
                        }
                        if(which == 2)
                        {

                            checker = Configuration.DOCFILE;
                            pickDoc();
//                            Intent intent = new Intent();
//                            intent.setAction(Intent.ACTION_PICK);
//                            intent.setType("Application/msword");
//                            intent.addCategory(Intent.CATEGORY_OPENABLE);
//                            startActivityForResult(Intent.createChooser(intent, "select doc"), RequestCodeForDocPick);
//                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                            startActivityForResult(intent, RequestCodeForDocPick);



                        }

//                        if(which == 3)
//                        {
//                            checker = Configuration.PDFFILE;
//                            Intent intent = new Intent();
//                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                            intent.setType("Application/pdf");
//                            intent.addCategory(Intent.CATEGORY_OPENABLE);
//                            startActivityForResult(Intent.createChooser(intent, "select pdf"), RequestCodeForDocPick);
//                        }
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

    @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
    public void pickPhotoClicked() {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER))
        {
            FilePickerBuilder.getInstance()
                    .setActivityTitle("Please select photos")
                    .enableVideoPicker(false)
                    .enableCameraSupport(true)
                    .showGifs(true)
                    .setMaxCount(3)
                    .showFolderView(true)
                    .enableImagePicker(true)
                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .pickPhoto(this, RequestCodeForImagePick);

        } else
        {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
                    RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
    public void pickVideoClicked()
    {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER))
        {
            FilePickerBuilder.getInstance()
                    .setActivityTitle("Please select video")
                    .enableVideoPicker(true)
                    .enableCameraSupport(false)
                    .setMaxCount(1)
                    .showFolderView(true)
                    .enableImagePicker(false)
                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .pickPhoto(this, RequestCodeForVideoPick);

        }
        else
        {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
                    RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
    public void pickDoc()
    {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER))
        {
            FilePickerBuilder.getInstance()
                    .setMaxCount(5)
                    .enableDocSupport(true)//optional
                    .setActivityTheme(R.style.LibAppTheme) //optional
                    .pickFile(this, RequestCodeForDocPick);
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//            startActivityForResult(intent, RequestCodeForDocPick);
        }
        else
        {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
                    RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null)
        {
            loadingBar.setMessage("Nothing selected");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            loadingBar.dismiss();
        }

        loadingBar.setMessage("Uploading " + (requestCode == RequestCodeForImagePick || requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE? "Image" : "Video"));
        loadingBar.setCanceledOnTouchOutside(false);
//        loadingBar.show();

        final int[] totalFiles1 = new int[] {0};
        ArrayList<Uri> dataList = null;
        if(requestCode == RequestCodeForImagePick && resultCode ==  RESULT_OK && data != null && requestCode != CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            dataList = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
            if (dataList != null)
            {
                if(dataList.size() > 0)
                {
                    totalFiles1[0] = dataList.size();
                }
                for(Uri ImageUri : dataList)
                {
                    CropImage.activity(ImageUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                }
            }
        }

        if (checker.equals(Configuration.IMAGEFILE) && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK)
                {
                    progressBar.setVisibility(View.VISIBLE);
                    final Uri resultUri = result.getUri();
//                    loadingBar.show();


                    final int[] uploadedFiles = new int[] {0};

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Image file").child(groupId);
                    final String messageSenderRef = FirebasePaths.MessagesPath + "/";
                    DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                            .child(groupId).push();

                    final String messagePushID = userMessageKeyRef.getKey();
                    final StorageReference filePath = storageReference.child(messagePushID + "." + "png");
                    Bitmap image = Utility.decodeFile(resultUri.getPath());

                    File actualImage = null;
                    File compressedFile = null;

                    try
                    {
                        actualImage = FileUtil.from(GroupChatActivity.this, resultUri);
                        compressedFile = new Compressor(this)
                                .setMaxHeight(Math.min(image.getHeight(), imageMaxHeight))
                                .setMaxWidth(Math.min(image.getWidth(), imageMaxWidth))
                                .setQuality(75)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .compressToFile(actualImage);

//                         progressBar = new ProgressBar(this);
                        progressBar.setProgress(20);

                        uploadTask = filePath.putFile(Uri.fromFile(compressedFile));
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                            {
                                double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
//                                loadingBar.setMessage("Uploading " + (int)p + "% complete.");
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
                        }).continueWithTask(new Continuation() {

                            @Override
                            public Object then(@NonNull Task task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task)
                            {
                                if (task.isSuccessful()) {
                                    uploadedFiles[0]++;
                                    progressBar.setVisibility(View.INVISIBLE);
                                    loadingBar.setMessage("Uploading " +uploadedFiles[0] +" of "+ totalFiles1[0] + " is complete.");

                                    Uri downloadUrl = task.getResult();
                                    myUrl = downloadUrl.toString();

                                    final GroupMessage messages = new GroupMessage();
                                    messages.setMessage(myUrl);
                                    messages.setFileName(resultUri.getLastPathSegment());
                                    messages.setType(MsgType.IMAGE.getMsgTypeId());
                                    messages.setFrom(messageSenderUserId);
                                    messages.setGroupId(groupId);
                                    messages.setTime(Utility.getCurrentTime() + " "+ Utility.getCurrentDate());
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
                                            else
                                            {
//                                                groupMessageList.add(messages);
//                                                groupMessageAdapter.notifyItemInserted(groupMessageList.size() - 1);
//                                                recyclerView.smoothScrollToPosition(groupMessageList.size() -1);
                                            }
                                            loadingBar.dismiss();
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
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        loadingBar.dismiss();
                    }
                }
            }

        }

        else if(requestCode == RequestCodeForVideoPick && data !=null )
        {
//            loadingBar.show();

            if (false)
            {
                if(data.getData() == null)
                    return;
                dataList = new ArrayList<>();
                dataList.add(data.getData());
            }
            else
            {
                dataList = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
            }


            for(final Uri resultUri : dataList)
            {

                try {
                    ParcelFileDescriptor f =  getContentResolver().openFileDescriptor(resultUri, "r");
                    long size = f.getStatSize();

                    if(size > Configuration.maxVideoFileUploadableSizeInBytes)
                    {
                        Toast.makeText(this, getString(R.string.filesizeerrormessage),  Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                progressBar.setVisibility(View.VISIBLE);
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Video file").child(groupId);
                final String messageSenderRef = FirebasePaths.MessagesPath + "/";
                final DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                        .child(groupId).push();
                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "3gp");

                uploadTask = filePath.putFile(resultUri);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

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
                }).continueWithTask(new Continuation() {

                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
//                            uploadedFiles[0]++;
                            final Uri downloadUrl = task.getResult();
                            final String fileName = getFileName(resultUri);
                            myUrl = downloadUrl.toString();

                            final GroupMessage messages = new GroupMessage();
                            messages.setMessage(myUrl);
                            messages.setFileName(fileName);
                            messages.setType(MsgType.VIDEO.getMsgTypeId());
                            messages.setFrom(messageSenderUserId);
                            messages.setGroupId(groupId);
                            messages.setTime(Utility.getCurrentTime() + " " + Utility.getCurrentDate());
                            messages.setMessageId(messagePushID);
                            messages.setSenderDisplayName(currentUserDisplayName);


                            final Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + groupId + "/" + messagePushID, messages);

                            FirebasePaths.firebaseDbRawRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (!task.isSuccessful())
                                    {
                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
//                                        groupMessageList.add(messages);
//                                        groupMessageAdapter.notifyItemInserted(groupMessageList.size() - 1);
//                                        recyclerView.smoothScrollToPosition(groupMessageList.size() -1);
                                    }
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(GroupChatActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        else if(requestCode == RequestCodeForDocPick)
        {


            if (false)
            {
                if(data.getData() == null)
                    return;
                dataList = new ArrayList<>();
                dataList.add(data.getData());
            }
            else
            {
                dataList = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS);
            }

            if(dataList == null || dataList.size() == 0) return;

            for(final Uri resultUri : dataList)
            {

                final String fileName = getFileName(resultUri);
//                final int lastIndexOfDot = fileName.lastIndexOf(".");
//                final String ext = fileName.substring(lastIndexOfDot, fileName.length());
                progressBar.setVisibility(View.VISIBLE);

                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Doc file").child(groupId);
                final String messageSenderRef = FirebasePaths.MessagesPath + "/";
                final DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                        .child(groupId).push();
                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                uploadTask = filePath.putFile(resultUri);
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        float progress = (float) (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        int currentprogress = (int) progress;

                        if (currentprogress > 20 && currentprogress <= 30) {
                            progressBar.setMax(100);
                            progressBar.setProgress(20);
                        }

                        if (currentprogress > 40 && currentprogress <= 50) {
                            progressBar.setMax(100);
                            progressBar.setProgress(40);
                        }

                        if (currentprogress > 50 && currentprogress <= 60) {
                            progressBar.setMax(100);
                            progressBar.setProgress(50);
                        }

                        if (currentprogress > 60 && currentprogress <= 70) {
                            progressBar.setMax(100);
                            progressBar.setProgress(60);
                        }

                        if (currentprogress > 70 && currentprogress <= 80) {
                            progressBar.setMax(100);
                            progressBar.setProgress(70);
                        }

                        if (currentprogress > 80 && currentprogress <= 90) {
                            progressBar.setMax(100);
                            progressBar.setProgress(80);
                        }

                        if (currentprogress > 90 && currentprogress < 100) {
                            progressBar.setMax(100);
                            progressBar.setProgress(90);
                        }

                        if (currentprogress == 100) {
                            progressBar.setMax(100);
                            progressBar.setProgress(100);
                        }


                    }
                }).continueWithTask(new Continuation() {

                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();

                            myUrl = downloadUrl.toString();

                            final GroupMessage messages = new GroupMessage();
                            messages.setMessage(myUrl);
                            messages.setFileName(fileName);
                            messages.setType(MsgType.DOC.getMsgTypeId());
                            messages.setFrom(messageSenderUserId);
                            messages.setGroupId(groupId);
                            messages.setTime(Utility.getCurrentTime() +" "+ Utility.getCurrentDate());
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
                                    else
                                    {
//                                        groupMessageList.add(messages);
//                                        groupMessageAdapter.notifyItemInserted(groupMessageList.size() - 1);
//                                        recyclerView.smoothScrollToPosition(groupMessageList.size() -1);
                                    }

                                    progressBar.setVisibility(View.INVISIBLE);
                                    groupMessageInputText.setText("");
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(GroupChatActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void sendMessageInGroup()
    {
        String messageText = groupMessageInputText.getText().toString();
        groupMessageInputText.setText("");


        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, getString(R.string.writefirstmessage), Toast.LENGTH_SHORT).show();
        }
        else
        {
            final DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                    .child(groupId).push();
            final String messagePushID = userMessageKeyRef.getKey();

            final GroupMessage messages = new GroupMessage();
            messages.setMessage(messageText);
            messages.setType(MsgType.TEXT.getMsgTypeId());
            messages.setFrom(messageSenderUserId);
            messages.setGroupId(groupId);
            messages.setTime(Utility.getCurrentTime() +" "+ Utility.getCurrentDate());
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
                        Toast.makeText(GroupChatActivity.this, "Could not send the last message", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
//                        groupMessageList.add(messages);
//                        groupMessageAdapter.notifyItemInserted(groupMessageList.size() - 1);
//                        recyclerView.smoothScrollToPosition(groupMessageList.size() -1);
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


    private void makeUserAbleToChat()
    {
        groupChatSubscribeButton.setVisibility(View.INVISIBLE);
        groupSendMessageButton.setVisibility(View.VISIBLE);
        groupSendFileButton.setVisibility(View.VISIBLE);
        groupMessageInputText.setVisibility(View.VISIBLE);
    }




}


