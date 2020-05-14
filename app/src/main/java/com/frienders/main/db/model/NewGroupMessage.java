package com.frienders.main.db.model;

public class NewGroupMessage extends Messages
{
    private String toGroupId;

    public NewGroupMessage()
    {
        super();
    }

    public String getToGroupId()
    {
        return toGroupId;
    }

    public void setToGroupId(String toGroupId)
    {
        this.toGroupId = toGroupId;
    }
}
