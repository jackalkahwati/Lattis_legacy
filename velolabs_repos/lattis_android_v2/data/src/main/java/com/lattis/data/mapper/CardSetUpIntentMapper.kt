package com.lattis.data.mapper

import com.lattis.data.entity.response.card.SetUpIntentResponse
import com.lattis.domain.models.SetUpIntent
import javax.inject.Inject

class CardSetUpIntentMapper @Inject
constructor() : AbstractDataMapper<SetUpIntentResponse, SetUpIntent>() {

    override fun mapIn(setUpIntentResponse: SetUpIntentResponse?): SetUpIntent {
        val setUpIntent = SetUpIntent()
        if(setUpIntentResponse!=null && setUpIntentResponse.setUpIntentDataResponse!=null){
            setUpIntent.application = setUpIntentResponse.setUpIntentDataResponse?.application
            setUpIntent.cancellation_reason = setUpIntentResponse.setUpIntentDataResponse?.cancellation_reason
            setUpIntent.client_secret = setUpIntentResponse.setUpIntentDataResponse?.client_secret
            setUpIntent.created = setUpIntentResponse.setUpIntentDataResponse?.created
            setUpIntent.customer = setUpIntentResponse.setUpIntentDataResponse?.customer
            setUpIntent.description = setUpIntentResponse.setUpIntentDataResponse?.description
            setUpIntent.id = setUpIntentResponse.setUpIntentDataResponse?.id
            setUpIntent.last_setup_error = setUpIntentResponse.setUpIntentDataResponse?.last_setup_error
            setUpIntent.livemode = setUpIntentResponse.setUpIntentDataResponse?.isLivemode
            setUpIntent.next_action = setUpIntentResponse.setUpIntentDataResponse?.next_action
            setUpIntent.`object` = setUpIntentResponse.setUpIntentDataResponse?.`object`
            setUpIntent.on_behalf_of = setUpIntentResponse.setUpIntentDataResponse?.on_behalf_of
            setUpIntent.payment_method = setUpIntentResponse.setUpIntentDataResponse?.payment_method
            setUpIntent.payment_method_types = setUpIntentResponse.setUpIntentDataResponse?.payment_method_types
            setUpIntent.status = setUpIntentResponse.setUpIntentDataResponse?.status
            setUpIntent.usage = setUpIntentResponse.setUpIntentDataResponse?.usage
        }
        return setUpIntent
    }

    override fun mapOut(setUpIntent: SetUpIntent?): SetUpIntentResponse? {
        return null
    }
}