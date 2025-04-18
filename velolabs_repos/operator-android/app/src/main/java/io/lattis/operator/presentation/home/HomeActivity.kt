package io.lattis.operator.presentation.home

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import io.lattis.domain.models.Fleet
import io.lattis.operator.R
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_create_ticket_search.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_loading.*
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject

class HomeActivity : BaseActivity<HomeActivityPresenter, HomeActivityView>(),
    HomeActivityView, FleetClickListener{
    @Inject
    override lateinit var presenter: HomeActivityPresenter
    override val activityLayoutId = R.layout.activity_home
    override var view: HomeActivityView = this
    private var adapter:FleetsAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun configureViews() {
        super.configureViews()
        toolbar_title.text = getString(R.string.select_fleet)
        setSupportActionBar(toolbar)
        showProgressLoading()
        presenter.getFleets()

        search_bar_home.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if(!TextUtils.isEmpty(query)){

                }
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                presenter.filter(newText)
                return false
            }


        })
    }

    override fun showFilteredList(fleets:ArrayList<Fleet>) {
        adapter?.setFilteredList(fleets)
    }

    override fun showOriginalList() {
        adapter?.setFilteredList(presenter.fleets!!)
    }

    override fun onFleetsSuccess() {
        hideProgressLoading()
        rv_fleets_in_home.setLayoutManager(LinearLayoutManager(this))
        adapter=FleetsAdapter(this, presenter.fleets,this)
        rv_fleets_in_home.setAdapter(adapter)
    }

    override fun onFleetsFailure() {
        hideProgressLoading()
    }

    override fun onFleetClicked(fleet: Fleet) {
        presenter.saveUserFleet(fleet)
    }

    override fun onUserFleetSaveSuccess() {
        openFleetDetailActivity()
    }

    override fun onUserFleetSaveFailure() {
        openFleetDetailActivity()
    }

    fun openFleetDetailActivity(){
        showProgressLoading()
        startActivity(FleetDetailActivity.getIntent(this,presenter.userSavedFleet!!))
        finishMe()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun showProgressLoading(){
        if(home_loading!=null){
            home_loading.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(home_loading!=null){
            home_loading.visibility= View.GONE
        }
    }
}