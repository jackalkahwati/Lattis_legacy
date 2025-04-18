package com.lattis.ellipse.presentation.ui.ride.service.util;

/**
 * Created by ssd3 on 7/28/17.
 */

public class UpdateTripData {
    private double duration;
    private float cost;
    private String currency;

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public UpdateTripData(double duration, float cost, String currency){
        this.duration=duration;
        this.cost=cost;
        this.currency=currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString(){
        return "Duration: "+duration + " cost: " + cost + " currency: " + currency;
    }

}
