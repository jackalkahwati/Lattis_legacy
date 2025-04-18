package com.lattis.ellipse.presentation.ui.bike.bikeList;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.activity.bluetooth.BaseBluetoothActivity;
import com.lattis.ellipse.presentation.ui.payment.AddCardActivity;
import com.lattis.ellipse.presentation.ui.profile.addcontact.AddMobileNumberActivity;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomPagerContainer;
import com.lattis.ellipse.presentation.view.CustomTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.lattis.ellipse.presentation.ui.bike.bikeList.ScanBikeQRCodeActivityPermissionsDispatcher.requestCameraPermissionWithPermissionCheck;
import static com.lattis.ellipse.presentation.ui.home.HomeActivity.REQUEST_CODE_BIKE_INFO;

/**
 * Created by ssd3 on 9/5/17.
 */
@RuntimePermissions
public class ScanBikeQRCodeActivity extends BaseBluetoothActivity<ScanBikeQRCodeActivityPresenter> implements ScanBikeQRCodeActivityView, BarcodeCallback {

    private final String TAG = ScanBikeQRCodeActivity.class.getName();
    private final int REQUEST_QR_CODE_BIKE_DETAILS_FAIL = 3101;
    private final int REQUEST_CODE_ADD_CARD_ACTIVITY = 7023;
    private static final int PHONE_NUMBER_ERROR_REQUEST_CODE = 4024;
    private static final int REQUEST_CODE_PHONE_NUMBER_ADD = 4025;
    public static final String QR_CODE_SCANNING_RIDE_STARTED = "QR_CODE_SCANNING_RIDE_STARTED";

    private ViewPager pager;
    ShowBikeListAdapter adapter;
    private List<Bike> bikeList=new ArrayList<>();
    private Boolean isErrorPopUpBeingShown=false;
    List<Card> cards=null;
    Bike bike=null;


    private String signedMessage;
    private String publicKey;

    private Disposable startScanTimerSubscription=null;
    private Disposable connectingTimerSubscription=null;
    private Disposable unlockingTimerSubscription=null;
    private Disposable startRideTimerSubscription=null;
    private static final int MILLISECONDS_FOR_START_SCAN = 60000;
    private static final int MILLISECONDS_FOR_CONNECTING_TIMER = 60000;
    private static final int MILLISECONDS_FOR_UNLOCKING_TIMER = 2000;
    private static final int MILLISECONDS_FOR_START_RIDE_TIMER = 10000;
    private boolean isBikeConnectedThruBluetooth = false;
    private boolean isBackPressed=false;


    @BindView(R.id.rl_connect_book_start_ride)
    RelativeLayout rl_connect_book_start_ride;

    @BindView((R.id.rl_loading_operation))
    View scan_bike_qr_code_loading_operation_view;

    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;

    @BindView(R.id.iv_lock)
    ImageView iv_lock;

    @BindView(R.id.decoratedBarcodeView)
    DecoratedBarcodeView decoratedBarcodeView;

    @BindView(R.id.pager_container)
    CustomPagerContainer pagerContainer;

    @BindView(R.id.rl_check_card_list)
    RelativeLayout checkCardListView;


    @Inject
    ScanBikeQRCodeActivityPresenter scanBikeQRCodeActivityPresenter;


    @OnClick(R.id.tv_add_card)
    public void addCardClicked() {
        checkCardListView.setVisibility(View.GONE);
        startActivityForResult(new Intent(this, AddCardActivity.class), REQUEST_CODE_ADD_CARD_ACTIVITY);
    }

    @OnClick(R.id.cancel)
    public void cancelClicked() {
        checkCardListView.setVisibility(View.GONE);
    }

