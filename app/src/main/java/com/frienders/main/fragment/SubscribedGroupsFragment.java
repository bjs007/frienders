package com.frienders.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.frienders.main.R;
import com.frienders.main.activity.group.GroupChatActivity;
import com.frienders.main.activity.group.GroupCreationActivity;
import com.frienders.main.model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubscribedGroupsFragment extends Fragment
{

    private View groupChatView;
    private DatabaseReference groupDatabaseReference, userDatabaseReference;

    private RecyclerView userSubscribedGroupsList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String language = "eng";

    public SubscribedGroupsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        groupChatView = inflater.inflate(R.layout.fragment_ginfox_groups, container,false);
        userSubscribedGroupsList = (RecyclerView) groupChatView.findViewById(R.id.groups_list);
        userSubscribedGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));

        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        groupDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups").child("leafs");


        return groupChatView;

    }

    @Override
    public void onStart()
    {
        super.onStart();

        final String currentUserId  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userDatabaseReference.child("lang").addListenerForSingleValueEvent(new ValueEventListener() {
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
        FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(userDatabaseReference.child("subscribed"), Group.class)
                        .build();


        try{
            FirebaseRecyclerAdapter<Group, SubscribedGroupsFragment.GroupViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Group, SubscribedGroupsFragment.GroupViewHolder>(options)
                    {
                        @Override
                        protected void onBindViewHolder(@NonNull final SubscribedGroupsFragment.GroupViewHolder holder, int position, @NonNull final Group model)
                        {
//                        final String groupNameId = getRef(position).toString();

                            holder.enterIntoButton.setVisibility(View.GONE);
                            holder.subScribeButton.setVisibility(View.GONE);
                            holder.groupViewImage.setVisibility(View.VISIBLE);

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

                            holder.itemView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent nestedGroupIntent = new Intent(getContext(), GroupChatActivity.class);
                                    nestedGroupIntent.putExtra("level", 1);
                                    nestedGroupIntent.putExtra("parentId", model.getId());

                                    startActivity(nestedGroupIntent);
                                }
                            });

                        }

                        @NonNull
                        @Override
                        public SubscribedGroupsFragment.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                        {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
                            return new SubscribedGroupsFragment.GroupViewHolder(view);
                        }

                    };

            userSubscribedGroupsList.setAdapter(adapter);
            adapter.startListening();
        }
        catch (Exception ex)
        {

            Log.wtf("My app", ex.getMessage());
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
