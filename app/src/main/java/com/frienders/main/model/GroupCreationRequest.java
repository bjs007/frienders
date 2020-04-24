package com.frienders.main.model;

public class GroupCreationRequest
{
   private String groupNameInEng;
   private String groupNameInHin;
   private String groupDescInEng;
   private String groupDescInHin;

   public GroupCreationRequest()
   {

   }

    public GroupCreationRequest(String groupNameInEng, String groupNameInHin, String groupDescInEng, String groupDescInHin) {
        this.groupNameInEng = groupNameInEng;
        this.groupNameInHin = groupNameInHin;
        this.groupDescInEng = groupDescInEng;
        this.groupDescInHin = groupDescInHin;
    }

    public String getGroupNameInEng() {
        return groupNameInEng;
    }

    public void setGroupNameInEng(String groupNameInEng) {
        this.groupNameInEng = groupNameInEng;
    }

    public String getGroupNameInHin() {
        return groupNameInHin;
    }

    public void setGroupNameInHin(String groupNameInHin) {
        this.groupNameInHin = groupNameInHin;
    }

    public String getGroupDescInEng() {
        return groupDescInEng;
    }

    public void setGroupDescInEng(String groupDescInEng) {
        this.groupDescInEng = groupDescInEng;
    }

    public String getGroupDescInHin() {
        return groupDescInHin;
    }

    public void setGroupDescInHin(String groupDescInHin) {
        this.groupDescInHin = groupDescInHin;
    }
}
