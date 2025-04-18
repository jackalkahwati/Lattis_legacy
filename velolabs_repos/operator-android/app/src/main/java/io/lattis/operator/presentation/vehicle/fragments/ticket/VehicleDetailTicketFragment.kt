package io.lattis.operator.presentation.vehicle.fragments.ticket

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import com.google.gson.Gson
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.fragment.BaseTabFragment
import io.lattis.operator.presentation.utils.DateTimeUtil.getDateFromUnixTimeStamp
import io.lattis.operator.presentation.utils.DateTimeUtil.ticketCreatedDateFormat
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.utils.ResourceUtils.convertCategory
import kotlinx.android.synthetic.main.fragment_ticket_detail_basic.view.*
import kotlinx.android.synthetic.main.fragment_vehicle_detail_ticket_detail.*
import kotlinx.android.synthetic.main.fragment_vehicle_detail_ticket_operator_note.view.*
import javax.inject.Inject

class VehicleDetailTicketFragment : BaseTabFragment<VehicleDetailTicketFragmentPresenter, VehicleDetailTicketFragmentView>()
    , VehicleDetailTicketFragmentView {
    @Inject
    override lateinit var presenter: VehicleDetailTicketFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_vehicle_detail_ticket_detail;
    override var view: VehicleDetailTicketFragmentView = this


    override fun getTitle(): String {
        return arguments?.getString(VehicleDetailActivity.TAB_TITLE, "Tickets")!!
    }

    companion object{
        fun getInstance(vehicle: Vehicle, ticket:Ticket,tabTitle:String): BaseTabFragment<*, *> {
            val bundle = Bundle()
            bundle.putString(VehicleDetailActivity.VEHICLE, Gson().toJson(vehicle))
            bundle.putString(VehicleDetailActivity.TICKET, Gson().toJson(ticket))
            bundle.putString(VehicleDetailActivity.TAB_TITLE,tabTitle)
            val fragment = VehicleDetailTicketFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun configureViews() {
        super.configureViews()
        configureClicks()
    }

    fun configureClicks(){
        fragment_vehicle_detail_ticket_operator_note.ct_ticket_operator_note_edit_value.setOnClickListener {
            showNotesDialog()
        }

        fragment_ticket_detail_basic.ct_ticket_assign_to_label.setOnClickListener {
            showAssigneeEditDialog()
        }

        btn_resolve_ticket.setOnClickListener {
            resolveTicket()
        }
    }

    override fun startShowingTicketInformation() {
        hideLoading()
        fragment_ticket_detail_basic.ct_ticket_created_value.text = if(presenter.ticket.createdAt!=null)getDateFromUnixTimeStamp(presenter.ticket.createdAt,
            ticketCreatedDateFormat) else ""
        fragment_ticket_detail_basic.ct_ticket_category_value.text = if(!TextUtils.isEmpty(presenter.ticket.category))convertCategory(requireContext(),presenter.ticket.category) else ""

        fragment_vehicle_detail_ticket_operator_note.ct_ticket_operator_note_value.text = if(!TextUtils.isEmpty(presenter.ticket.operatorNotes))presenter.ticket.operatorNotes else ""

    }

    override fun showAssignee(){
        hideLoading()
        fragment_ticket_detail_basic.ct_ticket_assign_to_value.text = if(presenter.assigneeColleague!=null)presenter.assigneeColleague?.firstName + " "+ presenter.assigneeColleague?.lastName else ""
    }

    private fun showNotesDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.notes))
        val input = EditText(requireContext())
        if(TextUtils.isEmpty(presenter.ticket?.operatorNotes)){
            input.setHint("Enter notes")
        }else{
            input.setText(presenter.ticket?.operatorNotes)
        }

        input.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            var notes = input.text.toString()
            editTicketDetails(null,notes)
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }


    private fun showAssigneeEditDialog() {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.assignee))
        var array = presenter.colleagues?.map { it.firstName + " " + it.lastName }
        builder.setSingleChoiceItems(array?.toTypedArray(),-1) { _, which ->
            try {
                editTicketDetails(presenter.colleagues?.get(which)?.id,null)
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    fun showLoading(){
        fragment_vehicle_detail_ticket_detail_loading.visibility = View.VISIBLE
    }

    fun hideLoading(){
        fragment_vehicle_detail_ticket_detail_loading.visibility = View.GONE
    }

    fun editTicketDetails(assignee:Int?,notes:String?){
        showLoading()
        presenter.setAssigneeOrNotesOrResolveTicket(assignee,notes,null)
    }

    fun resolveTicket(){
        showLoading()
        presenter.setAssigneeOrNotesOrResolveTicket(null,null,"resolved")
    }

    override fun onTicketResolved() {
        activity?.finish()
    }
}