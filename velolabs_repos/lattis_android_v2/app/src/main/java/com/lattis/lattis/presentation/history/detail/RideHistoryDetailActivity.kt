package com.lattis.lattis.presentation.history.detail

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.lattis.domain.models.RideHistory
import com.lattis.lattis.presentation.bikelist.BikeListFragmentPresenter
import com.lattis.lattis.presentation.ride.RideSummaryTaxesAdapter
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.presentation.utils.CurrencyUtil
import com.lattis.lattis.presentation.utils.MapboxUtil
import com.lattis.lattis.utils.UtilsHelper
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_ride_history_details.*
import javax.inject.Inject

class RideHistoryDetailActivity : BaseActivity<RideHistoryDetailActivityPresenter, RideHistoryDetailActivityView>(),
    RideHistoryDetailActivityView, OnMapReadyCallback {
    private val REQUEST_CODE_ERROR = 4393


    companion object{

        val RIDE_HISTORY_DATA = "ride_history_DATA"
        fun getIntent(context: Context, rideHistoryData: RideHistory.RideHistoryData): Intent {
            val intent = Intent(context, RideHistoryDetailActivity::class.java)
            intent.putExtra(RIDE_HISTORY_DATA, Gson()
                .toJson(rideHistoryData))
            return intent
        }
    }

    @Inject
    override lateinit var presenter: RideHistoryDetailActivityPresenter
    override val activityLayoutId = R.layout.activity_ride_history_details
    override var view: RideHistoryDetailActivityView = this

    private var mapboxMap: MapboxMap? = null

    override fun configureViews() {
        super.configureViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapview_in_ride_history.onCreate(savedInstanceState)
        mapview_in_ride_history.getMapAsync(this)

        iv_close_in_ride_history_detail.setOnClickListener {
            finish()
        }

    }


    override fun onResume() {
        super.onResume()
        mapview_in_ride_history.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapview_in_ride_history.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapview_in_ride_history.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapview_in_ride_history.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapview_in_ride_history.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapview_in_ride_history.onDestroy()
    }


    override fun onMapReady(mapboxMapInReady: MapboxMap) {
        mapboxMap = mapboxMapInReady
        mapboxMap?.uiSettings?.isRotateGesturesEnabled = false
        mapboxMap?.uiSettings?.isCompassEnabled = false


        mapboxMap?.setStyle(Style.LIGHT) { style ->

            val icon_start_drawable = BitmapFactory.decodeResource(
                resources,
                R.drawable.map_start_icon
            )
            mapboxMap?.style!!.addImage("icon_start_drawable", icon_start_drawable)


            val icon_end_drawable = BitmapFactory.decodeResource(
                resources,
                R.drawable.map_end_icon
            )
            mapboxMap?.style!!.addImage("icon_end_drawable", icon_end_drawable)

            startShowingRideHistory()


        }
    }


    fun startShowingRideHistory(){

        if(presenter.rideHistoryData!=null){


            if (presenter.rideHistoryData?.date_created != null) {
                ride_history_date_label.setText(
                    UtilsHelper.getDateCurrentTimeZone(
                        this,
                        presenter.rideHistoryData?.date_created!!.toLong()
                    )
                )
            }



            if (presenter.rideHistoryData?.steps != null && presenter.rideHistoryData?.steps?.size!! > 0) {
                val latLngBounds =
                    LatLngBounds.Builder()


                var startLatLng:LatLng?=null
                if(presenter.rideHistoryData?.steps!![0].size>1) {
                    startLatLng = LatLng(
                        presenter.rideHistoryData?.steps!![0][0],
                        presenter.rideHistoryData?.steps!![0][1]
                    )
                }


                var endLatLng:LatLng?=null
                if(presenter.rideHistoryData?.steps!![presenter.rideHistoryData?.steps?.size!! - 1].size>1) {
                    endLatLng = LatLng(
                        presenter.rideHistoryData?.steps!![presenter.rideHistoryData?.steps?.size!! - 1][0],
                        presenter.rideHistoryData?.steps!![presenter.rideHistoryData?.steps?.size!! - 1][1]
                    )
                }

                if(startLatLng!=null && endLatLng!=null) {

                    latLngBounds.include(
                        startLatLng
                    )

                    latLngBounds.include(
                        endLatLng
                    )

                    presenter.setMarkerData(startLatLng, endLatLng)
                    startMapboxWork()
                    mapboxMap!!.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            latLngBounds.build(),
                            100
                        )
                    )
                }
            }


            val duration = UtilsHelper.getTimeFromDuration(presenter.rideHistoryData?.duration)
            ride_history_duration_in_strip_value.text = duration

            ride_history_total_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency,UtilsHelper.getDotAfterNumber(presenter.rideHistoryData?.total))


            val metered_charges=  if(presenter.rideHistoryData?.charge_for_duration==null) "0" else presenter.rideHistoryData?.charge_for_duration!!
            val penaltyCharges = if(presenter.rideHistoryData?.penalty_fees==null) "0" else presenter.rideHistoryData?.penalty_fees!!
            val deposit = if(presenter.rideHistoryData?.deposit==null) 0 else presenter.rideHistoryData?.deposit!!.toInt()
            val over_usage_charges = if(presenter.rideHistoryData?.over_usage_fees ==null) "0" else presenter.rideHistoryData?.over_usage_fees!!
            val unlock_charges = if(presenter.rideHistoryData?.price_for_bike_unlock == null) "0" else presenter.rideHistoryData?.price_for_bike_unlock!!


            if(metered_charges!="0"){
                ride_history_metered_charges_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency, UtilsHelper.getDotAfterNumber(metered_charges.toString()))
            }else{
                ride_history_metered_charges_label.visibility = View.GONE
                ride_history_metered_charges_value.visibility = View.GONE
            }

            if(penaltyCharges!="0"){
                ride_history_parking_fee_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency, UtilsHelper.getDotAfterNumber(penaltyCharges.toString()))
            }else{
                ride_history_parking_fee_label.visibility = View.GONE
                ride_history_parking_fee_value.visibility = View.GONE
            }

            if(unlock_charges!="0"){
                ride_history_unlock_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency,UtilsHelper.getDotAfterNumber(unlock_charges.toString()))
            }else{
                ride_history_unlock_label.visibility = View.GONE
                ride_history_unlock_value.visibility = View.GONE
            }

            if(over_usage_charges!="0"){
                ride_history_surcharge_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency,UtilsHelper.getDotAfterNumber(over_usage_charges.toString()))
            }else{
                ride_history_surcharge_label.visibility = View.GONE
                ride_history_surcharge_value.visibility = View.GONE
            }


            displayStartAndEndAddresses()
