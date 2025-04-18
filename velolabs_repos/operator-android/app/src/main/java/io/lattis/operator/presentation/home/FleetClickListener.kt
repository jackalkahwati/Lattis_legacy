package io.lattis.operator.presentation.home

import io.lattis.domain.models.Fleet

interface FleetClickListener {
    fun onFleetClicked(fleet: Fleet)
}