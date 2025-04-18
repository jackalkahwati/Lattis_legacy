package com.lattis.ellipse.domain.interactor.card;

import com.lattis.ellipse.data.network.model.response.card.SetUpIntentResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.CardRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class GetSetUpIntentUseCase extends UseCase<SetUpIntentResponse> {

    private CardRepository cardRepository;

    @Inject
    public GetSetUpIntentUseCase(ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread,
                          CardRepository cardRepository) {
        super(threadExecutor, postExecutionThread);
        this.cardRepository = cardRepository;
    }

    @Override
    protected Observable<SetUpIntentResponse> buildUseCaseObservable() {
        return cardRepository.getSetUpIntent();
    }

}
