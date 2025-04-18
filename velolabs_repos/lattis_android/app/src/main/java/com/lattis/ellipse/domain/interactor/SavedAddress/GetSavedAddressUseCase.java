package com.lattis.ellipse.domain.interactor.SavedAddress;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.SavedAddress;
import com.lattis.ellipse.domain.repository.SavedAddressRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class GetSavedAddressUseCase extends UseCase<List<SavedAddress>>

{

    private SavedAddressRepository savedAddressRepository;
    private SavedAddress savedAddress;

    @Inject
    public GetSavedAddressUseCase(ThreadExecutor threadExecutor,
                               PostExecutionThread postExecutionThread,
                               SavedAddressRepository savedAddressRepository) {
        super(threadExecutor, postExecutionThread);
        this.savedAddressRepository = savedAddressRepository;
    }


    @Override
    protected Observable<List<SavedAddress>> buildUseCaseObservable() {
        return this.savedAddressRepository.getSavedAddresses();
    }
}
