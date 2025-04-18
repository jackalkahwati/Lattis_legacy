package com.lattis.data.entity.body.bike;

import com.google.gson.annotations.SerializedName;


public class CancelBikeBody {

    @SerializedName("bike_id")
    private Integer bike_id;

    @SerializedName("bike_damaged")
    private boolean bike_damaged;

    @SerializedName("lock_issue")
    private boolean lock_issue;


    public CancelBikeBody(Integer bike_id, boolean bike_damaged, boolean lockIssue){
        this.bike_id=bike_id;
        this.bike_damaged=bike_damaged;
        this.lock_issue = lockIssue;
    }
}
