package io.lattis.operator.presentation.home

import android.text.TextUtils
import io.lattis.domain.models.Fleet
import io.lattis.domain.usecase.fleet.GetFleetsUseCase
import io.lattis.domain.usecase.fleet.SaveUserFleetUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import javax.inject.Inject

class HomeActivityPresenter @Inject constructor(
    private val getFleetsUseCase: GetFleetsUseCase,
    val saveUserFleetUseCase: SaveUserFleetUseCase
) : ActivityPresenter<HomeActivityView>() {

    var fleets:List<Fleet>?=null
    var userSavedFleet:Fleet?=null

    fun getFleets() {
        subscriptions.add(
            getFleetsUseCase
                .execute(object : RxObserver<List<Fleet>>(view, false) {
                    override fun onNext(newFleets:List<Fleet>) {
                        super.onNext(newFleets)
                        fleets = newFleets
                        view?.onFleetsSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onFleetsFailure()

                    }
                })
        )
    }

    fun saveUserFleet(fleet: Fleet) {
        userSavedFleet  = fleet
        subscriptions.add(
            saveUserFleetUseCase
                .withFleet(fleet)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status:Boolean) {
                        super.onNext(status)
                        view?.onUserFleetSaveSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onUserFleetSaveFailure()
                    }
                })
        )
    }

    fun filter(text: String?) {

        if(fleets==null || fleets?.isEmpty()!!)
            return

        if(TextUtils.isEmpty(text)){
            view?.showOriginalList()
            return
        }

        val filteredlist = ArrayList<Fleet>()

        for (item in fleets!!) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name?.toLowerCase()?.contains(text?.toLowerCase()!!)!!) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        view?.showFilteredList(filteredlist)


    }

}