package io.lattis.operator.presentation.vehicle.fragments.equipment.other

import android.content.Context
import android.content.Intent
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.fragments.equipment.VehicleDetailEquipmentFragment
import io.lattis.operator.presentation.vehicle.fragments.equipment.VehicleDetailEquipmentFragment.Companion.CURRENT_EQUIPMENT_POSITION
import io.lattis.operator.utils.GeneralUtils
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject


class VehicleDetailOtherEquipmentActivity: BaseActivity<VehicleDetailOtherEquipmentPresenter, VehicleDetailOtherEquipmentActivityView>(),
    VehicleDetailOtherEquipmentActivityView  {

    @Inject
    override lateinit var presenter: VehicleDetailOtherEquipmentPresenter
    override val activityLayoutId = R.layout.activity_vehicle_detail_other_equipment
    override var view: VehicleDetailOtherEquipmentActivityView = this


    companion object{
        fun getIntent(context: Context, vehicle: Vehicle,equipmentCurrentPosition:Int): Intent {
            val intent = Intent(context, VehicleDetailOtherEquipmentActivity::class.java)
            intent.putExtra(VehicleDetailActivity.VEHICLE, Gson().toJson(vehicle))
            intent.putExtra(CURRENT_EQUIPMENT_POSITION, equipmentCurrentPosition)
            return intent
        }
    }

    override fun configureViews() {
        super.configureViews()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.marginEnd = GeneralUtils.dpToPx(this, 72)
        toolbar.layoutParams =params
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    override fun startShowingInformation() {
        toolbar_title.text = presenter.vehicle?.name
        replaceFragment(R.id.fl_vehicle_detail_other_equipment, VehicleDetailEquipmentFragment.getInstance(
            presenter.vehicle,
            getString(R.string.equipment),
            presenter.currentPosition
        ))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}