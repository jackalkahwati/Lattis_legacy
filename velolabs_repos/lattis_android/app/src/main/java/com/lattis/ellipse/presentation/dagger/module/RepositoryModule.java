package com.lattis.ellipse.presentation.dagger.module;


import com.lattis.ellipse.data.AlertDataRepository;
import com.lattis.ellipse.data.BikeDataRepository;
import com.lattis.ellipse.data.CardDataRepository;
import com.lattis.ellipse.data.ContactDataRepository;
import com.lattis.ellipse.data.LockDataRepository;
import com.lattis.ellipse.data.MaintenanceDataRepository;
import com.lattis.ellipse.data.ParkingDataRepository;
import com.lattis.ellipse.data.ParkingZoneDataRepository;
import com.lattis.ellipse.data.RideDataRepository;
import com.lattis.ellipse.data.SavedAddressDataRepository;
import com.lattis.ellipse.data.UploadImageDataRepository;
import com.lattis.ellipse.data.UserDataRepository;
import com.lattis.ellipse.data.database.ContactRealmDataStore;
import com.lattis.ellipse.data.database.LockRealmDataStore;
import com.lattis.ellipse.data.database.MediaRealmDataStore;
import com.lattis.ellipse.data.database.PrivateNetworkRealmDataStore;
import com.lattis.ellipse.data.database.RideRealmDataStore;
import com.lattis.ellipse.data.database.SavedAddressDataStore;
import com.lattis.ellipse.data.database.UserRealmDataStore;
import com.lattis.ellipse.data.network.store.AlertNetworkDataStore;
import com.lattis.ellipse.data.network.store.BikeNetworkDataStore;
import com.lattis.ellipse.data.network.store.CardNetworkDataStore;
import com.lattis.ellipse.data.network.store.LockNetwordDataStore;
import com.lattis.ellipse.data.network.store.MaintenanceDataStore;
import com.lattis.ellipse.data.network.store.ParkingNetworkDataStore;
import com.lattis.ellipse.data.network.store.ParkingZoneDataStore;
import com.lattis.ellipse.data.network.store.RideNetworkDataStore;
import com.lattis.ellipse.data.network.store.UploadImageDataStore;
import com.lattis.ellipse.data.network.store.UserNetworkDataStore;
import com.lattis.ellipse.domain.repository.AlertRepository;
import com.lattis.ellipse.domain.repository.BikeRepository;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.CardRepository;
import com.lattis.ellipse.domain.repository.ContactRepository;
import com.lattis.ellipse.domain.repository.LockRepository;
import com.lattis.ellipse.domain.repository.MaintenanceRepository;
import com.lattis.ellipse.domain.repository.ParkingRepository;
import com.lattis.ellipse.domain.repository.ParkingZoneRepository;
import com.lattis.ellipse.domain.repository.RideRepository;
import com.lattis.ellipse.domain.repository.SavedAddressRepository;
import com.lattis.ellipse.domain.repository.UploadImageRepository;
import com.lattis.ellipse.domain.repository.UserRepository;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;
import com.lattis.ellipse.presentation.dagger.qualifier.UserId;
import com.lattis.ellipse.presentation.model.mapper.BikeModelMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    @Provides
    @Singleton
    UserRepository provideUserRepository(UserNetworkDataStore userNetworkDataStore,
                                         UserRealmDataStore userRealmDataStore,
                                         MediaRealmDataStore mediaRealmDataStore,
                                         PrivateNetworkRealmDataStore privateNetworkRealmDataStore,
                                         @UserId String userId) {
        return new UserDataRepository(userNetworkDataStore, userRealmDataStore, mediaRealmDataStore,privateNetworkRealmDataStore, userId);
    }

    @Provides
    @Singleton
    ContactRepository provideContactRepository(ContactRealmDataStore contactRealmDataStore) {
        return new ContactDataRepository(contactRealmDataStore);
    }


    @Provides
    @Singleton
    AlertRepository provideAlertRepository(AlertNetworkDataStore alertNetworkDataStore) {
        return new AlertDataRepository(alertNetworkDataStore);
    }

    @Provides
    @Singleton
    BikeRepository provideBikeRepository(BikeNetworkDataStore bikeNetworkDataStore, RideRealmDataStore rideRealmDataStore, @FleetId String fleetId, BikeModelMapper bikeModelMapper, RideRepository rideRepository) {
        return new BikeDataRepository(bikeNetworkDataStore,rideRealmDataStore, fleetId,bikeModelMapper, rideRepository);
    }

    @Provides
    @Singleton
    ParkingRepository provideParkingRepository(ParkingNetworkDataStore parkingNetworkDataStore) {
        return new ParkingDataRepository(parkingNetworkDataStore);
    }

    @Provides
    @Singleton
    RideRepository provideTripRepository(RideNetworkDataStore rideNetworkDataStore,RideRealmDataStore rideRealmDataStore, @FleetId String fleetId, BikeModelMapper bikeModelMapper) {
        return new RideDataRepository(rideNetworkDataStore, rideRealmDataStore,fleetId,bikeModelMapper);
    }

    @Provides
    @Singleton
    MaintenanceRepository provideMaintenanceRepository(MaintenanceDataStore maintenanceDataStore) {
        return new MaintenanceDataRepository(maintenanceDataStore);
    }
    @Provides
    @Singleton
    UploadImageRepository provideUploadImageRepository(UploadImageDataStore uploadImageDataStore) {
        return new UploadImageDataRepository(uploadImageDataStore);
    }

    @Provides
    @Singleton
    ParkingZoneRepository parkingZoneRepository(ParkingZoneDataStore parkingZoneDataStore) {

        return  new ParkingZoneDataRepository(parkingZoneDataStore);
    }


    @Provides
    @Singleton
    LockRepository provideLockRepository(LockNetwordDataStore LockNetwordDataStore, LockRealmDataStore lockRealmDataStore, BluetoothRepository bluetoothRepository) {
        return new LockDataRepository(LockNetwordDataStore,lockRealmDataStore, bluetoothRepository);
    }

    @Provides
    @Singleton
    CardRepository provideCardRepository(CardNetworkDataStore cardNetworkDataStore) {
        return new CardDataRepository(cardNetworkDataStore);
    }

    @Provides
    @Singleton
    SavedAddressRepository provideSavedAddressRepository(SavedAddressDataStore savedAddressDataStore) {
        return new SavedAddressDataRepository(savedAddressDataStore);
    }
}