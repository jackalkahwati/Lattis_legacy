package com.lattis.ellipse.data.network.model.response.bike;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/3/17.
 */

public class ReserveBikeDataResponse {

    @SerializedName("booked_on")
    private long booked_on;
    @SerializedName("expires_in")
    private int expires_in;
    @SerializedName("on_call_operator")
    private String on_call_operator;

    public String getOn_call_operator() {
        return on_call_operator;
    }

    public void setOn_call_operator(String on_call_operator) {
        this.on_call_operator = on_call_operator;
    }

    public long getBooked_on() {
        return booked_on;
    }

    public void setBooked_on(long booked_on) {
        this.booked_on = booked_on;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }



}
