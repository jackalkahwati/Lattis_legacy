package com.lattis.data.entity.body.authentication

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 4/25/17.
 */

class GetNewTokensBody(@field:SerializedName("user_id")
                       var userId: String?, @field:SerializedName("password")
                       var password: String?)
