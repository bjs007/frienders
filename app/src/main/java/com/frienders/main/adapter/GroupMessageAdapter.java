package com.frienders.main.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.frienders.main.model.GroupMessage;
import com.frienders.main.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder>
{

    private List<GroupMessage> groupMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public GroupMessageAdapter(List<GroupMessage> groupMessages)
    {
        this.groupMessageList = groupMessages;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
       View view = LayoutInflater.from(parent.getContext())
               .inflate(R.layout.custom_group_message_layout, parent, false);

        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder groupMessageViewHolder, int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        GroupMessage message = groupMessageList.get(position);

        groupMessageViewHolder.reciverMessageTextInGroup.setVisibility(View.GONE);
        groupMessageViewHolder.senderMessageTextInGroup.setVisibility(View.GONE);
        groupMessageViewHolder.imageSentByReceiver.setVisibility(View.GONE);
        groupMessageViewHolder.imageSentBySender.setVisibility(View.GONE);


        if(message.getType().equals("text"))
        {
            if(message.getFrom().equals(messageSenderId))
            {
                groupMessageViewHolder.senderMessageTextInGroup.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderMessageTextInGroup.setBackgroundResource(R.drawable.sender_message_layout);
                groupMessageViewHolder.senderMessageTextInGroup.setTextColor(Color.BLACK);
                groupMessageViewHolder.senderMessageTextInGroup.setText(message.getMessage() +"\n"+ message.getTime()
                        +"-" + message.getDate());
            }
            else
            {
                groupMessageViewHolder.reciverMessageTextInGroup.setVisibility(View.VISIBLE);
                groupMessageViewHolder.reciverMessageTextInGroup.setBackgroundResource(R.drawable.sender_message_layout);
                groupMessageViewHolder.reciverMessageTextInGroup.setTextColor(Color.BLACK);
                groupMessageViewHolder.reciverMessageTextInGroup.setText(message.getMessage() +"\n"+ message.getTime()
                        +"-" + message.getDate());

            }
        }



    }

    @Override
    public int getItemCount() {

        return groupMessageList.size();
    }

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageTextInGroup, reciverMessageTextInGroup;
        public ImageView imageSentBySender, imageSentByReceiver;


        public GroupMessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            senderMessageTextInGroup = (TextView) itemView.findViewById(R.id.group_sender_message_text);
            reciverMessageTextInGroup = (TextView) itemView.findViewById(R.id.group_receiver_message_text);
//            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            imageSentBySender = itemView.findViewById(R.id.group_message_sender_image_view);
            imageSentByReceiver = itemView.findViewById(R.id.group_message_receiver_image_view);
        }
    }
}
