package com.lattis.data.database.base

import io.realm.RealmObject

open class RealmInt : RealmObject {

    var value: Int = 0

    constructor() {}

    constructor(value: Int) {
        this.value = value
    }
}