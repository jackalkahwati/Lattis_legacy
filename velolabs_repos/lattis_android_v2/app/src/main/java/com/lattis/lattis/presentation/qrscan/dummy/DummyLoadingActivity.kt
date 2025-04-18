package com.lattis.lattis.presentation.qrscan.dummy

import android.os.Bundle
import android.os.PersistableBundle
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_dummy.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import javax.inject.Inject


class DummyLoadingActivity : BaseActivity<DummyLoadingPresenter,DummyLoadingView>(),DummyLoadingView {
    @Inject
    override lateinit var presenter: DummyLoadingPresenter
    override val activityLayoutId = R.layout.activity_dummy
    override var view: DummyLoadingView = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dummy_activity_loading_operation_view.ct_loading_title.text = getString(R.string.starting_ride_loader)
        presenter.subscribeToDummyScreenTimer(true)
    }

    override fun onDummyScreenTimer() {
        finish()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.subscribeToDummyScreenTimer(false)
    }
}