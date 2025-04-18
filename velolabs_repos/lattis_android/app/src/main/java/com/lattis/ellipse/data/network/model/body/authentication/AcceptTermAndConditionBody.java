package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

public class AcceptTermAndConditionBody {

    @SerializedName("did_accept")
    public boolean didAccept;

    public AcceptTermAndConditionBody(boolean didAccept) {
        this.didAccept = didAccept;
    }
}
