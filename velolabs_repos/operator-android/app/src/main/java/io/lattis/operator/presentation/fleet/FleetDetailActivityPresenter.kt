package io.lattis.operator.presentation.fleet

import android.os.Bundle
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.User
import io.lattis.domain.usecase.user.GetMeUseCase
import io.lattis.domain.usecase.user.LogOutUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.lattis.operator.presentation.fleet.FleetDetailActivity.Companion.FLEET
import javax.inject.Inject

class FleetDetailActivityPresenter @Inject constructor(
        val getMeUseCase: GetMeUseCase,
        val logOutUseCase: LogOutUseCase
) : ActivityPresenter<FleetDetailActivityView>() {

    lateinit var fleet:Fleet
    var user:User.Operator?=null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(FLEET)) {
            val referencedFleetString = arguments.getString(FLEET)
            fleet = Gson().fromJson(referencedFleetString,Fleet::class.java)
        }
        getMe()
    }

    fun getMe() {
        subscriptions.add(
            getMeUseCase
                .execute(object : RxObserver<User.Operator>(view, false) {
                    override fun onNext(newUser:User.Operator) {
                        super.onNext(newUser)
                        user = newUser
                        view?.onUserSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }
                })
        )
    }

    fun logOut() {
        subscriptions.add(logOutUseCase.execute(object : RxObserver<Boolean>(view, false) {
            override fun onNext(success: Boolean) {
                view?.onLogOutSuccessfull()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                view?.onLogOutFailure()
            }
        }))
    }

}