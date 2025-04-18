package com.lattis.domain.models


class Version {

    var version: Int = 0
    var revision: Int = 0

    constructor() {}

    constructor(version: Int, revision: Int) {
        this.version = version
        this.revision = revision
    }

    fun isInferiorTo(version: Version): Boolean {
        return this.version < version.version || this.revision < version.revision
    }

    override fun toString(): String {
        return "Version{" +
                "version=" + version +
                ", revision=" + revision +
                '}'.toString()
    }

    class Challenge(private var latestVersion: Version?, var lock: Lock?) {

        var lockVersion: Version? = null

        val isSuccessful: Boolean
            get() = !lockVersion!!.isInferiorTo(latestVersion!!)

        fun setLatestVersion(latestVersion: Version) {
            this.latestVersion = latestVersion
        }
    }
}
