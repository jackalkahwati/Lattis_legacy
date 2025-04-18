package com.lattis.lattis.presentation.history

import com.lattis.domain.usecase.history.GetRideHistorUseCase
import com.lattis.domain.models.RideHistory
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import java.util.*
import javax.inject.Inject

class RideHistoryActivityPresenter @Inject constructor(
    val getRideHistorUseCase: GetRideHistorUseCase
) :ActivityPresenter<RideHistoryActivityView>(){
    var rideHistoryData : ArrayList<RideHistory.RideHistoryData> = arrayListOf<RideHistory.RideHistoryData>()

    fun getRideHistory() {
        subscriptions.add(
            getRideHistorUseCase
                .execute(object : RxObserver<RideHistory>() {
                    override fun onNext(rideHistory: RideHistory) {
                        if (rideHistory != null && rideHistory.rideHistoryData!=null && rideHistory.rideHistoryData!!.size>0) {
                            rideHistoryData.addAll(rideHistory?.rideHistoryData!!)
                                if (rideHistoryData!!.get(0).date_endtrip == null
                                ) {
                                    rideHistoryData!!.removeAt(0)
                                }
                                view?.onRideHistorySuccess()
                        }else{
                            view?.onNoRideHistory()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onRideHistoryFailure()
                    }
                })
        )
    }

}