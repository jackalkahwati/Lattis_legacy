package com.lattis.ellipse.presentation.ui.profile.help;

import android.util.Log;

import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

/**
 * Created by lattis on 15/05/17.
 */

public class HelpPresenter extends ActivityPresenter<HelpView> {

    private final GetRideUseCase getRideUseCase;

    @Inject
    HelpPresenter(GetRideUseCase getRideUseCase)
    {
        this.getRideUseCase = getRideUseCase;
    }



    public void getRide() {
        subscriptions.add(getRideUseCase
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);

                        Log.e("HelpPresenter","getRide::on_call_operator::"+ride.getBike_on_call_operator());
                        Log.e("HelpPresenter","getRide::Support_phone::"+ride.getSupport_phone());

                        if(ride.getBike_on_call_operator()==null || ride.getBike_on_call_operator() == ""
                                || ride.getBike_on_call_operator().equalsIgnoreCase("null")
                                || ride.getBike_on_call_operator().equalsIgnoreCase("undefined") ){

                            view.showDefaultLattisNumber(ride.getSupport_phone());

                        }else{
                            view.showOperatorNumber(ride.getBike_on_call_operator());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.showDefaultLattisNumber("415-503-9744");
                    }
                }));
    }
}
