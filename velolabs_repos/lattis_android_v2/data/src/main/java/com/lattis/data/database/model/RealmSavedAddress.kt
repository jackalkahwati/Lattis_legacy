package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class RealmSavedAddress : RealmObject() {
    @Required
    @PrimaryKey
    var id: String? = null
    var address1: String? = null
    var address2: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

}