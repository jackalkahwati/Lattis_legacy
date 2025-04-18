package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public abstract class AbstractApiResponse {

    @SerializedName("status")
    private int status;
    @SerializedName("error")
    private String error;

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "AbstractApiResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                '}';
    }
}
