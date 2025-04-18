package com.lattis.ellipse.domain.interactor.bike;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.BikeRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 8/31/17.
 */

public class UpdateBikeMetaDatUseCase extends UseCase<Boolean> {

    private BikeRepository bikeRepository;
    private String firmware_version;
    private Boolean shackle_jam;
    private int bike_battery_level = -1;
    private int lock_battery_level = -1;
    private int bike_id;


    @Inject
    public UpdateBikeMetaDatUseCase(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    BikeRepository bikeRepository) {
        super(threadExecutor, postExecutionThread);
        this.bikeRepository = bikeRepository;
    }


    public UpdateBikeMetaDatUseCase withFirmWare(String firmware_version) {
        this.firmware_version = firmware_version;
        return this;
    }

    public UpdateBikeMetaDatUseCase withShackleJamStatus(Boolean shackle_jam) {
        this.shackle_jam = shackle_jam;
        return this;
    }

    public UpdateBikeMetaDatUseCase withLockBattery(Integer lock_battery_level) {
        if(lock_battery_level!=null) {
            this.lock_battery_level = lock_battery_level;
        }
        return this;
    }

    public UpdateBikeMetaDatUseCase withBikeBattery(int bike_battery_level) {
        this.bike_battery_level = bike_battery_level;
        return this;
    }

    public UpdateBikeMetaDatUseCase forBike(int bike_id) {
        this.bike_id = bike_id;
        return this;
    }



    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return bikeRepository.updateBikeMetaData(bike_id, bike_battery_level, lock_battery_level, firmware_version, shackle_jam);
    }

}
