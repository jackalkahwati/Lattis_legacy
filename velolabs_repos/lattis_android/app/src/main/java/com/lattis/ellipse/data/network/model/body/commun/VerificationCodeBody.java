package com.lattis.ellipse.data.network.model.body.commun;

import com.google.gson.annotations.SerializedName;

public class VerificationCodeBody {

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("account_type")
    private String account_type;

    @SerializedName("confirmation_code")
    private String confirmationCode;

    public VerificationCodeBody(String user_id, String account_type, String confirmationCode) {
        this.user_id = user_id;
        this.account_type = account_type;
        this.confirmationCode = confirmationCode;
    }
}
