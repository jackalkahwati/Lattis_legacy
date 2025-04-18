package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmPromotionUsers: RealmObject() {
    @PrimaryKey
    var promotion_users_id: Int? = null
    var promotion_id: Int? = null
    var user_id: Int? = null
    var claimed_at: String? = null
}