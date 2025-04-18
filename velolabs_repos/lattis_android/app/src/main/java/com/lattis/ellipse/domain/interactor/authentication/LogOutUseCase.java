package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.DataBaseManager;
import com.lattis.ellipse.presentation.setting.IntPref;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_COUNT;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_WALK_THROUGH_STRING;

public class LogOutUseCase extends UseCase<Boolean> {

    private final AccountRepository accountRepository;
    private final DataBaseManager dataBaseManager;
    private final BluetoothRepository bluetoothRepository;
    private IntPref rideTimeWalkThroughPref;
    private IntPref rideCountPref;
    @Inject
    LogOutUseCase(ThreadExecutor threadExecutor,
                  PostExecutionThread postExecutionThread,
                  AccountRepository accountRepository,
                  DataBaseManager dataBaseManager,
                  BluetoothRepository bluetoothRepository,
                  @Named(KEY_RIDE_WALK_THROUGH_STRING) IntPref rideTimeWalkThroughPref,
                  @Named(KEY_RIDE_COUNT) IntPref rideCountPref) {
        super(threadExecutor, postExecutionThread);
        this.accountRepository = accountRepository;
        this.dataBaseManager = dataBaseManager;
        this.bluetoothRepository = bluetoothRepository;
        this.rideTimeWalkThroughPref =rideTimeWalkThroughPref;
        this.rideCountPref = rideCountPref;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.accountRepository.signOut()
                .flatMap(deleteDatabase)
                .flatMap(disconnectAllLocks)
                .flatMap(deleteSharedPreference);
    }

    private Function<Boolean, Observable<Boolean>> deleteDatabase = new Function<Boolean, Observable<Boolean>>() {
        @Override
        public Observable<Boolean> apply(Boolean status) {
            return dataBaseManager.deleteDataBase();
        }
    };

    private Function<Boolean, Observable<Boolean>> disconnectAllLocks = new Function<Boolean, Observable<Boolean>>() {
        @Override
        public Observable<Boolean> apply(Boolean success) {
            return bluetoothRepository.disconnectAllLocks();
        }
    };

    private Function<Boolean, Observable<Boolean>> deleteSharedPreference = new Function<Boolean, Observable<Boolean>>() {
        @Override
        public Observable<Boolean> apply(Boolean status) {
            rideTimeWalkThroughPref.remove();
            rideCountPref.remove();
            return  Observable.just(true);
        }
    };
}
