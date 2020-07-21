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
import com.frienders.main.utility.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupDetailDisplayActivity extends AppCompatActivity {

    private TextView groupName;
    private TextView groupDescription;
    private TextView groupDetails;
    private String groupId;
    private String language = Utility.getDeviceLanguage();

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

        FirebasePaths.firebaseGroupDbRef().child("leafs").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    Group group =  dataSnapshot.getValue(Group.class);
                    groupName.setText(group.getEngName());
                    groupDescription.setText(group.getEngDesc());

                    String groupDisplayName = null;
                    String groupDesc = null;
                    String groupDetail = null;

                    if(language.equals("hi"))
                    {
                        groupDisplayName = group.getHinName();
                        groupDesc = group.getHinDesc();
                        groupDetail = group.getHinDetail();
                    }
                    else
                    {
                        groupDisplayName = group.getEngName();
                        groupDesc = group.getEngDesc();
                        groupDetail = group.getEngDetail();
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
                        groupDetails.setText(groupDetail);

                    }


//                    FirebasePaths.firebaseGroupDbRef().child("leafs").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                        {
//                            if(dataSnapshot.exists())
//                            {
//                                String detail = dataSnapshot.getValue().toString();
//                                groupDetails.setText(detail);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError)
//                        {
//
//                        }
//                    });
                }
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
        groupDetails = findViewById(R.id.groupDetail_groupDetail_layout);
    }
}
