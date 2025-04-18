package com.lattis.data.entity.body.bike

import com.google.gson.annotations.SerializedName


class BikeDetailBody(bike_id: Int, qr_code_id: Int, iot_qr_code: String?, trip_id: Int) {

    @SerializedName("bike_id")
    private var bike_id: Int? = null

    @SerializedName("qr_code_id")
    private var qr_code_id: Int? = null

    @SerializedName("trip_id")
    private var trip_id: Int? = null

    @SerializedName("iot_qr_code")
    private var iot_qr_code: String? = null




    init {

        if (qr_code_id > 0) {
            this.qr_code_id = qr_code_id
        }

        if (bike_id > 0) {
            this.bike_id = bike_id
        }

        if (trip_id > 0) {
            this.trip_id = trip_id
        }

        this.iot_qr_code = iot_qr_code

    }
}
