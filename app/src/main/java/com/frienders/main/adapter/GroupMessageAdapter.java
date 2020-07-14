package com.frienders.main.adapter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.frienders.main.SplashActivity;
import com.frienders.main.activity.ImageViwer;
import com.frienders.main.config.Configuration;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.MsgType;
import com.frienders.main.db.model.GroupMessage;
import com.frienders.main.R;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.explayer.ExoplayerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class GroupMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GroupMessage> groupMessageList;
    private List<GroupMessage> moreMessages;
    private FirebaseAuth mAuth;
    private Context context;
    private String groupId;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private RecyclerView recyclerView;
    boolean isLoading = false;
    private boolean reachedEnd = false;
    private int currentCount = 0;
    private int totalCount = 0;
    private int scrolledCount = 0;
    private LinearLayoutManager linearLayoutManager;


    public GroupMessageAdapter(List<GroupMessage> groupMessages, Context context, final RecyclerView groupMessagesRecyclerView, final String groupId, LinearLayoutManager linearLayoutManager) {
        this.groupMessageList = groupMessages;
        this.groupId = groupId;
        this.recyclerView = groupMessagesRecyclerView;
        this.linearLayoutManager = linearLayoutManager;
        moreMessages = new LinkedList<>();
        mAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_group_message_layout, parent, false);

            return new GroupMessageViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }

    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder instanceof GroupMessageViewHolder)
        {
            final String currentUserId = mAuth.getCurrentUser().getUid();
            final GroupMessage message = groupMessageList.get(position);
            GroupMessageViewHolder groupMessageViewHolder = (GroupMessageViewHolder) viewHolder;
            groupMessageViewHolder.receiverProfileDisplayName.setVisibility(View.GONE);
            groupMessageViewHolder.senderProfileDisplayName.setVisibility(View.GONE);
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
            groupMessageViewHolder.senderDocumentName.setVisibility(View.GONE);
            groupMessageViewHolder.recieverDocumentName.setVisibility(View.GONE);
            groupMessageViewHolder.groupMessageSenderNotificationIcon.setVisibility(View.GONE);
            groupMessageViewHolder.messageLikes.setVisibility(View.GONE);
            groupMessageViewHolder.group_reciever_message_like_button.setVisibility(View.GONE);
            groupMessageViewHolder.groupMessageLikeHolder.setVisibility(View.GONE);


            if (message != null && message.getMessage() != null)
            {
                String[] timestamptoken = message.getTime().split(",");

                if(message.getType().equals(MsgType.TEXT.getMsgTypeId()))
                {
                    if(message.getFrom().equals(currentUserId))
                    {
                        groupMessageViewHolder.groupMessageSenderNotificationIcon.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        groupMessageViewHolder.groupMessageLikeHolder.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.messageLikes.setVisibility(View.VISIBLE);
                        if(message.getLikes() != null)
                        {
                            groupMessageViewHolder.messageLikes.setText(String.valueOf(message.getLikes()));
                        }
                        groupMessageViewHolder.group_reciever_message_like_button.setVisibility(View.VISIBLE);
                    }

                }

                if (message.getFrom().equals(currentUserId))
                {

                    groupMessageViewHolder.receiverProfileDisplayName.setVisibility(View.GONE);
                    groupMessageViewHolder.senderProfileDisplayName.setVisibility(View.VISIBLE);
                    groupMessageViewHolder.senderProfileDisplayName.setText("Me @" + timestamptoken[0]);

                }
                else
                {
                    groupMessageViewHolder.receiverProfileDisplayName.setVisibility(View.VISIBLE);
                    groupMessageViewHolder.senderProfileDisplayName.setVisibility(View.GONE);
                    groupMessageViewHolder.receiverProfileDisplayName.setText(message.getSenderDisplayName() +" @" +timestamptoken[0]);
                }


                if (message.getType().equals(MsgType.TEXT.getMsgTypeId()))
                {
                    if (message.getFrom().equals(currentUserId)) {
                        groupMessageViewHolder.senderMessageTextInGroup.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.senderMessageTextInGroup.setBackgroundResource(R.drawable.sender_message_layout);
                        groupMessageViewHolder.senderMessageTextInGroup.setTextColor(Color.BLACK);
                        groupMessageViewHolder.senderMessageTextInGroup.setText(message.getMessage() != null ? message.getMessage(): "");
                        groupMessageViewHolder.senderMessageTextInGroup.setMovementMethod(BetterLinkMovementMethod.getInstance());
                        Linkify.addLinks(groupMessageViewHolder.senderMessageTextInGroup, Linkify.ALL);

                    }
                    else
                    {
                        groupMessageViewHolder.reciverMessageTextInGroup.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.reciverMessageTextInGroup.setBackgroundResource(R.drawable.receiver_messages_layout);
                        groupMessageViewHolder.reciverMessageTextInGroup.setTextColor(Color.BLACK);
                        groupMessageViewHolder.reciverMessageTextInGroup.setText(message.getMessage() != null ? message.getMessage() : "");
                        groupMessageViewHolder.reciverMessageTextInGroup.setMovementMethod(BetterLinkMovementMethod.getInstance());
                        Linkify.addLinks(groupMessageViewHolder.reciverMessageTextInGroup, Linkify.ALL);
                    }
                }

                else if (message.getType().equals(MsgType.IMAGE.getMsgTypeId()))
                {
                    if (message.getFrom().equals(currentUserId))
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
                else if (message.getType().equals(MsgType.VIDEO.getMsgTypeId()))
                {
                    try
                    {
                        if (message.getFrom().equals(currentUserId))
                        {
                            groupMessageViewHolder.groupVideoSender.setVisibility(View.VISIBLE);

                            Glide.with(context)
                                    .asBitmap()
                                    .load(message.getMessage() != null ? message.getMessage() : "") // or URI/path
                                    .into(groupMessageViewHolder.groupVideoSender); //imageview to set thumbnail to
                            groupMessageViewHolder.playIconSender.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            groupMessageViewHolder.groupVideoReceiver.setVisibility(View.VISIBLE);
                            groupMessageViewHolder.playIconReceiver.setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .asBitmap()
                                    .load(message.getMessage() != null ? message.getMessage() : "") // or URI/path
                                    .into(groupMessageViewHolder.groupVideoReceiver); //imageview to set thumbnail to
                        }
                    }
                    catch (Exception ex)
                    {


                    }
                }
                else if (message.getType().equals(MsgType.DOC.getMsgTypeId()) || message.getType().equals(MsgType.PDF.getMsgTypeId()))
                {
                    if (message.getFrom().equals(currentUserId))
                    {
                        groupMessageViewHolder.senderDocumentName.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentBySender.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentBySender.setBackgroundResource(R.drawable.document);
                        groupMessageViewHolder.senderDocumentName.setText(message.getMessageId());
                    }
                    else
                    {
                        groupMessageViewHolder.recieverDocumentName.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentByReciver.setVisibility(View.VISIBLE);
                        groupMessageViewHolder.docSentByReciver.setBackgroundResource(R.drawable.document);
                        groupMessageViewHolder.recieverDocumentName.setText(message.getMessageId());

                    }

                    groupMessageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
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
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloadfileName");
                                request.setMimeType("*/*");
                                downloadManager.enqueue(request);
                                Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception ex)
                            {
                                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        }
        else if (viewHolder instanceof LoadingViewHolder)
        {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
            loadingViewHolder.progressBar.setIndeterminate(true);
            loadingViewHolder.progressBar.setVisibility(View.VISIBLE);
        }
    }


        @Override
        public int getItemCount () {

            return groupMessageList == null ? 0 : groupMessageList.size();
        }

        @Override
        public int getItemViewType ( int position){
            return groupMessageList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }


    public class GroupMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
            public TextView senderMessageTextInGroup, reciverMessageTextInGroup;
            public TextView receiverProfileDisplayName, senderProfileDisplayName;
            public TextView recieverDocumentName, senderDocumentName;
            public ImageView imageSentBySender, imageSentByReceiver, docSentBySender, docSentByReciver;
            public ImageView groupVideoSender, groupVideoReceiver, playIconSender, playIconReceiver;
            public ImageButton groupMessageSenderNotificationIcon, group_reciever_message_like_button;
            public TextView messageLikes;
            public LinearLayout groupMessageLikeHolder;
//            public ImageButton groupMessageRecieverTimeStamp;


            public GroupMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                senderMessageTextInGroup = itemView.findViewById(R.id.group_sender_message_text);
                reciverMessageTextInGroup = itemView.findViewById(R.id.group_receiver_message_text);

                receiverProfileDisplayName = itemView.findViewById(R.id.group_message_receiver_profile_image);
                senderProfileDisplayName = itemView.findViewById(R.id.group_sender_message_profile_image);
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
                groupMessageSenderNotificationIcon = itemView.findViewById(R.id.group_sender_message_notification_icon);
                messageLikes = itemView.findViewById(R.id.group_message_likes);
                group_reciever_message_like_button = itemView.findViewById(R.id.group_reciever_message_like_button);
                groupMessageLikeHolder = itemView.findViewById(R.id.group_message_like_holder);
//                groupMessageRecieverTimeStamp = itemView.findViewById(R.id.group_reciever_message_like_button);

            }


        @Override
        public void onClick(View v)
        {

        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder
    {
        public ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView)
        {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar1);
        }
    }

    public String getLastItemId()
    {
        return groupMessageList.get(groupMessageList.size() - 1).getGroupId();
    }

}
