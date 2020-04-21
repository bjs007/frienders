package com.frienders.main.model;

import java.util.List;

public class Group
{
    private String name;
    private String id;
    private String createdBy;
    private String date;
    private String time;
    private boolean isLeaf;
    private int level = 0;
    private String parentId;
    private List<String> childrenIds;

    public Group()
    {

    }

    public Group(String name, String id, String createdBy, String date, String time, boolean isLeaf, int level, List<String> childrenIds)
    {
        this.name = name;
        this.id = id;
        this.createdBy = createdBy;
        this.date = date;
        this.time = time;
        this.isLeaf = isLeaf;
        this.level = level;
        this.childrenIds = childrenIds;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getChildrenIds() {
        return childrenIds;
    }

    public void setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

    public void setLeaf(boolean leaf)
    {
        isLeaf = leaf;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }
}
