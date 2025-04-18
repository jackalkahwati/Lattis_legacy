package com.lattis.ellipse.domain.interactor.bike;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.BikeRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/4/17.
 */

public class CancelReserveBikeUseCase extends UseCase<Boolean> {

    private BikeRepository bikeRepository;
    private int bike_id;
    private boolean bike_damaged = false;
    private boolean lockIssue=false;

    @Inject
    public CancelReserveBikeUseCase(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    BikeRepository bikeRepository) {
        super(threadExecutor, postExecutionThread);
        this.bikeRepository = bikeRepository;
    }

    public CancelReserveBikeUseCase withBikeId(int bike_id) {
        this.bike_id = bike_id;
        return this;
    }

    public CancelReserveBikeUseCase withDamage(boolean bike_damaged) {
        this.bike_damaged = bike_damaged;
        return this;
    }

    public CancelReserveBikeUseCase withLockIssue(boolean lockIssue) {
        this.lockIssue = lockIssue;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return bikeRepository.cancelBike(bike_id, bike_damaged,lockIssue);
    }
}
