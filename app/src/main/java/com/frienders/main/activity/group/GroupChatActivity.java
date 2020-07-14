package com.frienders.main.activity.group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frienders.main.SplashActivity;
import com.frienders.main.activity.ImageViwer;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import id.zelory.compressor.Compressor;
//import nl.bravobit.ffmpeg.FFmpeg;
//import nl.bravobit.ffmpeg.FFmpeg;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.media.tv.TvTrackInfo.TYPE_VIDEO;
import static com.frienders.main.config.Configuration.RequestCodeForDocPick;
import static com.frienders.main.config.Configuration.RequestCodeForImagePick;
import static com.frienders.main.config.Configuration.RequestCodeForVideoPick;
import static com.frienders.main.config.Configuration.imageMaxHeight;
import static com.frienders.main.config.Configuration.imageMaxWidth;


public class GroupChatActivity extends AppCompatActivity {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        groupId = getIntent().getExtras().get(ActivityParameters.groupId).toString();

        if (getIntent().getExtras().get(ActivityParameters.Group) != null) {
            group = (Group) getIntent().getExtras().get(ActivityParameters.Group);
            groupId = group.getId();
        }

        initializeUi();
        initializeUiButtons();
        populateGroupName();
    }

    private void initializeUi() {
        groupMessageProgressBar = findViewById(R.id.groupMessageProgressBar);
        groupSendFileButton = findViewById(R.id.group_send_file_btn);
        groupChatToolBar = findViewById(R.id.group_chat_toolbar);
        groupChatSubscribeButton = findViewById(R.id.group_display_message);
        progressBar = findViewById(R.id.progressbar_group_chat);
        newmessagenotification = findViewById(R.id.new_message_notification);
        newmessagenotification.setVisibility(View.GONE);
        setSupportActionBar(groupChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
               view.findViewById(R.id.group_message_receiver_image_view)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utility.displayImage(GroupChatActivity.this, groupMessageList.get(position).getMessage());
                            }
                        });

               view.findViewById(R.id.group_message_sender_image_view)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utility.displayImage(GroupChatActivity.this, groupMessageList.get(position).getMessage());
                            }
                        });

               view.findViewById(R.id.groupVideoViewSender)
                       .setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               Utility.displayImage(GroupChatActivity.this, groupMessageList.get(position).getMessage());
                           }
                        });

               view.findViewById(R.id.groupVideoViewSender)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utility.playVideo(GroupChatActivity.this, groupMessageList.get(position).getMessage());
                            }
                        });

               view.findViewById(R.id.groupVideoViewReceiver)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utility.playVideo(GroupChatActivity.this, groupMessageList.get(position).getMessage());
                            }
                        });

               view.findViewById(R.id.group_reciever_message_like_button)
                       .setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               final DatabaseReference databaseReference = FirebasePaths.firebaseMessageLikeDbRef()
                                       .child(groupId)
                                       .child(groupMessageList.get(position).getMessageId())
                                       .child(FirebaseAuthProvider.getCurrentUserId());

                               databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       Long likes = 0L;

                                       try
                                       {
                                           if(groupMessageList.get(position).getLikes() != null)
                                           {
                                               likes = groupMessageList.get(position).getLikes();
                                           }

                                           if (likes == null) {
                                               likes = 0L;
                                           }

                                           if (dataSnapshot.exists() )
                                           {
                                               databaseReference.removeValue();
                                               likes--;
                                           }
                                           else
                                           {
                                               databaseReference.setValue("liked");
                                               likes++;
                                           }
                                           if (likes != null)
                                               groupMessageList.get(position).setLikes(likes);

                                           groupMessageList.set(position, groupMessageList.get(position));
                                           recyclerView.getAdapter().notifyItemChanged(position);
                                           Toast.makeText(GroupChatActivity.this, "Position is " + groupMessageList.get(position).getMessage() + " - " + position, Toast.LENGTH_LONG).show();

                                       }
                                       catch (Exception ex)
                                       {
                                           Toast.makeText(GroupChatActivity.this, "Could not register likes!", Toast.LENGTH_SHORT).show();
                                       }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });

                           }
                       });

               view.findViewById(R.id.group_sender_message_notification_icon)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final DatabaseReference databaseReference = FirebasePaths.firebaseUsersNotificationTimeDbRef()
                                        .child(FirebaseAuthProvider.getCurrentUserId())
                                        .child(groupMessageList.get(position).getGroupId());

                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        Date date = new Date();
                                        //This method returns the time in millis
                                        long timeMilli = date.getTime();

                                        if(dataSnapshot.exists())
                                        {
                                            Long timestamp = null;
                                            try
                                            {
                                                timestamp = Long.parseLong(dataSnapshot.getValue().toString());
                                            }
                                            catch (Exception ex)
                                            {

                                            }

                                            if(timestamp != null && timeMilli - timestamp < 16 * 60 * 1000)
                                            {
                                                Toast.makeText(GroupChatActivity.this, "You can't send notification \nwithin 15 minutes in the same group.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                databaseReference.setValue(timeMilli);
                                            }
                                        }
                                        else
                                        {
                                            databaseReference.setValue(timeMilli);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });

                                final DatabaseReference userMessageKeyRef =
                                        FirebasePaths
                                                .firebaseDbRawRef()
                                                .child("Notification")
                                                .child(groupId)
                                                .child(groupMessageList.get(position).getGroupId())
                                                .push();

                                final String messagePushID = userMessageKeyRef.getKey();
                                final Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messagePushID, groupMessageList.get(position));

                                FirebasePaths.firebaseDbRawRef().child("Notification").child(groupId).updateChildren(messageBodyDetails);
                            }
                        });

               view.findViewById(R.id.group_message_receiver_doc_view)
                       .setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               Utility.downloadDoc(GroupChatActivity.this, groupMessageList.get(position).getMessage());
                           }
                       });

               view.findViewById(R.id.group_message_sender_doc_view)
                       .setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               Utility.downloadDoc(GroupChatActivity.this, groupMessageList.get(position).getMessage());
                           }
                       });


            }

            @Override
            public void onLongClick(View view, int position) {
                Toast.makeText(GroupChatActivity.this, "Long press on position :"+position,
                        Toast.LENGTH_LONG).show();
            }
        }));

        initScrollListener();


        loadingBar = new ProgressDialog(GroupChatActivity.this);
        loadingBar.setCanceledOnTouchOutside(false);

        FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.child(UsersFirebaseFields.name) != null) {

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

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    int childrenCount = (int) dataSnapshot.getChildrenCount();

                    ref.endAt(childrenCount).limitToLast(1).addChildEventListener(new ChildEventListener()
                    {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                        {
                            if (dataSnapshot.exists())
                            {
                                GroupMessage groupMessage = dataSnapshot.getValue(GroupMessage.class);
                                if (!messageDownloaded.contains(groupMessage.getMessageId()))
                                {
                                    groupMessageList.add(groupMessage);
                                    groupMessageAdapter.notifyItemInserted(groupMessageList.size() - 1);
                                }
                                else if (groupMessageList != null && groupMessageList.size() == 0)
                                {
                                    groupMessageList.add(groupMessage);
                                    groupMessageAdapter.notifyItemInserted(0);
                                }

                                if (istheLatestMessageTheLastVisibleItem)
                                {
                                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                                }
                                else
                                {
                                    if (!groupMessage.getFrom().equals(FirebaseAuthProvider.getCurrentUserId()))
                                    {
                                        notvisiblenewmessagecount[0]++;
                                        newmessagenotification.setVisibility(View.VISIBLE);
                                        newmessagenotification.setText("New messages : " + String.valueOf(notvisiblenewmessagecount[0]));
                                    }
                                    else
                                    {
                                        recyclerView.smoothScrollToPosition(groupMessageAdapter.getItemCount());
                                    }
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
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }


    public void getMessages(final String nodeId) {
        Query query;
        moreMessages.clear();

        if (nodeId == null && groupMessageList.size() == 0) {
            groupMessageList.add(0, null);
            groupMessageAdapter.notifyItemInserted(0);
        }


        if (nodeId == null) {
            query = FirebasePaths
                    .firebaseMessageRef()
                    .child(groupId)
                    .orderByKey()
                    .limitToLast(12);
        } else {
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

                if (dataSnapshot != null && dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    if (dataSnapshot.getChildrenCount() == 1) {
                        reachedEnd = false;
                    } else {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            reachedEnd = false;
                            GroupMessage message = ds.getValue(GroupMessage.class);


                            if (message != null && message.getMessageId() != null) {
                                if (nodeId == null) {
                                    if (!messageDownloaded.contains(message.getMessageId())) {
                                        messageDownloaded.add(message.getMessageId());
                                        moreMessages.add(message);
                                    } else if (message.getMessageId().equals(nodeId)) {
                                        continue;
                                    } else {
                                        reachedEnd = true;
                                        break;
                                    }
                                } else {
                                    if (!messageDownloaded.contains(message.getMessageId())) {
                                        messageDownloaded.add(message.getMessageId());
                                        moreMessages.add(message);
                                    } else if (message.getMessageId().equals(nodeId)) {
                                        continue;
                                    } else {
                                        reachedEnd = true;
                                        break;
                                    }
                                }

                            }
                        }
                    }
                } else {
                    while (groupMessageList != null && groupMessageList.size() > 0 && groupMessageList.get(0) == null) {
                        groupMessageList.remove(0);
                        groupMessageAdapter.notifyItemRemoved(0);
                    }
                    Toast.makeText(GroupChatActivity.this, "Be the first one to chat", Toast.LENGTH_SHORT).show();
                }


                if (reachedEnd == false) {
                    while (groupMessageList != null && groupMessageList.size() > 0 && groupMessageList.get(0) == null) {
                        groupMessageList.remove(0);
                        groupMessageAdapter.notifyItemRemoved(0);
                    }


                    if (moreMessages.size() > 0) {
                        groupMessageList.addAll(0, moreMessages);
                        groupMessageAdapter.notifyDataSetChanged();
                        moreMessages.clear();
                    }

                    if (nodeId == null) {
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                    }

                } else {
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
                if (newState == ListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isLoading = true;

                    int lastCompletellyVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                    if (lastCompletellyVisibleItemPosition >= groupMessageList.size() - 4) {
                        istheLatestMessageTheLastVisibleItem = true;
                    } else {
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

                if (isLoading) {
                    if (dy < 0 && dx == 0) {
                        groupMessageList.add(0, null);
                        groupMessageAdapter.notifyItemInserted(0);

                        if (linearLayoutManager != null) {
                            //bottom of list!
                            if (groupMessageList.size() > 0) {
                                final String messageId = (groupMessageList.get(1) != null ? groupMessageList.get(1).getMessageId() : null);
                                if (messageId != null) {
                                    getMessages(messageId);
                                } else {
                                    groupMessageList.remove(0);
                                    groupMessageAdapter.notifyItemRemoved(0);
                                }
                            }
                        }
                    } else {
                        isLoading = false;
                    }

                }
            }
        });


    }

    private void initializeUiButtons() {

        if (messageSenderUserId == null) {
            messageSenderUserId = FirebaseAuthProvider.getCurrentUserId();
        }

        final DatabaseReference firebaseRef = FirebasePaths.firebaseUsersDbRef()
                .child(messageSenderUserId)
                .child("subscribed").child(groupId);

        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    groupSendMessageButton.setVisibility(View.GONE);
                    groupSendFileButton.setVisibility(View.GONE);
                    groupMessageInputText.setVisibility(View.GONE);
                    groupChatSubscribeButton.setVisibility(View.VISIBLE);
                    groupChatSubscribeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebasePaths.firebaseUserRef(messageSenderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(UsersFirebaseFields.device_token)) {

                                        final String token = dataSnapshot.child(UsersFirebaseFields.device_token).getValue().toString();
                                        final DatabaseReference leaveRef = FirebasePaths.firebaseSubscribedRef()
                                                .child(groupId)
                                                .child(messageSenderUserId)
                                                .child(UsersFirebaseFields.device_token);
                                        leaveRef.setValue(token);

                                        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (group == null) {
                                                    FirebasePaths.firebaseGroupsLeafsRef()
                                                            .child(groupId)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.exists()) {
                                                                        group = dataSnapshot.getValue(Group.class);
                                                                        firebaseRef.setValue(group);
                                                                        makeUserAbleToChat();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                } else {
                                                    firebaseRef.setValue(group);
                                                    makeUserAbleToChat();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    } else {
                                        FirebaseInstanceId.getInstance().getInstanceId()
                                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                        if (task.isSuccessful()) {
                                                            final String deviceToken = task.getResult().getToken();
                                                            FirebasePaths.firebaseUserRef(messageSenderUserId).child(UsersFirebaseFields.device_token)
                                                                    .setValue(deviceToken)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                final DatabaseReference leaveRef = FirebasePaths.firebaseSubscribedRef()
                                                                                        .child(groupId)
                                                                                        .child(messageSenderUserId)
                                                                                        .child(UsersFirebaseFields.device_token);
                                                                                leaveRef.setValue(deviceToken);
                                                                                firebaseRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        firebaseRef.setValue(group);
                                                                                        makeUserAbleToChat();
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                } else {
                    makeUserAbleToChat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageInGroup();
            }
        });

        groupChatToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMainActivity();
            }
        });

        groupSendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence options[] = new CharSequence[]
                        {
                                getString(R.string.image),
                                getString(R.string.video) + " 16MB MAX",
                                "DOCS",
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle(getString(R.string.selectfiletype));
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = Configuration.IMAGEFILE;
                            pickPhotoClicked();
                        }
                        if (which == 1) {
                            checker = Configuration.VIDEOFILE;

                            if (true) {
                                pickVideoClicked();

//                                Intent takeVideoIntent = new Intent(Intent.ACTION_PICK);
//                                takeVideoIntent.setType("video/*");
////                                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//                                    try {
//
//                                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
//                                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//                                        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                        Uri capturedUri =
////                                                Uri.fromFile(createMediaFile(TYPE_VIDEO));
//                                        FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider" , createMediaFile(TYPE_VIDEO));
//                                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri);
//
//                                        startActivityForResult(takeVideoIntent, RequestCodeForVideoPick);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//
////                                }
                            } else {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("video/*");
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                startActivityForResult(Intent.createChooser(intent, "select video"), RequestCodeForVideoPick);
                            }
                        }
                        if (which == 2) {
                            checker = Configuration.DOCFILE;
                            pickDoc();
                        }

                    }
                });
                builder.show();
            }
        });

        groupChatToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupDetailDisplayActivity = new Intent(GroupChatActivity.this, GroupDetailDisplayActivity.class);
                groupDetailDisplayActivity.putExtra(ActivityParameters.groupId, groupId);
                groupDetailDisplayActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(groupDetailDisplayActivity);
            }
        });
    }

    private File createMediaFile(int type) throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = type == 1 ? "JPEG_" + timeStamp + "_" : "VID_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                type == 1 ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES);
        File file = File.createTempFile(
                fileName,  /* prefix */
                type == 1 ? ".jpg" : ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents

        return file;
    }
    @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
    public void pickPhotoClicked() {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
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

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
                    RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
    public void pickVideoClicked() {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
            FilePickerBuilder.getInstance()
                    .setActivityTitle("Please select video")
                    .enableVideoPicker(true)
                    .enableCameraSupport(false)
                    .setMaxCount(1)
                    .showFolderView(true)
                    .enableImagePicker(false)
                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .pickPhoto(this, RequestCodeForVideoPick);

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
                    RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
    public void pickDoc() {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
            FilePickerBuilder.getInstance()
                    .setMaxCount(5)
                    .enableDocSupport(true)//optional
                    .setActivityTheme(R.style.LibAppTheme) //optional
                    .pickFile(this, RequestCodeForDocPick);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
                    RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            loadingBar.setMessage("Nothing selected");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            loadingBar.dismiss();
        }

        loadingBar.setMessage("Uploading " + (requestCode == RequestCodeForImagePick || requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ? "Image" : "Video"));
        loadingBar.setCanceledOnTouchOutside(false);
//        loadingBar.show();

        final int[] totalFiles1 = new int[]{0};
        ArrayList<Uri> dataList = null;
        if (requestCode == RequestCodeForImagePick && resultCode == RESULT_OK && data != null && requestCode != CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            dataList = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
            if (dataList != null) {
                if (dataList.size() > 0) {
                    totalFiles1[0] = dataList.size();
                }
                for (Uri ImageUri : dataList) {
                    CropImage.activity(ImageUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                }
            }
        }

        if (checker.equals(Configuration.IMAGEFILE) && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {
                    progressBar.setVisibility(View.VISIBLE);
                    final Uri resultUri = result.getUri();
//                    loadingBar.show();


                    final int[] uploadedFiles = new int[]{0};

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Image file").child(groupId);
                    final String messageSenderRef = FirebasePaths.MessagesPath + "/";
                    DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                            .child(groupId).push();

                    final String messagePushID = userMessageKeyRef.getKey();
                    final StorageReference filePath = storageReference.child(messagePushID + "." + "png");
                    Bitmap image = Utility.decodeFile(resultUri.getPath());

                    File actualImage = null;
                    File compressedFile = null;

                    try {
                        actualImage = FileUtil.from(GroupChatActivity.this, resultUri);
                        compressedFile = new Compressor(this)
                                .setMaxHeight(Math.min(image.getHeight(), imageMaxHeight))
                                .setMaxWidth(Math.min(image.getWidth(), imageMaxWidth))
                                .setQuality(75)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .compressToFile(actualImage);

                        progressBar.setProgress(20);

                        uploadTask = filePath.putFile(Uri.fromFile(compressedFile));
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                double p = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                                loadingBar.setMessage("Uploading " + (int)p + "% complete.");
                                float progress = (float) (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                int currentprogress = (int) progress;
//                                progressBar.setProgress(currentprogress);

                                setProgressBarProgress(currentprogress);
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
                                    uploadedFiles[0]++;
                                    progressBar.setVisibility(View.INVISIBLE);
                                    loadingBar.setMessage("Uploading " + uploadedFiles[0] + " of " + totalFiles1[0] + " is complete.");

                                    Uri downloadUrl = task.getResult();
                                    myUrl = downloadUrl.toString();

//                                    final GroupMessage messages = new GroupMessage();
                                    final GroupMessage messages = createAndReturnGroupMessage(myUrl, messagePushID, MsgType.IMAGE.getMsgTypeId(),
                                            messageSenderUserId, groupId, currentUserDisplayName, null);
//


                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(messageSenderRef + groupId + "/" + messagePushID, messages);

                                    FirebasePaths.firebaseDbRawRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            } else {
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
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.setMessage("Upload failed");
                                loadingBar.dismiss();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                        loadingBar.dismiss();
                    }
                }
            }

        } else if (requestCode == RequestCodeForVideoPick && data != null) {
//            loadingBar.show();

            if (false) {
                if (data.getData() == null)
                    return;
                dataList = new ArrayList<>();
                dataList.add(data.getData());
            } else {
                dataList = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
//                dataList = new ArrayList<>();
//                dataList.add(data.getData());
            }


            for (final Uri resultUri : dataList) {
                ParcelFileDescriptor[] f1 = new ParcelFileDescriptor[dataList.size()];
                try {
                     f1[0] = getContentResolver().openFileDescriptor(resultUri, "r");
                    long size = f1[0].getStatSize();
                    File file = new File(resultUri.getPath());
                    String name = getFileName(resultUri);

                    if (size > Configuration.maxVideoFileUploadableSizeInBytes) {
                        Toast.makeText(this, getString(R.string.filesizeerrormessage), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ///
//
//                if(FFmpeg.getInstance(this).isSupported())
//                {
//                    Toast.makeText(this, "Fmpeg is supported", Toast.LENGTH_SHORT).show();
//                }
//               final VideoCompressor mVideoCompressor = new VideoCompressor(this);
//                String[] projection = {MediaStore.MediaColumns.DATA};
//
//                ContentResolver cr = getApplicationContext().getContentResolver();
//                Cursor c = cr.query(resultUri, projection, null, null, null);
//                c.moveToFirst();
////                int col =
//                String pat2 = c.getString(0);
//
//                mVideoCompressor.startCompressing(pat2, new VideoCompressor.CompressionListener() {
//                    @Override
//                    public void compressionFinished(int status, boolean isVideo, String fileOutputPath) {
//
//                        if (mVideoCompressor.isDone()) {
//                            File outputFile = new File(fileOutputPath);
//                            long outputCompressVideosize = outputFile.length();
//                            long fileSizeInKB = outputCompressVideosize / 1024;
//                            long fileSizeInMB = fileSizeInKB / 1024;
//
//                            String s = "Output video path : " + fileOutputPath + "\n" +
//                                    "Output video size : " + fileSizeInMB + "mb";
//
//                            Toast.makeText(GroupChatActivity.this, "Compression succeeded " + s, Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//
//                    @Override
//                    public void onFailure(String message) {
//                        Toast.makeText(GroupChatActivity.this, "compression failed" + message, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onProgress(final int progress) {
//                        Toast.makeText(GroupChatActivity.this, "compression progress" + progress, Toast.LENGTH_SHORT).show();
//
//                    }
//                });

                ////


                progressBar.setVisibility(View.VISIBLE);
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("MessageMedia").child("Video file").child(groupId);
                final String messageSenderRef = FirebasePaths.MessagesPath + "/";
                final DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                        .child(groupId).push();
                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "3gp");

                uploadTask = filePath.putFile(resultUri);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(5);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                        float progress = (float) (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        int currentprogress = (int) progress;

                        setProgressBarProgress(currentprogress);
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

                            final GroupMessage messages = createAndReturnGroupMessage(
                                    myUrl,
                                    messagePushID,
                                    MsgType.VIDEO.getMsgTypeId(),
                                    messageSenderUserId,
                                    groupId,
                                    currentUserDisplayName,
                                    null
                            );

                            final Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + groupId + "/" + messagePushID, messages);

                            FirebasePaths.firebaseDbRawRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    } else {
//                                        groupMessageList.add(messages);
//                                        groupMessageAdapter.notifyItemInserted(groupMessageList.size() - 1);
//                                        recyclerView.smoothScrollToPosition(groupMessageList.size() -1);
                                    }
                                    progressBar.setProgress(20);
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
        } else if (requestCode == RequestCodeForDocPick) {


            if (false) {
                if (data.getData() == null)
                    return;
                dataList = new ArrayList<>();
                dataList.add(data.getData());
            } else {
                dataList = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS);
            }

            if (dataList == null || dataList.size() == 0) return;

            for (final Uri resultUri : dataList) {

                final String fileName = getFileName(resultUri);
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
                        setProgressBarProgress(currentprogress);
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

                            final GroupMessage messages =
                                    createAndReturnGroupMessage(
                                            myUrl,
                                            messagePushID,
                                            MsgType.DOC.getMsgTypeId(),
                                            messageSenderUserId,
                                            groupId,
                                            currentUserDisplayName,
                                            fileName);

                            final Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + groupId + "/" + messagePushID, messages);

                            FirebasePaths.firebaseDbRawRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    } else {
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

    private void setProgressBarProgress(int currentprogress) {
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

    private void populateGroupName() {
        final DatabaseReference groupLeafsDbRef = FirebasePaths.firebaseGroupsLeafsRef();
        final DatabaseReference userDbRef = FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId());

        userDbRef
                .child(UsersFirebaseFields.language)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            language = dataSnapshot.getValue().toString();
                        }

                        if (group == null) {
                            groupLeafsDbRef
                                    .child(groupId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                Group grp = dataSnapshot.getValue(Group.class);
                                                if (grp != null) {
                                                    prepareGroupNameToDisplay(grp);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        } else {
                            prepareGroupNameToDisplay(group);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void prepareGroupNameToDisplay(Group grp) {
        String groupDisplayNameString = null;
        String groupDescString = null;

        if (language.equals("eng")) {
            groupDisplayNameString = grp.getEngName();
            groupDescString = grp.getEngDesc();
        } else {
            groupDisplayNameString = grp.getHinName();
            groupDescString = grp.getHinDesc();
        }

        if (groupDisplayNameString != null && groupDescString != null) {
            String groupDisplayNameMayContainRootName = Utility.getGroupDisplayNameFromDbGroupName(groupDisplayNameString);
            groupDisplayName.setText(groupDisplayNameMayContainRootName);
            groupDescription.setText(groupDescString);
        }
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

    private void sendMessageInGroup() {
        String messageText = groupMessageInputText.getText().toString();
        groupMessageInputText.setText("");


        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, getString(R.string.writefirstmessage), Toast.LENGTH_SHORT).show();
        } else {
            final DatabaseReference userMessageKeyRef = FirebasePaths.firebaseMessageRef()
                    .child(groupId).push();
            final String messagePushID = userMessageKeyRef.getKey();

            final GroupMessage messages = createAndReturnGroupMessage(messageText,
                    messagePushID,
                    MsgType.TEXT.getMsgTypeId(),
                    messageSenderUserId,
                    groupId,
                    currentUserDisplayName,
                    null);


            final Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(groupId + "/" + messagePushID, messages);

            FirebasePaths.firebaseMessageRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(GroupChatActivity.this, "Could not send the last message", Toast.LENGTH_SHORT).show();
                    } else {
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
    protected void onStart() {
        super.onStart();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(GroupChatActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
    }


    private void makeUserAbleToChat() {
        groupChatSubscribeButton.setVisibility(View.INVISIBLE);
        groupSendMessageButton.setVisibility(View.VISIBLE);
        groupSendFileButton.setVisibility(View.VISIBLE);
        groupMessageInputText.setVisibility(View.VISIBLE);
    }

    private GroupMessage createAndReturnGroupMessage(String messageText, String id, String msgType, String from, String groupId, String senderDisplayName, String fileName) {
        final GroupMessage messages = new GroupMessage();
        if (messageText == null || id == null || msgType == null) return null;

        messages.setMessage(messageText);
        messages.setTime(Utility.getCurrentTime() + " " + Utility.getCurrentDate());
        messages.setMessageId(id);

        if (fileName != null)
            messages.setFileName(fileName);

        if (msgType != null)
            messages.setType(msgType);

        if (from != null)
            messages.setFrom(from);

        if (groupId != null)
            messages.setGroupId(groupId);

        if (senderDisplayName != null)
            messages.setSenderDisplayName(senderDisplayName);

        return messages;
    }


    /****
     * Video compression code
     ****/


//    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {
//
//        Context mContext;
//
////        public VideoCompressAsyncTask(Context context) {
////            mContext = context;
////        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_photo_camera_white_48px));
////            compressionMsg.setVisibility(View.VISIBLE);
////            picDescription.setVisibility(View.GONE);
//        }
//
//        @Override
//        protected String doInBackground(String... paths) {
//            String filePath = null;
//            try {
//
//                filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1]);
//
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//            return filePath;
//
//        }
//
//        @Override
//        protected void onPostExecute(String compressedFilePath) {
//            super.onPostExecute(compressedFilePath);
//            File imageFile = new File(compressedFilePath);
//            float length = imageFile.length() / 1024f; // Size in KB
//            String value;
//            if (length >= 1024)
//                value = length / 1024f + " MB";
//            else
//                value = length + " KB";
//
//
//
//        }
//    }

}


