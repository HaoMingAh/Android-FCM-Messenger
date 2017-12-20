package com.jis.enigmamessenger.model;

import com.jis.enigmamessenger.common.Constants;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by JIS on 6/6/2017.
 */

public class UserEntity implements Serializable {

    int _id = 0;
    String _name = "";
    String _phoneNumber = "";
    String _profile = "";

    public UserEntity() {}

    public UserEntity(int id) {
        _id = id;
    }

    public UserEntity(int id, String name, String phone, String profile) {

        _id = id;
        _name = name;
        _phoneNumber = phone;
        _profile = profile;
    }

    public UserEntity(JSONObject jsonObject) {

        try {
            _id = jsonObject.getInt(Constants.RES_USERID);
            _phoneNumber = jsonObject.getString(Constants.RES_PHONENUMBER);
            _name = jsonObject.getString(Constants.RES_NAME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public String getPhoneNumber() {
        return _phoneNumber;
    }

    public void setPhoneNumber(String _phoneNumber) {
        this._phoneNumber = _phoneNumber;
    }

    public String getProfile() {
        return _profile;
    }

    public void setProfile(String _profile) {
        this._profile = _profile;
    }

    @Override
    public boolean equals(Object obj) {

        UserEntity one = (UserEntity) obj;
        return _id == one.getId();
    }
}
