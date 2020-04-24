package com.frienders.main.db;

import android.util.Log;

import androidx.annotation.NonNull;

import com.frienders.main.model.ChildNodeWithDBReference;
import com.frienders.main.model.Group;
import com.frienders.main.model.GroupCreationRequest;
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
    List<ChildNodeWithDBReference> childNodeWithDBReferences;
    public ParentCreation(List<ChildNodeWithDBReference> childNodeWithDBReferences)
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

    private void createGroup(final List<ChildNodeWithDBReference> groupPathWithName, final int level)
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
                    String newGroupName = groupPathWithName.get(level).getRequest().getGroupNameInEng();
                    if(group != null && group.getEngName().equals((groupPathWithName.get(level).getRequest().getGroupNameInEng())))
                    {
                        requiredNodeAtCurrentLevel = group;
                        break;
                    }
                }

                if(requiredNodeAtCurrentLevel == null)
                {
                    DatabaseReference newGroupDBRefId = firebaseDatabase.child(currentLevel).push();
                    final String groupId = newGroupDBRefId.getKey();
                    requiredNodeAtCurrentLevel = createGroup(groupPathWithName.get(level).getRequest(), groupId, level, leafLevel);


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

                
                final Map<String, Object> updatedNodeAtCurrentLevelDetail = new HashMap<>();
                updatedNodeAtCurrentLevelDetail.put(currentLevel +"/"+ requiredNodeAtCurrentLevel.getId() + "/", requiredNodeAtCurrentLevel);
                final Group finalNodeHere = requiredNodeAtCurrentLevel;

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
                                Map<String, Object> updateLeafNodes = new HashMap<>();
                                updateLeafNodes.put("leafs" + "/" + finalNodeHere.getId() + "/",finalNodeHere);
                                firebaseDatabase.updateChildren(updateLeafNodes);
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

    private Group createGroup(GroupCreationRequest request, String id, int level, boolean isLeaf)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUser = firebaseAuth.getCurrentUser().getUid();
        Group grp = new Group(request.getGroupNameInEng(), id, currentUser, getCurrentDate(), getCurrentTime(), isLeaf, level, new ArrayList<String>());
        grp.setEngDesc(request.getGroupDescInEng());
        grp.setHinName(request.getGroupNameInHin());
        grp.setHinDesc(request.getGroupDescInHin());
        return grp ;
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
