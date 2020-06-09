package com.frienders.main.activity.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.frienders.main.R;
import com.frienders.main.config.ActivityParameters;
import com.frienders.main.db.model.Group;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
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
    private String groupId;
    private String language = "eng";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail_display);

        if(getIntent().getExtras().get(ActivityParameters.groupId) != null)
        {
            groupId = getIntent().getExtras().get(ActivityParameters.groupId).toString();
        }

        initializeUi();
        populateFields();
    }

    private void populateFields()
    {

        DatabaseReference userLangugae = FirebasePaths.firebaseUsersDbRef().child(FirebaseAuthProvider.getCurrentUserId()).child("lang");

        userLangugae.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    language = dataSnapshot.getValue().toString();
                }

                FirebasePaths.firebaseGroupDbRef().child("leafs").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            Group group =  dataSnapshot.getValue(Group.class);
                            groupName.setText(group.getEngName());
                            groupDescription.setText(group.getEngDesc());

//                            if(group != null)
//                            {
//                                if(language.equals("eng"))
//                                {
//                                    groupName.setText(group.getEngName());
//                                    groupDescription.setText(group.getEngDesc());
//                                }
//                                else
//                                {
//                                    groupName.setText(group.getHinName());
//                                    groupDescription.setText(group.getHinDesc());
//                                }
//                            }

                            String groupDisplayName = null;
                            String groupDesc = null;

                            if(language.equals("eng"))
                            {
                                groupDisplayName = group.getEngName();
                                groupDesc = group.getEngDesc();
                            }
                            else
                            {
                                groupDisplayName = group.getHinName();
                                groupDesc = group.getHinDesc();

                            }


                            if(groupDisplayName != null && groupDesc != null) {
                                String[] groupWithParentNameWithoutAsterisk = null;
                                if (groupDisplayName.indexOf('*') != -1) {
                                    groupWithParentNameWithoutAsterisk = group.getEngName().split("\\*");
                                }

                                String groupDisplayNameMayContainRootName = null;
                                if (groupWithParentNameWithoutAsterisk.length == 2) {
                                    groupDisplayNameMayContainRootName = groupWithParentNameWithoutAsterisk[0] + " - " + groupWithParentNameWithoutAsterisk[1];
                                } else {
                                    groupDisplayNameMayContainRootName = groupWithParentNameWithoutAsterisk[0];
                                }
                                groupName.setText(groupDisplayNameMayContainRootName);
                               groupDescription.setText(groupDesc);
                            }



                                FirebasePaths.firebaseGroupDbRef().child("details").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void initializeUi()
    {
        groupName = findViewById(R.id.groupName_groupDetail_layout);
        groupDescription = findViewById(R.id.groupDescription_groupDetail_layout);
        groupDetail = findViewById(R.id.groupName_groupDetail_layout);
    }
}
