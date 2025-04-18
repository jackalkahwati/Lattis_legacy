package com.lattis.ellipse.presentation.ui.bike.bikeList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.lattis.ellipse.Utils.AddressUtils;
import com.lattis.ellipse.Utils.MapboxUtil;
import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.ui.bike.BikeBaseFragment;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragmentPresenter;
import com.lattis.ellipse.presentation.ui.payment.AddCardActivity;
import com.lattis.ellipse.presentation.ui.profile.addcontact.AddMobileNumberActivity;
import com.lattis.ellipse.presentation.ui.profile.changeMail.ConfirmCodeForChangeEmailActivity;
import com.lattis.ellipse.presentation.ui.profile.fleet.AddPrivateFleetActivity;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomPagerContainer;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.lattis.ellipse.presentation.view.toolTip.Tooltip;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

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

import static android.app.Activity.RESULT_OK;
import static com.lattis.ellipse.Utils.MapboxUtil.addMarker;
import static com.lattis.ellipse.Utils.MapboxUtil.addUserLocation;
import static com.lattis.ellipse.Utils.MapboxUtil.convertDpToPixel;
import static com.lattis.ellipse.Utils.MapboxUtil.isNotSymbolOfuserLocation;
import static com.lattis.ellipse.Utils.MapboxUtil.selected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.setFixedZoomForSinglePoint;
import static com.lattis.ellipse.Utils.MapboxUtil.unselected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.zoomToMarkers;
import static com.lattis.ellipse.Utils.ResourceUtil.getBikeResource;
import static com.lattis.ellipse.Utils.ResourceUtil.ic_pick_location;
import static com.lattis.ellipse.Utils.ResourceUtil.user_location;
import static com.lattis.ellipse.presentation.ui.bike.SearchPlacesActivity.SEARCH_CURRENT_LOCATION;
import static com.lattis.ellipse.presentation.ui.bike.SearchPlacesActivity.SEARCH_LOCATION_LATITUDE;
import static com.lattis.ellipse.presentation.ui.bike.SearchPlacesActivity.SEARCH_LOCATION_LONGITUDE;
import static com.lattis.ellipse.presentation.ui.bike.bikeList.NoServiceActivity.USER_ID_FOR_ADD_PRIVATE_FLEET;
import static com.lattis.ellipse.presentation.ui.bike.bikeList.ScanBikeQRCodeActivity.QR_CODE_SCANNING_RIDE_STARTED;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.ARG_USER_ACCOUNT_TYPE;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.ARG_USER_ID;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.USER_ACCOUNT_TYPE_PRIVATE;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.ACTIONBTN_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.SUBTITLE1_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.SUBTITLE2_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.TITLE_POP_UP;


public class BikeListFragment extends BaseFragment<BikeListFragmentPresenter> implements BikeListView {

    private final int REQUEST_QR_CODE_BIKE_DETAILS_FAIL = 3001;
    private static final String TAG = BikeListFragment.class.getName();
    private final int REQUEST_CODE_ADD_CARD_ACTIVITY = 1811;
    private ViewPager pager;
    private List<Bike> bikeList;

    private List<Symbol> symbols = new ArrayList<>();
    List<SymbolOptions> options = new ArrayList<>();

    private OnSymbolClickListener symbolClickListener;
    private Bike selectedBike;
    private static final int RESERVE_BIKE_REQUEST_CODE = 3000;
    private static final int SERVER_ERROR_REQUEST_CODE = 3239;
    private static final int REEQUEST_CODE_SCAN_QR_CODE = 3240;
    private static final int REQUEST_CODE_NO_SERVICE_ACTIVITY = 3241;
    private static final int PHONE_NUMBER_ERROR_REQUEST_CODE = 3242;
    private static final int REQUEST_CODE_PHONE_NUMBER_ADD = 3243;
    private static final int REEQUEST_CODE_ADD_EMAIL_PRIVATE_FLEET = 2910;
    private static final int REEQUEST_CODE_CONFIRM_CODE_FOR_ADD_PRIVATE_FLEET = 2109;
    private List<Card> cards;
    private Disposable findRidesTimerSubscription =null;
    private static final int MILLISECONDS_FOR_FIND_RIDES_TIMER = 10000;




