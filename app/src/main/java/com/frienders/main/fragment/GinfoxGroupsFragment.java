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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.frienders.main.R;
import com.frienders.main.activity.group.NestedGroupDisplayActivity;
import com.frienders.main.config.ActivityParameters;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.model.Group;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GinfoxGroupsFragment extends Fragment
{
    private View groupChatView;
    private RecyclerView userSubscribedGroupsList;
    private String language = "eng";

    public GinfoxGroupsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initializeUi(inflater, container);

        return groupChatView;
    }

    private void initializeUi(LayoutInflater inflater, ViewGroup container)
    {
        groupChatView = inflater.inflate(R.layout.fragment_ginfox_groups, container,false);
        userSubscribedGroupsList = (RecyclerView) groupChatView.findViewById(R.id.groups_list);
        userSubscribedGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId())
                .child(UsersFirebaseFields.profileImageLink)
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

        createGroupList();
    }

    private void createGroupList()
    {

        FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(FirebasePaths.firebaseGroupsAtLevelDBRef(0), Group.class)
                        .build();


        try{
            FirebaseRecyclerAdapter<Group, GroupViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Group, GroupViewHolder>(options)
                    {
                        @Override
                        protected void onBindViewHolder(@NonNull final GroupViewHolder holder, int position, @NonNull final Group model)
                        {
                            final String groupName = getRef(position).getKey();

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
                                    nestedGroupIntent.putExtra(ActivityParameters.level, 1);
                                    nestedGroupIntent.putExtra(ActivityParameters.parentId, model.getId());
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
