package com.lattis.data.database.base

class RealmIntMapper : AbstractRealmDataMapper<Int, RealmInt>() {

    override fun mapIn(integer: Int): RealmInt {
        return RealmInt(integer)
    }

    override fun mapOut(realmInt: RealmInt): Int {
        return realmInt.value
    }
}
