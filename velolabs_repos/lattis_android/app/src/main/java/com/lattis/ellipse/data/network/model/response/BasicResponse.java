package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class BasicResponse extends AbstractApiResponse {

    @SerializedName("payload")
    private String[] data;

    public String[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "BasicResponse{" +
                super.toString() +
                "errors=" + Arrays.toString(data) +
                '}';
    }
}
