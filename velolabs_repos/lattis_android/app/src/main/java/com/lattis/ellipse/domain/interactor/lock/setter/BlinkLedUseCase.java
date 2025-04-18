package com.lattis.ellipse.domain.interactor.lock.setter;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.BluetoothRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class BlinkLedUseCase extends UseCase<Void> {

    private BluetoothRepository repository;
    private String macAddress;

    @Inject
    protected BlinkLedUseCase(ThreadExecutor threadExecutor,
                              PostExecutionThread postExecutionThread,
                              BluetoothRepository repository) {
        super(threadExecutor, postExecutionThread);
        this.repository = repository;
    }

    public BlinkLedUseCase withMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return repository.blinkLed(macAddress);
    }
}
