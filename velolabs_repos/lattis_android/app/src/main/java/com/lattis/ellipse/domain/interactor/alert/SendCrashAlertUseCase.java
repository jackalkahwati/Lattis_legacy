package com.lattis.ellipse.domain.interactor.alert;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.AlertRepository;
import com.lattis.ellipse.domain.repository.LocationRepository;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class SendCrashAlertUseCase extends UseCase<Boolean> {

    private LocationRepository locationRepository;
    private UserRepository userRepository;
    private AlertRepository alertRepository;

    @Inject
    protected SendCrashAlertUseCase(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    LocationRepository locationRepository,
                                    AlertRepository alertRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        // TODO: Send crash alert
        return Observable.just(null);
//        return Observable.zip(locationRepository.getLastLocation(),
//                Observable.just(null), Observable.just(null),
//                new Func3<Location, List<Contact>, String, Boolean>() {
//
//                    @Override
//                    public Boolean call(Location location, List<Contact> emergencyContactList, String macId) {
//                        return userRepository.sendCrashAlert(emergencyContactList, location, macId);
//                    }
//
//                });
    }

}
