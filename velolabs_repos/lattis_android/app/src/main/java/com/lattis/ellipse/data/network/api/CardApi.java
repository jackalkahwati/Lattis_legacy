package com.lattis.ellipse.data.network.api;

import com.lattis.ellipse.data.network.model.body.card.AddCardBody;
import com.lattis.ellipse.data.network.model.body.card.DeleteCardBody;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.card.AddCardResponse;
import com.lattis.ellipse.data.network.model.response.card.GetCardResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentDataResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/26/17.
 */

public interface CardApi {

    @POST("users/add-cards/")
    Observable<AddCardResponse> addCard(@Body AddCardBody addCardBody);

    @POST("users/add-cards?action=setup_intent")
    Observable<SetUpIntentResponse> getSetUpIntent();

    @POST("users/get-cards/")
    Observable<GetCardResponse> getCard();

    @POST("users/delete-card/")
    Observable<BasicResponse> deleteCard(@Body DeleteCardBody deleteCardBody);

    @POST("users/set-card-primary/")
    Observable<BasicResponse> updateCard(@Body DeleteCardBody deleteCardBody);
}
