package com.lattis.data.database.base

import io.realm.RealmObject

open class RealmString : RealmObject {

    var value: String? = null

    constructor() {}

    constructor(value: String) {
        this.value = value
    }
}