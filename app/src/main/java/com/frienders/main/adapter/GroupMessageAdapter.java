package com.frienders.main.adapter;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.frienders.main.SplashActivity;
import com.frienders.main.config.Configuration;
import com.frienders.main.db.MsgType;
import com.frienders.main.db.model.GroupMessage;
import com.frienders.main.R;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.explayer.ExoplayerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder>
{

    private List<GroupMessage> groupMessageList;
    private FirebaseAuth mAuth;
    private Context context;


    public GroupMessageAdapter(List<GroupMessage> groupMessages, Context context, final RecyclerView groupMessagesListView, final String groupId)
    {
        this.groupMessageList = groupMessages;

        FirebasePaths.firebaseMessageRef().child(groupId).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    GroupMessage message = dataSnapshot.getValue(GroupMessage.class);
                    groupMessageList.add(message);
                    notifyDataSetChanged();
                    groupMessagesListView.smoothScrollToPosition(groupMessagesListView.getAdapter().getItemCount());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        mAuth = FirebaseAuth.getInstance();
        this.context = context;
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
    public void onBindViewHolder(@NonNull final GroupMessageViewHolder groupMessageViewHolder, final int position)
    {
        String currentUserId = mAuth.getCurrentUser().getUid();
        final GroupMessage message = groupMessageList.get(position);

        groupMessageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        groupMessageViewHolder.senderProfileImage.setVisibility(View.GONE);
        groupMessageViewHolder.imageSentByReceiver.setVisibility(View.GONE);
        groupMessageViewHolder.imageSentBySender.setVisibility(View.GONE);
        groupMessageViewHolder.senderMessageTextInGroup.setVisibility(View.GONE);
        groupMessageViewHolder.reciverMessageTextInGroup.setVisibility(View.GONE);
        groupMessageViewHolder.docSentBySender.setVisibility(View.GONE);
        groupMessageViewHolder.docSentByReciver.setVisibility(View.GONE);
        groupMessageViewHolder.playIconReceiver.setVisibility(View.GONE);
        groupMessageViewHolder.playIconSender.setVisibility(View.GONE);
        groupMessageViewHolder.groupVideoSender.setVisibility(View.GONE);
        groupMessageViewHolder.groupVideoReceiver.setVisibility(View.GONE);
        groupMessageViewHolder.recieverDocumentName.setVisibility(View.GONE);
        groupMessageViewHolder.senderDocumentName.setVisibility(View.GONE);

        if(message != null && message.getMessage() != null)
        {
            if(message.getFrom().equals(currentUserId))
            {
                groupMessageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                groupMessageViewHolder.senderProfileImage.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderProfileImage.setText("Me");
            }
            else
            {
                groupMessageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderProfileImage.setVisibility(View.GONE);
                groupMessageViewHolder.receiverProfileImage.setText(message.getSenderDisplayName());
            }

            if(message.getType().equals(MsgType.TEXT.getMsgTypeId()))
            {
                if(message.getFrom().equals(currentUserId))
                {
                    groupMessageViewHolder.senderMessageTextInGroup.setVisibility(View.VISIBLE);
                    groupMessageViewHolder.senderMessageTextInGroup.setBackgroundResource(R.drawable.sender_message_layout);
                    groupMessageViewHolder.senderMessageTextInGroup.setTextColor(Color.BLACK);
                    groupMessageViewHolder.senderMessageTextInGroup.setText(message.getMessage() != null? message.getMessage() : "");
                }
                else
                {
                    groupMessageViewHolder.reciverMessageTextInGroup.setVisibility(View.VISIBLE);
                    groupMessageViewHolder.reciverMessageTextInGroup.setBackgroundResource(R.drawable.receiver_messages_layout);
                    groupMessageViewHolder.reciverMessageTextInGroup.setTextColor(Color.BLACK);
                    groupMessageViewHolder.reciverMessageTextInGroup.setText(message.getMessage() != null? message.getMessage() : "");

                }
            }
            else if(message.getType().equals(MsgType.IMAGE.getMsgTypeId()))
            {
                if(message.getFrom().equals(currentUserId))
                {
                    groupMessageViewHolder.imageSentBySender.setVisibility(View.VISIBLE);
                    groupMessageViewHolder.imageSentBySender.setMinimumHeight(Configuration.imageMaxHeight);
                    groupMessageViewHolder.imageSentBySender.setMaxWidth(Configuration.imageMaxWidth);
                    Glide.with(groupMessageViewHolder.itemView.getContext()).load(message.getMessage())
                            .placeholder(R.drawable.image_icon).dontAnimate()
                            .centerCrop()
                            .into(
                            groupMessageViewHolder.imageSentBySender
                    );
                }
                else
                {
                    groupMessageViewHolder.imageSentByReceiver.setVisibility(View.VISIBLE);
                    groupMessageViewHolder.imageSentByReceiver.setMinimumHeight(Configuration.imageMaxHeight);
                    groupMessageViewHolder.imageSentByReceiver.setMaxWidth(Configuration.imageMaxWidth);
                    Glide.with(groupMessageViewHolder.itemView.getContext()).load(message.getMessage())
                            .placeholder(R.drawable.image_icon).dontAnimate()
                            .centerCrop()
                            .into(
                                    groupMessageViewHolder.imageSentByReceiver
                            );

                }
            }
            else if(message.getType().equals(MsgType.VIDEO.getMsgTypeId()))
            {
                try
                {

                    if(message.getFrom().equals(currentUserId))
                    {
                        groupMessageViewHolder.groupVideoSender.setVisibility(View.VISIBLE);

                        Glide.with(context)
                                .asBitmap()
//                                .placeholder(R.drawable.video_preview_icon)
                                .load(message.getMessage() != null ? message.getMessage() : "") // or URI/path
                                .into(groupMessageViewHolder.groupVideoSender); //imageview to set thumbnail to
                        groupMessageViewHolder.playIconSender.setVisibility(View.VISIBLE);

                        groupMessageViewHolder.groupVideoSender.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent playMusi = new Intent(context, ExoplayerActivity.class);
                                playMusi.putExtra("videoLink", message.getMessage() != null? message.getMessage() : "");
                                context.startActivity(playMusi);
                            }
                        });

                    }
                    else
                    {
                        groupMessageViewHolder.groupVideoReceiver.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.playIconReceiver.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .asBitmap()
//                                .placeholder(R.drawable.video_preview_icon)
                                .load(message.getMessage() != null ? message.getMessage() : "") // or URI/path
                                .into(groupMessageViewHolder.groupVideoReceiver); //imageview to set thumbnail to

                        groupMessageViewHolder.groupVideoReceiver.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent playMusi = new Intent(context, SplashActivity.class);
                                playMusi.putExtra("videoLink", message.getMessage() != null? message.getMessage() : "");
                                context.startActivity(playMusi);
                            }
                        });

                    }
                }
                catch (Exception ex)
                {

                }
            }

            else if(message.getType().equals(MsgType.DOC.getMsgTypeId())||message.getType().equals(MsgType.PDF.getMsgTypeId()))
            {
                try
                {

                    if(message.getFrom().equals(currentUserId))
                    {
                        groupMessageViewHolder.senderDocumentName.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentBySender.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentBySender.setBackgroundResource(R.drawable.document);
                        groupMessageViewHolder.senderDocumentName.setText(message.getFileName());
                    }
                    else
                    {
                        groupMessageViewHolder.recieverDocumentName.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentByReciver.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentByReciver.setBackgroundResource(R.drawable.document);
                        groupMessageViewHolder.recieverDocumentName.setText(message.getFileName());
                    }

                    groupMessageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {

//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupMessageList.get(position).getMessage()));
//                            groupMessageViewHolder.itemView.getContext().startActivity(intent);
                            try
                            {
                                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(message.getMessage()));
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                        DownloadManager.Request.NETWORK_MOBILE);

// set title and description
                                request.setTitle("Data Download");
                                request.setDescription("Android Data download using DownloadManager.");

                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

//set the local destination for download file to a path within the application's external files directory
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"downloadfileName");
                                request.setMimeType("*/*");
                                downloadManager.enqueue(request);
                                Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception ex)
                            {
                                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                            }

