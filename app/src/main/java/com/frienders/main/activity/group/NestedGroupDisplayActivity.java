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
import de.hdodenhof.circleimageview.CircleImageView;

public class NestedGroupDisplayActivity extends AppCompatActivity
{
    private DatabaseReference groupDatabaseReference;
    private RecyclerView groupList;
    private int level;
    private String parentId = "root";

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

        groupDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups").child("level - " + level);
        groupList = (RecyclerView)findViewById(R.id.nested_group_recycler_list);
        groupList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
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

                        if(model.getParentId().equals(parentId))
                        {
                            holder.groupViewImage.setVisibility(View.GONE);
                            holder.groupName.setText(model.getName());

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
                                                    holder.subScribeButton.setText("Subscribe");
                                                    firebaseRef.removeValue();
                                                }
                                                else
                                                {
                                                    holder.subScribeButton.setText("Unsubscribe");
                                                    firebaseRef.setValue("subscribed");
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
                            holder.groupViewImage.setVisibility(View.GONE);
                            holder.groupName.setVisibility(View.GONE);
                            holder.groupDescription.setVisibility(View.GONE);
                        }
                    }

                    @NonNull
                    @Override
                    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
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
                "Subscribe"
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
                        status[0]  = "Unsubscribe";
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

    @Override
    protected void onRestart()
    {
        super.onRestart();

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

                            if(model.getParentId().equals(parentId))
                            {
                                holder.groupViewImage.setVisibility(View.GONE);
                                holder.groupName.setText(model.getName());

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
                                                        holder.subScribeButton.setText("Subscribe");
                                                        firebaseRef.removeValue();
                                                    }
                                                    else
                                                    {
                                                        holder.subScribeButton.setText("Unsubscribe");
                                                        firebaseRef.setValue("subscribed");
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
                                holder.groupViewImage.setVisibility(View.GONE);
                                holder.groupName.setVisibility(View.GONE);
                                holder.groupDescription.setVisibility(View.GONE);
                            }
                        }

                        @NonNull
                        @Override
                        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                        {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
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

    public static class GroupViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView groupViewImage;
        TextView groupName, groupDescription;
        Button subScribeButton, enterIntoButton;

        public GroupViewHolder(@NonNull View itemView)
        {
            super(itemView);
            groupViewImage = itemView.findViewById(R.id.group_profile_image);
            groupName = itemView.findViewById(R.id.group_name);
            groupDescription = itemView.findViewById(R.id.group_description);
            subScribeButton =  itemView.findViewById(R.id.subscribe_group_button);
            enterIntoButton = itemView.findViewById(R.id.enter_into_group_button);
        }
    }
}


