package com.frienders.main.config;

public class Configuration
{
    public static String firebaseappname = "Frienders";

    //Firebase DB Names
    public static String firebasegroupdb = "Groups";
    public static String firebasemessagedb = "Messages";
    public static String firebaseuserdb = "Users";
    public static String firebasegroupsubscribeddb = "subscribed";

    //Firestore DB names
    public static String userprofileimagesdb = "profileimages";
    public static String grouodisplayimagesdb = "groupdisplayimages";
    public static String groupmessageimagesdb = "groupmessageimages";
    public static String groupvideomessagesdb  = "groupvideomessages";


    //Channel configuration
    public static String default_channel_id = "app_channel_id";
    public static String default_channel_name = "app_channel_name";
    public static String default_channel_desc = "app_channel_desc";

    public static int RequestCodeForImagePick = 1;
    public static int RequestCodeForVideoPick = 2;
    public static int RequestCodeForDocPick = 3;
    public static String VIDEOFILE = "VIDEO";
    public static String IMAGEFILE = "IMAGE";
    public static String PDFFILE = "PDF";
    public static String DOCFILE = "MSDOCX";
    public static int imageMaxWidth = 1920;
    public static int imageMaxHeight = 2560;

    public static long maxVideoFileUploadableSizeInBytes = 17000000;
    public static long longVideoFileUploadableSizeInBytes = 10000000;
}
