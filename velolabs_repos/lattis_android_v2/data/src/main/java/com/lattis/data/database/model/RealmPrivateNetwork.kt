package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by ssd3 on 5/8/17.
 */

open class RealmPrivateNetwork : RealmObject() {

    @PrimaryKey
    var private_fleet_user_id: Int = 0
    var userId: Int = 0
    var email: String? = null
    var fleet_id: Int = 0
    var verified: Int = 0
    var fleet_name: String? = null
    var type: String? = null
    var logo: String? = null

    companion object {


        val COLUMN_NAME_PRIVATE_FLEET_USER_ID = "private_fleet_user_id"
    }


}
