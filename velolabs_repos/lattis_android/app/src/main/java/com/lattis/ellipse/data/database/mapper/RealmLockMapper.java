package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;
import android.util.Log;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmLock;
import com.lattis.ellipse.domain.model.Alert;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Version;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;

import javax.inject.Inject;

public class RealmLockMapper extends AbstractRealmDataMapper<Lock,RealmLock> {

    private RealmLocationMapper locationMapper;
    private String fleetId;

    @Inject
    public RealmLockMapper(RealmLocationMapper locationMapper, @FleetId String fleetId) {
        this.locationMapper = locationMapper;
        this.fleetId = fleetId;
    }

    @NonNull
    @Override
    public RealmLock mapIn(@NonNull Lock lock) {
        RealmLock realmLock = new RealmLock();

        realmLock.setId(fleetId);
        realmLock.setLockId(lock.getLockId());
        realmLock.setMacId(lock.getMacId());
        realmLock.setMacAddress(lock.getMacAddress());
        realmLock.setName(lock.getName());
        realmLock.setSerialNumber(lock.getSerialNumber());
        realmLock.setPublicKey(lock.getPublicKey());
        realmLock.setUserId(lock.getUserId());

        realmLock.setSignedMessage(lock.getSignedMessage());

        realmLock.setUsersId(lock.getUsersId());
        realmLock.setShareId(lock.getShareId());
        realmLock.setShareWithUserId(lock.getSharedWithUserId());
        realmLock.setSharedWithMe(lock.isSharedWithMe());
        realmLock.setSharedWithOther(lock.isSharedWithOther());

        realmLock.setLocked(lock.isLocked());
        realmLock.setLockedDate(lock.getLockedDate());
        realmLock.setConnectedDate(lock.getConnectedDate());
        realmLock.setAutoProximityLock(lock.isAutoProximityLock());
        realmLock.setAutoProximityUnlock(lock.isAutoProximityUnlock());
        realmLock.setUseDefaultPinCode(lock.useDefaultPinCode());

        if(lock.getVersion()!=null){
            realmLock.setVersion(String.valueOf(lock.getVersion().getVersion()));
            realmLock.setRevision(String.valueOf(lock.getVersion().getRevision()));
        }
        if(lock.getAlertMode() != null){
            realmLock.setAlertMode(lock.getAlertMode().name());
        } else {
            realmLock.setAlertMode(Alert.OFF.name());
        }
        if(lock.getLastLocation()!=null){
            realmLock.setLastLocation(locationMapper.mapIn(lock.getLastLocation()));
        }

        Log.e("RealmLockMapper","RealmLock: "+ realmLock.toString());

        return realmLock;
    }

    @NonNull
    @Override
    public Lock mapOut(@NonNull RealmLock realmLock) {
        Lock lock = new Lock();
        lock.setLockId(realmLock.getLockId());
        lock.setMacId(realmLock.getMacId());
        lock.setMacAddress(realmLock.getMacAddress());
        lock.setName(realmLock.getName());
        lock.setUserId(realmLock.getUserId());
        lock.setSerialNumber(realmLock.getSerialNumber());
        lock.setPublicKey(realmLock.getPublicKey());

        lock.setSignedMessage(realmLock.getSignedMessage());

        lock.setUserId(realmLock.getUserId());
        lock.setUsersId(realmLock.getUsersId());
        lock.setShareId(realmLock.getShareId());
        lock.setSharedWithUserId(realmLock.getShareWithUserId());
        lock.setIsSharedWithMe(realmLock.isSharedWithMe());
        lock.setIsSharedWithOther(realmLock.isSharedWithOther());

        lock.setLocked(realmLock.isLocked());
        lock.setLockedDate(realmLock.getLockedDate());
        lock.setConnectedDate(realmLock.getConnectedDate());
        lock.setAutoProximityLock(realmLock.isAutoProximityLock());
        lock.setAutoProximityUnlock(realmLock.isAutoProximityUnlock());
        lock.setUseDefaultPinCode(realmLock.useDefaultPinCode());

        if(realmLock.getVersion() != null && realmLock.getRevision() != null){
            lock.setVersion(new Version(Integer.valueOf(realmLock.getVersion()),Integer.valueOf(realmLock.getRevision())));
        }
        if(realmLock.getAlertMode()!=null){
            lock.setAlertMode(Alert.forValue(realmLock.getAlertMode()));
        } else {
            lock.setAlertMode(Alert.OFF);
        }
        if(realmLock.getLastLocation()!=null){
            lock.setLastLocation(locationMapper.mapOut(realmLock.getLastLocation()));
        }

        Log.e("RealmLockMapper","Lock: "+ lock.toString());

        return lock;
    }
}
