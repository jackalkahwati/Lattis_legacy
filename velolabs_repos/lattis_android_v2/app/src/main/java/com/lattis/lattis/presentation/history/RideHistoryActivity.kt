package com.lattis.lattis.presentation.history

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lattis.lattis.presentation.fleet.PrivateFleetListAdapter
import com.lattis.lattis.presentation.help.HelpActivityPresenter
import com.lattis.lattis.presentation.help.HelpActivityView
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.presentation.utils.FirebaseUtil
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_private_fleets.*
import kotlinx.android.synthetic.main.activity_ride_history.*
import javax.inject.Inject

class RideHistoryActivity : BaseActivity<RideHistoryActivityPresenter, RideHistoryActivityView>(),
    RideHistoryActivityView {


    private val REQUEST_CODE_ERROR = 4393

    @Inject
    override lateinit var presenter: RideHistoryActivityPresenter
    override val activityLayoutId = R.layout.activity_ride_history
    override var view: RideHistoryActivityView = this

    override fun configureViews() {
        super.configureViews()
        iv_close_in_ride_history.setOnClickListener {
            finish()
        }
        showLoadingForRideHistory(getString(R.string.loading))
        presenter.getRideHistory()
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.RIDE_HISTORY, FirebaseUtil.RIDE_HISTORY)

    }

    override fun onRideHistorySuccess() {
        hideLoadingForRideHistory()
        rv_ride_history.setLayoutManager(LinearLayoutManager(this))
        rv_ride_history.setAdapter(RideHistoryAdapter(this, presenter.rideHistoryData!!))
    }

    override fun onNoRideHistory() {
        hideLoadingForRideHistory()

    }

    override fun onRideHistoryFailure() {
        showServerGeneralError(REQUEST_CODE_ERROR)
    }


    //// loading :start
    fun showLoadingForRideHistory(message: String?) {
        ride_history_activity_loading_view.visibility = (View.VISIBLE)
        ride_history_activity_loading_view.ct_loading_title.text = (message)
    }

    fun hideLoadingForRideHistory() {
        ride_history_activity_loading_view.visibility = (View.GONE)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}