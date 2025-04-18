package com.lattis.lattis.presentation.base.activity

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.lattis.lattis.presentation.base.BaseView

abstract class BaseSearchPlaceActivity<Presenter : ActivityPresenter<V>,V:BaseView> :
    BaseCloseActivity<Presenter,V>(), OnConnectionFailedListener {
    var mGoogleApiClient: GoogleApiClient? = null
}