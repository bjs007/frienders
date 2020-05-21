package com.frienders.main.db.model;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable
{
    private String engName;
    private String hinName;
    private String engDesc;
    private String hinDesc;
    private String id;
    private String createdBy;
    private String date;
    private String time;
    private boolean leaf;
    private int level = 0;
    private String parentId;
    private List<String> childrenIds;


    public Group()
    {

    }

    public Group(String createdBy, String date, String engDesc, String engName, String hinDesc, String hinName, String id, boolean Leaf, int level, String parentId, String time)
    {
        this.engDesc = engDesc;
        this.engName = engName;
        this.hinDesc = hinDesc;
        this.hinName = hinName;
        this.id = id;
        this.createdBy = createdBy;
        this.date = date;
        this.time = time;
        this.leaf = Leaf;
        this.level = level;
        this.parentId = parentId;
        this.time = time;
    }

    public Group(String engName, String id, String createdBy, String date, String time, boolean Leaf, int level, List<String> childrenIds)
    {
        this.engName = engName;
        this.id = id;
        this.createdBy = createdBy;
        this.date = date;
        this.time = time;
        this.leaf = Leaf;
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

    public String getEngName()
    {
        return engName;
    }

    public void setEngName(String engName)
    {
        this.engName = engName;
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
        return leaf;
    }

    public void setLeaf(boolean leaf)
    {
        this.leaf = leaf;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getHinName() {
        return hinName;
    }

    public void setHinName(String hinName) {
        this.hinName = hinName;
    }

    public String getEngDesc() {
        return engDesc;
    }

    public void setEngDesc(String engDesc) {
        this.engDesc = engDesc;
    }

    public String getHinDesc() {
        return hinDesc;
    }

    public void setHinDesc(String hinDesc) {
        this.hinDesc = hinDesc;
    }
}
