package com.lattis.data.repository.implementation.api

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Looper
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.*
import com.lattis.data.mapper.LocationMapper
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.domain.repository.LocationRepository
import com.lattis.domain.models.Location
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class LocationRepositoryImp @Inject constructor(
    private val context: Context,
    private val locationMapper: LocationMapper,
    @param:Named("GoogleApiKey")private val googleApiKey:String
) : LocationCallback(), LocationRepository, ConnectionCallbacks, OnConnectionFailedListener {
    private val subject = PublishSubject.create<Location>()
    private val locationSettingsSubject: PublishSubject<com.lattis.domain.models.LocationSettingsResult> = PublishSubject.create()
    private var subscriberCount = 0
    private var googleApiClient: GoogleApiClient? = null
    private val placesClient: PlacesClient
    private var locationRequest: LocationRequest? = null
    private var currentLocation: android.location.Location? = null
    private val fusedLocationProviderClient : FusedLocationProviderClient?

    override fun getLocationUpdates(freshLocationData:Boolean): Observable<Location> {
        return subject.doOnSubscribe { disposable: Disposable ->
            if (subscriberCount == 0 || freshLocationData) {
                startLocationUpdates()
            }
            subscriberCount += 1

            currentLocation = if(freshLocationData) null else currentLocation

            if (currentLocation != null) {
                Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { aLong: Long ->
                            subject.onNext(
                                locationMapper.mapIn(currentLocation)
                            )
                        }
                    ) { error: Throwable ->
                        Log.e(TAG,"Error: "+error.message)
                    }
            }

        }.doOnDispose {
            if (subscriberCount > 0) {
//                stopLocationUpdates()
                subscriberCount -= 1
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) { //        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
            currentLocation = location
            if (currentLocation != null) {
                subject.onNext(locationMapper.mapIn(currentLocation))
            }
        }
//        if (subscriberCount > 0) {
            startLocationUpdates()
//        }
    }

    override fun onConnectionSuspended(i: Int) {
        if (googleApiClient != null) {
            googleApiClient?.connect()
        }
//        locationUpdateStarted=false
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(
            TAG,
            "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.errorCode
        )
//        locationUpdateStarted=false
        subject.onError(Throwable(connectionResult.errorMessage))
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (googleApiClient != null && googleApiClient?.isConnected()?:false) {
//            locationUpdateStarted=true
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                this,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(this)
    }

    override fun onLocationResult(locationResult: LocationResult) {
        if (locationResult == null) {
            return
        }
        for (location in locationResult.locations) {
            if (location != null) {
                currentLocation = location
                subject.onNext(locationMapper.mapIn(location))
            }
        }
    }

    //this is the key ingredient to show dialog always when GPS is off
    override fun getLocationSettings(): Observable<com.lattis.domain.models.LocationSettingsResult>
        {
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest!!)
            builder.setAlwaysShow(true) //this is the key ingredient to show dialog always when GPS is off
            val task =
                LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())
            task.addOnSuccessListener {
                val locationSettingsResult = com.lattis.domain.models.LocationSettingsResult()
                locationSettingsResult.status = LocationSettingsStatusCodes.SUCCESS
                locationSettingsSubject.onNext(locationSettingsResult)
            }
            task.addOnFailureListener { e ->
                val locationSettingsResult = com.lattis.domain.models.LocationSettingsResult()
                locationSettingsResult.status = ((e as ApiException).statusCode)
                locationSettingsResult.apiException = (e)
                locationSettingsSubject.onNext(locationSettingsResult)
            }
            return locationSettingsSubject
        }

    override fun getPlaces(constraint: String): Observable<ArrayList<PlaceAutocomplete>> {
        return Observable.create<ArrayList<PlaceAutocomplete>> { emitter: ObservableEmitter<ArrayList<PlaceAutocomplete>> ->
            val token = AutocompleteSessionToken.newInstance()
            val bounds =
                RectangularBounds.newInstance(BOUNDS_WORLD)
            val request =
                FindAutocompletePredictionsRequest.builder() //                    .setLocationBias(bounds)
//                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .setQuery(constraint)
                    .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    val resultList: ArrayList<PlaceAutocomplete> =
                        ArrayList<PlaceAutocomplete>(response.autocompletePredictions.size)
                    for (prediction in response.autocompletePredictions) {
                        val placeAutocomplete = PlaceAutocomplete()
                        placeAutocomplete.placeId = (prediction.placeId)
                        placeAutocomplete.address1 = (
                            prediction.getPrimaryText(
                                STYLE_BOLD
                            )
                        )
                        placeAutocomplete.address2 =(
                            prediction.getSecondaryText(
                                STYLE_BOLD
                            )
                        )
                        resultList.add(placeAutocomplete)
                    }
                    emitter.onNext(resultList)
                }.addOnFailureListener { exception: Exception? ->
                    emitter.onError(
                        Throwable("MATCH_NOT_FOUND")
                    )
                }
        }
    }

    override fun getPlaceBuffer(placeId: String): Observable<Place> {
        return Observable.create { emitter: ObservableEmitter<Place> ->
            val placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
            val request = FetchPlaceRequest.newInstance(placeId!!, placeFields)
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    Log.e(
                        TAG,
                        "Place found: " + place.name
                    )
                    emitter.onNext(place)
                }
                .addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        // Handle error with given status code.
                        Log.e(
                            TAG,
                            "Place not found: " + exception.message
                        )
                    }
                    emitter.onError(Throwable("MATCH_NOT_FOUND"))
                }
        }
    }

    companion object {
        private val TAG = LocationRepositoryImp::class.java.simpleName
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
        private val BOUNDS_WORLD =
            LatLngBounds(
                LatLng((-0).toDouble(), 0.0),
                LatLng(0.0, 0.0)
            )
        private val STYLE_BOLD: CharacterStyle = StyleSpan(Typeface.NORMAL)
    }

    init {
        googleApiClient = GoogleApiClient.Builder(context)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        Places.initialize(context, googleApiKey)
        placesClient = Places.createClient(context)
        googleApiClient?.connect()
        locationRequest = LocationRequest()
        locationRequest?.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        locationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest?.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context) //initiate in onCreate
    }
}