//            if(presenter.rideHistoryData?.price_for_membership!=null && presenter.rideHistoryData?.price_type_value!=null && presenter.rideHistoryData?.price_type!=null) {
//                ride_history_trip_price_value.text =
//                    CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency) + presenter.rideHistoryData?.price_for_membership
//                        .toString() + " " + getString(R.string.label_per) + " " +
//                            presenter.rideHistoryData?.price_type_value
//                                .toString() + LocaleTranslatorUtils.getLocaleString(
//                        this,
//                        presenter.rideHistoryData?.price_type
//                    ).toString()
//            }else{
//                ride_history_trip_price_value.visibility = View.GONE
//                ride_history_trip_price_label.visibility = View.GONE
//            }


            if(presenter.rideHistoryData?.membership_discount!=null && !presenter.rideHistoryData?.membership_discount.equals("0")){
                ct_membership_discount_value_in_ride_history.visibility = View.VISIBLE
                ct_membership_discount_label_in_ride_history.visibility = View.VISIBLE
                ct_membership_discount_value_in_ride_history.text = "-" + CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency,presenter.rideHistoryData?.membership_discount!!)
            }else{
                ct_membership_discount_label_in_ride_history.visibility = View.GONE
                ct_membership_discount_value_in_ride_history.visibility = View.GONE
            }


            if(presenter.rideHistoryData?.promo_code_discount!=null && !presenter.rideHistoryData?.promo_code_discount.equals("0")){
                ct_promotion_label_in_ride_history.visibility = View.VISIBLE
                ct_promotion_value_in_ride_history.visibility = View.VISIBLE
                ct_promotion_value_in_ride_history.text = "-" + CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency,presenter.rideHistoryData?.promo_code_discount!!)
            }else{
                ct_promotion_label_in_ride_history.visibility = View.GONE
                ct_promotion_value_in_ride_history.visibility = View.GONE
            }


            if(presenter.rideHistoryData?.taxes!=null && presenter.rideHistoryData?.taxes?.size!!>0){
                rv_taxes_in_ride_history.visibility = View.VISIBLE
                rv_taxes_in_ride_history.setLayoutManager(
                    LinearLayoutManager(this)
                )
                rv_taxes_in_ride_history.setAdapter(
                    RideSummaryTaxesAdapter(this, presenter.rideHistoryData?.taxes!!,presenter.rideHistoryData?.currency)
                )
            }else{
                rv_taxes_in_ride_history.visibility = View.GONE
            }

            if(presenter.rideHistoryData?.refunds!=null && presenter.rideHistoryData?.refunds?.size!!>0){
                var totalRefundAmount =0.0F
                for(refund in presenter.rideHistoryData?.refunds!!){
                    if(!TextUtils.isEmpty(refund.amount_refunded)){
                        var refundAmount=0.0F
                        if(refund.amount_refunded?.toDoubleOrNull()!=null){
                            refundAmount = refund.amount_refunded?.toDouble()!!.toFloat()
                        }else if(refund.amount_refunded?.toFloatOrNull()!=null){
                            refundAmount = refund.amount_refunded?.toFloat()!!
                        }else if(refund.amount_refunded?.toIntOrNull()!=null){
                            refundAmount = refund.amount_refunded?.toInt()!!.toFloat()
                        }
                        totalRefundAmount = totalRefundAmount + refundAmount
                    }
                }
                ct_refunds_value_in_ride_history.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideHistoryData?.currency,totalRefundAmount.toString())
                ct_refunds_label_in_ride_history.visibility = View.VISIBLE
                ct_refunds_value_in_ride_history.visibility = View.VISIBLE
            }else{
                ct_refunds_label_in_ride_history.visibility = View.GONE
                ct_refunds_value_in_ride_history.visibility = View.GONE
            }



        }
    }

    private fun displayStartAndEndAddresses() {
        if (presenter.rideHistoryData?.steps != null && presenter.rideHistoryData?.steps?.size!! > 0) {
            if (presenter.rideHistoryData?.start_address != null && !presenter.rideHistoryData?.start_address!!
                    .isEmpty()
            ) {
                ct_start_address.setText(presenter.rideHistoryData?.start_address)
            }
            if (presenter.rideHistoryData?.end_address == null || !presenter.rideHistoryData?.end_address!!
                    .isEmpty()
            ) {
                ct_end_address.setText(presenter.rideHistoryData?.end_address)
            }
        }
    }




    fun startMapboxWork(){
        setUpSource()
        setUpLayer(mapboxMap?.style!!)
    }

    fun setUpSource(){
        removeLayerAndSource()
        val geoJsonSource= GeoJsonSource(
            MapboxUtil.MARKER_SOURCE, presenter.featureCollection,
            GeoJsonOptions()
        )
        mapboxMap?.style!!.addSource(geoJsonSource!!)

    }
    fun removeLayerAndSource(){
        mapboxMap?.style?.removeSource(MapboxUtil.MARKER_SOURCE)
    }


    private fun setUpLayer(@NonNull loadedMapStyle: Style) {
        loadedMapStyle.addLayer(
            SymbolLayer(MapboxUtil.MARKER_LAYER, MapboxUtil.MARKER_SOURCE)
                .withProperties(
                    PropertyFactory.iconImage("{poi}"),  /* allows show all icons */
                    PropertyFactory.iconAllowOverlap(true), /* when feature is in selected state, grow icon */
                    PropertyFactory.iconIgnorePlacement(true),
                    PropertyFactory.iconOffset(
                        arrayOf(0f, -9f)
                    ),
                    PropertyFactory.iconSize(
                        Expression.match(
                            Expression.toString(Expression.get(BikeListFragmentPresenter.BIKE_SELECTED)), // property selected is a number
                            Expression.literal(1.0f),        // default value
                            Expression.stop("false", 1.0),
                            Expression.stop(
                                "true",
                                1.0
                            )         // if selected set it to original size
                        )
                    )
                )
        )
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}