package com.lattis.ellipse.presentation.ui.ride.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.lattis.ellipse.Lattis;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.lock.observe.ObserveConnectionStateUseCase;
import com.lattis.ellipse.domain.interactor.lock.observe.ObserveLockPositionUseCase;
import com.lattis.ellipse.domain.interactor.ride.RideSummaryUseCase;
import com.lattis.ellipse.domain.interactor.ride.UpdateRideUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.model.mapper.LockModelMapper;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.ride.service.util.ServiceAction;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.lattis.ellipse.domain.model.Lock.Connection.Status.GUEST_VERIFIED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.OWNER_VERIFIED;

/**
 * Created by ssd3 on 4/18/17.
 */

public class ActiveTripService extends Service {

    private final String TAG = ActiveTripService.class.getName();
    private int trip_id = 0;
    private int start_id;
    private LockModel lockModel;
    private static final int NOTIFICATION_ID_SERVICE_FOREGROUND = 10;
    private final PublishSubject<UpdateTripData> updateTripDataBehaviorSubject = PublishSubject.create();

    @Inject
    RideSummaryUseCase rideSummaryUseCase;
    private Disposable getTripDetailsSubscription;
    private int GET_TRIP_DETAILS_REFRESH_INTERVAL_MILLIS=10000;

    @Inject
    GetLocationUpdatesUseCase getLocationUpdatesUseCase;

    @Inject
    UpdateRideUseCase updateRideUseCase;

    @Inject
    ObserveLockPositionUseCase observeLockPositionUseCase;


    @Inject
    ObserveConnectionStateUseCase observeConnectionStateUseCase;
    @Inject
    public LockModelMapper lockModelMapper;

    @Inject
    ServiceAction serviceAction;

    private IBinder mBinder = new UpdateTripServiceBinder();


    private AndroidLocationInService androidLocationInService;
    private Location currentUserLocation, lastLocation = null;
    private Disposable locationSubscription = null;
    private Disposable positionSubscription = null;
    private Disposable connectionSubscription = null;
    private Lock.Hardware.Position lastPosition = null;
    private boolean sendPosition = false;
    private int lockPositionIntValue = 0;
    private Lock.Connection.Status connectionState=null;
    protected CompositeDisposable subscriptions = new CompositeDisposable();

    private NotificationManager notificationManager=null;
    private String updateTripServiceNotificationId = "update.trip.service";
    private Context context;
    private ServiceConnection serviceConnection;

    private boolean unbinded = false;


