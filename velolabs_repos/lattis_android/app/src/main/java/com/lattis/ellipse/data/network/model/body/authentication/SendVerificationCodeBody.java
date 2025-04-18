package com.lattis.ellipse.data.network.model.body.authentication;

import com.google.gson.annotations.SerializedName;

public class SendVerificationCodeBody {

//    @SerializedName("user_id")
//    public String userId;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("account_type")
    public String account_type;

    public SendVerificationCodeBody(String user_id, String account_type) {
        this.user_id = user_id;
        this.account_type = account_type;
    }
}
