package com.lattis.ellipse.domain.interactor.bike;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.repository.BikeRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/13/17.
 */

public class BikeDetailUseCase extends UseCase<Bike> {

    private BikeRepository bikeRepository;
    private int bike_id;
    private int qr_code_id=-1;


    @Inject
    public BikeDetailUseCase(ThreadExecutor threadExecutor,
                            PostExecutionThread postExecutionThread,
                            BikeRepository bikeRepository) {
        super(threadExecutor, postExecutionThread);
        this.bikeRepository = bikeRepository;
    }


    public BikeDetailUseCase withBikeId(int bike_id) {
        this.bike_id = bike_id;
        return this;
    }

    public BikeDetailUseCase withQRCodeId(int qr_code_id) {
        this.qr_code_id = qr_code_id;
        return this;
    }


    @Override
    protected Observable<Bike> buildUseCaseObservable() {
        return bikeRepository.bikeDetails(bike_id,qr_code_id);
    }

}
