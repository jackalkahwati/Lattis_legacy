package com.lattis.lattis.presentation.utils

/**
 * Created by ssd3 on 8/3/17.
 */
object IsRidePaid {
    const val FLEET_TYPE_PUBLIC_PAYMENT = "public"
    const val FLEET_TYPE_PUBLIC_NO_PAYMENT = "public_no_payment"
    const val FLEET_TYPE_PRIVATE_PAYMENT = "private"
    const val FLEET_TYPE_PRIVATE_NO_PAYMENT = "private_no_payment"
    fun isRidePaidForFleet(fleet_type: String?): Boolean {
        return if (fleet_type == null) {
            false
        } else if (fleet_type == "") {
            false
        } else if (fleet_type.equals(
                FLEET_TYPE_PUBLIC_PAYMENT,
                ignoreCase = true
            ) || fleet_type.equals(FLEET_TYPE_PRIVATE_PAYMENT, ignoreCase = true)
        ) {
            true
        } else {
            false
        }
    }
}