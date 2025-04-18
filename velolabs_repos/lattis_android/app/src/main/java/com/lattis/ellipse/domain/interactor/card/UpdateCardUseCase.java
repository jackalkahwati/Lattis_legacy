package com.lattis.ellipse.domain.interactor.card;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.CardRepository;

import javax.inject.Inject;

import io.reactivex.Observable;


public class UpdateCardUseCase extends UseCase<BasicResponse> {

    private CardRepository cardRepository;


    public UpdateCardUseCase setCardId(int cardId) {
        this.cardId = cardId;
        return this;
    }

    private int cardId;


    @Inject
    public UpdateCardUseCase(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                             CardRepository cardRepository) {
        super(threadExecutor, postExecutionThread);
        this.cardRepository = cardRepository;
    }


    @Override
    protected Observable<BasicResponse> buildUseCaseObservable() {
        return cardRepository.updateCard(cardId);
    }


}