    @OnClick(R.id.rl_connect_book_start_ride)
    public void startConnectingBookingRide(){
        if(bikeList.size()>0){
            if(bikeList.get(0)!=null){
                bike = bikeList.get(0);

                if(IsRidePaid.isRidePaidForFleet(bike.getFleet_type())){
                    if(cards==null || cards.size()==0){
                        checkCardListView.setVisibility(View.VISIBLE);
                        return;
                    }
                }

                if(!getPresenter().phoneNumberCheckPassed(bike)){
                    PopUpActivity.launchForResult(this,PHONE_NUMBER_ERROR_REQUEST_CODE, getString(R.string.mandatory_phone_title),
                            getString(R.string.mandatory_phone_text), null, getString(R.string.mandatory_phone_action));
                    return;
                }

                showLoadingForQRCodeScan(getString(R.string.scan_qr_code_connecting_loading));
                getPresenter().reserveBike(bike);
                return;
            }
        }
        showQRCodeFailure(getString(R.string.alert_error_server_title),getString(R.string.alert_error_server_subtitle));
    }


    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected ScanBikeQRCodeActivityPresenter getPresenter() {
        return scanBikeQRCodeActivityPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_scan_bike_qr_code;
    }


    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.find_ride_qr_page_title));
        setToolBarBackGround(Color.WHITE);
        decoratedBarcodeView.setStatusText("");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCameraPermissionWithPermissionCheck(this);
        getPresenter().requestLocationUpdates();
        getPresenter().getUserProfile();
        setUIComponentAsPerValue(false);
        initializeView();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ScanBikeQRCodeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.CAMERA})
    public void requestCameraPermission() {
        decoratedBarcodeView.decodeSingle(this);
    }



    @Override
    public void restartScanner() {

        Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    public void accept(Long aLong) {
                        decoratedBarcodeView.decodeSingle(ScanBikeQRCodeActivity.this);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });

    }



    @Override
    public void onBikeDetailsSuccess(Bike bike) {
        restartScanner();
        bikeList.clear();
        bikeList.add(bike);
        configureViewPagerAdapater();
    }

    @Override
    public void onBikeDetailsFailure() {
        showQRCodeFailure(getString(R.string.alert_error_server_title),getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void onBikeUnAuthorised() {
        showQRCodeFailure(getString(R.string.alert_error_server_title),getString(R.string.qr_error_fleet_access_denied));
    }

    @Override
    public void onBikeAlreadyRented() {
        showQRCodeFailure(getString(R.string.alert_error_server_title),getString(R.string.qr_error_bike_is_rent));
    }

    @Override
    public void onBikeNotAvailable() {
        showQRCodeFailure(getString(R.string.alert_error_server_title),getString(R.string.qr_error_bike_not_live));
    }

    @Override
    public void onBikeNotFound() {
        showQRCodeFailure(getString(R.string.alert_error_server_title),getString(R.string.qr_error_bike_not_found));
    }

    @Override
    public void onInvalidQRCode() {
        showQRCodeFailure(getString(R.string.alert_error_server_title),getString(R.string.qr_error_bike_not_found));
    }

    @Override
    public void possibleResultPoints(List<ResultPoint> list) {

    }

    @Override
    public void onGetCardSuccess(List<Card> cards) {
        this.cards = cards;
        rl_connect_book_start_ride.setVisibility(View.VISIBLE);
        setCardDetailsInAdapter();
    }

    @Override
    public void onGetCardFailure() {
        rl_connect_book_start_ride.setVisibility(View.VISIBLE);
    }
    

    @Override
    public void barcodeResult(BarcodeResult barcodeResult) {
        getPresenter().RequestToAddBikeFromQRCode(barcodeResult.getText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeScanner();
        if(this.cards==null)
            getPresenter().getCards();
    }

    protected void resumeScanner() {
        if (!decoratedBarcodeView.isActivated())
            decoratedBarcodeView.resume();
    }

    protected void pauseScanner() {
        decoratedBarcodeView.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseScanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAllSubscription();
    }

    private void showQRCodeFailure(String title, String subTitle){
        setUIComponentAsPerValue(false);
        bikeList.clear();
        if(!isErrorPopUpBeingShown) {
            isErrorPopUpBeingShown=true;
            PopUpActivity.launchForResult(this, REQUEST_QR_CODE_BIKE_DETAILS_FAIL, title, subTitle, null, getString(R.string.ok));
        }
    }


    private void initializeView() {
        pager = pagerContainer.getViewPager();
    }

    private void setCardDetailsInAdapter(){
        if(cards!=null && cards.size()>0) {
            if(adapter!=null && pager.getAdapter()!=null){
                for (Card card : cards) {
                    if (card.getIs_primary()) {
                        adapter.setCardList(card);
                        pager.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        }else{
            if(adapter!=null && pager.getAdapter()!=null) {
                adapter.setCardList(null);
                pager.getAdapter().notifyDataSetChanged();
            }
        }
    }


    public void configureViewPagerAdapater() {
        setUIComponentAsPerValue(true);
        adapter = new ShowBikeListAdapter(this, this.bikeList,true);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(adapter.getCount());
        pager.setPageMargin(15);
        pager.setClipChildren(false);
        setCardDetailsInAdapter();
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setUIComponentAsPerValue(boolean visible){
        if(visible){
            pagerContainer.getViewPager().setVisibility(View.VISIBLE);
            pagerContainer.setVisibility(View.VISIBLE);
            rl_connect_book_start_ride.setEnabled(true);
            rl_connect_book_start_ride.setBackgroundColor(Color.parseColor("#00AAD1"));
            iv_lock.setImageResource(R.drawable.blue_lock);
        }else{
            pagerContainer.getViewPager().setVisibility(View.INVISIBLE);
            pagerContainer.setVisibility(View.INVISIBLE);
            rl_connect_book_start_ride.setEnabled(false);
            rl_connect_book_start_ride.setBackgroundColor(Color.parseColor("#B7C1CD"));
            iv_lock.setImageResource(R.drawable.white_lock);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_QR_CODE_BIKE_DETAILS_FAIL){
            isErrorPopUpBeingShown=false;
            restartScanner();
        }else if(requestCode == REQUEST_CODE_BIKE_INFO || requestCode == REQUEST_CODE_ADD_CARD_ACTIVITY){
            getPresenter().getCards();
        }else if(requestCode == PHONE_NUMBER_ERROR_REQUEST_CODE && resultCode == RESULT_OK){
            launchPhoneNumberFillingActivity();
        }else if(requestCode == REQUEST_CODE_PHONE_NUMBER_ADD ){
            rl_connect_book_start_ride.setEnabled(false);
            getPresenter().getUserProfile();
        }
    }


    ////////////////////////////////// RESERVE BIKE CODE : START //////////////////////////////////////////////////

    @Override
    public void OnReserveBikeSuccess() {
        isBikeConnectedThruBluetooth=false;
        getPresenter().deleteLock();
        getPresenter().getSignedMessagePublicKey(bike);
    }

    @Override
    public void OnReserveBikeFail() {
        showErrorPopupWith(getString(R.string.alert_error_server_title),getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void OnReserveBikeNotFound() {
        showErrorPopupWith(getString(R.string.alert_error_server_title),getString(R.string.qr_error_bike_not_live));
    }


    ////////////////////////////////// LOCK CONNECTION CODE : START //////////////////////////////////////////////////


    @Override
    public void OnSignedMessagePublicKeySuccess(String signedMessage, String publicKey) {
        this.signedMessage = signedMessage;
        this.publicKey = publicKey;
        getPresenter().setDisconnectRequiredForApp(false);

        LockModel lockModel = new LockModel();
        lockModel.setSignedMessage(signedMessage);
        lockModel.setPublicKey(publicKey);
        lockModel.setUserId(bike.getFleet_key());
        lockModel.setMacId(bike.getMac_id());
        getPresenter().setLockModel(lockModel);

        getPresenter().disconnectAllLocks();    //this will disconnect all previous connections and start scanning for required lock
    }



    @Override
    public void onLockScanned(LockModel lockModel) {
        if (lockModel != null) {
            if (lockModel.getMacId().equals(bike.getMac_id())) {
                subscribeToStartScanTimer(false);
                lockModel.setSignedMessage(signedMessage);
                lockModel.setPublicKey(publicKey);
                lockModel.setUserId(bike.getFleet_key());
                getPresenter().setLockModel(lockModel);
                getPresenter().connectTo();
            }
        }
    }


    @Override
    public void onLockConnected(LockModel lockModel) {
        showLoadingForQRCodeScan(getString(R.string.scan_qr_code_unlocking_loading));
        subscribeToConnectingTimer(false);
        subscribeToStartScanTimer(false);
        isBikeConnectedThruBluetooth = true;
        getPresenter().saveLock(lockModel);

        Log.e(TAG,"onLockConnected::");

        subscribeToStartRideTimer(false);
        subscribeToUnlockTimer(false);
        subscribeToStartRideTimer(true);
        subscribeToUnlockTimer(true);
    }

    @Override
    public void onSaveLockSuccess(Lock lock) {

    }


    @Override
    public void OnSetPositionStatus(Boolean status) {

        Log.e(TAG,"OnSetPositionStatus::"+status);

        if(status){
            startRide();
        }else{
            subscribeToUnlockTimer(true);
        }

    }

    @Override
    public void startRide(){
        Log.e(TAG,"startRide::");
        subscribeToUnlockTimer(false);
        subscribeToStartRideTimer(false);
        getPresenter().cancelSetPositionSubscription();
        getPresenter().startRide(bike,false);
    }

    @Override
    public void onStartRideSuccess() {

        Log.e(TAG,"onStartRideSuccess::");
        Intent data = new Intent();
        data.putExtra(QR_CODE_SCANNING_RIDE_STARTED, true);
        setResult(RESULT_OK, data);
        finish();
    }



    @Override
    public void onCancelBikeSuccess() {
        if(isBackPressed){
            finish();
        }
    }



    @Override
    public void onCancelBikeFail() {
        if(isBackPressed){
            finish();
        }
    }



    @Override
    public void onBluetoothEnabled() {

    }

    @Override
    public void requestEnableBluetooth() {
        super.requestEnableBluetooth();
    }



    @Override
    public void onStartRideFail() {
        showErrorPopupWith(getString(R.string.alert_error_server_title),getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void onSaveLockFailure() {
        showErrorPopupWith(getString(R.string.alert_error_server_title),getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void showConnecting(boolean requiresReconnection) {
        if(requiresReconnection) {
            subscribeToConnectingTimer(false);
        }
        subscribeToConnectingTimer(true);
    }

    @Override
    public void onLockConnectionFailed() {
        getPresenter().getSignedMessagePublicKey(bike);
    }

    @Override
    public void onLockConnectionAccessDenied() {
        showErrorPopupWith(getString(R.string.ellipse_access_denided_title),getString(R.string.ellipse_access_denided_text));
    }

    @Override
    public void onScanStart() {
        subscribeToStartScanTimer(true);
    }

    @Override
    public void OnSignedMessagePublicKeyFailure() {
        showErrorPopupWith(getString(R.string.alert_error_server_title),getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void onScanStop() {
        if(!isBikeConnectedThruBluetooth)
            getPresenter().getSignedMessagePublicKey(bike);
    }

    void showErrorPopupWith(String title, String subTitle){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideLoadingForQRCodeScan();
            }
        });

        cancelAllSubscription();
        cancelBikeBookingAndDisconnectLock();
        PopUpActivity.launchForResult(this, REQUEST_QR_CODE_BIKE_DETAILS_FAIL, title, subTitle, null, getString(R.string.ok));
    }


////////////////////////////////// SCAN CONNECTING TIMER CODE : START //////////////////////////////////////////////////

    public synchronized void subscribeToStartScanTimer(boolean active) {
        if (active) {
            if (startScanTimerSubscription == null) {
                startScanTimerSubscription = Observable.timer(MILLISECONDS_FOR_START_SCAN, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                subscribeToStartScanTimer(false);

                                showErrorPopupWith(getString(R.string.alert_error_server_title),getString(R.string.qr_error_ellipse_not_around));
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {


                            }
                        });
            }
        } else {
            if (startScanTimerSubscription != null) {
                startScanTimerSubscription.dispose();
                startScanTimerSubscription = null;
            }
        }
    }


    public synchronized void subscribeToConnectingTimer(boolean active) {
        if (active) {
            if (connectingTimerSubscription == null) {
                connectingTimerSubscription = Observable.timer(MILLISECONDS_FOR_CONNECTING_TIMER, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                subscribeToConnectingTimer(false);
                                showErrorPopupWith(getString(R.string.bike_booking_end_ride_title),getString(R.string.bike_out_of_range_connection_error));
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        });
            }
        } else {
            if (connectingTimerSubscription != null) {
                connectingTimerSubscription.dispose();
                connectingTimerSubscription = null;
            }
        }
    }

    public synchronized void subscribeToStartRideTimer(boolean active) {
        Log.e(TAG,"subscribeToStartRideTimer::"+active);
        if (active) {
            if (startRideTimerSubscription == null) {
                startRideTimerSubscription = Observable.timer(MILLISECONDS_FOR_START_RIDE_TIMER, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                Log.e(TAG,"subscribeToStartRideTimer::"+active+" call");
                                startRide();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        });
            }
        } else {
            if (startRideTimerSubscription != null) {
                startRideTimerSubscription.dispose();
                startRideTimerSubscription = null;
            }
        }
    }

    public synchronized void subscribeToUnlockTimer(boolean active) {
        Log.e(TAG,"subscribeToUnlockTimer::"+active);
        if (active) {
            if (unlockingTimerSubscription == null) {
                unlockingTimerSubscription = Observable.timer(MILLISECONDS_FOR_UNLOCKING_TIMER, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                Log.e(TAG,"subscribeToUnlockTimer::"+active+" call");
                                subscribeToUnlockTimer(false);
                                getPresenter().setPosition(false);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        });
            }
        } else {
            if (unlockingTimerSubscription != null) {
                unlockingTimerSubscription.dispose();
                unlockingTimerSubscription = null;
            }
        }
    }




    void cancelBikeBookingAndDisconnectLock(){
        getPresenter().setDisconnectRequiredForApp(true);
        getPresenter().deleteLock();
        getPresenter().disconnectAllLocks();
        if(bike!=null)
            getPresenter().cancelBikeReservation(bike);
    }



    void cancelAllSubscription(){
        subscribeToConnectingTimer(false);
        subscribeToStartScanTimer(false);
        subscribeToStartRideTimer(false);
        subscribeToUnlockTimer(false);
        getPresenter().cancelAllSubscription();
    }


    public void showLoadingForQRCodeScan(String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scan_bike_qr_code_loading_operation_view.setVisibility(View.VISIBLE);
                loading_operation_name.setText(message);
            }
        });
    }

    public void hideLoadingForQRCodeScan() {
        scan_bike_qr_code_loading_operation_view.setVisibility(View.GONE);
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

    @Override
    public void onBackPressed() {

        Log.e(TAG,"On Back Pressed");
        isBackPressed=true;
        if(getPresenter().getQrScanProgress()== ScanBikeQRCodeActivityPresenter.QRScanProgress.NOTHING){
            super.onBackPressed();
            return;
        }else if(getPresenter().getQrScanProgress()== ScanBikeQRCodeActivityPresenter.QRScanProgress.LOCK_CONNECTED
                || getPresenter().getQrScanProgress()== ScanBikeQRCodeActivityPresenter.QRScanProgress.BIKE_RESERVE){

            cancelAllSubscription();
            cancelBikeBookingAndDisconnectLock();
        }else if(getPresenter().getQrScanProgress()== ScanBikeQRCodeActivityPresenter.QRScanProgress.RIDE_STARTED){

        }
    }

    private void launchPhoneNumberFillingActivity(){
        startActivityForResult(new Intent(this, AddMobileNumberActivity.class),REQUEST_CODE_PHONE_NUMBER_ADD);
    }

    @Override
    public void onGetUserProfile() {
        rl_connect_book_start_ride.setEnabled(true);
    }

}
