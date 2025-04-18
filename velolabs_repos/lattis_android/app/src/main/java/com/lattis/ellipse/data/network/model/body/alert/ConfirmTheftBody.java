package com.lattis.ellipse.data.network.model.body.alert;

import com.google.gson.annotations.SerializedName;

public class ConfirmTheftBody {

    @SerializedName("theft_id")
    private int theftId;
    @SerializedName("is_confirmed")
    private boolean isConfirmed;

    public ConfirmTheftBody(int theftId, boolean isConfirmed) {
        this.theftId = theftId;
        this.isConfirmed = isConfirmed;
    }
}
