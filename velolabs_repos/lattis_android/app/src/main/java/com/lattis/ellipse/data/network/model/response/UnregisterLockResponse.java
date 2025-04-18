package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class UnregisterLockResponse extends AbstractApiResponse {

    @SerializedName("payload")
    String[] unregister;

    public String[] getUnregister() {
        return unregister;
    }

    @Override
    public String toString() {
        return "UnregisterLockResponse{" +
                "unregister=" + Arrays.toString(unregister) +
                '}';
    }
}
