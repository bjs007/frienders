package com.frienders.main.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChildCreation implements Runnable {
    private DatabaseReference firebaseDatabase;
    List<GroupHandler.ChildNodeWithDBReference> childNodeWithDBReferences;

    public ChildCreation(  List<GroupHandler.ChildNodeWithDBReference> childNodeWithDBReferences)
    {
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("Groups");
        this.childNodeWithDBReferences = childNodeWithDBReferences;
    }
    @Override
    public void run()
    {

        onCallBack(childNodeWithDBReferences, 0);

    }

    public void onCallBack(List<GroupHandler.ChildNodeWithDBReference> list, final int level) {

        for(int i = 0; i < list.size() - 1; i++)
        {
            final String parentId = list.get(i).getCurrentNodeDbRef();
            final String childId = list.get(i + 1).getCurrentNodeDbRef();
            final String path = "level - " + i;

            firebaseDatabase.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                        while (dataSnapshotIterator.hasNext())
                        {
                            DataSnapshot ds = dataSnapshotIterator.next();

                            Group group = ds.getValue(Group.class);
                            if(group.getId().equals(parentId))
                            {
                                boolean childExist = false;
                                if(group.getChildrenIds() == null)
                                {
                                    group.setChildrenIds(new ArrayList<String>());

                                }else
                                {
                                    for(String child: group.getChildrenIds())
                                    {
                                        if(child.equals(childId))
                                        {
                                            childExist = true;
                                            break;
                                        }
                                    }
                                }

                                if(!childExist)
                                {
                                    group.getChildrenIds().add(childId);
                                    Map<String, Object> updatedNodeAtCurrentLevelDetail = new HashMap<>();

                                    updatedNodeAtCurrentLevelDetail.put(path +"/"+ group.getId() + "/", group);

                                    firebaseDatabase.updateChildren(updatedNodeAtCurrentLevelDetail);
                                }

                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }

    }
}
