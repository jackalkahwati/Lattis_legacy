package com.lattis.data.entity.body.authentication

import com.google.gson.annotations.SerializedName

class SendVerificationCodeBody(//    @SerializedName("user_id")
    //    public String userId;
    @field:SerializedName("user_id") var user_id: String?,
    @field:SerializedName("account_type") var account_type: String?
)