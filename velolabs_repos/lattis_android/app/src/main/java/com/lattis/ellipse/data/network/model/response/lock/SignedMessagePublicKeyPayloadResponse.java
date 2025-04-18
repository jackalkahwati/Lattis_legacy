package com.lattis.ellipse.data.network.model.response.lock;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/27/17.
 */

public class SignedMessagePublicKeyPayloadResponse {

    @SerializedName("signed_message")
    private String signed_message;

    @SerializedName("public_key")
    private String public_key;

    public String getSigned_message() {
        return signed_message;
    }

    public void setSigned_message(String signed_message) {
        this.signed_message = signed_message;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }





}
