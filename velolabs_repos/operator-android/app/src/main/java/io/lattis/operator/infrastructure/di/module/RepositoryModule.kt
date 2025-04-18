package io.lattis.operator.infrastructure.di.module

import dagger.Binds
import dagger.Module
import io.lattis.data.executor.JobExecutor
import io.lattis.data.repository.implementation.api.*
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.repository.*
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindThreadExecutor(jobExecutor: JobExecutor): ThreadExecutor


    @Binds
    abstract fun lattisAuthenticator(lattisAuthenticator: OperatorAuthenticator): Authenticator

    @Binds
    abstract fun accountRepository(accountRepository: AndroidAccountRepository): AccountRepository

    @Binds
    abstract fun fleetRepository(fleetRepositoryImp: FleetRepositoryImp):FleetRepository

    @Binds
    abstract fun vehicleRepository(vehicleRepository: VehicleRepositoryImp):VehicleRepository

    @Binds
    abstract fun ticketRepository(ticketRepository: TicketRepositoryImp):TicketRepository

    @Binds
    abstract fun thingRepository(thingRepository: ThingRepositoryImp):ThingRepository

    @Binds
    abstract fun userRepository(userRepository: UserRepositoryImp):UserRepository

    @Binds
    abstract fun locationRepository(locationRepository: LocationRepositoryImp):LocationRepository
}