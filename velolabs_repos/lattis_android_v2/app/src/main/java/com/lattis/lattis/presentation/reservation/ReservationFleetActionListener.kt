package com.lattis.lattis.presentation.reservation

import com.lattis.domain.models.Bike

interface ReservationFleetActionListener {
    fun onReservationFleetSelected(fleet: Bike.Fleet)
}