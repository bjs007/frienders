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

//    private interface UpdateChildOfParent
//    {
//        void onCallBack(List<ChildNodeWithDBReference> list,int level, String childDfRef);
//    }
//
//    private class UpdateChildOfParentImpl implements UpdateChildOfParent
//    {
//
//        @Override
//        public void onCallBack(List<ChildNodeWithDBReference> list, final int level, String childDbRef) {
//            String parentId = list.get(level).getCurrentNodeDbRef();
//            final String childId = list.get(level).getChildDbRef();
//            final String path = "level - " + level;
//
//            firebaseDatabase.child(path).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                {
//                    if(dataSnapshot.exists())
//                    {
//                        Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
//                        while (dataSnapshotIterator.hasNext())
//                        {
//                            DataSnapshot ds = dataSnapshotIterator.next();
//
//                            Group group = ds.getValue(Group.class);
//                            if(group.getChildrenIds() == null)
//                            {
//                                group.setChildrenIds(new ArrayList<String>());
//                            }
//                            group.getChildrenIds().add(childId);
//                            Map<String, Object> updatedNodeAtCurrentLevelDetail = new HashMap<>();
//
//                            updatedNodeAtCurrentLevelDetail.put(level +"/"+ group.getId() + "/", group);
//
//                            firebaseDatabase.updateChildren(updatedNodeAtCurrentLevelDetail);
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError)
//                {
//
//                }
//            });
//        }
//    }


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
                                group.getChildrenIds().add("TEST CHILD");
                                Map<String, Object> updatedNodeAtCurrentLevelDetail = new HashMap<>();

                                updatedNodeAtCurrentLevelDetail.put(path +"/"+ group.getId() + "/", group);

                                firebaseDatabase.updateChildren(updatedNodeAtCurrentLevelDetail);
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


//                final String childGroupDBRefId = leafLevel? null : groupPathWithName.get(level + 1).getChildDbRef();
//
//                if(requiredNodeAtCurrentLevel.getChildrenIds() == null)
//                {
//                    requiredNodeAtCurrentLevel.setChildrenIds(new ArrayList<String>());
//                }
//                else
//                {
//                    requiredNodeAtCurrentLevel.getChildrenIds().add(childGroupDBRefId);
//                }



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

    private void fillChildIdInParentNode(final List<ChildNodeWithDBReference> groupPathWithName)
    {

        for(int i  = 0; i < groupPathWithName.size() - 1; i++)
        {
            final String currentLevel  = "level - " + i;
            String parentId = groupPathWithName.get(i).getCurrentNodeDbRef();
            final String childId = groupPathWithName.get(i).getCurrentNodeDbRef();

            firebaseDatabase.child(currentLevel).child(parentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                   Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                   while (dataSnapshotIterator.hasNext())
                   {
                       Group it = dataSnapshotIterator.next().getValue(Group.class);
                       if(it.getChildrenIds() == null)
                       {
                           it.setChildrenIds(new ArrayList<String>());
                       }

                       it.getChildrenIds().add(childId);
                       Map<String, Object> map = new HashMap<>();
                       map.put(currentLevel + "/" + it.getId(), it);
                       firebaseDatabase.updateChildren(map);
                   }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });


        }
    }

    private Group createGroup(String name, String id, int level, boolean isLeaf)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        return new Group(name, id, currentUser, getCurrentDate(), getCurrentTime(),isLeaf, level, new ArrayList<String>());
    }

//    public class ChildNodeCreation implements Runnable
//    {
//
//        List<ChildNodeWithDBReference> childNodeWithDBReferences;
//        public ChildNodeCreation(List<ChildNodeWithDBReference> childNodeWithDBReferences)
//        {
//            this.childNodeWithDBReferences = childNodeWithDBReferences;
//        }
//
//        @Override
//        public void run()
//        {
//            createGroup(childNodeWithDBReferences, 0);
//        }
//    }

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

//        lc = new CountDownLatch(pathNodes.size());

        for(final String dbNode : pathNodes)
        {

            list.add(new ChildNodeWithDBReference(dbNode, null, null));
        }

        Thread t1  = new Thread(new ParentCreation(list));

        t1.start();


        t1.join();

        Thread.sleep(2000);
        Thread t2 = new Thread(new ChildCreation(list));
        t2.start();
//        lc.await();

//        int level = 0;
//        for(ChildNodeWithDBReference node : list)
//        {
//            onCallBack(node.getCurrentNodeDbRef(), level, node.getChildDbRef());
//            level++;
//        }
    }


    public void onCallBack(String parentDbRef, final int level, final String childDbRef) {

            final String path = "level - " + level;

            firebaseDatabase.child(path).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                        while (dataSnapshotIterator.hasNext()) {
                            DataSnapshot ds = dataSnapshotIterator.next();

                            Group group = ds.getValue(Group.class);
                            if (group.getChildrenIds() == null) {
                                group.setChildrenIds(new ArrayList<String>());
                            }
                            group.getChildrenIds().add(childDbRef);

                            Map<String, Object> updatedNodeAtCurrentLevelDetail = new HashMap<>();

                            updatedNodeAtCurrentLevelDetail.put(level + "/" + group.getId() + "/", group);

                            firebaseDatabase.updateChildren(updatedNodeAtCurrentLevelDetail);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
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