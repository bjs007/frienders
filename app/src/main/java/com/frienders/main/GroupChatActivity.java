package com.frienders.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private TextView groupDisplayName, groupDescription;
    private CircleImageView groupProfileImage;
    private FirebaseAuth mAuth;
    private String messageSenderUserId;

    private ImageButton groupSendMessageButton, groupSendFileButton;
    private EditText groupMessageInputText;
    private androidx.appcompat.widget.Toolbar groupChatToolBar;

    private final   List<GroupMessage> groupMessageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter groupMessageAdapter;
    private RecyclerView groupMessagesListView;
    private String groupName;
    private ProgressDialog loadingBar;
    private DatabaseReference rootRef;
    private String saveCurrentTime, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderUserId = mAuth.getCurrentUser().getUid();
        groupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, "Group Name" + groupName,Toast.LENGTH_SHORT).show();
        InitializeControllers();

        groupDisplayName.setText(groupName);
        groupDescription.setText("demo description");
        groupSendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessageInGroup();
            }
        });
    }

    private void sendMessageInGroup()
    {
        String messageText = groupMessageInputText.getText().toString();
        Toast.makeText(this, "Message" + messageText , Toast.LENGTH_SHORT).show();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/";

            DatabaseReference userMessageKeyRef = rootRef.child("Groups")
                    .child(groupName).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderUserId);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);

            rootRef.child("Groups").child(groupName).updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    groupMessageInputText.setText("");
                }
            });
        }
    }

    private void InitializeControllers()
    {
        groupChatToolBar = (Toolbar)findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(groupChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater  = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_group_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        groupDisplayName = (TextView) findViewById(R.id.custom_group_name);
        groupDescription = (TextView) findViewById(R.id.custom_group_desc);
        groupProfileImage = (CircleImageView) findViewById(R.id.custom_group_profile_image);

        groupSendMessageButton = (ImageButton) findViewById(R.id.group_send_message_btn);
        groupSendFileButton = (ImageButton) findViewById(R.id.group_send_file_btn);
        groupMessageInputText = (EditText) findViewById(R.id.group_input_message);

        groupMessageAdapter = new GroupMessageAdapter(groupMessageList);
        groupMessagesListView = (RecyclerView) findViewById(R.id.group_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        groupMessagesListView.setLayoutManager(linearLayoutManager);
        groupMessagesListView.setAdapter(groupMessageAdapter);
        loadingBar = new ProgressDialog(GroupChatActivity.this);
        rootRef = FirebaseDatabase.getInstance().getReference();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        messageSenderUserId = mAuth.getCurrentUser().getUid();

        rootRef.child("Groups").child(groupName).child("Messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
             if(dataSnapshot.exists())
             {
                 GroupMessage message = dataSnapshot.getValue(GroupMessage.class);
                 groupMessageList.add(message);
                 Toast.makeText(GroupChatActivity.this, "Message" + message.getFrom(), Toast.LENGTH_SHORT).show();

                 groupMessageAdapter.notifyDataSetChanged();
                 groupMessagesListView.smoothScrollToPosition(groupMessagesListView.getAdapter().getItemCount());
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

//        Toast.makeText(this, "Mesages size " + groupMessageList.get(0).getFrom(), Toast.LENGTH_SHORT).show();
    }
}
