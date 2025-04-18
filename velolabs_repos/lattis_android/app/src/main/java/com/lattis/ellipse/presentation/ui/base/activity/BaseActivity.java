package com.lattis.ellipse.presentation.ui.base.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lattis.ellipse.Lattis;
import com.lattis.ellipse.Utils.FirebaseUtil;
import com.lattis.ellipse.presentation.dagger.component.ApplicationComponent;
import com.lattis.ellipse.presentation.ui.base.BaseView;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.view.CustomTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lattis.ellipse.R;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity<Presenter extends ActivityPresenter> extends AppCompatActivity implements BaseView, View.OnClickListener, ConnectivityChangeReceiver.OnConnectivityChangedListener {

    protected CompositeDisposable subscriptions = new CompositeDisposable();

    @Nullable
    @BindView(R.id.progressBarBackground)
    View progressBarBackground;
    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.toolbar_title)
    CustomTextView toolBarTitle;
    @Nullable
    @BindView(R.id.toolbar_subtitle)
    CustomTextView toolBarSubTitle;
    @Nullable
    @BindView(R.id.ll_currentLocation)
    LinearLayout currentLocationLayout;


    @Nullable
    @BindView(R.id.rl_no_internet)
    protected RelativeLayout noInternetView;

    private static final int NO_STUB_VIEW = -1;
    private static final int DEFAULT_HOME_AS_UP_INDICATOR = -1;

    private static final int ARROW_WITH_WHITE = 01;
    private static final int ARROW_WITH_HALF_WHITE = 02;
    private static final int CLOSE_ICON_WHITE = 03;
    private static final int CLOSE_ICON_HALF_WHITE = 04;

    ////// CODE FOR INTERNET CONNECTION ////////////
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    protected abstract void inject();

    @NonNull
    protected abstract Presenter getPresenter();

    @LayoutRes
    protected abstract int getActivityLayoutId();

    protected abstract void onInternetConnectionChanged(boolean isConnected);


    protected int getViewStubLayoutId() {
        return NO_STUB_VIEW;
    }

    protected int getViewStubId() {
        return NO_STUB_VIEW;
    }

    protected void configureViews() {
        setupAppbar(toolbar, true);
    }

    protected void setToolBarBackGround(int bgColor) {
        toolbar.setBackgroundColor(bgColor);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        initComponent();
        inject();
        super.onCreate(savedInstanceState);
        configureWindow();

        setContentView(getActivityLayoutId());

        if (getViewStubId() != NO_STUB_VIEW) {
            ViewStub stub = (ViewStub) findViewById(getViewStubId());
            stub.setLayoutResource(getViewStubLayoutId());
            stub.inflate();
        }

        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            getPresenter().onReCreate(savedInstanceState, this);
        } else {
            getPresenter().onCreate(getIntent().getExtras(), this);
        }
    }

    private void startListeningToInternet(){
        ////// CODE FOR INTERNET CONNECTION ////////////
        connectivityChangeReceiver = new ConnectivityChangeReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(connectivityChangeReceiver, filter);
    }


    ////// CODE FOR INTERNET CONNECTION ////////////
    @Override
    public void onConnectivityChanged(boolean isConnected) {
        // TODO handle connectivity change

        if(noInternetView!=null){
            if(isConnected)
                noInternetView.setVisibility(View.GONE);
            else
                noInternetView.setVisibility(View.VISIBLE);
        }

        onInternetConnectionChanged(isConnected);

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        getPresenter().onReenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().onResume(this);
        configureViews();
        configureSubscriptions();
        getToolbar(toolbar);
        startListeningToInternet();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenter().saveArguments(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenter().onPause();
        subscriptions.clear();
        ////// CODE FOR INTERNET CONNECTION ////////////
        if(connectivityChangeReceiver!=null) {
            unregisterReceiver(connectivityChangeReceiver);
            connectivityChangeReceiver=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().onDestroy(isFinishing());
    }


    @Override
    public void showLoading(boolean hideContent) {
        getPresenter().setLoading(true);
        if (progressBarBackground != null) {
            progressBarBackground.setBackgroundColor(hideContent ? ContextCompat.getColor(this, android.R.color.white) : ContextCompat.getColor(this, android.R.color.transparent));
            progressBarBackground.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }


    @Override
    public void hideLoading() {
        getPresenter().setLoading(false);
        if (progressBarBackground != null) {
            progressBarBackground.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getPresenter().onClick(item.getItemId());
        return false;
    }

    @Override
    public void showError(String message, int duration) {
        showToastMessage(message, duration);
    }

    @Override
    public void showErrorWithAction(String message, View.OnClickListener listener, String actionLabel) {
        showSnackBar(message, listener, actionLabel);
    }

    @Override
    public void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToastMessage(String message, int duration) {
        Toast.makeText(getApplicationContext(), message, duration).show();
    }

    @Override
    public void showSnackBar(String message, View.OnClickListener action, int actionLabel) {
        /*Snackbar snackbar = Snackbar.make(getContentLayout(), message, Snackbar.LENGTH_LONG);

        if (action != null && actionLabel > 0) {
            snackbar.setAction(actionLabel, action);
        }

        snackbar.show();*/
    }

    @Override
    public void showSnackBar(String message, View.OnClickListener listener, String actionLabel) {

        /*Snackbar snackbar = Snackbar.make(getContentLayout(), message, Snackbar.LENGTH_LONG);

        if (listener != null && actionLabel != null && !actionLabel.isEmpty()) {
            snackbar.setAction(actionLabel, listener);
        }

        snackbar.show();*/
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        getPresenter().onClick(v.getId());
    }

    protected void configureSubscriptions() {
    }

    protected void setupAppbar(Toolbar toolbar, boolean homeAsUpEnabled) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle("");
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(homeAsUpEnabled);
                if (getHomeAsUpIndicator() != DEFAULT_HOME_AS_UP_INDICATOR) {
                    actionBar.setHomeAsUpIndicator(getHomeAsUpIndicator());
                }
            }
            if (homeAsUpEnabled) {
                setToolBarBackGround(Color.parseColor("#F9F9F9"));
            } else {
                setToolBarBackGround(Color.WHITE);
            }
            setToolbarBackArrowAction();
        }
    }

    protected
    @DrawableRes
    int getHomeAsUpIndicator() {
        return DEFAULT_HOME_AS_UP_INDICATOR;
    }

    protected void setToolbarBackArrowAction() {
        if (toolbar != null) {

            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    protected void setupAppbarCloseIcon(Toolbar toolbar, int arrowType) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                if (arrowType == ARROW_WITH_WHITE) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow_white);
                    setToolBarBackGround(Color.parseColor("#00AAD1"));
                    toolBarTitle.setTextColor(Color.WHITE);
                    toolBarSubTitle.setTextColor(Color.WHITE);
                } else if (arrowType == ARROW_WITH_HALF_WHITE) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow_half_white);
                    setToolBarBackGround(Color.WHITE);
                } else if (arrowType == CLOSE_ICON_WHITE) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
                }

            }
            setToolbarBackArrowAction();
        }
    }

    protected void configureWindow() {
    }

    protected void getToolbar(Toolbar toolbar) {

    }


    public int getEnterAnimation() {
        return R.anim.slide_in_right;
    }

    public int getExitAnimation() {
        return R.anim.slide_out_right;
    }

    @Override
    public void finish() {
        super.finish();
        subscriptions.clear();
        overridePendingTransition(R.anim.no_animation, getExitAnimation());
    }

    private void initComponent() {
        // this.a = DaggerActivityComponent.builder()
        //       .build();
    }

    protected ApplicationComponent getComponent() {
        return ((Lattis) getApplication()).getApplicationComponent();
    }

    protected void removeFragment(@IdRes int containerId, BaseFragment fragment) {
    }

    protected void replaceFragment(@IdRes int containerId, BaseFragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(fragment.getFragmentTag())
                .commit();
    }

    protected void replaceFragment(@IdRes int containerId, Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void addFragment(@IdRes int containerId,
                               Fragment fragment,
                               boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .add(containerId, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commit();
    }

    protected String getFragmentTag(@IdRes int containerId) {
        return getSupportFragmentManager().findFragmentById(containerId).getTag();
    }

    protected Fragment getFragment(@IdRes int containerId) {
        return getSupportFragmentManager().findFragmentById(containerId);
    }

    @Override
    public void setToolbarHeader(String title) {
        toolBarTitle.setText(title);
        toolBarTitle.setVisibility(View.VISIBLE);

    }

    @Override
    public void hideToolbar() {
        toolBarTitle.setText("");
        toolBarSubTitle.setText("");
        toolBarSubTitle.setVisibility(View.GONE);
        currentLocationLayout.setVisibility(View.GONE);
    }

    @Override
    public void setToolbarDescription(String subtitle) {
        toolBarSubTitle.setText(subtitle);
        toolBarTitle.setVisibility(View.GONE);
        toolBarSubTitle.setVisibility(View.VISIBLE);
        currentLocationLayout.setVisibility(View.VISIBLE);
    }

    protected void clearBackStack() {
        getSupportFragmentManager().popBackStack();
    }

    protected void logCustomException(Throwable e){
        getPresenter().logCustomException(e);
    }
}
