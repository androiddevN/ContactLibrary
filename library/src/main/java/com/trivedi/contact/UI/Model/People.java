package com.trivedi.contact.UI.Model;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Neeraj on 19/4/16.
 */
public class People implements Serializable{


    String name;
    String phone;
    String emailId;
    String id;
    String sectionHeader;
    boolean hasNumber=false;
    String photoId;
    String _mContactUri;


    public String getContactUri() {
        return _mContactUri;
    }

    public void setContactUri(String _mContactUri) {
        this._mContactUri = _mContactUri;
    }

    public String getSectionHeader() {
        return sectionHeader;
    }

    public void setSectionHeader(String sectionHeader) {
        this.sectionHeader = sectionHeader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHasNumber() {
        return hasNumber;
    }

    public void setHasNumber(boolean hasNumber) {
        this.hasNumber = hasNumber;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }
}
