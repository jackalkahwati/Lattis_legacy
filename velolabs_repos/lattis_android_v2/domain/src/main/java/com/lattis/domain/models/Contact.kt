package com.lattis.domain.models

class Contact {

    var id: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var photoURL: String? = null
    var phoneNumber: String? = null
    var countryCode: String? = null

    override fun toString(): String {
        return "Contact{" +
                "id='" + id + '\''.toString() +
                ", firstName='" + firstName + '\''.toString() +
                ", lastName='" + lastName + '\''.toString() +
                ", photoURL='" + photoURL + '\''.toString() +
                ", phoneNumber='" + phoneNumber + '\''.toString() +
                ", countryCode='" + countryCode + '\''.toString() +
                '}'.toString()
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is Contact) {
            this.id === obj.id
        } else super.equals(obj)
    }
}
