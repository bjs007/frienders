package com.frienders.main.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.frienders.main.R;
import com.frienders.main.activity.NestedGroupDisplayActivity;
import com.frienders.main.db.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GinfoxGroupsFragment extends Fragment
{
private View groupChatView;
private DatabaseReference groupDatabaseReference, userDatabaseReference;
private RecyclerView userSubscribedGroupsList;
private FirebaseAuth mAuth;
private String currentUserId;

    public GinfoxGroupsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        groupChatView = inflater.inflate(R.layout.fragment_ginfox_groups, container,false);
        userSubscribedGroupsList = (RecyclerView) groupChatView.findViewById(R.id.groups_list);
        userSubscribedGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));

        groupDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups").child("level - 0");

        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");


//        initializeFields();

        return groupChatView;
    }

//    private void initializeFields()
//    {
//
//    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(groupDatabaseReference, Group.class)
                        .build();


        try{
            FirebaseRecyclerAdapter<Group, GroupViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Group, GroupViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final GroupViewHolder holder, int position, @NonNull final Group model)
                        {
                            final String groupName = getRef(position).getKey();

                            holder.groupName.setText(model.getName());
                            holder.groupViewImage.setVisibility(View.GONE);
                            if(!model.isLeaf())
                            {
                                holder.enterIntoButton.setVisibility(View.GONE);
                                holder.subScribeButton.setVisibility(View.GONE);
                            }
                            holder.itemView.setOnClickListener(new View.OnClickListener()
                            {

                                @Override
                                public void onClick(View v)
                                {
                                    Intent nestedGroupIntent = new Intent(getContext(), NestedGroupDisplayActivity.class);
                                    nestedGroupIntent.putExtra("level", 1);
                                    nestedGroupIntent.putExtra("parentId", model.getId());
                                    startActivity(nestedGroupIntent);
                                }
                            });
                        }

                        @NonNull
                        @Override
                        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                        {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
                            return new GroupViewHolder(view);
                        }

                    };

            userSubscribedGroupsList.setAdapter(adapter);
            adapter.startListening();

        }catch (Exception ex)
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
