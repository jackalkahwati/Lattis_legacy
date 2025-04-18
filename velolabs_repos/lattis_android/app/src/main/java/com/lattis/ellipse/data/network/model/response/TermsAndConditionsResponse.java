package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class TermsAndConditionsResponse {

    @SerializedName("terms_and_conditions")
    private TermsAndConditionsDataResponse termsAndConditionsDataResponse;


    public String getVersion() {
        return termsAndConditionsDataResponse.getVersion();
    }

    public String getTerms() {
        return termsAndConditionsDataResponse.getTerms();
    }

    @Override
    public String toString() {
        return "TermsAndConditionsResponse{" +
                "TermsAndConditionsResponse='" + termsAndConditionsDataResponse + '\'' +
                '}';
    }
}
