package io.lattis.operator.presentation.vehicle.fragments.tickets

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.fragment.BaseTabFragment
import io.lattis.operator.presentation.fleet.fragments.tickets.FleetDetailTicketAdapter
import io.lattis.operator.presentation.fleet.fragments.tickets.FleetDetailTicketListener
import io.lattis.operator.presentation.ticket.CreateTicketActivity
import io.lattis.operator.presentation.ticket.CreateTicketActivity.Companion.CREATE_TICKET_RETURN_TICKET
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity.Companion.TAB_TITLE
import kotlinx.android.synthetic.main.fragment_vehicle_detail_ticket.*
import javax.inject.Inject

class VehicleDetailTicketsFragment : BaseTabFragment<VehicleDetailTicketsFragmentPresenter, VehicleDetailTicketsFragmentView>()
        , VehicleDetailTicketsFragmentView,FleetDetailTicketListener {
    @Inject
    override lateinit var presenter: VehicleDetailTicketsFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_vehicle_detail_ticket;
    override var view: VehicleDetailTicketsFragmentView = this

    val REQUEST_CODE_CREATE_TICKET_ACTIVITY = 201
    val REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY = 202

    override fun getTitle(): String {
        return arguments?.getString(VehicleDetailActivity.TAB_TITLE,"Tickets")!!
    }

    companion object{
        fun getInstance(vehicle: Vehicle,tabTitle:String): BaseTabFragment<*, *> {
            val bundle = Bundle()
            bundle.putString(VehicleDetailActivity.VEHICLE, Gson().toJson(vehicle))
            bundle.putString(TAB_TITLE,tabTitle)
            val fragment = VehicleDetailTicketsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun configureViews() {
        super.configureViews()
        showProgressLoading()
        btn_create_ticket_vehicle_detail_ticket.setOnClickListener {
            startActivityForResult(CreateTicketActivity.getIntent(requireContext(),presenter.vehicle?.fleet?.id!!,
                    presenter.vehicle,
                    presenter.user,
                    presenter.colleagues),
                    REQUEST_CODE_CREATE_TICKET_ACTIVITY)

        }

        iv_filter_in_vehicle_detail_ticket.setOnClickListener {
            showAssigneeEditDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_CREATE_TICKET_ACTIVITY &&
                resultCode == RESULT_OK &&
                data!=null &&
                data.hasExtra(CREATE_TICKET_RETURN_TICKET)){
            getTickets()
        }else if (requestCode == REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY){
            presenter.filteredColleague = null
            getTickets()
        }
    }

    override fun onTicketsSuccess() {
        hideProgressLoading()
        rv_tickets_in_vehicle_detail_ticket.setLayoutManager(LinearLayoutManager(requireContext()))
        rv_tickets_in_vehicle_detail_ticket.setAdapter(FleetDetailTicketAdapter(requireContext(),this, presenter.tickets!!))
    }

    override fun onTicketFailure() {
        hideProgressLoading()
    }

    fun showProgressLoading(){
        if(fragment_vehicle_detail_ticket_loading!=null){
            fragment_vehicle_detail_ticket_loading.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(fragment_vehicle_detail_ticket_loading!=null){
            fragment_vehicle_detail_ticket_loading.visibility= View.GONE
        }
    }

    override fun onTicketClicked(position: Int) {
        startVehicleDetailActivity(presenter.tickets?.get(position)!!)
    }

    fun startVehicleDetailActivity(ticket: Ticket){
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
                getTickets()
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    fun getTickets(){
        showProgressLoading()
        presenter.getTickets()
    }
}