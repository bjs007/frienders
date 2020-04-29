package com.frienders.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.frienders.main.model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupDetailDisplayActivity extends AppCompatActivity {

    private TextView groupName;
    private TextView groupDescription;
    private TextView groupDetail;
    private DatabaseReference databaseReference;
    private String groupId;
    private FirebaseAuth mAuth;
    private String userId;
    private String language = "eng";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail_display);
        databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        if(getIntent().getExtras().get("groupId") != null)
        {
            groupId = getIntent().getExtras().get("groupId").toString();
        }

        InitializeFields();
        PopulateFields();
    }

    private void PopulateFields()
    {

        final DatabaseReference getGroupDetail = FirebaseDatabase.getInstance().getReference().child("Groups").child("leafs");
        DatabaseReference userLangugae = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).child("lang");

        userLangugae.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    language = dataSnapshot.getValue().toString();
                }

                databaseReference.child("leafs").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            Group group =  dataSnapshot.getValue(Group.class);
                            groupName.setText(group.getEngName());
                            groupDescription.setText(group.getEngDesc());

                            if(group != null)
                            {
                                if(language.equals("eng"))
                                {
                                    groupName.setText(group.getEngName());
                                    groupDescription.setText(group.getEngDesc());
                                }
                                else
                                {
                                    groupName.setText(group.getHinName());
                                    groupDescription.setText(group.getHinDesc());
                                }
                            }


                            databaseReference.child("details").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        String detail = dataSnapshot.getValue().toString();
                                        groupDetail.setText(detail);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void InitializeFields()
    {
        groupName = findViewById(R.id.groupName_groupDetail_layout);
        groupDescription = findViewById(R.id.groupDescription_groupDetail_layout);
        groupDetail = findViewById(R.id.groupName_groupDetail_layout);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
    }
}
