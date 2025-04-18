package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class CheckTermsConditionResultResponse {

    @SerializedName("has_accepted")
    public boolean hasAccepted;

    public boolean hasAccepted() {
        return hasAccepted;
    }

    @Override
    public String toString() {
        return "CheckTermsConditionResultResponse{" +
                "hasAccepted=" + hasAccepted +
                '}';
    }
}
