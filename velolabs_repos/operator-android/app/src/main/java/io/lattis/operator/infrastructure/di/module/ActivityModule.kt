package io.lattis.operator.infrastructure.di.module


import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.lattis.domain.executor.PostExecutionThread
import io.lattis.operator.executor.UiThread
import io.lattis.operator.presentation.authentication.launch.LaunchActivity
import io.lattis.operator.infrastructure.di.scope.ViewScope
import io.lattis.operator.presentation.authentication.SignInActivity
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.home.HomeActivity
import io.lattis.operator.presentation.map.filter.FilterVehiclesActivity
import io.lattis.operator.presentation.map.locate.LocateVehicleActivity
import io.lattis.operator.presentation.popup.PopUpActivity
import io.lattis.operator.presentation.qrcodescan.ScanQRCodeActivity
import io.lattis.operator.presentation.ticket.CreateTicketActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.fragments.equipment.other.VehicleDetailOtherEquipmentActivity

@Module
abstract class ActivityModule {

    @Binds
    abstract fun bindPostExecutionThread(uiThread: UiThread): PostExecutionThread

    @ViewScope
    @ContributesAndroidInjector
    abstract fun LaunchActivityInjector(): LaunchActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun SignInActivityInjector(): SignInActivity


    @ViewScope
    @ContributesAndroidInjector
    abstract fun HomeActivityActivityInjector(): HomeActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun FleetDetailActivityInjector():FleetDetailActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun VehicleDetailActivityInjector():VehicleDetailActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ScanQRCodeActivityInjector():ScanQRCodeActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun MapsActivityInjector(): LocateVehicleActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun PopUpActivityInjector():PopUpActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun CreateTicketActivityInjector():CreateTicketActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun FilterVehiclesActivityInjector():FilterVehiclesActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun VehicleDetailOtherEquipmentActivityInjector():VehicleDetailOtherEquipmentActivity
}