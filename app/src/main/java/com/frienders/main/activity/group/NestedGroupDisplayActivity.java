package com.frienders.main.activity.group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.frienders.main.activity.profile.SettingActivity;
import com.frienders.main.config.ActivityParameters;
import com.frienders.main.config.Firebasedatabasefields;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.model.Group;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.db.refs.FirestorePath;
import com.frienders.main.utility.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class NestedGroupDisplayActivity extends AppCompatActivity
{
    private RecyclerView groupList;
    private int level;
    private String parentId = Firebasedatabasefields.rootParent;
    private String language = "eng";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested_group_display);

        if(getIntent().getExtras().get(ActivityParameters.level)!= null)
        {
            level = Integer.parseInt(getIntent().getExtras().get(ActivityParameters.level).toString());
        }

        if(getIntent().getExtras().get(ActivityParameters.parentId) != null)
        {
            parentId = getIntent().getExtras().get(ActivityParameters.parentId).toString();
        }
        initializeUi();
    }

    private void initializeUi()
    {
        groupList = findViewById(R.id.nested_group_recycler_list);
        groupList.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        final String currentUserId  = FirebaseAuthProvider.getCurrentUserId();

        FirebasePaths.firebaseUserRef(currentUserId).child(UsersFirebaseFields.language)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    language = dataSnapshot.getValue().toString();
                }

                createGroupList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private void createGroupList()
    {
        final String currentUserId  = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(FirebasePaths.firebaseGroupsAtLevelDBRef(level).child(parentId), Group.class)
                        .build();

        try
        {
            FirebaseRecyclerAdapter<Group, GroupViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Group, GroupViewHolder>(options)
                    {
                        @Override
                        protected void onBindViewHolder(@NonNull final GroupViewHolder holder, int position, @NonNull final Group model)
                        {
                            holder.enterIntoButton.setVisibility(View.GONE);
                            holder.subScribeButton.setVisibility(View.GONE);
                            holder.groupName.setVisibility(View.GONE);
                            holder.groupDescription.setVisibility(View.GONE);

                            if(model.getParentId().equals(parentId))
                            {
                                holder.groupName.setVisibility(View.VISIBLE);
                                holder.groupDescription.setVisibility(View.VISIBLE);

//
                                String groupDisplayName = null;
                                String groupDesc = null;

                                if(language.equals("eng"))
                                {
                                    groupDisplayName = model.getEngName();
                                    groupDesc = model.getEngDesc();
                                }
                                else
                                {
                                    groupDisplayName = model.getHinName();
                                    groupDesc = model.getHinDesc();

                                }

                                if(groupDisplayName != null && groupDesc != null) {
                                    String groupDisplayNameMayContainRootName = Utility.getGroupDisplayNameFromDbGroupName(groupDisplayName);
                                    holder.groupName.setText(groupDisplayNameMayContainRootName);
                                    holder.groupDescription.setText(groupDesc);
                                }


                                if(model.isLeaf())
                                {
                                    holder.enterIntoButton.setVisibility(View.VISIBLE);
                                    holder.subScribeButton.setVisibility(View.VISIBLE);
                                    setInitialStatusForEachButton(holder, model.getId());
                                    holder.subScribeButton.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            final DatabaseReference firebaseRef = FirebasePaths.firebaseUsersDbRef()
                                                    .child(currentUserId);

                                            firebaseRef.child(UsersFirebaseFields.subscribed).child(model.getId()).addListenerForSingleValueEvent(new ValueEventListener()
                                            {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                {
                                                    //if datasnapshot exists it means users has subcribed to the group and buttton is now showing "Remove from my groups"
                                                    if(dataSnapshot.exists())
                                                    {
                                                        holder.subScribeButton.setText(getText(R.string.subscribe));
                                                        firebaseRef.child(UsersFirebaseFields.subscribed).child(model.getId()).removeValue();
                                                        Toast.makeText(NestedGroupDisplayActivity.this, getString(R.string.subscribed_group_removed), Toast.LENGTH_SHORT).show();

                                                    }
                                                    else
                                                    {
                                                        holder.subScribeButton.setText(R.string.unsubscribe);
                                                        firebaseRef.child(UsersFirebaseFields.subscribed).child(model.getId()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(NestedGroupDisplayActivity.this, getString(R.string.subscribed_group_added), Toast.LENGTH_SHORT).show();

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

                                    holder.enterIntoButton.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent groupDetailDisplayActivity = new Intent(NestedGroupDisplayActivity.this, GroupDetailDisplayActivity.class);
                                            groupDetailDisplayActivity.putExtra(ActivityParameters.groupId, model.getId());
                                            startActivity(groupDetailDisplayActivity);
                                        }
                                    });
                                }

                                if(!model.isLeaf())
                                {
                                    holder.itemView.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v) {
                                            Intent nestedGroupIntent = new Intent(NestedGroupDisplayActivity.this, NestedGroupDisplayActivity.class);
                                            nestedGroupIntent.putExtra(ActivityParameters.level, level + 1);
                                            nestedGroupIntent.putExtra(ActivityParameters.parentId, model.getId());
                                            startActivity(nestedGroupIntent);
                                        }
                                    });
                                }
                            }
                            else
                            {
                                holder.groupName.setVisibility(View.GONE);
                                holder.groupDescription.setVisibility(View.GONE);
                            }
                        }

                        @NonNull
                        @Override
                        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                        {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nested_groups_display_layout, parent, false);
                            return new GroupViewHolder(view);
                        }
                    };

            groupList.setAdapter(adapter);
            adapter.startListening();

        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }


    private void setInitialStatusForEachButton(final GroupViewHolder holder, final String id)
    {
        final String currentUserId = FirebaseAuthProvider.getCurrentUserId();

        final CharSequence[] status = new CharSequence[]
            {
                getText(R.string.subscribe)
            };

        final DatabaseReference firebaseRef = FirebasePaths.firebaseUserRef(currentUserId).child("subscribed");

        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild(id))
                    {
                        status[0]  = getText(R.string.unsubscribe);
                        holder.subScribeButton.setText(status[0]);
                    }
                    else
                    {
                        holder.subScribeButton.setText(status[0]);
                    }
                }
                else
                {
                    holder.subScribeButton.setText(status[0]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    public static class GroupViewHolder extends RecyclerView.ViewHolder
    {
        TextView groupName, groupDescription;
        Button subScribeButton, enterIntoButton;

        public GroupViewHolder(@NonNull View itemView)
        {
            super(itemView);
            groupName = itemView.findViewById(R.id.nested_group_name);
            groupDescription = itemView.findViewById(R.id.nested_group_description);
            subScribeButton =  itemView.findViewById(R.id.nested_subscribe_group_button);
            enterIntoButton = itemView.findViewById(R.id.nested_enter_into_group_button);
        }
    }
    
}


