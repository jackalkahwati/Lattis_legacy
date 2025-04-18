package io.lattis.operator.presentation.vehicle

import android.content.Context
import android.content.Intent
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.fleet.FleetDetailPagerAdapter
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import io.lattis.operator.presentation.vehicle.fragments.equipment.VehicleDetailEquipmentFragment
import io.lattis.operator.presentation.vehicle.fragments.ticket.VehicleDetailTicketFragment
import io.lattis.operator.presentation.vehicle.fragments.tickets.VehicleDetailTicketsFragment
import io.lattis.operator.presentation.vehicle.fragments.vehicle.VehicleDetailVehicleFragment
import io.lattis.operator.utils.GeneralUtils.dpToPx
import kotlinx.android.synthetic.main.activity_vehicle_detail.*
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject

class VehicleDetailActivity : BaseActivity<VehicleDetailActivityPresenter, VehicleDetailActivityView>(),
    VehicleDetailActivityView {

    @Inject
    override lateinit var presenter: VehicleDetailActivityPresenter
    override val activityLayoutId = R.layout.activity_vehicle_detail
    override var view: VehicleDetailActivityView = this


    companion object{
        val VEHICLE = "VEHICLE"
        val TICKET = "TICKET"
        val TAB_TITLE = "TAB_TITLE"
        fun getIntent(context: Context, vehicle: Vehicle, ticket: Ticket? = null): Intent {
            val intent = Intent(context, VehicleDetailActivity::class.java)
            intent.putExtra(VEHICLE, Gson().toJson(vehicle))
            if(ticket!=null)intent.putExtra(TICKET, Gson().toJson(ticket))
            return intent
        }
    }

    override fun configureViews() {
        super.configureViews()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.marginEnd = dpToPx(this,72)
        toolbar.layoutParams =params

        view_pager_vehicle_detail.offscreenPageLimit = 3


        if(presenter.ticket==null) {
            view_pager_vehicle_detail.adapter = FleetDetailPagerAdapter(
                supportFragmentManager,
                listOf(
                    VehicleDetailVehicleFragment.getInstance(
                        presenter.vehicle,
                        getString(R.string.vehicle)
                    ),
                    VehicleDetailEquipmentFragment.getInstance(
                        presenter.vehicle,
                        getString(R.string.equipment)
                    ),
                    VehicleDetailTicketsFragment.getInstance(
                        presenter.vehicle,
                        getString(R.string.tickets)
                    )
                )
            )
        }else{
            view_pager_vehicle_detail.adapter = FleetDetailPagerAdapter(
                supportFragmentManager,
                listOf(

                    VehicleDetailTicketFragment.getInstance(
                        presenter.vehicle,
                        presenter.ticket!!,
                        getString(R.string.ticket)
                    ),
                    VehicleDetailVehicleFragment.getInstance(
                        presenter.vehicle,
                        getString(R.string.vehicle)
                    ),
                    VehicleDetailEquipmentFragment.getInstance(
                        presenter.vehicle,
                        getString(R.string.equipment)
                    )
                )
            )
        }
    }

    override fun setTitle(title: String) {
        toolbar_title.text = title
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}