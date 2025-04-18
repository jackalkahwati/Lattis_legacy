package com.lattis.ellipse.presentation.ui.payment;


import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.card.AddCardResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentResponse;
import com.lattis.ellipse.domain.interactor.card.AddCardUseCase;
import com.lattis.ellipse.domain.interactor.card.DeleteCardUseCase;
import com.lattis.ellipse.domain.interactor.card.GetSetUpIntentUseCase;
import com.lattis.ellipse.domain.interactor.user.GetUserUseCase;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.Customer;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.SetupIntent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.adapter.rxjava2.HttpException;

public class AddCardPresenter extends ActivityPresenter<AddCardView> {
    private final AddCardUseCase addCardUseCase;
    private com.lattis.ellipse.domain.model.Card card;
    private final DeleteCardUseCase deleteCardUseCase;
    private final Stripe stripe;
    private final GetSetUpIntentUseCase getSetUpIntentUseCase;
    private final GetUserUseCase getUserUseCase;
    private User user;
    private Card addNewCard;
    private SetUpIntentResponse setUpIntentResponse;



    @Inject
    AddCardPresenter(AddCardUseCase addCardUseCase,
                     DeleteCardUseCase deleteCardUseCase,
                     Stripe stripe,
                     GetSetUpIntentUseCase getSetUpIntentUseCase,
                     GetUserUseCase getUserUseCase) {
        this.addCardUseCase = addCardUseCase;
        this.deleteCardUseCase = deleteCardUseCase;
        this.stripe = stripe;
        this.getSetUpIntentUseCase = getSetUpIntentUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    public Stripe getStripe() {
        return stripe;
    }

    public String getClient_secret() {
        return setUpIntentResponse!=null ? setUpIntentResponse.getSetUpIntentDataResponse().getClient_secret():null;
    }

    @DebugLog
    public void addCard(SetupIntent setupIntent) {

        if(addNewCard==null){
            return;
        }

        subscriptions.add(addCardUseCase
                .withCardNumber(addNewCard.getNumber())
                .withExpiryMonth(addNewCard.getExpMonth())
                .withExpiryYear(addNewCard.getExpYear())
                .withCVC(addNewCard.getCVC())
                .withIntent(getSetupIntent(setupIntent))
                .execute(new RxObserver<AddCardResponse>(view) {
                    @Override
                    public void onNext(AddCardResponse addCardResponse) {
                        super.onNext(addCardResponse);
                        if (addCardResponse == null) {
                            view.onCardAddFailure();
                        } else {
                            view.onCardAddSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 410) {
                                view.onCardAlreadyExists();
                            }else if (exception.code() == 409) {
                                view.onCardInvalid();
                            }else{
                                view.onCardAddFailure();
                            }
                        }else{
                            view.onCardAddFailure();
                        }
                    }
                }));
    }

    @DebugLog
    public void deleteCard(int id) {
        subscriptions.add(deleteCardUseCase
                .setCardId(id)
                .execute(new RxObserver<BasicResponse>() {
                    @Override
                    public void onNext(BasicResponse o) {
                        super.onNext(o);
                        view.onDeleteCardSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onDeleteCardFailure();
                    }
                }));
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null && arguments.containsKey("CARD_DETAILS")) {
            this.card = new Gson().fromJson(arguments.getString("CARD_DETAILS")
                    , com.lattis.ellipse.domain.model.Card.class);
            view.setCardDetails(this.card);
        }else{
            getSetUpIntent();
        }
    }



    private void getSetUpIntent(){
        subscriptions.add(getSetUpIntentUseCase.execute(new RxObserver<SetUpIntentResponse>() {
            @Override
            public void onNext(SetUpIntentResponse setUpIntentRes) {
                super.onNext(setUpIntentRes);
                setUpIntentResponse =setUpIntentRes;
            }
            @Override
            public void onError(Throwable e) {
                super.onError(e);
                view.showError();
            }
        }));
    }



    public void createPaymentMethod(Card card) {
        this.addNewCard = card;
        PaymentMethodCreateParams.Card paymentMethodParamsCard = card.toPaymentMethodParamsCard();
        PaymentMethodCreateParams cardPaymentMethodCreateParams = PaymentMethodCreateParams.create(paymentMethodParamsCard, null);
        Observable<PaymentMethod> paymentMethodObservable = Observable.fromCallable(() ->
            stripe.createPaymentMethodSynchronous(cardPaymentMethodCreateParams)
        );
        subscriptions.add(paymentMethodObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<PaymentMethod>() {
                    @Override
                    public void onNext(PaymentMethod paymentMethod) {
                        if(view!=null){
                            view.confirmSetupIntent(paymentMethod.id);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(view!=null){
                            view.showError();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                }));

    }


    public JSONObject getSetupIntent(SetupIntent setupIntent){
        try {
            JSONObject intent = new JSONObject();
            intent.put("id",setupIntent.getId());
            intent.put("object","setup_intent");
            intent.put("cancellation_reason",setupIntent.getCancellationReason());
            intent.put("client_secret",setupIntent.getClientSecret());
            intent.put("created",setupIntent.getCreated());
            intent.put("description",setupIntent.getDescription());
            intent.put("last_setup_error",setupIntent.getLastSetupError());
            intent.put("livemode",setupIntent.isLiveMode());
            intent.put("next_action",setupIntent.getNextActionType());
            intent.put("payment_method",setupIntent.getPaymentMethodId());
            if(setupIntent.getPaymentMethodTypes()!=null && setupIntent.getPaymentMethodTypes().size()>0){
                JSONArray jsonArray = new JSONArray();
                for(String methodTypes: setupIntent.getPaymentMethodTypes()){
                    jsonArray.put(methodTypes);
                }
                intent.put("payment_method_types",jsonArray);
            }
            intent.put("status",setupIntent.getStatus());
            intent.put("usage",setupIntent.getUsage());
            return intent;
        } catch (JSONException e) {

        }
        return null;
    }


    public void getUserProfile(){
       subscriptions.add(getUserUseCase.execute(new RxObserver<User>(view, false) {
            @Override
            public void onNext(User currUser) {
                user = currUser;
            }
            @Override
            public void onError(Throwable e) {
                super.onError(e);

            }
        }));
    }



}
