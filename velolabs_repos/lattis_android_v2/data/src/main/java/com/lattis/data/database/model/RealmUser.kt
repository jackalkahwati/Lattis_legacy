package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmUser : RealmObject() {
    @PrimaryKey
    var id: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var registrationId: String? = null
    var isUsingFacebook:Boolean? = false
    var username: String? = null
    var usersId: String? = null
    var phoneNumber: String? = null
    var email: String? = null
    var isVerified:Boolean? = false
    var maxLocks:Int? = 0
    var imageURI: String? = null

    override fun toString(): String {
        return "RealmUser{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", LastName='" + lastName + '\'' +
                ", registrationId='" + registrationId + '\'' +
                ", isUsingFacebook=" + isUsingFacebook + '\'' +
                ",imageUri=" + imageURI +
                '}'
    }
}