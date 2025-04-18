package com.lattis.ellipse.data.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmUser extends RealmObject {

    @PrimaryKey
    private String id;
    private String firstName;
    private String LastName;
    private String registrationId;
    private boolean isUsingFacebook;
    private String username;
    private String usersId;
    private String phoneNumber;
    private String email;
    private boolean verified;
    private int maxLocks;
    private String imageUri;

    public String getImageURI() {
        return imageUri;
    }

    public void setImageURI(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public boolean isUsingFacebook() {
        return isUsingFacebook;
    }

    public void setUsingFacebook(boolean usingFacebook) {
        isUsingFacebook = usingFacebook;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsersId() {
        return usersId;
    }

    public void setUsersId(String usersId) {
        this.usersId = usersId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getMaxLocks() {
        return maxLocks;
    }

    public void setMaxLocks(int maxLocks) {
        this.maxLocks = maxLocks;
    }

    @Override
    public String toString() {
        return "RealmUser{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", LastName='" + LastName + '\'' +
                ", registrationId='" + registrationId + '\'' +
                ", isUsingFacebook=" + isUsingFacebook +'\''+
                ",imageUri="+imageUri+
                '}';
    }
}
