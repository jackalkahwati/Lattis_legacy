package com.lattis.ellipse.presentation.ui.base.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by lattis on 29/08/17.
 */

public abstract class BaseSearchPlaceActivity < Presenter extends ActivityPresenter> extends BaseCloseActivity
implements GoogleApiClient.OnConnectionFailedListener{
    GoogleApiClient mGoogleApiClient;



}
