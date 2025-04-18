package com.lattis.data.entity.response.axa

data class GetCloudIdResponse(
    val count:Int=0,
    val now:String?=null,
    val result:List<Result>?=null,
    val status:String?=null
){

    data class Result (
        val batch_code:String?=null,
        val created: String?=null,
        val firmware_modified: String?=null,
        val firmware_version: String?=null,
        val hardware_model: String?=null,
        val hardware_version: String?=null,
        val id: String?=null,
        val key: String?=null,
        val lock_model: String?=null,
        val lock_status: String?=null,
        val lock_uid: String?=null,
        val lock_version: String?=null,
        val mac_address: String?=null,
        val modified: String?=null,
        val nr_of_slots: Int=0,
        val reference: String?=null,
        val software_modified: String?=null,
        val software_version: String?=null
    )
}