// Create request for android download manager

                        }
                    });
                }
                catch (Exception ex)
                {

                }
            }
        }
    }

    @Override
    public int getItemCount()
    {

        return groupMessageList.size();
    }

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageTextInGroup, reciverMessageTextInGroup;
        public TextView receiverProfileImage, senderProfileImage;
        public TextView recieverDocumentName, senderDocumentName;
        public ImageView imageSentBySender, imageSentByReceiver, docSentBySender, docSentByReciver;
        public ImageView groupVideoSender, groupVideoReceiver, playIconSender, playIconReceiver;


        public GroupMessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            senderMessageTextInGroup = itemView.findViewById(R.id.group_sender_message_text);
            reciverMessageTextInGroup = itemView.findViewById(R.id.group_receiver_message_text);

            receiverProfileImage = itemView.findViewById(R.id.group_message_receiver_profile_image);
            senderProfileImage = itemView.findViewById(R.id.group_sender_message_profile_image);
            imageSentBySender = itemView.findViewById(R.id.group_message_sender_image_view);
            imageSentByReceiver = itemView.findViewById(R.id.group_message_receiver_image_view);
            groupVideoSender = itemView.findViewById(R.id.groupVideoViewSender);
            groupVideoReceiver = itemView.findViewById(R.id.groupVideoViewReceiver);
            docSentByReciver = itemView.findViewById(R.id.group_message_receiver_doc_view);
            docSentBySender = itemView.findViewById(R.id.group_message_sender_doc_view);
            playIconSender = itemView.findViewById(R.id.groupvideomessageplayiconSender);
            playIconReceiver = itemView.findViewById(R.id.groupvideomessageplayiconReceiver);
            recieverDocumentName = itemView.findViewById(R.id.group_message_receiver_doc_name);
            senderDocumentName = itemView.findViewById(R.id.group_message_sender_doc_name);

        }
    }
}
