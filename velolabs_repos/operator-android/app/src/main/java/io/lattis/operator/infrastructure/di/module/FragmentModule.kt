package io.lattis.operator.infrastructure.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.lattis.operator.infrastructure.di.scope.ViewScope
import io.lattis.operator.presentation.fleet.fragments.map.FleetDetailMapFragment
import io.lattis.operator.presentation.fleet.fragments.tickets.FleetDetailTicketFragment
import io.lattis.operator.presentation.fleet.fragments.vehicles.FleetDetailVehicleFragment
import io.lattis.operator.presentation.vehicle.fragments.equipment.VehicleDetailEquipmentFragment
import io.lattis.operator.presentation.vehicle.fragments.ticket.VehicleDetailTicketFragment
import io.lattis.operator.presentation.vehicle.fragments.tickets.VehicleDetailTicketsFragment
import io.lattis.operator.presentation.vehicle.fragments.vehicle.VehicleDetailVehicleFragment

@Module
abstract class FragmentModule {

    @ViewScope
    @ContributesAndroidInjector
    abstract fun FleetDetailVehicleFragmentInjector():FleetDetailVehicleFragment

    @ViewScope
    @ContributesAndroidInjector
    abstract fun FleetDetailTicketFragmentInjector():FleetDetailTicketFragment

    @ViewScope
    @ContributesAndroidInjector
    abstract fun FleetDetailMapFragmentInjector():FleetDetailMapFragment

            @ViewScope
    @ContributesAndroidInjector
    abstract fun VehicleDetailEquipmentFragmentInjector():VehicleDetailEquipmentFragment

    @ViewScope
    @ContributesAndroidInjector
    abstract fun VehicleDetailVehicleFragmentInjector():VehicleDetailVehicleFragment

    @ViewScope
    @ContributesAndroidInjector
    abstract fun VehicleDetailTicketsFragmentInjector():VehicleDetailTicketsFragment

    @ViewScope
    @ContributesAndroidInjector
    abstract fun VehicleDetailTicketFragmentInjector():VehicleDetailTicketFragment
}