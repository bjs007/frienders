package com.frienders.main.db;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ParentCreation implements Runnable
{
    private DatabaseReference firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    List<GroupHandler.ChildNodeWithDBReference> childNodeWithDBReferences;
    public ParentCreation(List<GroupHandler.ChildNodeWithDBReference> childNodeWithDBReferences)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("Groups");
        this.childNodeWithDBReferences = childNodeWithDBReferences;
    }
    @Override
    public void run()
    {
        createGroup(childNodeWithDBReferences, 0);
    }

    private void createGroup(final List<GroupHandler.ChildNodeWithDBReference> groupPathWithName, final int level)
    {

        Log.i("Frienders", "I will be executing first");
        final String currentLevel = "level - " + level;
        boolean isFunctionCallAtLeafNode = false;

        if(level == groupPathWithName.size() - 1)
        {
            isFunctionCallAtLeafNode = true;
        }

        final boolean leafLevel = isFunctionCallAtLeafNode;

        firebaseDatabase.child(currentLevel).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                Iterator<DataSnapshot> iterator  = dataSnapshot.getChildren().iterator();
                Group requiredNodeAtCurrentLevel = null;

                while (iterator.hasNext())
                {
                    Group group = iterator.next().getValue(Group.class);
                    String newGroupName = groupPathWithName.get(level).name;
                    if(group != null && group.getName().equals((groupPathWithName.get(level).getName())))
                    {
                        requiredNodeAtCurrentLevel = group;
                        break;
                    }
                }

                if(requiredNodeAtCurrentLevel == null)
                {
                    DatabaseReference newGroupDBRefId = firebaseDatabase.child(currentLevel).push();
                    final String groupId = newGroupDBRefId.getKey();
                    requiredNodeAtCurrentLevel = createGroup(groupPathWithName.get(level).getName(), groupId, level, leafLevel);


                    if(level == 0)
                    {
                        requiredNodeAtCurrentLevel.setParentId("root");
                    }
                    else
                    {
                        requiredNodeAtCurrentLevel.setParentId(groupPathWithName.get(level - 1).getCurrentNodeDbRef());
                    }
                }


                groupPathWithName.get(level).setCurrentNodeDbRef(requiredNodeAtCurrentLevel.getId());

                
                Map<String, Object> updatedNodeAtCurrentLevelDetail = new HashMap<>();
                updatedNodeAtCurrentLevelDetail.put(currentLevel +"/"+ requiredNodeAtCurrentLevel.getId() + "/", requiredNodeAtCurrentLevel);

                firebaseDatabase.updateChildren(updatedNodeAtCurrentLevelDetail).addOnCompleteListener(new OnCompleteListener()
                {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            if(!leafLevel)
                            {
                                createGroup(groupPathWithName, level + 1);

                            }
                            else
                            {

                            }
                        }
                        else
                        {

                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private Group createGroup(String name, String id, int level, boolean isLeaf)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUser = firebaseAuth.getCurrentUser().getUid();
        return new Group(name, id, currentUser, getCurrentDate(), getCurrentTime(),isLeaf, level, new ArrayList<String>());
    }


    private String getCurrentTime()
    {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        return  currentDate.format(calendar.getTime());
    }

    private String getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        return currentTime.format(calendar.getTime());
    }
}
