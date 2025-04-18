package com.lattis.domain.models

class TermsAndConditions {

    var version: String? = null
    var content: String? = null

    override fun toString(): String {
        return "TermsAndConditions{" +
                "version='" + version + "'" +
                ", content='" + content + "'}"
    }

}
