package com.lattis.domain.models

class User {

    var id: String? = null
    var username: String? = null
    var usersId: String? = null
    var title: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var phoneNumber: String? = null
    var email: String? = null
    var password: String? = null
    var registrationId: String? = null
    var isUsingFacebook: Boolean? = false
    var isVerified: Boolean? = false
    var maxLocks: Int = 0
    var emergencyContactNumber: String? = null
    var imageUri: String? = null
    var privateNetworks: List<PrivateNetwork>? = null
    var restToken:String?=null;
    var refreshToken:String?=null;
    var userType:String?=null;

    override fun toString(): String {
        return "User{" +
                "id='" + id + '\''.toString() +
                ", firstName='" + firstName + '\''.toString() +
                ", LastName='" + lastName + '\''.toString() +
                ", email='" + email + '\''.toString() +
                ", registrationId='" + registrationId + '\''.toString() +
                ", isUsingFacebook=" + isUsingFacebook + '\''.toString() +
                ",imageUri=" + imageUri + '\''.toString() +
                ",phoneNumber=" + phoneNumber +
                '}'.toString()
    }

    enum class Type private constructor(val value: String) {

        FACEBOOK("facebook"), LATTIS("lattis");

        fun forValue(value: String): Type? {
            for (type in values()) {
                if (type.value == value)
                    return type
            }
            return null
        }
    }
}
