package io.lattis.operator.presentation.ticket

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import io.lattis.domain.models.Colleague
import io.lattis.domain.models.User
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.fleet.fragments.vehicles.FleetDetailVehicleAdapter
import io.lattis.operator.presentation.fleet.fragments.vehicles.FleetDetailVehicleListener
import io.lattis.operator.presentation.qrcodescan.ScanQRCodeActivity
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import io.lattis.operator.utils.GeneralUtils
import io.lattis.operator.utils.ResourceUtils.convertCategory
import io.lattis.operator.utils.ResourceUtils.returnCategoryMap
import kotlinx.android.synthetic.main.activity_create_ticket.*
import kotlinx.android.synthetic.main.activity_create_ticket_main.*
import kotlinx.android.synthetic.main.activity_create_ticket_search.*
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject

class CreateTicketActivity : BaseActivity<CreateTicketActivityPresenter, CreateTicketActivityView>(),
        CreateTicketActivityView, FleetDetailVehicleListener {

    @Inject
    override lateinit var presenter: CreateTicketActivityPresenter
    override val activityLayoutId = R.layout.activity_create_ticket
    override var view: CreateTicketActivityView = this

    private val CREATE_MAIN =0
    private val CREATE_SEARCH =1
    val REQUEST_CODE_SCAN_QR_CODE_ACTIVITY = 123

    companion object{
        val VEHICLE = "VEHICLE"
        val OPERATOR = "OPERATOR"
        val COLLEAGUES = "COLLEAGUES"
        val FLEET_ID = "FLEET_ID"
        val CREATE_TICKET_RETURN_TICKET = "CREATE_TICKET_RETURN_TICKET"
        fun getIntent(context: Context,fleet_id:Int, vehicle: Vehicle?, operator: User.Operator?, colleagues:List<Colleague>?): Intent {
            val intent = Intent(context, CreateTicketActivity::class.java)
            intent.putExtra(FLEET_ID, fleet_id)
            intent.putExtra(VEHICLE, Gson().toJson(vehicle))
            intent.putExtra(OPERATOR, Gson().toJson(operator))
            intent.putExtra(COLLEAGUES, Gson().toJson(colleagues))
            return intent
        }
    }

    fun showMain(){
        if(view_flipper_in_create_activity.displayedChild != CREATE_MAIN)
            view_flipper_in_create_activity.displayedChild = CREATE_MAIN
    }

    fun showSearch(){
        if(view_flipper_in_create_activity.displayedChild != CREATE_SEARCH)
            view_flipper_in_create_activity.displayedChild = CREATE_SEARCH
    }

    override fun configureViews() {
        super.configureViews()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.marginEnd = GeneralUtils.dpToPx(this, 72)
        toolbar.layoutParams =params

        toolbar_title.text = getString(R.string.create_ticket)

        btn_create_ticket_in_create_ticket.setOnClickListener {
            showProgressLoading()
            presenter.tryCreatingTicket()
        }


        cl_create_ticket_vehicle.setOnClickListener {
            showVehicleFindingOptions()
        }
        cl_create_ticket_category.setOnClickListener {
            showCategoryDialog()
        }

        cl_create_ticket_notes.setOnClickListener {
            showNotesDialog()
        }

        cl_create_ticket_assignee.setOnClickListener {
            showAssigneeDialog()
        }
        configureSearchBar()
    }

    fun configureSearchBar(){
        search_bar_create_ticket.isSubmitButtonEnabled=true
        search_bar_create_ticket.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if(!TextUtils.isEmpty(query)){
                    showProgressLoading()
                    presenter.name=query
                    presenter.searchVehicles()
                }
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                if(TextUtils.isEmpty(newText)){

                }
                return false
            }

        })
    }

    override fun onVehicleClicked(position: Int) {
        presenter.vehicle = presenter.vehicles?.get(position)!!
        showVehicleInfo()
        showMain()
    }


    private fun showCategoryDialog() {
        val mapOfCategory = returnCategoryMap(this)
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.category))
        val array = mapOfCategory.values.toTypedArray()
        builder.setSingleChoiceItems(array,-1) { _, which ->
            try {
                val key =
                        (mapOfCategory.filterValues { it == array[which] }.keys).toTypedArray().get(0)
                presenter.categoryId = key
                ct_create_ticket_category_value.text = convertCategory(this,key)
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    private fun showAssigneeDialog() {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.assignee))
        val array = presenter.colleagues?.map { it.firstName + " " + it.lastName }
        builder.setSingleChoiceItems(array?.toTypedArray(),-1) { _, which ->
            try {
                presenter.assigneeId = presenter.colleagues?.get(which)?.id
                ct_create_ticket_assignee_value.text = presenter.colleagues?.get(which)?.firstName + " " +presenter.colleagues?.get(which)?.lastName
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    fun showNotesDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.notes))
        val input = EditText(this)
        input.setHint("Enter notes")
        input.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            var notes = input.text.toString()
            ct_create_ticket_notes_value.text = notes
            presenter.notes = notes
            ct_create_ticket_notes_value.visibility = View.VISIBLE
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

    private fun showVehicleFindingOptions() {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        val array = arrayOf(getString(R.string.search),getString(R.string.scan_qr_code))
        builder.setSingleChoiceItems(array,-1) { _, which ->
            try {
                if(which==0){
                    showSearch()
                }else {
                    startActivityForResult(ScanQRCodeActivity.getIntent(this,presenter.fleet_id!!),REQUEST_CODE_SCAN_QR_CODE_ACTIVITY)
                }
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    override fun onVehiclesSearchSuccess() {

        rv_vehicles_in_create_ticket.setLayoutManager(LinearLayoutManager(this))
        rv_vehicles_in_create_ticket.setAdapter(
                FleetDetailVehicleAdapter(
                        this,
                        presenter.vehicles!!,
                        this
                )
        )

        hideProgressLoading()
    }

    override fun onVehiclesSearchFailure() {
        hideProgressLoading()
    }

    override fun showVehicleInfo() {
        ct_create_ticket_vehicle_value.text = presenter.vehicle?.name
    }

    override fun onCreateTicketSuccess() {
        finishWithSuccess()
    }

    override fun onCreateTicketFailure() {
        hideProgressLoading()
    }

    override fun onCreateTicketValidationFailure() {
        hideProgressLoading()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_CODE_SCAN_QR_CODE_ACTIVITY && data!=null &&
                data.hasExtra(ScanQRCodeActivity.VEHICLE_DATA)){
            presenter.vehicle = data.getSerializableExtra(ScanQRCodeActivity.VEHICLE_DATA) as Vehicle
            showVehicleInfo()
        }
    }

    private fun finishWithSuccess() {
        val intent = Intent()
        intent.putExtra(CREATE_TICKET_RETURN_TICKET,presenter.ticket)
        setResult(RESULT_OK,intent)
        finish()
    }

    fun showProgressLoading(){
        if(create_activity_loading_view!=null){
            create_activity_loading_view.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(create_activity_loading_view!=null){
            create_activity_loading_view.visibility= View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}