package com.lattis.data.entity.response.axa

import com.lattis.domain.models.axa.AxaKey

class GetKeyResponse (
    val now: String?=null,
    val result: AxaKey,
    val status:String?=null
)