    ShowBikeListAdapter adapter;
    @BindView(R.id.pager_container)
    CustomPagerContainer pagerContainer;

    @BindView(R.id.rl_no_bikes)
    RelativeLayout rl_no_bikes;

    @BindView(R.id.tv_bikes_not_available)
    CustomTextView noBikesView;

    @BindView(R.id.button_Reservebike)
    CustomButton button_Reservebike;

    Location currentLocation, pickUpLocation;

    @BindView(R.id.rl_check_card_list)
    RelativeLayout checkCardListView;

    BikeBaseFragment parentFragment;

    @BindView(R.id.tooltip_view)
    RelativeLayout toolTipView;

    @Inject
    BikeListFragmentPresenter bikeListFragmentPresenter;

    private final int REQUEST_CODE_SEARCH_ADDRESS = 4646;
    private String currentAddress = null;
    private String pickupAddress = null;
    boolean isPickupLocation = false;

    private MenuItem menuItem;

    Toolbar mToolbar;

    @OnClick(R.id.tv_add_card)
    public void addCardClicked() {
        checkCardListView.setVisibility(View.GONE);
        startActivityForResult(new Intent(getActivity(), AddCardActivity.class), REQUEST_CODE_ADD_CARD_ACTIVITY);
    }

    private void launchActiveRideFragement() {
        Observable.timer(10, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    public void accept(Long aLong) {
                        parentFragment.showActiveRideFragment();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });
    }


    private void launchQRCodeScannerActivity() {
        Intent intent = new Intent(getActivity(), ScanBikeQRCodeActivity.class);
        startActivityForResult(intent, REEQUEST_CODE_SCAN_QR_CODE);
    }


    public void findBikes() {
        parentFragment.showOperationLoading(getString(R.string.find_ride_search));
        clearMapBox();
        clearPagerContainer();
        bikeList = null;
        if (!isPickupLocation && currentLocation != null) {
            getPresenter().findBikes(currentLocation);
        } else if (isPickupLocation && pickUpLocation != null) {
            getPresenter().findBikes(pickUpLocation);
        }
    }

    @OnClick(R.id.cancel)
    public void cancelClicked() {
        checkCardListView.setVisibility(View.GONE);
    }

    @Override
    public void setUserPosition(Location location) {
        getActivity().invalidateOptionsMenu();
        this.currentLocation = location;
        setBikeCardAndReserveBtnVisibility(false);
        setToolbarDescription(AddressUtils.getAddress(getActivity(), location));
        hideSearchView();
        findBikes();

        subscribeToFindRidesTimer(false);
        subscribeToFindRidesTimer(true);
    }


