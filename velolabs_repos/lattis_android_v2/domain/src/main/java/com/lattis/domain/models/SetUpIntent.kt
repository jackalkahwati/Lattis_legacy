package com.lattis.domain.models

import com.google.gson.annotations.SerializedName

class SetUpIntent(

){
     var id: String? = null
     var `object`: String? = null
     var application: String? = null
     var cancellation_reason: String? = null
     var client_secret: String? = null
     var created: Long? = null
     var customer: String? = null
     var description: String? = null
     var last_setup_error: String? = null
     var livemode:Boolean? = null
     var next_action: String? = null
     var on_behalf_of: String? = null
     var payment_method: String? = null
     var status: String? = null
     var usage: String? = null
     var payment_method_types: List<String>? = null
}