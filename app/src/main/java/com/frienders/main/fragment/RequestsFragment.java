package com.frienders.main.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.frienders.main.model.Contacts;
import com.frienders.main.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestsRef, UserRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myRequestsList = (RecyclerView) RequestsFragmentView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestsRef.child(currentUserId), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model)
                    {
                                                holder.itemView.findViewById(R.id.request_accept_btnn).setVisibility(View.VISIBLE);
                                                holder.itemView.findViewById(R.id.request_cancel_btnn).setVisibility(View.VISIBLE);

                                                final String list_user_id = getRef(position).getKey();

                                                final DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                                                getTypeRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                    {
                                                        if(dataSnapshot.exists())
                                                        {
                                                            String type = dataSnapshot.getValue().toString();
                                                            if(type.equals("received"))
                                                            {
                                                                UserRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                                                {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                                    {
                                                                        if(dataSnapshot.exists() && dataSnapshot.hasChild("image"))
                                                                        {

                                                                            final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                                                        }

                                                                            final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                                            final String requestUseStatus = dataSnapshot.child("status").getValue().toString();
                                                                            holder.userName.setText(requestUserName);
                                                                            holder.userStatus.setText("wants to connect with you");



                                                                        holder.itemView.setOnClickListener(new View.OnClickListener()
                                                                        {
                                                                            @Override
                                                                            public void onClick(View v)
                                                                            {
                                                                                CharSequence options[]= new CharSequence[]
                                                                                        {
                                                                                                "Accept",
                                                                                                "Cancel"
                                                                                        };

                                                                                AlertDialog.Builder builder =  new AlertDialog.Builder(getContext());
                                                                                builder.setTitle(requestUserName + "Chat requests");

                                                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        if(which == 0)
                                                                                        {
                                                                                            ContactsRef.child(currentUserId).child(list_user_id).child("Contact")
                                                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful())
                                                                                                    {
                                                                                                        ContactsRef.child(list_user_id).child(currentUserId).child("Contact")
                                                                                                                .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                ChatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                                            {
                                                                                                                                if(task.isSuccessful())
                                                                                                                                {
                                                                                                                                    ChatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                                                            .removeValue()
                                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                                @Override
                                                                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                                                                {
                                                                                                                                                    if(task.isSuccessful())
                                                                                                                                                    {

                                                                                                                                                        Toast.makeText(getContext(), "New Contact saved", Toast.LENGTH_SHORT).show();
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            });
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                        else if(which == 1)
                                                                                        {
                                                                                            ChatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                ChatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                                            {
                                                                                                                                if(task.isSuccessful())
                                                                                                                                {

                                                                                                                                    Toast.makeText(getContext(), "Conctact deleted", Toast.LENGTH_SHORT).show();
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }
                                                                                                    });

                                                                                        }
                                                                                    }
                                                                                });
                                                                                builder.show();

                                                                            }
                                                                        });

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                                                    {

                                                                    }
                                                                 });

                                                            }
                                                            else if(type.equals("sent"))
                                                            {
                                                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btnn);
                                                                request_sent_btn.setText("Req Sent");

                                                                holder.itemView.findViewById(R.id.request_cancel_btnn).setVisibility(View.INVISIBLE);

                                                                UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot)
                                                                    {
                                                                        if (dataSnapshot.hasChild("image"))
                                                                        {
                                                                            final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                                        }

                                                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                                        holder.userName.setText(requestUserName);
                                                                        holder.userStatus.setText("you have sent a request to " + requestUserName);


                                                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view)
                                                                            {
                                                                                CharSequence options[] = new CharSequence[]
                                                                                        {
                                                                                                "Cancel Chat Request"
                                                                                        };

                                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                                builder.setTitle("Already Sent Request");

                                                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialogInterface, int i)
                                                                                    {
                                                                                        if (i == 0)
                                                                                        {
                                                                                            ChatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            if (task.isSuccessful())
                                                                                                            {
                                                                                                                ChatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                                            {
                                                                                                                                if (task.isSuccessful())
                                                                                                                                {
                                                                                                                                    Toast.makeText(getContext(), "you have cancelled the chat request.", Toast.LENGTH_SHORT).show();
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                                builder.show();
                                                                            }
                                                                        });

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });


                                                            }

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                                    {

                                                    }
                                                });

                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;
        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btnn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btnn);


        }


    }
}
