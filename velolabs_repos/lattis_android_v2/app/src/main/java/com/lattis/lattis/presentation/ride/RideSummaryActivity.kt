package com.lattis.lattis.presentation.ride

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.lattis.domain.models.RideSummary
import com.lattis.lattis.presentation.bikelist.BikeListFragmentPresenter
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
import kotlinx.android.synthetic.main.activity_ride_summary.*
import javax.inject.Inject

class RideSummaryActivity : BaseActivity<RideSummaryPresenter, RideSummaryView>(),RideSummaryView,
    OnMapReadyCallback {

    override val activityLayoutId = R.layout.activity_ride_summary;
    override var view: RideSummaryView = this
    @Inject
    override lateinit var presenter: RideSummaryPresenter

    private var mapboxMap: MapboxMap? = null
    private var ratings = -1

    companion object{
        fun getIntent(context: Context,rideSummary: RideSummary): Intent {
            val intent = Intent(context, RideSummaryActivity::class.java)
            intent.putExtra(BikeBookedOrActiveRideFragment.RIDE_SUMMARY_DATA,rideSummary)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapview_in_ride_summary.onCreate(savedInstanceState)
        mapview_in_ride_summary.getMapAsync(this)
        configureClicks()
    }

    fun configureClicks(){
        rate1.setOnClickListener {
            ratings=1
            onRatingClicked()
        }
        rate2.setOnClickListener {
            ratings=2
            onRatingClicked()
        }
        rate3.setOnClickListener {
            ratings=3
            onRatingClicked()
        }
        rate4.setOnClickListener {
            ratings=4
            onRatingClicked()
        }
        rate5.setOnClickListener {
            ratings=5
            onRatingClicked()
        }

        cl_next_ride_summary.setOnClickListener {
            if(ratings!=-1){
                pb_progress_in_ride_summary.visibility= View.VISIBLE
                btn_next_ride_summary.visibility = View.GONE
                presenter.rateTheRide(ratings)
            }else{
                finish()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        mapview_in_ride_summary.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapview_in_ride_summary.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapview_in_ride_summary.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapview_in_ride_summary.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapview_in_ride_summary.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapview_in_ride_summary.onDestroy()
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

            startShowingRideSummary()


        }
    }


    fun startShowingRideSummary(){

        if(presenter.rideSummary!=null){


            if (presenter.rideSummary?.date_created != null) {
                ride_summary_date_label.setText(
                    UtilsHelper.getDateCurrentTimeZone(
                        this,
                        presenter.rideSummary?.date_created!!.toLong()
                    )
                )
            }



            if (presenter.rideSummary?.steps != null && presenter.rideSummary?.steps?.size!! > 0) {
                val latLngBounds =
                    LatLngBounds.Builder()

                var startLatLng:LatLng?=null
                if(presenter.rideSummary?.steps!![0].size>1) {
                    startLatLng = LatLng(
                        presenter.rideSummary?.steps!![0][0],
                        presenter.rideSummary?.steps!![0][1]
                    )
                }



                var endLatLng:LatLng?=null
                if(presenter.rideSummary?.steps!![presenter.rideSummary?.steps?.size!! - 1].size>1) {
                    endLatLng = LatLng(
                        presenter.rideSummary?.steps!![presenter.rideSummary?.steps?.size!! - 1][0],
                        presenter.rideSummary?.steps!![presenter.rideSummary?.steps?.size!! - 1][1]
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



            val duration = UtilsHelper.getTimeFromDuration(presenter.rideSummary?.duration)
            ride_summary_duration_in_strip_value.text = duration

            ride_summary_total_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency,UtilsHelper.getDotAfterNumber(presenter.rideSummary?.total))


            val metered_charges=  if(presenter.rideSummary?.charge_for_duration==null) "0" else presenter.rideSummary?.charge_for_duration!!
            val penaltyCharges = if(presenter.rideSummary?.penalty_fees==null) "0" else presenter.rideSummary?.penalty_fees!!
            val deposit = if(presenter.rideSummary?.deposit==null) 0 else presenter.rideSummary?.deposit!!.toInt()
            val over_usage_charges = if(presenter.rideSummary?.over_usage_fees ==null) "0" else presenter.rideSummary?.over_usage_fees!!
            val unlock_charges = if(presenter.rideSummary?.price_for_bike_unlock ==null) "0" else presenter.rideSummary?.price_for_bike_unlock!!


            ride_summary_trip_price_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency,UtilsHelper.getDotAfterNumber(metered_charges))

            if(over_usage_charges=="0"){
                ride_summary_surcharge_label.visibility = View.GONE
                ride_summary_surcharge_value.visibility = View.GONE
            }else{
                ride_summary_surcharge_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency,UtilsHelper.getDotAfterNumber(over_usage_charges))
            }

            if(unlock_charges=="0"){
                ride_summary_unlock_label.visibility = View.GONE
                ride_summary_unlock_value.visibility = View.GONE
            }else{
                ride_summary_unlock_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency,UtilsHelper.getDotAfterNumber(unlock_charges))
            }


            if(penaltyCharges=="0"){
                ride_summary_parking_label.visibility = View.GONE
                ride_summary_parking_value.visibility = View.GONE
            }else{
                ride_summary_parking_value.text = CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency,UtilsHelper.getDotAfterNumber(penaltyCharges))
            }




//            if(presenter.rideSummary?.price_for_membership!=null && presenter.rideSummary?.price_type_value!=null && presenter.rideSummary?.price_type!=null) {
//                ride_summary_trip_price_value.text =
//                    CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency) + presenter.rideSummary?.price_for_membership
//                        .toString() + " " + getString(R.string.label_per) + " " +
//                            presenter.rideSummary?.price_type_value
//                                .toString() + LocaleTranslatorUtils.getLocaleString(
//                        this,
//                        presenter.rideSummary?.price_type
//                    ).toString()
//            }else{
//                ride_summary_trip_price_value.visibility = View.GONE
//                ride_summary_trip_price_label.visibility = View.GONE
//            }


            if(presenter.rideSummary?.membership_discount!=null && !presenter.rideSummary?.membership_discount.equals("0")){
                ct_membership_discount_label_in_ride_summary.visibility = VISIBLE
                ct_membership_discount_value_in_ride_summary.visibility = VISIBLE
                ct_membership_discount_value_in_ride_summary.text = "-" + CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency,presenter.rideSummary?.membership_discount!!)
            }else{
                ct_membership_discount_label_in_ride_summary.visibility = GONE
                ct_membership_discount_value_in_ride_summary.visibility = GONE
            }


            if(presenter.rideSummary?.promo_code_discount!=null && !presenter.rideSummary?.promo_code_discount.equals("0")){
                ct_promotion_label_in_ride_summary.visibility = VISIBLE
                ct_promotion_value_in_ride_summary.visibility = VISIBLE
                ct_promotion_value_in_ride_summary.text = "-" + CurrencyUtil.getCurrencySymbolByCode(presenter.rideSummary?.currency,presenter.rideSummary?.promo_code_discount!!)
            }else{
                ct_promotion_label_in_ride_summary.visibility = GONE
                ct_promotion_value_in_ride_summary.visibility = GONE
            }


            if(presenter.rideSummary?.taxes!=null && presenter.rideSummary?.taxes?.size!!>0){
                rv_taxes_in_ride_summary.visibility = VISIBLE
                rv_taxes_in_ride_summary.setLayoutManager(
                    LinearLayoutManager(this)
                )
                rv_taxes_in_ride_summary.setAdapter(
                    RideSummaryTaxesAdapter(this, presenter.rideSummary?.taxes!!,presenter.rideSummary?.currency)
                )
            }else{
                rv_taxes_in_ride_summary.visibility = GONE
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
            GeoJsonOptions())
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


    fun onRatingClicked() {
        if (ratings ==1 ) {
                rate1.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
                rate2.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
                rate3.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
                rate4.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
                rate5.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
        } else if (ratings ==2) {
            rate1.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate2.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate3.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
            rate4.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
            rate5.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
        } else if (ratings ==3) {
            rate1.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate2.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate3.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate4.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
            rate5.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
        } else if (ratings === 4) {
            rate1.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate2.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate3.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate4.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate5.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_unselected));
        } else if (ratings === 5) {
            rate1.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate2.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate3.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate4.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
            rate5.setColorFilter(ContextCompat.getColor(this, R.color.ride_summary_star_selected));
        }
    }

    override fun onRideRatingSuccess() {
        finish()
    }

    override fun onRideRatingFailure() {
        finish()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}