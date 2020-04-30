package com.frienders.main.activity.group;

import androidx.annotation.NonNull;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.frienders.main.R;
import com.frienders.main.model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NestedGroupDisplayActivity extends AppCompatActivity
{
    private DatabaseReference groupDatabaseReference, userDatabaseReference;
    private RecyclerView groupList;
    private int level;
    private String parentId = "root";
    private String userLang;
    private String currentUser;
    private FirebaseAuth firebaseAuth;
    private String language = "eng";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested_group_display);
        if(getIntent().getExtras().get("level")!= null)
        {
            level = Integer.parseInt(getIntent().getExtras().get("level").toString());
        }

        if(getIntent().getExtras().get("parentId") != null)
        {
            parentId = getIntent().getExtras().get("parentId").toString();
        }

        groupDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups").child("level - " + level).child(parentId);
        groupList = (RecyclerView)findViewById(R.id.nested_group_recycler_list);
        groupList.setLayoutManager(new LinearLayoutManager(this));
        firebaseAuth = FirebaseAuth.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        final String currentUserId  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userDatabaseReference.child(currentUserId).child("lang").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        .setQuery(groupDatabaseReference, Group.class)
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
                                holder.groupName.setText(model.getEngName());
                                holder.groupName.setVisibility(View.VISIBLE);
                                holder.groupDescription.setVisibility(View.VISIBLE);

                                if(language.equals("eng"))
                                {
                                    holder.groupName.setText(model.getEngName());
                                    holder.groupDescription.setText(model.getEngDesc());
                                }
                                else
                                {
                                    holder.groupName.setText(model.getHinName());
                                    holder.groupDescription.setText(model.getHinDesc());
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
                                            final DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("subscribed").child(model.getId());
                                            firebaseRef.addListenerForSingleValueEvent(new ValueEventListener()
                                            {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                {
                                                    if(dataSnapshot.exists())
                                                    {
                                                        holder.subScribeButton.setText(getText(R.string.subscribe));
                                                        firebaseRef.removeValue();
                                                    }
                                                    else
                                                    {
                                                        holder.subScribeButton.setText(R.string.unsubscribe);
                                                        firebaseRef.setValue(model);
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
                                            groupDetailDisplayActivity.putExtra("groupId", model.getId());
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
                                            nestedGroupIntent.putExtra("level", level + 1);
                                            nestedGroupIntent.putExtra("parentId", model.getId());
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
        final String path = "level - " +level+ "/" + id;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final CharSequence[] status = new CharSequence[]
            {
                getText(R.string.subscribe)
        };

        final DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("subscribed");

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


