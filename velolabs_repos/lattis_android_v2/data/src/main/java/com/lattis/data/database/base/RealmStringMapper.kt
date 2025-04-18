package com.lattis.data.database.base

class RealmStringMapper : AbstractRealmDataMapper<String, RealmString>() {

    override fun mapIn(string: String): RealmString {
        return RealmString(string)
    }

    override fun mapOut(realmString: RealmString): String {
        return realmString.value?:""
    }
}
