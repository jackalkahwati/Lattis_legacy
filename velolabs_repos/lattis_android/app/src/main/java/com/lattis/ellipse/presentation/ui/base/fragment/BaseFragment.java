package com.lattis.ellipse.presentation.ui.base.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.lattis.ellipse.Lattis;
import com.lattis.ellipse.presentation.dagger.component.ApplicationComponent;
import com.lattis.ellipse.presentation.ui.base.BaseView;
import com.lattis.ellipse.presentation.ui.base.DataView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

import static android.content.Context.INPUT_METHOD_SERVICE;

public abstract class BaseFragment<Presenter extends FragmentPresenter> extends Fragment
        implements BaseView, View.OnClickListener {


    protected boolean meVisible = false;

    private static final int NO_MENU_ID = -1;
    protected CompositeDisposable subscriptions = new CompositeDisposable();

    private DataView parentDataView;

    @NonNull
    public abstract String getFragmentTag();

    protected abstract void inject();

    @NonNull
    protected abstract Presenter getPresenter();

    @LayoutRes
    protected abstract int getFragmentLayoutId();

    protected int getMenuId() {
        return NO_MENU_ID;
    }

    protected void configureViews() {
    }

    protected void configureSubscriptions() {
    }

    protected void getToolbar(Toolbar toolbar) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getMenuId() != NO_MENU_ID) {
            inflater.inflate(getMenuId(), menu);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataView) {
            this.parentDataView = (DataView) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.parentDataView = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);

        Presenter presenter = getPresenter();
        if (savedInstanceState == null) {
            presenter.onCreate(getArguments(), this);
        } else {
            presenter.onRecreate(savedInstanceState, this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayoutId(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViews();
        setHasOptionsMenu(getMenuId() != NO_MENU_ID);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPresenter().onResume(this);
        configureSubscriptions();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPresenter().onPause();
        subscriptions.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        meVisible=false;
        boolean isFinishing = getActivity().isFinishing() || isRemoving();
        getPresenter().onDestroy(isFinishing);
    }

    @Override
    public void showLoading(boolean hideContent) {
        if (parentDataView != null) {
            parentDataView.showLoading(hideContent);
        }
    }

    @Override
    public void setToolbarDescription(String subtitle) {
        if (parentDataView != null) {
            parentDataView.setToolbarDescription(subtitle);
        }
    }

    @Override
    public void setToolbarHeader(String title) {
        if (parentDataView != null) {
            parentDataView.setToolbarHeader(title);
        }
    }

    @Override
    public void hideLoading() {
        if (parentDataView != null) {
            parentDataView.hideLoading();
        }
    }

    @Override
    public void hideToolbar() {
        if (parentDataView != null) {
            parentDataView.hideToolbar();
        }
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
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToastMessage(String message, int duration) {
        Toast.makeText(getActivity(), message, duration).show();
    }

    @Override
    public void showSnackBar(String message, View.OnClickListener action, @StringRes int actionLabel) {
        /*if (listener == null) {
            return;
        }

        Snackbar snackbar = Snackbar.make(listener.getContentLayout(), message, Snackbar.LENGTH_LONG);

        if (action != null && actionLabel > 0) {
            snackbar.setAction(actionLabel, action);
        }

        snackbar.show();*/
    }

    @Override
    public void showSnackBar(String message, View.OnClickListener action, String actionLabel) {
        /*if (listener == null) {
            return;
        }

        Snackbar snackbar = Snackbar.make(listener.getContentLayout(), message, Snackbar.LENGTH_LONG);

        if (action != null && actionLabel !=null) {
            snackbar.setAction(actionLabel, action);
        }

        snackbar.show();*/
    }

    @Override
    public void hideKeyboard() {
        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        getPresenter().onClick(v.getId());
    }

    protected ApplicationComponent getComponent() {
        return ((Lattis) getActivity().getApplication()).getApplicationComponent();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    public void setScrollListener() {
    }

    public void setMapBox(MapboxMap mapBox) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        meVisible = !hidden;
    }

    protected void logCustomException(Throwable e){
       getPresenter().logCustomException(e);
    }

}
