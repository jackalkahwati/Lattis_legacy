package com.lattis.lattis.presentation.utils

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.lattis.lattis.presentation.utils.CrashlyticsUtil.ReleaseReportingTree
import io.lattis.lattis.BuildConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

class FirebaseUtil {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun instantiateSDK(application: Application?) {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ReleaseReportingTree())
        }
        mFirebaseAnalytics = Firebase.analytics
    }

    fun addUserIdAndEmail(userId: String, email: String) {
        mFirebaseAnalytics!!.setUserId(userId)
        mFirebaseAnalytics!!.setUserProperty("email", email)
        CrashlyticsUtil.addUserInformation(userId, email)
    }

    fun addSignInEvent(userId: String, email: String) {
        addUserIdAndEmail(userId, email)
        val bundle = Bundle()
        bundle.putString("user_id", userId)
        bundle.putString("email", email)
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
        addCustomEvent(LOG_IN,LOG_IN)
    }

    fun addSignUpEvent(userId: String, email: String) {
        addUserIdAndEmail(userId, email)
        val bundle = Bundle()
        bundle.putString("user_id", userId)
        bundle.putString("email", email)
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
        addCustomEvent(SIGN_UP,SIGN_UP)
    }

    fun addCustomEvent(event_name: String?, message: String?) {
        mFirebaseAnalytics!!.logEvent(event_name!!){
            param(FirebaseAnalytics.Param.ITEM_NAME, message!!)
        }
    }

    fun logInTimber(e: Throwable?) {
        Timber.e(e)
    }

    companion object {
        private var firebaseUtil: FirebaseUtil? = null
        val instance: FirebaseUtil?
            get() {
                if (firebaseUtil == null) {
                    firebaseUtil = FirebaseUtil()
                }
                return firebaseUtil
            }

        var startRideEventName = "START_RIDE"
        var startRideEventMessage = "Ride %d started"
        var endRideEventName = "END_RIDE"
        var endRideEventMessage = "Ride %d ended"

        var outOfParkingEventName = "OUT_OF_PARKING"
        var outOfParkingEventMessage = "Ride %d tried to park outside"

        var OPEN_APPLICATION = "OPEN_APPLICATION"
        var QR_CODE_SCANNING = "QR_CODE_SCANNING"
        var CONFIRM = "CONFIRM"
        var QR_SCAN_CODE_CONFIRM = "QR_SCAN_CODE_CONFIRM"
        var NORMAL_CONFIRM = "NORMAL_CONFIRM"
        var SIGN_UP = "SIGN_UP"
        var LOG_IN = "LOG_IN"
        var ADD_CREDIT_CARD = "ADD_CREDIT_CARD"
        var DELETE_CREDIT_CARD = "DELETE_CREDIT_CARD"
        var STRIPE = "STRIPE"
        var MERCADOPEGO = "MERCADOPEGO"
        var SAVE_CREDIT_CARD = "SAVE_CREDIT_CARD"
        var PARKING_VIEW = "PARKING_VIEW"
        var BEFORE_BIKE_BOOKED_PARKING_VIEW = "BEFORE_BIKE_BOOKED_PARKING_VIEW"
        var AFTER_BIKE_BOOKED_PARKING_VIEW = "AFTER_BIKE_BOOKED_PARKING_VIEW"
        var WHILE_RESERVING_BIKE_PARKING_VIEW = "WHILE_RESERVING_BIKE_PARKING_VIEW"
        var QR_CODE_SCAN_MAIN = "QR_CODE_SCAN_MAIN"
        var QR_CODE_SCAN_VEHICLE = "QR_CODE_SCAN_VEHICLE"
        var RESERVE = "RESERVE"
        var QR_SCAN_CODE_RESERVE = "QR_SCAN_CODE_RESERVE"
        var NORMAL_RESERVE = "NORMAL_RESERVE"
        var HELP = "HELP"
        var SEARCH = "SEARCH"
        var RIDE_HISTORY = "RIDE_HISTORY"
        var LOGOUT = "LOGOUT"

    }
}