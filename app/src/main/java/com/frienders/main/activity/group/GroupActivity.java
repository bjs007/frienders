
package com.frienders.main.activity.group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.frienders.main.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.SimpleTimeZone;

public class GroupActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar mToolBar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, GroupNameRef, GroupMessageRefKey;

    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        currentGroupName = getIntent().getExtras().getString("groupName");
//        Toast.makeText(this, "Current Name " + currentGroupName, Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference("Groups").child(currentGroupName);



        InitializeFields();

        GetUserInfo();



     SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDataBase();
                userMessageInput.setText(null);
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
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

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue().toString();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue().toString();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue().toString();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue().toString();

            displayTextMessage.append(chatName + " :\n" + chatMessage + "\n"+ chatTime+  "     "  + chatDate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void SaveMessageInfoToDataBase() {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Blank message", Toast.LENGTH_SHORT).show();

        }
        else
        {
            Calendar ccalForDate  = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM DD, YYYY");
            currentDate = currentDateFormat.format(ccalForDate.getTime());

            Calendar ccalForTime  = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm: a");
            currentTime = currentTimeFormat.format(ccalForTime.getTime());

            HashMap<String, Object> grouipMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(grouipMessageKey);
            GroupMessageRefKey = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageRefKey.updateChildren(messageInfoMap);
        }
    }

    private void GetUserInfo() {


        RootRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentUserName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void InitializeFields() {
        mToolBar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_group_message_button);

        userMessageInput = (EditText) findViewById(R.id.input_group_message);

        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
    }
}
