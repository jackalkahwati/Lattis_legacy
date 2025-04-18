package com.lattis.ellipse.data.network.model.response.lock;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

/**
 * Created by ssd3 on 4/27/17.
 */

public class SignedMessagePublicKeyResponse extends AbstractApiResponse {

    @SerializedName("payload")
    SignedMessagePublicKeyPayloadResponse signedMessagePublicKeyPayloadResponse;

    public SignedMessagePublicKeyPayloadResponse getSignedMessagePublicKeyPayloadResponse() {
        return signedMessagePublicKeyPayloadResponse;
    }

    public  void setSignedMessagePublicKeyPayloadResponse(SignedMessagePublicKeyPayloadResponse signedMessagePublicKeyPayloadResponse) {
        this.signedMessagePublicKeyPayloadResponse = signedMessagePublicKeyPayloadResponse;
    }
}
