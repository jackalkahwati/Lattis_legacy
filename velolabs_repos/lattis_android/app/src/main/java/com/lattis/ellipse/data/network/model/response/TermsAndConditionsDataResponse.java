package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/19/17.
 */

public class TermsAndConditionsDataResponse {

    @SerializedName("version")
    private String version;
    @SerializedName("terms")
    private String terms;

    public String getVersion() {
        return version;
    }

    public String getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return "TermsAndConditionsResponse{" +
                "version='" + version + '\'' +
                ", terms='" + terms + '\'' +
                '}';
    }
}