package com.jis.enigmamessenger.message;

import com.stfalcon.chatkit.commons.models.IUser;

/*
 * Created by troy379 on 04.04.17.
 */
public class MessageUser implements IUser {

    private String id;
    private String name;
    private String avatar;


    public MessageUser(String id, String name, String avatar) {

        this.id = id;
        this.name = name;
        this.avatar = avatar.isEmpty()? "avatar" : avatar;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

}
