package com.lattis.lattis.infrastructure.di.module

import com.lattis.data.executor.JobExecutor
import com.lattis.data.repository.implementation.api.*
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.*
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun userRepository(userRespository: UserRepositoryImp): UserRepository


    @Binds
    abstract fun bindThreadExecutor(jobExecutor: JobExecutor): ThreadExecutor


    @Binds
    abstract fun lattisAuthenticator(lattisAuthenticator: LattisAuthenticator):Authenticator

    @Binds
    abstract fun accountRepository(accountRepository: AndroidAccountRepository):AccountRepository

    @Binds
    @Singleton
    abstract fun locationRepository(locationRepository: LocationRepositoryImp):LocationRepository

    @Binds
    abstract fun bikeRepository(bikeRepository: BikeRepositoryImp):BikeRepository

    @Binds
    abstract fun cardRepository(cardRepository: CardRepositoryImp):CardRepository

    @Binds
    abstract fun lockRepository(lockRepository: LockRepositoryImp):LockRepository

    @Binds
    abstract fun bluetoothRepository(bluetoothRepository: BluetoothRepositoryImp):BluetoothRepository

    @Binds
    abstract fun rideRepository(rideRepository: RideRepositoryImp):RideRepository

    @Binds
    @Singleton
    abstract fun activeTripRepository(activeTripRepositoryImp: ActiveTripRepositoryImp):ActiveTripRepository

    @Binds
    abstract fun uploadImageRepository(uploadImageRepositoryImp: UploadImageRepositoryImp):UploadImageRepository

    @Binds
    abstract fun parkingRepository(parkingRepositoryImp: ParkingRepositoryImp):ParkingRepository

    @Binds
    abstract fun savedAddressRepository(savedAddressRepositoryImp: SavedAddressRepositoryImp):SavedAddressRepository

    @Binds
    abstract fun maintenanceRepository(maintenaceRepository: MaintenaceRepositoryImp):MaintenanceRepository

    @Binds
    abstract fun reservationRepository(reservationRepositoryImp: ReservationRepositoryImp):ReservationRepository

    @Binds
    abstract fun axaLockRepository(axaLockRepository: AxaLockRepositoryImp):AxaLockRepository

    @Binds
    abstract fun membershipRepository(membershipRepositoryImp: MembershipRepositoryImp):MembershipRepository

    @Binds
    abstract fun dockHubsRepository(dockHubRepositoryImp: DockHubRepositoryImp):DockHubRepository

    @Binds
    abstract fun promotionRepository(promotionRepository: PromotionRepositoryImp):PromotionRepository

    @Binds
    abstract fun appsRepository(appsRepositoryImp: AppsRepositoryImp):AppsRepository

    @Binds
    abstract fun saSOrPSLockRepository(saSOrPSLockRepositoryImp: SaSOrPSLockRepositoryImp):SaSOrPSLockRepository

    @Binds
    abstract fun v2Repository(v2ApiRepositoryImp: V2ApiRepositoryImp):V2ApiRepository

}