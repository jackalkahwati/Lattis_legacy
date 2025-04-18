package com.lattis.lattis.presentation.fleet

import com.lattis.lattis.presentation.base.BaseView

interface PrivateFleetActivityView : BaseView{


    fun showPrivateFleetListView()
    fun showNoPrivateFleetView()

    fun onProfileFetchError()

    fun showLoadingForPrivateFleet(message: String?)
    fun hideLoadingForPrivateFleet()
}