    @Override
    protected void configureViews() {
        super.configureViews();
        initializeView();
        hideSearchView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            subscribeToFindRidesTimer(false);
            getPresenter().cancelGetUserSubscription();
            removeListeningToSymbolClick();
        }else{
            isPickupLocation=false;
            pickUpLocation=null;
            setReserveButtonStatus(true);
            getPresenter().getUserProfile();
            startListeningToSymbolClick();
        }
    }

    @OnClick(R.id.button_Reservebike)
    void reserveBike() {

        if (selectedBike == null) {
            return;
        }

        if(!getPresenter().phoneNumberCheckPassed(selectedBike)){
            launchPopUpActivity(PHONE_NUMBER_ERROR_REQUEST_CODE, getString(R.string.mandatory_phone_title),
                    getString(R.string.mandatory_phone_text), null, getString(R.string.mandatory_phone_action));
            return;
        }

        if (!IsRidePaid.isRidePaidForFleet(selectedBike.getFleet_type())) {
            tryReserveBike();
        } else {
            if (cards == null || cards.size() == 0) {
                checkCardListView.setVisibility(View.VISIBLE);
            } else {
                tryReserveBike();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public void configureViewPagerAdapater() {
        pagerContainer.setVisibility(View.VISIBLE);
        adapter = new ShowBikeListAdapter(getActivity(), this.bikeList,false);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(adapter.getCount());
        pager.setPageMargin(15);
        pager.setClipChildren(false);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setMarker(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    private void clearPagerContainer(){
        if(adapter!=null && bikeList!=null) {
            this.bikeList.clear();
            adapter.clearList();
        }
    }


    private void setBoundsForMarkers() {
        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
        List<LatLng> latLngs = new ArrayList<>();
        if (bikeList != null && bikeList.size() > 0) {
            for (Bike bike : bikeList) {
                LatLng latLng =new LatLng(bike.getLatitude(), bike.getLongitude());
                latLngBounds.include(latLng);
                latLngs.add(latLng);
            }
            if (isPickupLocation) {
                LatLng pickUpLatLng = new LatLng(pickUpLocation.getLatitude(), pickUpLocation.getLongitude());
                latLngBounds.include(pickUpLatLng);
                latLngs.add(pickUpLatLng);
            } else {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                latLngBounds.include(currentLatLng);
                latLngs.add(currentLatLng);
            }
            zoomToMarkers(getMapBoxMap(),latLngBounds,latLngs);
        } else {
            if (isPickupLocation && pickUpLocation != null) {
                latLngs.add(new LatLng(pickUpLocation.getLatitude(), pickUpLocation.getLongitude()));
                setFixedZoomForSinglePoint(getMapBoxMap(),latLngs);
            }else {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                latLngs.add(currentLatLng);
                setFixedZoomForSinglePoint(getMapBoxMap(),latLngs);
            }
        }
    }


    @Override
    public void onGetCardSuccess(List<Card> cards) {
        this.cards = cards;
        if (adapter != null && this.cards != null && this.cards.size()>0) {
            for (Card card : cards) {
                if (card.getIs_primary()) {
                    adapter.setCardList(card);
                    pager.getAdapter().notifyDataSetChanged();
                }
            }
        }else if(adapter!=null){
            adapter.setCardList(null);
            pager.getAdapter().notifyDataSetChanged();
        }

    }

    @Override
    public void onGetCardFailure() {
        this.cards = null;
        if(adapter!=null) {
            adapter.setCardList(null);
            pager.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void hideOperatingLoading() {
        subscribeToFindRidesTimer(false);
        parentFragment.hideOperationLoading();
    }

    @Override
    public void showToolTip() {
        Tooltip.Builder builder = new Tooltip.Builder(toolTipView, R.style.Tooltip)
                .setCancelable(true)
                .setDismissOnClick(false)
                .setCornerRadius(5f)
                .setGravity(Gravity.BOTTOM)
                .setText(R.string.find_ride_qr_tip);
        builder.show();

    }

    @Override
    public void onFindAvailableBikeSuccess(List<Bike> bikes) {
        noBikesView.setText(getString(R.string.find_ride_warning_available));
        showFarBikesView();
        this.bikeList = bikes;
        configureViewPagerAdapater();
        clearSymbols();
        clearMapBox();
        setMarker(0);
        setBoundsForMarkers();
        setBikeCardAndReserveBtnVisibility(true);
    }

    @Override
    public void onFindNearBikesSuccess(List<Bike> bikes) {
        this.bikeList = bikes;
        rl_no_bikes.setVisibility(View.GONE);
        configureViewPagerAdapater();
        clearSymbols();
        clearMapBox();
        setMarker(0);
        setBoundsForMarkers();
        setBikeCardAndReserveBtnVisibility(true);
        hideSearchView();
    }

    @Override
    public void onFindBikeNoServiceAvailable() {
        hideSearchView();
        rl_no_bikes.setVisibility(View.GONE);
        button_Reservebike.setVisibility(View.GONE);
        pagerContainer.getViewPager().setVisibility(View.GONE);
        clearSymbols();
        clearMapBox();
        setMarker(0);
        setBoundsForMarkers();
        hideOperatingLoading();
        startActivityForResult(new Intent(getActivity(), NoServiceActivity.class),REQUEST_CODE_NO_SERVICE_ACTIVITY);
    }

    @Override
    public void onFindBikeFailure() {
        rl_no_bikes.setVisibility(View.GONE);
        hideSearchView();
        button_Reservebike.setVisibility(View.GONE);
        pagerContainer.getViewPager().setVisibility(View.GONE);
        clearSymbols();
        clearMapBox();
        setMarker(0);
        setBoundsForMarkers();
        PopUpActivity.launchForResult(getActivity(), SERVER_ERROR_REQUEST_CODE, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), null, getString(R.string.ok));
    }

    @Override
    public void onFindBikesEmpty() {
        noBikesView.setText(getString(R.string.find_ride_warning_busy));
        showNoBikesView();
        clearSymbols();
        clearMapBox();
        setMarker(0);
        setBoundsForMarkers();
    }

    private void showNoBikesView() {
        hideSearchView();
        rl_no_bikes.setVisibility(View.VISIBLE);
        button_Reservebike.setVisibility(View.GONE);
        pagerContainer.getViewPager().setVisibility(View.GONE);
    }

    private void showFarBikesView() {
        hideSearchView();
        rl_no_bikes.setVisibility(View.VISIBLE);
    }

    @Override
    public void initializeView() {
        pager = pagerContainer.getViewPager();

    }

    @NonNull
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected BikeListFragmentPresenter getPresenter() {
        return bikeListFragmentPresenter;
    }

    @OnClick(R.id.iv_no_bike_cancel)
    public void resetView() {
        rl_no_bikes.setVisibility(View.GONE);
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.show_bike_list_fragment;
    }

    public void setParentFragment(BikeBaseFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    @Override
    public void setMapBox(MapboxMap mapBox) {
        super.setMapBox(mapBox);
        if (getMapBoxMap() != null)
            MapboxUtil.enableLocationComponent(getActivity(),getMapBoxMap(),getMapBoxMap().getStyle());
    }

    private void setMarker(int position) {

        if (getMapBoxMap() != null) {
            selectedBike=null;
            if (symbols.size() == 0) {
                if (bikeList != null && bikeList.size() > 0) {
                    for (Bike bike : bikeList) {
                        if (position == bikeList.indexOf(bike)) {
                            addMarker(options,bike.getLatitude(), bike.getLongitude(), ResourceUtil.getBikeResource(bike.getType().toUpperCase(),false),unselected_size);
                            selectedBike = bike;
                        } else {
                            addMarker(options,bike.getLatitude(), bike.getLongitude(), ResourceUtil.getBikeResource(bike.getType().toUpperCase(),false),unselected_size);
                        }
                    }
                }
                addUserLocation(options,currentLocation.getLatitude(),currentLocation.getLongitude(),ResourceUtil.getUserLocationResource(),unselected_size);
                if (isPickupLocation && pickUpLocation != null) {
                    addMarker(options,pickUpLocation.getLatitude(), pickUpLocation.getLongitude(), ic_pick_location,unselected_size);
                }
                symbols = getSymbolManager().create(options);
            } else {
                if (bikeList != null && bikeList.size() > 0) {
                    for (Bike bike : bikeList) {
                        if (position == bikeList.indexOf(bike)) {
                            selectedBike = bike;
                        }
                    }
                }
            }

            if(symbols.size()>position){
                int index =0;
                for(Symbol symbol : symbols){
                    if(symbols.get(position)!=symbol && isNotSymbolOfuserLocation(symbol)) {
                        symbol.setIconImage(getBikeResource(bikeList.get(index).getType().toUpperCase(), false));
                        symbol.setIconSize(unselected_size);
                        symbol.setZIndex(10);
                        getSymbolManager().update(symbol);
                    }
                    if(isNotSymbolOfuserLocation(symbol))
                        index++;
                }

                if(selectedBike!=null && symbols.get(position).getLatLng().getLatitude() == selectedBike.getLatitude() && symbols.get(position).getLatLng().getLongitude() == selectedBike.getLongitude()){
                    symbols.get(position).setIconImage(getBikeResource(selectedBike.getType().toUpperCase(),true));
                    symbols.get(position).setIconSize(selected_size);
                    symbols.get(position).setZIndex(25);
                    getSymbolManager().update(symbols.get(position));
                }
            }
        }
    }





    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.home_menu, menu);  // Use filter.xml from step 1
        Log.e(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menuItem = menu.findItem(R.id.action_search);
        if (HomeMapFragment.currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP)
            menu.findItem(R.id.action_search).setVisible(true);
        else
            menu.findItem(R.id.action_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (parentFragment.getInternetStatus() && item.getItemId() == R.id.action_search) {
            launchQRCodeScannerActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setBikeCardAndReserveBtnVisibility(Boolean visible) {
        if (visible) {
            pagerContainer.getViewPager().setVisibility(View.VISIBLE);
            button_Reservebike.setVisibility(View.VISIBLE);
        } else {
            pagerContainer.getViewPager().setVisibility(View.GONE);
            button_Reservebike.setVisibility(View.GONE);
        }
    }

    private void setAddressTextInFindBike() {
        if (currentLocation != null && !isPickupLocation) {
            currentAddress = AddressUtils.getAddress(getActivity(), currentLocation);
            setToolbarDescription(currentAddress);
        } else if (pickUpLocation != null && isPickupLocation) {
            pickupAddress = AddressUtils.getAddress(getActivity(), pickUpLocation);
            setToolbarDescription(pickupAddress);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESERVE_BIKE_REQUEST_CODE && resultCode == RESULT_OK) {
            tryReserveBike();
        } else if (requestCode == REQUEST_CODE_ADD_CARD_ACTIVITY && resultCode == RESULT_OK) {
            getPresenter().getCards();
        } else if (requestCode == REEQUEST_CODE_SCAN_QR_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                if (data.hasExtra(QR_CODE_SCANNING_RIDE_STARTED)) {
                    if (data.getBooleanExtra(QR_CODE_SCANNING_RIDE_STARTED, false)) {
                        launchActiveRideFragement();
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_SEARCH_ADDRESS) {
            if (resultCode == RESULT_OK && data != null) {
                if (data.hasExtra(SEARCH_CURRENT_LOCATION)) {
                    if (data.getBooleanExtra(SEARCH_CURRENT_LOCATION, false)) {
                        this.pickUpLocation = null;
                        isPickupLocation = false;
                        setAddressTextInFindBike();
                        findBikes();
                    } else {
                        hideSearchView();
                    }
                } else {
                    pickUpLocation = new Location(data.getDoubleExtra(SEARCH_LOCATION_LATITUDE, 0),
                            data.getDoubleExtra(SEARCH_LOCATION_LONGITUDE, 0));
                    isPickupLocation = true;
                    setAddressTextInFindBike();
                    findBikes();
                }

            } else {
                hideSearchView();
            }
        }else if (requestCode == REQUEST_CODE_NO_SERVICE_ACTIVITY && resultCode == RESULT_OK){
                if(data!=null && data.hasExtra(USER_ID_FOR_ADD_PRIVATE_FLEET)){
                    String userId = data.getStringExtra(USER_ID_FOR_ADD_PRIVATE_FLEET);
                    if(userId!=null){
                                startActivityForResult(new Intent(getActivity(), AddPrivateFleetActivity.class)
                                .putExtra(ARG_USER_ACCOUNT_TYPE, USER_ACCOUNT_TYPE_PRIVATE)
                                .putExtra(ARG_USER_ID, userId),REEQUEST_CODE_ADD_EMAIL_PRIVATE_FLEET);
                    }
                }
        }else if (requestCode == REEQUEST_CODE_ADD_EMAIL_PRIVATE_FLEET && resultCode == RESULT_OK){
            if(data!=null && data.hasExtra(USER_ID_FOR_ADD_PRIVATE_FLEET)){
                String userId = data.getStringExtra(USER_ID_FOR_ADD_PRIVATE_FLEET);
                if(userId!=null){
                    startActivityForResult(new Intent(getActivity(), ConfirmCodeForChangeEmailActivity.class)
                            .putExtra(ARG_USER_ACCOUNT_TYPE, USER_ACCOUNT_TYPE_PRIVATE)
                            .putExtra(ARG_USER_ID, userId),REEQUEST_CODE_CONFIRM_CODE_FOR_ADD_PRIVATE_FLEET);
                }
            }
        }else if(requestCode == REEQUEST_CODE_CONFIRM_CODE_FOR_ADD_PRIVATE_FLEET && resultCode == RESULT_OK){
                findBikes();
        }else if(requestCode == PHONE_NUMBER_ERROR_REQUEST_CODE && resultCode == RESULT_OK){
            launchPhoneNumberFillingActivity();
        }else if(requestCode == REQUEST_CODE_PHONE_NUMBER_ADD){
            setReserveButtonStatus(false);
            getPresenter().getUserProfile();
        }
    }


    private void tryReserveBike() {
        setReserveButtonStatus(false);
        parentFragment.bookBikeAfterAcceptance(selectedBike);
    }

    public void hideSearchView() {
        if (menuItem != null) {
            menuItem.setIcon(R.drawable.ic_qr_code);
        }
    }

    @Override
    public void setScrollListener() {
        super.setScrollListener();
        setBikeCardAndReserveBtnVisibility(false);
        int index = 0;
        for (Symbol symbol : symbols) {
            if (isNotSymbolOfuserLocation(symbol)) {
                symbol.setIconImage(getBikeResource(bikeList.get(index).getType().toUpperCase(), false));
                symbol.setIconSize(unselected_size);
                symbol.setZIndex(10);
                getSymbolManager().update(symbol);
            }
            if (isNotSymbolOfuserLocation(symbol))
                index++;
        }
    }


    public synchronized void subscribeToFindRidesTimer(boolean active) {
        Log.e(TAG,"subscribeToFindRidesTimer::"+active);
        if (active) {
            if (findRidesTimerSubscription == null) {
                findRidesTimerSubscription = Observable.timer(MILLISECONDS_FOR_FIND_RIDES_TIMER, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                Log.e(TAG,"subscribeToFindRidesTimer::"+active+" call");
                                subscribeToFindRidesTimer(false);
                                if(meVisible){
                                    findBikes();
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        });
            }
        } else {
            if (findRidesTimerSubscription != null) {
                findRidesTimerSubscription.dispose();
                findRidesTimerSubscription = null;
            }
        }
    }

    public void refreshCard(){
        getPresenter().getCards();
    }


    private MapboxMap getMapBoxMap(){
        return  parentFragment.getMapboxMap();
    }

    private void launchPhoneNumberFillingActivity(){
        startActivityForResult(new Intent(getActivity(), AddMobileNumberActivity.class),REQUEST_CODE_PHONE_NUMBER_ADD);
    }

    private void launchPopUpActivity(int requestCode, String title, String subTitle1, String subTitle2, String actionBtn){
        Intent intent = new Intent(getActivity(), PopUpActivity.class);
        intent.putExtra(TITLE_POP_UP,title);
        intent.putExtra(SUBTITLE1_POP_UP,subTitle1);
        intent.putExtra(SUBTITLE2_POP_UP,subTitle2);
        intent.putExtra(ACTIONBTN_POP_UP,actionBtn);
        startActivityForResult(intent,requestCode);
    }

    @Override
    public void onGetUserProfile() {
        setReserveButtonStatus(true);
    }


    private void startListeningToSymbolClick(){
        getSymbolManager().addClickListener(symbolClickListener = new OnSymbolClickListener() {
            @Override
            public void onAnnotationClick(Symbol symbol) {
                if(isNotSymbolOfuserLocation(symbol)) {
                    for (int i = 0; i < symbols.size(); i++) {
                        if (symbols.get(i) == symbol) {
                            rl_no_bikes.setVisibility(View.GONE);
                            pager.setCurrentItem(i, true);
                            setBikeCardAndReserveBtnVisibility(true);
                            setMarker(i);
                        }
                    }
                }
            }
        });
    }

    private void removeListeningToSymbolClick(){
        if(symbolClickListener!=null) {
            getSymbolManager().removeClickListener(symbolClickListener);
            symbolClickListener = null;
        }
    }


    private SymbolManager getSymbolManager(){
        return parentFragment.getSymbolManager();
    }

    public void clearSymbols(){
        getSymbolManager().deleteAll();
        getSymbolManager().delete(symbols);
        symbols.clear();
        options.clear();
    }


    private void clearMapBox(){
        if(getActivity()!=null && getMapBoxMap()!=null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    getMapBoxMap().clear();
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscribeToFindRidesTimer(false);
    }


    public void setReserveButtonStatus(boolean active){
        button_Reservebike.setEnabled(active);
    }
}
