package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by raverat on 4/6/17.
 */

open class RealmMedia : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var url: String? = null

}
