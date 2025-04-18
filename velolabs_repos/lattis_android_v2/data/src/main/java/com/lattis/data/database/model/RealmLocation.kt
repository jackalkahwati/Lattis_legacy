package com.lattis.data.database.model

import io.realm.RealmObject

open class RealmLocation : RealmObject() {
    var latitude = 0.0
    var longitude = 0.0

    override fun toString(): String {
        return "RealmLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}'
    }
}