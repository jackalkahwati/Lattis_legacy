package io.lattis.operator.presentation.fleet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.activity.BaseAuthenticatedActivity
import io.lattis.operator.presentation.fleet.fragments.map.FleetDetailMapFragment
import io.lattis.operator.presentation.fleet.fragments.tickets.FleetDetailTicketFragment
import io.lattis.operator.presentation.fleet.fragments.vehicles.FleetDetailVehicleFragment
import io.lattis.operator.presentation.home.HomeActivity
import io.lattis.operator.presentation.popup.PopUpActivity
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_fleet_detail.*
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.view_toolbar.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FleetDetailActivity : BaseAuthenticatedActivity<FleetDetailActivityPresenter, FleetDetailActivityView>(),
    FleetDetailActivityView {

    @Inject
    override lateinit var presenter: FleetDetailActivityPresenter
    override val activityLayoutId = R.layout.activity_fleet_detail
    override var view: FleetDetailActivityView = this
    val REQUEST_CODE_USER_PROFILE = 123
    private val REQUEST_CODE_LOGOUT_ERROR=124

    companion object{
        val FLEET = "FLEET"
        val TAB_TITLE = "TAB_TITLE"
        fun getIntent(context: Context,fleet:Fleet):Intent{
            val intent = Intent(context, FleetDetailActivity::class.java)
            intent.putExtra(FLEET, Gson().toJson(fleet))
            return intent
        }
    }

    override fun configureViews() {
        super.configureViews()

        clickable_toolbar_title.text = presenter.fleet.name
        clickable_toolbar_title.visibility = View.VISIBLE
        toolbar_title.visibility = View.GONE
        iv_profile_settings.visibility = View.VISIBLE

        clickable_toolbar_title.setOnClickListener {
            val i = Intent(this, HomeActivity::class.java)
            startActivity(i)
            finishMe()
        }

        iv_profile_settings.setOnClickListener {
            showUserProfilePopUp()
        }

        view_pager_fleet_detail.offscreenPageLimit = 3

        view_pager_fleet_detail.adapter = FleetDetailPagerAdapter(
            supportFragmentManager,
            listOf(
                FleetDetailTicketFragment.getInstance(presenter.fleet,getString(R.string.tickets)),
                FleetDetailMapFragment.getInstance(presenter.fleet,getString(R.string.map)),
                FleetDetailVehicleFragment.getInstance(presenter.fleet,getString(R.string.vehicles))
            )
        )

    }

    override fun onUserSuccess() {
        iv_profile_settings.isClickable = true
    }

    override fun onLogOutSuccessfull() {
        authenticateAccount()
        finishMe()
    }



    override fun onLogOutFailure() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_USER_PROFILE && data != null &&
            data.hasExtra(PopUpActivity.POSITIVE_LEVEL) &&
            data.getIntExtra(PopUpActivity.POSITIVE_LEVEL, -1)==1){
            Observable.timer(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    presenter.logOut()
                }, {

                })
        }
    }


    fun showUserProfilePopUp(){
        launchPopUpActivity(
            REQUEST_CODE_USER_PROFILE,
            presenter.user?.email + "\n" + presenter.user?.firstName + " "+presenter.user?.lastName,
            null,
            null,
            getString(R.string.log_out),
            null,
            null,
            getString(R.string.cancel)
        )
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}