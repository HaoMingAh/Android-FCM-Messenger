package com.jis.enigmamessenger.message;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;


public class Message implements IMessage, MessageContentType.Image, MessageContentType {

    public static final int TEXT_MESSAGE = 0;
    public static final int IMAGE_MESSAGE = 1;
    public static final int LOCATION_MESSAGE = 2;


    private String id;
    private String text;
    private Date createdAt;
    private MessageUser user;
    private Image image;
    private int type = 0;
    private String body;

    public Message(MessageUser user, String text) {
        this(user, text, TEXT_MESSAGE);
    }

    public Message(MessageUser user, String text, int type) {
        this(user, text, new Date(), type);
    }

    public Message(MessageUser user, String text, Date createdAt, int type) {

        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
        this.type = type;
    }

    public Message(String id, MessageUser user, String text, Date createdAt, int type) {

        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public MessageUser getUser() {
        return this.user;
    }

    @Override
    public String getImageUrl() {
        return image == null ? null : image.url;
    }

    public String getStatus() {
        return "Sent";
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static class Image {

        private String url;
        public Image(String url) {
            this.url = url;
        }
    }


}
