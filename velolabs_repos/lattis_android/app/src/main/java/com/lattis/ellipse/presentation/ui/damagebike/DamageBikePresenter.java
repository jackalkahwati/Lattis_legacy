package com.lattis.ellipse.presentation.ui.damagebike;

import android.util.Log;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.uploadImage.UploadImageResponse;
import com.lattis.ellipse.domain.interactor.maintenance.DamageBikeUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.RideSummaryUseCase;
import com.lattis.ellipse.domain.interactor.uploadImage.UploadImageUseCase;
import com.lattis.ellipse.domain.interactor.user.GetCurrentUserStatusUseCase;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class DamageBikePresenter extends ActivityPresenter<DamageBikeView> {
    private String[] category;
    DamageBikeUseCase damageBikeUseCase;
    UploadImageUseCase uploadImageUseCase;
    private int position = 0;
    private final GetCurrentUserStatusUseCase getCurrentUserStatusUseCase;
    private final RideSummaryUseCase rideSummaryUseCase;
    private final GetRideUseCase getRideUseCase;


    @Inject
    public DamageBikePresenter(DamageBikeUseCase damageBikeUseCase,
                               UploadImageUseCase uploadImageUseCase
            , GetCurrentUserStatusUseCase getCurrentUserStatusUseCase,
                               RideSummaryUseCase rideSummaryUseCase,
                               GetRideUseCase getRideUseCase) {
        this.damageBikeUseCase = damageBikeUseCase;
        this.uploadImageUseCase = uploadImageUseCase;
        this.getCurrentUserStatusUseCase = getCurrentUserStatusUseCase;
        this.rideSummaryUseCase = rideSummaryUseCase;
        this.getRideUseCase =getRideUseCase;
    }

    protected void updateViewState() {
        view.updateSpinner();
    }

    public void updateDamageReport(int bikeId, String maintenanceNotes, String imageLink,int tripId) {
        subscriptions.add(damageBikeUseCase
                .withCategory(category[position])
                .withBikeId(bikeId)
                .withTripId(tripId)
                .withRiderNotes(maintenanceNotes)
                .withMaintenanceImage(imageLink)
                .execute(new RxObserver<BasicResponse>(view) {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        view.damageReportSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                            view.damageReportFailure();
                    }
                }));
    }


    public void getCurrentUserStatus() {
        subscriptions.add(getCurrentUserStatusUseCase
                .execute(new RxObserver<GetCurrentUserStatusResponse>(view) {
                    @Override
                    public void onNext(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
                        view.hideProgressLoading();
                        super.onNext(getCurrentUserStatusResponse);
                        if (getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() != null) {
                            view.isRideStarted(true);
                            view.setTripID(getCurrentUserStatusResponse.getCurrentUserStatusTripResponse().getTrip_id());
                        } else if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() != null) {
                            view.setBikeId(getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse().getBike_id());
                            view.isRideStarted(false);
                        } else {
                            view.showBikeNotYetBooked();
                            view.isRideStarted(false);
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.hideProgressLoading();
                    }
                }));
    }


    public void getRide() {
        subscriptions.add(getRideUseCase
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        if(ride!=null){
                            view.setBikeId(ride.getBikeId());
                            Log.e("",""+ride.getRideId());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                    }
                }));
    }






    public void uploadImage(String filePath) {

        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("maintenance", "" + new Date().getTime(), requestFile);
        subscriptions.add(uploadImageUseCase.withFile(body)
                .withUploadType("maintenance")
                .execute(new RxObserver<UploadImageResponse>() {
                    @Override
                    public void onNext(UploadImageResponse uploadImageResponse) {
                        super.onNext(uploadImageResponse);
                        view.uploadImageSuccess(uploadImageResponse.uploadedUrl());
                        view.hideProgressLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.hideProgressLoading();
                        view.uploadImageFail();

                    }
                }));
    }


    public void setPosition(int position) {
        this.position = position;
        Log.i("Position", "" + position);
    }

    public void setCategoryList(String[] list) {
        this.category = list;
    }

    public void getRideSummary(int trip_id) {
        subscriptions.add(rideSummaryUseCase
                .withTripId(trip_id)
                .execute(new RxObserver<RideSummaryResponse>(view) {
                    @Override
                    public void onNext(RideSummaryResponse rideSummaryResponse) {
                        super.onNext(rideSummaryResponse);
                        view.setBikeId(rideSummaryResponse.getRideSummaryResponse().getBike_id());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

}

