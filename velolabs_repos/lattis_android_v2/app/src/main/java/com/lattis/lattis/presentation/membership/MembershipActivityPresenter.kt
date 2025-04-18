package com.lattis.lattis.presentation.membership

import com.lattis.domain.models.*
import com.lattis.domain.usecase.card.GetCardUseCase
import com.lattis.domain.usecase.membership.GetMembershipsAndSubscriptionsUseCase
import com.lattis.domain.usecase.membership.SubscribeToMembershipUseCase
import com.lattis.domain.usecase.membership.UnSubscribeToMembershipUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject

class MembershipActivityPresenter @Inject constructor(
    val getMembershipsAndSubscriptionsUseCase: GetMembershipsAndSubscriptionsUseCase,
    val subscribeToMembershipUseCase: SubscribeToMembershipUseCase,
    val unSubscribeToMembershipUseCase: UnSubscribeToMembershipUseCase,
    val getCardUseCase: GetCardUseCase
) : ActivityPresenter<MembershipActivityView>(){

    var memberships:Memberships?=null
    var cards: List<Card>? = null
    var primaryUserCard:Card?=null
    var selectedMembership:Membership?=null
    var selectedSubscription:Subscription?=null



    fun getMembershipsAndSubscriptions(){
        memberships=null
        selectedSubscription=null
        selectedMembership=null
        subscriptions.add(
            getMembershipsAndSubscriptionsUseCase.execute(object : RxObserver<Memberships>(view, false) {
                override fun onNext(newMemberships: Memberships) {
                    super.onNext(newMemberships)
                    memberships = newMemberships
                    view?.onMembershipsSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onMembershipsFailure()
                }
            })
        )
    }

    fun subscribe(){
        subscriptions.add(
            subscribeToMembershipUseCase
                .withMembershipId(selectedMembership?.fleet_membership_id!!)
                .execute(object : RxObserver<Boolean>(view, false) {
                override fun onNext(status: Boolean) {
                    super.onNext(status)
                    getMembershipsAndSubscriptions()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onMembershipSubscribeFailure()
                }
            })
        )
    }

    fun alreadySubscribedToFleet():Boolean{
        if(memberships!=null && memberships?.subscriptions!=null && memberships?.subscriptions?.size!!>0){
            for(subscription in memberships?.subscriptions!!) {
                if (subscription.fleet_membership?.fleet_id == selectedMembership?.fleet_id) {
                    return true
                }
            }
        }
        return false
    }

    fun unsubscribe(){
        subscriptions.add(
            unSubscribeToMembershipUseCase
                .withMembershipId(selectedSubscription?.fleet_membership_id!!)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        view?.onMembershipUnSubscribeSuccess()
                        getMembershipsAndSubscriptions()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onMembershipUnSubscribeFailure()
                    }
                })
        )
    }

    fun getCards(){
        subscriptions.add(
            getCardUseCase.execute(object : RxObserver<List<Card>>(view) {
                override fun onNext(newCards: List<Card>) {
                    cards = newCards
                    primaryUserCard = getPrimaryCard()
                    view?.onCardListSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    cards=null
                    primaryUserCard=null
                }

            })
        )
    }

    fun getPrimaryCard():Card?{
        if(cards==null || cards?.size==0)
            return null

        for(card in cards!!){
            if(card.is_primary){
                return card
            }
        }

        return null

    }


}