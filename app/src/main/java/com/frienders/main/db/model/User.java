package com.frienders.main.db.model;

import java.io.Serializable;


public class User implements Serializable
{
    private String device_token;
    private String lang;
    private String name;
    private String status;
    private String uid;

    public User(String device_token)
    {
        this.device_token = device_token;
    }
    public User(String device_token, String lang, String name, String status, String uid)
    {
        this.device_token = device_token;
        this.lang = lang;
        this.name = name;
        this.status = status;
        this.uid = uid;
    }

    public String getDevice_token()
    {
        return device_token;
    }

    public void setDevice_token(String device_token)
    {
        this.device_token = device_token;
    }

    public String getLang()
    {
        return lang;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }
}