    @Override
    public void onCreate() {
        super.onCreate();
        ((Lattis) getApplication()).getApplicationComponent().inject(this);
        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        androidLocationInService = new AndroidLocationInService(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class UpdateTripServiceBinder extends Binder {
        public ActiveTripService getService() {
            return ActiveTripService.this;
        }
    }


    public PublishSubject<UpdateTripData> startActiveTrip(int trip_id) {
        this.trip_id = trip_id;
        startReleventUpdateDataThread();
        return updateTripDataBehaviorSubject;
    }

    private void startReleventUpdateDataThread(){
        if(locationSubscription==null){
            startGetTripDetails();
        }
    }

    public Observable<Boolean> startLocationTracking(LockModel lockModel){
        this.lockModel = lockModel;
        if (trip_id != 0) {
            stopGetTripDetails();
            requestLocationUpdates();
        }


        if (lockModel != null) {
            observeLockPosition();
            observeConnectionState();
        }

        return Observable.just(true);
    }


    public Observable<Boolean> stopLocationTracking(){
        requestStopLocationUpdates();
        requestStopLockPositionAndConnectionUpdates();
        startGetTripDetails();
        return Observable.just(true);
    }

    public void startInForeground(Context context, ServiceConnection serviceConnection) {
        this.context =context;
        this.serviceConnection = serviceConnection;
        Intent in = new Intent("updatetrip.service.notification.clicked");
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(updateTripServiceNotificationId, "notify_update_trip", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification notification = new NotificationCompat.Builder(this,updateTripServiceNotificationId)
                    .setContentTitle("Lattis")
                    .setContentText("Lattis app")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(NOTIFICATION_ID_SERVICE_FOREGROUND, notification);
        }else{
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Lattis")
                    .setContentText("Lattis app")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            startForeground(NOTIFICATION_ID_SERVICE_FOREGROUND, notification);
        }
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinded=true;
        Log.e(TAG, "ActiveTripService is destroyed");
        stopEverything();
    }


    private void stopEverything(){
        stopForeground(true);
        androidLocationInService.removeKalmanFilterLocationUpdates();
        requestStopLocationUpdates();
        requestStopLockPositionAndConnectionUpdates();
        stopGetTripDetails();
    }


    public void requestLocationUpdates() {
        requestStopLocationUpdates();

        locationSubscription = androidLocationInService.getLocationUpdates()
                .subscribeOn(Schedulers.io())
                .subscribeWith(new RxObserver<Location>() {
                    @Override
                    public void onComplete() {
                        Log.e(TAG, "requestLocationUpdates::onCompleted::");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "requestLocationUpdates::onError::" + e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(Location location) {
                        currentUserLocation=location;
                        updateTrip();
                    }
                });
    }



    private float getDistanceinMeters() {
//        return 300;
        float[] results = new float[3];
        android.location.Location.distanceBetween(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                lastLocation.getLatitude(), currentUserLocation.getLongitude(), results);
        return results[0];// results in meters
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null) {
            locationSubscription.dispose();
            locationSubscription = null;
        }
    }

    public void requestStopLockPositionAndConnectionUpdates() {
        if (positionSubscription != null) {
            positionSubscription.dispose();
            positionSubscription = null;
        }

        if (connectionSubscription != null){
            connectionSubscription.dispose();
            connectionSubscription = null;
        }
    }

    @DebugLog
    public void updateTrip() {

        if (trip_id == 0) {
            return;
        }

        if(currentUserLocation==null)
            return;

        subscriptions.add(updateRideUseCase
                .withTripId(trip_id)
                .withSteps(getSteps())
                .execute(new RxObserver<UpdateTripResponse>() {
                    @Override
                    public void onNext(UpdateTripResponse updateTripResponse) {
                        super.onNext(updateTripResponse);
                        lastLocation = currentUserLocation;
                        if(updateTripResponse.getUpdateTripDataResponse().getUpdateTripEndedResponse()==null || updateTripResponse.getUpdateTripDataResponse().getUpdateTripEndedResponse().getDate_endtrip()==null) {
                            updateTripDataBehaviorSubject.onNext(getUpdateTripDataObject(updateTripResponse));
                        }else{
                            stopService();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }


    public void observeLockPosition() {

        if (positionSubscription != null)
            positionSubscription.dispose();

        if (lockModel == null)
            return;


        subscriptions.add(positionSubscription = observeLockPositionUseCase
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Lock.Hardware.Position>() {
                    @Override
                    public void onNext(Lock.Hardware.Position position) {
                        if (position == Lock.Hardware.Position.LOCKED) {
                            Log.e(TAG, "######observeLockPosition: Lock is LOCKED");
                            if (lastPosition == null || lastPosition != Lock.Hardware.Position.LOCKED) {
                                sendPosition = true;
                                lockPositionIntValue=1;
                                lastPosition = position;
                                updateTrip();
                            }
                        } else if (position == Lock.Hardware.Position.UNLOCKED) {
                            //Log.e(TAG, "######observeLockPosition: Lock is UNLOCKED");
                            if (lastPosition == null || lastPosition != Lock.Hardware.Position.UNLOCKED) {
                                sendPosition = true;
                                lockPositionIntValue=0;
                                lastPosition = position;
                                updateTrip();
                            }
                        } else if (position == Lock.Hardware.Position.INVALID || position == Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED) {
                            //Log.e(TAG, "######observeLockPosition: Lock is INVALID || BETWEEN_LOCKED_AND_UNLOCKED");
                            lastPosition = position;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        Log.e(TAG, "######observeLockPosition: State is complete");
                    }

                }));
    }


    public void observeConnectionState() {

        if (connectionSubscription != null)
            connectionSubscription.dispose();

        if (lockModel == null)
            return;


        subscriptions.add(connectionSubscription = observeConnectionStateUseCase
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Lock.Connection.Status>() {
                    @Override
                    public void onNext(Lock.Connection.Status state) {
                        super.onNext(state);
                        connectionState = state;

                        if (state == OWNER_VERIFIED || state == GUEST_VERIFIED) {
                            Log.e(TAG, "######observeConnectionState: connectionState is CONNECTED");
                        } else if (state.equals(Lock.Connection.Status.DISCONNECTED)) {
                            Log.e(TAG, "######observeConnectionState: connectionState is DISCONNECTED");
                            stopLocationTracking();
                        } else if (state.equals(Lock.Connection.Status.ACCESS_DENIED)) {
                            Log.e(TAG, "######observeConnectionState: connectionState is ACCESS_DENIED");
                            stopLocationTracking();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        Log.e(TAG, "######observeConnectionState: State is complete");

                    }
                }));
    }


    private UpdateTripData getUpdateTripDataObject(UpdateTripResponse updateTripResponse){

        if(updateTripResponse==null){
            return  new UpdateTripData(0,0, "");
        }else if(updateTripResponse.getUpdateTripDataResponse()==null){
            return  new UpdateTripData(0,0,"");
        }else{
            return new UpdateTripData(updateTripResponse.getUpdateTripDataResponse().getDuration(),updateTripResponse.getUpdateTripDataResponse().getCharge_for_duration(), updateTripResponse.getUpdateTripDataResponse().getCurrency());
        }

    }


    private UpdateTripData getUpdateTripDataObject(RideSummaryResponse rideSummaryResponse){

        if(rideSummaryResponse==null){
            return  new UpdateTripData(0,0, "");
        }else if(rideSummaryResponse.getRideSummaryResponse()==null){
            return  new UpdateTripData(0,0,"");
        }else{
            return new UpdateTripData(Double.parseDouble(rideSummaryResponse.getRideSummaryResponse().getDuration()),Float.parseFloat(rideSummaryResponse.getRideSummaryResponse().getTotal()), rideSummaryResponse.getRideSummaryResponse().getCurrency());
        }
    }

    double[][] getSteps() {

        if (sendPosition) {
            sendPosition = false;
            return new double[][]{
                    new double[]{currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), getDoubleTime(), lockPositionIntValue}
            };
        } else {
            return new double[][]{
                    new double[]{currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), getDoubleTime()}
            };
        }
    }

    double getDoubleTime() {
        Date dte = new Date();
        return (double) dte.getTime() / 1000;
    }




    ////// Get Trip details for displaying trip cost when GPS tracking is OFF ////////////

    private void startGetTripDetails(){
        subscribeToGetTripDetails(false);
        subscribeToGetTripDetails(true);
    }

    private void stopGetTripDetails(){
        subscribeToGetTripDetails(false);
    }


    private Observable<Boolean> subscribeToGetTripDetails(){

        return Observable.interval(GET_TRIP_DETAILS_REFRESH_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .flatMap(requestGetTripDetails);
    }

    private Function<Long, Observable<Boolean>> requestGetTripDetails = new Function<Long, Observable<Boolean>>() {
        @Override
        public Observable<Boolean> apply(Long aLong) {
            getRideSummary();
            return Observable.just(true);
        }
    };


    public synchronized void subscribeToGetTripDetails(boolean active){
        cancelGetTripDetailsSubscription();
        if(active){
            if (getTripDetailsSubscription == null){
                getTripDetailsSubscription = subscribeToGetTripDetails()
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<Boolean>() {

                            @Override
                            public void onComplete() {}

                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onNext(Boolean success) {

                            }
                        });
            }
        }
    }

    public void cancelGetTripDetailsSubscription(){
        if(getTripDetailsSubscription!=null){
            getTripDetailsSubscription.dispose();
            getTripDetailsSubscription = null;
        }
    }

    public void getRideSummary() {
        subscriptions.add(rideSummaryUseCase
                .withTripId(trip_id)
                .execute(new RxObserver<RideSummaryResponse>() {
                    @Override
                    public void onNext(RideSummaryResponse rideSummaryResponse) {
                        super.onNext(rideSummaryResponse);

                        if(rideSummaryResponse.getRideSummaryResponse().getDate_endtrip() == null) {
                            updateTripDataBehaviorSubject.onNext(getUpdateTripDataObject(rideSummaryResponse));
                        }else{
                            stopService();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    private void stopService(){
        if(context!=null && serviceConnection!=null && !unbinded){
            context.unbindService(serviceConnection);
        }
    }



}
