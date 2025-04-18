package com.lattis.lattis.presentation.fleet


import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lattis.lattis.presentation.fleet.add.EmailSecretCodeVerificationActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_private_fleets.*
import kotlinx.android.synthetic.main.activity_private_fleets_fleet_list.*
import kotlinx.android.synthetic.main.activity_private_fleets_no_fleets.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PrivateFleetActivity : BaseActivity<PrivateFleetActivityPresenter, PrivateFleetActivityView>(),
    PrivateFleetActivityView {



    private val REQUEST_GENERAL_ERROR = 4393
    private val REQUEST_ADD_FLEET = 4394
    private val NO_FLEET =0
    private val FLEET_LIST =1

    @Inject
    override lateinit var presenter: PrivateFleetActivityPresenter
    override val activityLayoutId = R.layout.activity_private_fleets
    override var view: PrivateFleetActivityView = this


    override fun configureViews() {
        super.configureViews()
        fetchPrivateFleetList()
        configureClicks()
    }

    fun configureClicks(){
        iv_close_in_private_fleets_no_fleets.setOnClickListener {
            finishWithCondition()
        }

        iv_close_in_private_fleets_list.setOnClickListener {
            finishWithCondition()
        }

        btn_add_private_fleets_no_fleets.setOnClickListener {
            EmailSecretCodeVerificationActivity.launchForResult(
                this,
                REQUEST_ADD_FLEET,
                presenter.fleetPresent()
            )
        }

        btn_add_private_fleets_fleets_list.setOnClickListener {
            EmailSecretCodeVerificationActivity.launchForResult(
                this,
                REQUEST_ADD_FLEET,
                presenter.fleetPresent()
            )
        }
    }

    fun finishWithCondition(){
        if(presenter.attemptToAddPrivateFleet)setResult(RESULT_OK)
        finish()
    }

    fun fetchPrivateFleetList(){
        showLoadingForPrivateFleet(getString(R.string.loading))
        presenter.getUserProfile()
    }

    override fun showNoPrivateFleetView(){
        if(view_flipper_in_private_fleet.displayedChild != NO_FLEET)
            view_flipper_in_private_fleet.displayedChild = NO_FLEET

    }

    override fun showPrivateFleetListView() {
        if (view_flipper_in_private_fleet.displayedChild != FLEET_LIST)
            view_flipper_in_private_fleet.displayedChild = FLEET_LIST

        rv_fleets_in_private_fleets_list.setLayoutManager(LinearLayoutManager(this))
        rv_fleets_in_private_fleets_list.setAdapter(PrivateFleetListAdapter(this, presenter.user?.privateNetworks!!))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ADD_FLEET &&
                resultCode == Activity.RESULT_OK){
                    showLoadingForPrivateFleet(getString(R.string.loading))
//                    Observable.timer(5000,TimeUnit.MILLISECONDS)
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({
                            presenter.attemptToAddPrivateFleet = true
                            fetchPrivateFleetList()
//                        },{
//                            hideLoadingForPrivateFleet()
//                        })

        }
    }

    //// loading :start
    override fun showLoadingForPrivateFleet(message: String?) {
        private_fleet_activity_loading_view.visibility = (View.VISIBLE)
        private_fleet_activity_loading_view.ct_loading_title.text = (message)
    }

    override fun hideLoadingForPrivateFleet() {
        private_fleet_activity_loading_view.visibility = (View.GONE)
    }

    override fun onProfileFetchError() {
        showServerGeneralError(REQUEST_GENERAL_ERROR)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

}