package com.lattis.ellipse.data.network.model.body.bike;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 9/27/17.
 */

public class CancelBikeBody {

    @SerializedName("bike_id")
    private int bike_id;

    @SerializedName("bike_damaged")
    private boolean bike_damaged;

    @SerializedName("lock_issue")
    private boolean lock_issue;


    public CancelBikeBody(int bike_id,boolean bike_damaged,boolean lockIssue){
        this.bike_id=bike_id;
        this.bike_damaged=bike_damaged;
        this.lock_issue = lockIssue;
    }
}
