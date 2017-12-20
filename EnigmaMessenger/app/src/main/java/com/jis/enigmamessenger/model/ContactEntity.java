package com.jis.enigmamessenger.model;

/**
 * Created by JIS on 6/11/2017.
 */

public class ContactEntity {

    private String name = "";
    private String phoneNumber = "";

    public ContactEntity(String name, String phone) {

        this.name = name;
        phoneNumber = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object obj) {

        ContactEntity one = (ContactEntity) obj;
        return one.getPhoneNumber().endsWith(phoneNumber) ||
                this.phoneNumber.endsWith(one.getPhoneNumber());
    }
}
