package io.lattis.operator.presentation.fleet.fragments.tickets

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.fragment.BaseTabFragment
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.ticket.CreateTicketActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_fleet_detail_ticket.*
import javax.inject.Inject

class FleetDetailTicketFragment : BaseTabFragment<FleetDetailTicketFragmentPresenter, FleetDetailTicketFragmentView>()
    , FleetDetailTicketFragmentView,FleetDetailTicketListener {
    @Inject
    override lateinit var presenter: FleetDetailTicketFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_fleet_detail_ticket;
    override var view: FleetDetailTicketFragmentView = this
    val REQUEST_CODE_CREATE_TICKET_ACTIVITY = 123
    val REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY = 124

    companion object{
        fun getInstance(fleet: Fleet,tabTitle:String):BaseTabFragment<*,*>{
            val bundle = Bundle()
            bundle.putString(FleetDetailActivity.FLEET, Gson().toJson(fleet))
            bundle.putString(FleetDetailActivity.TAB_TITLE,tabTitle)
            val fragment = FleetDetailTicketFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun getTitle(): String {
        return arguments?.getString(VehicleDetailActivity.TAB_TITLE,"Tickets")!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_CREATE_TICKET_ACTIVITY &&
                resultCode == RESULT_OK &&
                data!=null &&
                data.hasExtra(CreateTicketActivity.CREATE_TICKET_RETURN_TICKET)){
            getTicketsWithProgressLoading()
        }else if (requestCode == REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY){
            getTicketsWithProgressLoading()
        }
    }

    override fun configureViews() {
        super.configureViews()
        getTicketsWithProgressLoading()


        // Configure the refreshing colors
        srl_rv_tickets_in_fleet_details_ticket.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        srl_rv_tickets_in_fleet_details_ticket.setOnRefreshListener {
            getTicketsWithoutProgressLoading()
        }

        btn_create_ticket_fleet_detail.setOnClickListener {
            startCreateTicketActivity()
        }

        iv_filter_in_fleet_details_ticket.setOnClickListener {
            showAssigneeEditDialog()
        }
    }

    override fun onTicketsSuccess() {
        srl_rv_tickets_in_fleet_details_ticket.setRefreshing(false)
        hideProgressLoading()
        rv_tickets_in_fleet_details_ticket.setLayoutManager(LinearLayoutManager(requireContext()))
        rv_tickets_in_fleet_details_ticket.setAdapter(FleetDetailTicketAdapter(requireContext(),this, presenter.tickets!!))
    }

    override fun onTicketFailure() {
        srl_rv_tickets_in_fleet_details_ticket.setRefreshing(false)
        hideProgressLoading()
    }

    fun showProgressLoading(){
        if(fragment_ticket_loading!=null){
            fragment_ticket_loading.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(fragment_ticket_loading!=null){
            fragment_ticket_loading.visibility= View.GONE
        }
    }

    fun startCreateTicketActivity(){
        startActivityForResult(CreateTicketActivity.getIntent(requireContext(),presenter.fleet?.id!!,
                null,
                null,
                null),
                REQUEST_CODE_CREATE_TICKET_ACTIVITY)
    }

    override fun onTicketClicked(position: Int) {
        startVehicleDetailActivity(presenter.tickets?.get(position)!!)
    }

    fun startVehicleDetailActivity(ticket:Ticket){
        showProgressLoading()
        startActivityForResult(VehicleDetailActivity.getIntent(requireContext(),ticket.vehicle!!,ticket),REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY)
    }


    private fun showAssigneeEditDialog() {

        if(presenter.colleagues==null)return

        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.assignee))
        val array = arrayListOf<String>(getString(R.string.clear))
        presenter.colleagues?.map { it.firstName + " " + it.lastName }?.let { array.addAll(it) }
        builder.setSingleChoiceItems(array?.toTypedArray(),-1) { _, which ->
            try {
                if(which==0){
                    presenter.filteredColleague = null
                }else{
                    presenter.filteredColleague = presenter.colleagues?.get(which-1)
                }
                getTicketsWithProgressLoading()
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    fun getTicketsWithProgressLoading(){
        showProgressLoading()
        presenter.getTickets()
    }

    fun getTicketsWithoutProgressLoading(){
        presenter.getTickets()
    }

}