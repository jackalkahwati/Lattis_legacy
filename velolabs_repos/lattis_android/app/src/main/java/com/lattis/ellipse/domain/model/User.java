package com.lattis.ellipse.domain.model;

import java.util.List;

public class User {

    private String id;
    private String username;
    private String usersId;
    private String title;
    private String firstName;
    private String LastName;
    private String phoneNumber;
    private String email;
    private String password;
    private String registrationId;
    private boolean isUsingFacebook;
    private boolean verified;
    private int maxLocks;
    private String emergencyContactNumber;
    private String imageUri;
    private List<PrivateNetwork>privateNetworks;


    public List<PrivateNetwork> getPrivateNetworks() {
        return privateNetworks;
    }

    public void setPrivateNetworks(List<PrivateNetwork> privateNetworks) {
        this.privateNetworks = privateNetworks;
    }


    public String getId() {
        return id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    public void setEmergencyContactNumber(String emergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", LastName='" + LastName + '\'' +
                ", email='" + email + '\'' +
                ", registrationId='" + registrationId + '\'' +
                ", isUsingFacebook=" + isUsingFacebook +'\''+
                ",imageUri="+imageUri+'\''+
                ",phoneNumber="+phoneNumber+
                '}';
    }

    public enum Type{

        FACEBOOK("facebook"),LATTIS("lattis");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public Type forValue(String value){
            for(Type type:values()){
                if (type.value.equals(value))
                    return type;
            }
            return null;
        }
    }
}
