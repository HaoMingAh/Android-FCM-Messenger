package com.jis.enigmamessenger.common;


import com.jis.enigmamessenger.main.MainActivity;
import com.jis.enigmamessenger.message.MessageActivity;
import com.jis.enigmamessenger.model.ContactEntity;
import com.jis.enigmamessenger.model.RoomEntity;
import com.jis.enigmamessenger.model.UserEntity;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by JIS on 4/20/2017.
 */

public class Common {

    public static boolean g_isAppRunning = false;

    public static MainActivity g_main = null;
    public static UserEntity g_user = null;
    public static ArrayList<UserEntity> g_users = new ArrayList<>();
    public static ArrayList<RoomEntity> g_rooms = new ArrayList<>();
    public static ArrayList<ContactEntity> g_contacts = new ArrayList<>();

    public static MessageActivity g_messageActivity = null;

    public static boolean g_isAuthenticated = false; // for test
    public static boolean g_fromOther = false;

    public static final String SLASH = "ssllaasshh";


    public static String encode(String param) {

        try {
            return URLEncoder.encode(param.trim().replace(" ", "%20").replace("/", SLASH), "utf-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return param;

    }

    public static String encode(String[] params) {

        String result = "";

        for (int i = 0; i < params.length; i++) {

            result += encode(params[i]);

            if (i < params.length - 1) {
                result += "/";
            }
        }

        return result;
    }

    public static String getRectString(int len) {

        String ret = "";
        for (int i = 0; i < len; i++) {
            ret += "□";
        }
        return ret;
    }

    public static String getCircleString(int len) {

        String ret = "";
        for (int i = 0; i < len; i++) {
            ret += "○";
        }
        return ret;
    }

}
