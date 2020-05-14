package com.frienders.main.db;

public enum MsgType
{
    TEXT("text"),
    DOC("doc"),
    PDF("pdf"),
    IMAGE("image"),
    VIDEO("video");

    private final String id;

    MsgType(String id)
    {
        this.id = id;
    }

    public String getMsgTypeId()
    {
        return this.id;
    }
}
