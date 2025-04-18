package com.lattis.ellipse.domain.interactor.card;

import com.lattis.ellipse.data.network.model.response.card.AddCardResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.CardRepository;

import org.json.JSONObject;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/26/17.
 */

public class AddCardUseCase extends UseCase<AddCardResponse> {

    private CardRepository cardRepository;
    private String cardNumber;
    private int expiry_month;
    private int expiry_year;
    private String cvc;
    private JSONObject intent;


    @Inject
    public AddCardUseCase(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                          CardRepository cardRepository) {
        super(threadExecutor, postExecutionThread);
        this.cardRepository = cardRepository;
    }

    public AddCardUseCase withCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public AddCardUseCase withExpiryMonth(int expiry_month) {
        this.expiry_month = expiry_month;
        return this;
    }

    public AddCardUseCase withExpiryYear(int expiry_year) {
        this.expiry_year = expiry_year;
        return this;
    }

    public AddCardUseCase withCVC(String cvc) {
        this.cvc = cvc;
        return this;
    }

    public AddCardUseCase withIntent(JSONObject intent) {
        this.intent = intent;
        return this;
    }

    @Override
    protected Observable<AddCardResponse> buildUseCaseObservable() {
        return cardRepository.addCard(cardNumber,expiry_month,expiry_year,cvc,intent);
    }


}
