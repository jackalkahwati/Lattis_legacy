package com.lattis.lattis.presentation.reservation

interface AvailableVehiclesActionListener {
    fun onBikeSelected(position: Int)
    fun onMapSelected(position: Int)
    fun onBikeInfoSelected(position: Int)
}