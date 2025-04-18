package com.lattis.ellipse.presentation.ui.ride.service.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import android.util.Log;

import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.ride.service.ActiveTripService;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


/**
 * Created by ssd3 on 7/17/17.
 */

public class ServiceAction {

    private  final String TAG = ServiceAction.class.getName();
    private ActiveTripService activeTripService =null;
    private  ServiceConnection serviceConnection;
    protected Context context;

    private ServiceAction(Context context) {
        this.context = context;
    }


    public static ServiceAction newInstance(@NonNull Context context) {
        return new ServiceAction(context);
    }

    public Observable<Boolean> stopActiveTripService(){
        if(activeTripService !=null){
            context.unbindService(serviceConnection);
            activeTripService = null;
        }
        return Observable.just(true);
    }


    public Observable<UpdateTripData> startActiveTripService(int trip_id) {
        return getActiveTripService().flatMap(new Function<ActiveTripService, Observable<UpdateTripData>>() {
            @Override
            public Observable<UpdateTripData> apply(ActiveTripService activeTripService) {
                return activeTripService.startActiveTrip(trip_id);
            }
        });

    }


    public Observable<Boolean> startLocationTracking(LockModel lockModel) {
        return getActiveTripService().flatMap(new Function<ActiveTripService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(ActiveTripService activeTripService) {
                return activeTripService.startLocationTracking(lockModel);
            }
        });
    }


    public Observable<Boolean> stopLocationTracking() {
        if(activeTripService==null){
            return Observable.just(true);
        }


        return getActiveTripService().flatMap(new Function<ActiveTripService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(ActiveTripService activeTripService) {
                return activeTripService.stopLocationTracking();
            }
        });
    }

    private Observable<ActiveTripService> getActiveTripService(){
        return Observable.create(emitter ->  {

                if (activeTripService == null) {
                    serviceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder binder) {
                            Log.e(TAG, "onServiceConnected-->" + name.getClassName());
                            activeTripService = ((ActiveTripService.UpdateTripServiceBinder) binder).getService();
                            activeTripService.startInForeground(context,serviceConnection);
                            emitter.onNext(activeTripService);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            Log.e(TAG, "onServiceDisconnected-->" + name.getClassName());
                            emitter.onComplete();
                            activeTripService = null;
                        }

                    };
                    context.bindService(new Intent(context, ActiveTripService.class),
                            serviceConnection, Context.BIND_AUTO_CREATE);

                } else {
                    emitter.onNext(activeTripService);
                }


        });

    }

    public Observable<Boolean> stopGetTripDetailsThreadIfApplicable() {
        if(activeTripService!=null){
            activeTripService.cancelGetTripDetailsSubscription();
        }
        return Observable.just(true);
    }

}
