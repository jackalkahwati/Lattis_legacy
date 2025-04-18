package com.lattis.ellipse.domain.model;

import org.parceler.Parcel;

@Parcel
public class Location {

    private double latitude;
    private double longitude;
    private float accuracy;
    private long time;
    private boolean hasSpeed;
    private boolean hasAccuracy;
    private float speed;
    private String provider;

    public Location() {
        this(0.0, 0.0);
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public long getTime(){
        return time;
    }

    public void setTime(long time){
         this.time = time;
    }

    public boolean hasSpeed() {
        return hasSpeed;
    }

    public void setHasSpeed(boolean hasSpeed) {
        this.hasSpeed = hasSpeed;
    }

    public boolean hasAccuracy() {
        return hasAccuracy;
    }

    public void setHasAccuracy(boolean hasAccuary) {
        this.hasAccuracy = hasAccuary;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }





    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
