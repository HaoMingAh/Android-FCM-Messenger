package com.jis.enigmamessenger.model;

import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by JIS on 4/22/2017.
 */

public class RoomEntity implements Serializable {

    String mRoomName = "";
    String mDisplayName = "";
    String mProfileUrl = "";
    String mMessage = "";
    Date mLastDate;
    int mTargetId = 0;
    String mPhonenumber = "";
    int mUnread = 0;


    public RoomEntity(String roomname) {
        mRoomName = roomname;
    }

    public RoomEntity(JSONObject jsonObject) {

        try {
            mRoomName = jsonObject.getString(Constants.RES_ROOMNAME);
            mTargetId = jsonObject.getInt(Constants.RES_TARGETID);
            mPhonenumber = jsonObject.getString(Constants.RES_PHONENUMBER);
            mMessage = jsonObject.getString(Constants.RES_LASTMESSAGE);
            mUnread = jsonObject.getInt(Constants.RES_UNREAD);

            String date = jsonObject.getString(Constants.RES_LASTTIME);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("gmt"));
            mLastDate = format.parse(date);

            int find = Common.g_users.indexOf(new UserEntity(mTargetId));

            if (find == -1) {

                find = Common.g_contacts.indexOf(new ContactEntity("", mPhonenumber));

                if (find != -1)
                    mDisplayName = Common.g_contacts.get(find).getName();
                else
                    mDisplayName = mPhonenumber;
            } else {
                mDisplayName = Common.g_users.get(find).getName();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public String getRoomName() {
        return mRoomName;
    }

    public void setRoomName(String mRoomName) {
        this.mRoomName = mRoomName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public String getProfileUrl() {
        return mProfileUrl;
    }

    public void setProfileUrl(String mProfileUrl) {
        this.mProfileUrl = mProfileUrl;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }


    public Date getLastDate() {
        return mLastDate;
    }

    public void setLastDate(Date mLastDate) {
        this.mLastDate = mLastDate;
    }

    public int getUnread() {
        return mUnread;
    }

    public void setUnread(int mUnread) {
        this.mUnread = mUnread;
    }

    public int getTargetId() {
        return mTargetId;
    }

    public void setTargetId(int mTargetId) {
        this.mTargetId = mTargetId;
    }

    public String getPhonenumber() {
        return mPhonenumber;
    }

    public void setPhonenumber(String mPhonenumber) {
        this.mPhonenumber = mPhonenumber;
    }

    @Override
    public boolean equals(Object obj) {

        RoomEntity other = (RoomEntity) obj;
        return mRoomName.equals(other.getRoomName());
    }
}
