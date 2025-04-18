package com.lattis.lattis.presentation.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

object CrashlyticsUtil {
    //////////////////////// TIMBER ///////////////////////////////////
    /////////////////////// USER ID AND EMAIL WILL BE STORED AFTER SUCCESSFUL LOGIN ///////////////////////
    fun addUserInformation(userId: String, userEmail: String) {
        if (userEmail != null && userId != null && userEmail != "" && userId != "") {
            FirebaseCrashlytics.getInstance().setUserId(userId)
            FirebaseCrashlytics.getInstance().setCustomKey("user_email", userEmail)
        }
    }

    /////////////////////// LOGS CAN BE ADDED FOR SOME HIGH PRIORITY FAILURE ///////////////////////
    fun addLog(log: String?) {
        if (log != null) {
            FirebaseCrashlytics.getInstance().log(log)
        }
    }

    fun addLogExpection(throwable: Throwable?) {
        if (throwable != null) {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    ///////////////////////  CUSTOM KEYS WHICH WILL BE SENT ALONG WITH CRASH REPORT ///////////////////////
    fun addCustomStringKey(key: String?, value: String?) {
        if (key != null && value != null) {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        }
    }

    fun addCustomBoolKey(key: String?, value: Boolean) {
        if (key != null) {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        }
    }

    fun addCustomIntKey(key: String?, value: Int) {
        if (key != null) {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        }
    }

    fun addCustomDoubleKey(key: String?, value: Double) {
        if (key != null) {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        }
    }

    fun addCustomFloatKey(key: String?, value: Float) {
        if (key != null) {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        }
    }

    //////////////////////// TIMBER ///////////////////////////////////
    class ReleaseReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            if (t != null && priority == Log.ERROR) {
                FirebaseCrashlytics.getInstance().recordException(t)
            } else {
                FirebaseCrashlytics.getInstance().log(message)
            }
        }
    }
}