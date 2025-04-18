package com.lattis.data.entity.body.authentication

import com.google.gson.annotations.SerializedName

class RefreshTokenBody(@field:SerializedName("user_id")
                       var userId: String, @field:SerializedName("refresh_token")
                       var refreshToken: String)
