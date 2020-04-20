package com.frienders.main.db;

import java.util.List;

public class GroupFactory
{
    public static Group getGroup(String name, String id, String createdBy, String date, String time, boolean isLeave, int level,List<String> childrenIds)
    {
        return new Group( name,  id,  createdBy,  date,  time,  isLeave,  level, childrenIds);
    }

}
