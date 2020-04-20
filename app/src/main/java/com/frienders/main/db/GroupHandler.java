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
import com.google.firebase.storage.internal.Sleeper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class GroupHandler
{
    private DatabaseReference firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private String currentUser;
//    CountDownLatch lc = null;

    private static GroupHandler groupHandler = null;

    public static GroupHandler getGroupHandler()
    {
        if(groupHandler == null)
        {

            groupHandler = new GroupHandler();
        }

        return groupHandler;
    }

        public void onCallBack(List<ChildNodeWithDBReference> list, final int level) {
            final String parentId = list.get(level).getCurrentNodeDbRef();
            final String childId = list.get(level+1).getCurrentNodeDbRef();
            final String path = "level - " + level;

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
                                if(group.getChildrenIds() == null)
                                {
                                    group.setChildrenIds(new ArrayList<String>());
                                }
                                boolean childExist = false;

                                for(String childId : group.getChildrenIds())
                                {
                                    if(childId.equals(childId))
                                    {
                                        childExist = true;
                                        break;
                                    }
                                }

                                if(!childExist)
                                {
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

    public void createGroup(String groupPathWithName) throws InterruptedException {
        String[] tokens = groupPathWithName.split("/");

        firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        final List<ChildNodeWithDBReference> list = new ArrayList();

        int levels = -1;

        List<String> pathNodes = getPathNodes(tokens);

        if(pathNodes.size() == 0)
        {
            return;
        }


        for(final String dbNode : pathNodes)
        {

            list.add(new ChildNodeWithDBReference(dbNode, null, null));
        }

        Thread t1  = new Thread(new ParentCreation(list));

        t1.start();


        t1.join();

        Thread.sleep(6000);
        Thread t2 = new Thread(new ChildCreation(list));
        t2.start();
    }

    class ChildNodeWithDBReference {
        String name;
        String childDbRef;
        String currentNodeDbRef;


        public ChildNodeWithDBReference()
        {

        }
        public ChildNodeWithDBReference(String name
                , String dbRef, String currentNodeDbRef)
        {
            this.name = name;
            this.childDbRef = dbRef;
            this.currentNodeDbRef = currentNodeDbRef;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getChildDbRef() {
            return childDbRef;
        }

        public void setChildDbRef(String childDbRef) {
            this.childDbRef = childDbRef;
        }

        public String getCurrentNodeDbRef() {
            return currentNodeDbRef;
        }

        public void setCurrentNodeDbRef(String currentNodeDbRef) {
            this.currentNodeDbRef = currentNodeDbRef;
        }
    }

   private List<String> getPathNodes(String[] tokens)
   {
       List<String> nodes = new ArrayList<>();
       for(String token : tokens)
       {
           if(!token.equals("/"))
           {
               nodes.add(token);
           }
       }

       return nodes;
   }


}