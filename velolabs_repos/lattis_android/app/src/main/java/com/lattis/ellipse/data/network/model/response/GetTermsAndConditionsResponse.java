package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class GetTermsAndConditionsResponse extends AbstractApiResponse {

    @SerializedName("payload")
    TermsAndConditionsResponse termsAndConditionsResponse;

    public String getVersion() {
        return termsAndConditionsResponse.getVersion();
    }

    public String getTerms() {
        return termsAndConditionsResponse.getTerms();
    }

    @Override
    public String toString() {
        return "GetTermsAndConditionsResponse{" +
                "termsAndConditionsResponse=" + termsAndConditionsResponse +
                '}';
    }
}
