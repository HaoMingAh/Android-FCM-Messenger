package com.jis.enigmamessenger.common;

import okhttp3.MediaType;

/**
 * Created by sss on 8/24/2016.
 */
public class Constants {

    public static final String PREFKEY_ID = "user_id";
    public static final String PREFKEY_TOKEN = "token";

    public static final String SERVER_ADDR = "";
    public static final String BASE_URL = SERVER_ADDR + "/index.php/api/";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Requests
    public static final String REQ_SENDCODE = "sendCode/";
    public static final String REQ_REGISTER = "register/";
    public static final String REQ_LOGIN = "login/";
    public static final String REQ_GETCONTACTS = "getContacts/";
    public static final String REQ_SAVECONTACTS = "saveContacts";
    public static final String REQ_REGISTERTOKEN = "registerToken/";
    public static final String REQ_CREATEROOM = "createRoom/";
    public static final String REQ_LOADROOMS = "loadRooms/";
    public static final String REQ_SENDMESSAGE = "sendMessage";
    public static final String REQ_LOADMESSAGES = "loadMessages/";
    public static final String REQ_LOADMESSAGESBYUSER = "loadMessagesByUser/";
    public static final String REQ_READMESSAGE = "readMessage/";
    public static final String REQ_DELETEROOM = "deleteAllMessages/";


    public static final String PARAM_ID = "id";
    public static final String PARAM_CONTACTS = "contacts";
    public static final String PARAM_SENDER = "sender";
    public static final String PARAM_TARGET = "target";
    public static final String PARAM_MESSAGE = "message";

    // params
    public static final String RES_CODE = "result_code";
    public static final String RES_ID = "id";
    public static final String RES_USERID = "user_id";
    public static final String RES_PHONENUMBER = "phonenumber";
    public static final String RES_NAME = "name";
    public static final String RES_CONTACTINFOS = "contact_infos";
    public static final String RES_ROOMINFOS = "room_infos";
    public static final String RES_TARGETID = "target_id";
    public static final String RES_ROOMNAME = "room_name";
    public static final String RES_LASTMESSAGE = "last_message";
    public static final String RES_LASTTIME = "last_time";
    public static final String RES_UNREAD = "unread";
    public static final String RES_MESSAGES = "messages";
    public static final String RES_SENDER = "sender";
    public static final String RES_MESSAGE = "message";
    public static final String RES_CREATEDAT = "created_at";
    public static final String RES_STATUS = "status";


    public static final int CODE_SUCCESS = 0;


    public static final String TOWHERE = "towhere";
    public static final String INTENTUSER = "user";
    public static final String NEED_AUTH = "auth";
    public static final int TOMAIN = 100;
    public static final int TOCONTACTS = 101;
    public static final int TOMESSAGE = 102;

}
