package com.lattis.ellipse.domain.interactor.card;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.repository.CardRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/26/17.
 */

public class GetCardUseCase extends UseCase<List<Card>> {

    private CardRepository cardRepository;

    @Inject
    public GetCardUseCase(ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread,
                          CardRepository cardRepository) {
        super(threadExecutor, postExecutionThread);
        this.cardRepository = cardRepository;
    }

    @Override
    protected Observable<List<Card>> buildUseCaseObservable() {
        return cardRepository.getCard();
    }

}
