package com.lattis.ellipse.domain.interactor.emergency;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Contact;
import com.lattis.ellipse.domain.repository.EmergencyRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class SendEmergencyAlertContactUseCase extends UseCase<Void> {

    public final static String TAG = SendEmergencyAlertContactUseCase.class.getSimpleName();

    private EmergencyRepository emergencyRepository;
    private Contact contact;

    @Inject
    protected SendEmergencyAlertContactUseCase(ThreadExecutor threadExecutor,
                                               PostExecutionThread postExecutionThread,
                                               EmergencyRepository emergencyRepository) {
        super(threadExecutor, postExecutionThread);
        this.emergencyRepository = emergencyRepository;
    }

    public SendEmergencyAlertContactUseCase withCurrentObservable(Contact contact) {
        this.contact = contact;
        return this;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return emergencyRepository.sendEmergencyAlert(contact);
    }
}
