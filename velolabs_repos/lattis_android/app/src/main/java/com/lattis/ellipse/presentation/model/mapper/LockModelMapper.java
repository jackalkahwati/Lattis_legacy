package com.lattis.ellipse.presentation.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Version;
import com.lattis.ellipse.presentation.model.LockModel;

import javax.inject.Inject;

public class LockModelMapper extends AbstractDataMapper<Lock,LockModel> {

    @Inject
    public LockModelMapper() {
    }

    @NonNull
    @Override
    public LockModel mapIn(@NonNull Lock lock) {
        LockModel lockModel = new LockModel();
        lockModel.setLockId(lock.getLockId());
        lockModel.setMacId(lock.getMacId());
        lockModel.setMacAddress(lock.getMacAddress());
        lockModel.setUserId(lock.getUserId());
        lockModel.setSerialNumber(lock.getSerialNumber());

        lockModel.setUsersId(lock.getUsersId());
        lockModel.setShareId(lock.getShareId());
        lockModel.setSharedWithUserId(lock.getSharedWithUserId());
        lockModel.setSharedWithMe(lock.isSharedWithMe());
        lockModel.setSharedWithOther(lock.isSharedWithOther());

        lockModel.setLastLocation(lock.getLastLocation());
        lockModel.setLocked(lock.isLocked());
        lockModel.setLockedDate(lock.getLockedDate());
        lockModel.setName(lock.getName());
        lockModel.setAlert(lock.getAlertMode());
        lockModel.setAutoProximityLock(lock.isAutoProximityLock());
        lockModel.setAutoProximityUnlock(lock.isAutoProximityUnlock());
        lockModel.setConnectedDate(lock.getConnectedDate());
        lockModel.setUseDefaultPinCode(lock.useDefaultPinCode());

        lockModel.setSignedMessage(lock.getSignedMessage());
        lockModel.setPublicKey(lock.getPublicKey());

        if(lock.getVersion()!=null){
            lockModel.setVersion(lock.getVersion().getVersion()+"."+lock.getVersion().getRevision());
        }
        return lockModel;
    }

    @NonNull
    @Override
    public Lock mapOut(@NonNull LockModel lockModel) {
        Lock lock = new Lock();
        lock.setLockId(lockModel.getLockId());
        lock.setMacId(lockModel.getMacId());
        lock.setMacAddress(lockModel.getMacAddress());
        lock.setUserId(lockModel.getUserId());
        lock.setName(lockModel.getName());
        lock.setSerialNumber(lockModel.getSerialNumber());

        lock.setUsersId(lockModel.getUsersId());
        lock.setShareId(lockModel.getShareId());
        lock.setSharedWithUserId(lockModel.getSharedWithUserId());
        lock.setIsSharedWithMe(lockModel.isSharedWithMe());
        lock.setIsSharedWithOther(lockModel.isSharedWithOther());

        lock.setLastLocation(lockModel.getLastLocation());
        lock.setLocked(lockModel.isLocked());
        lock.setLockedDate(lockModel.getLockedDate());
        lock.setAlertMode(lockModel.getAlert());
        lock.setUseDefaultPinCode(lockModel.useDefaultPinCode());

        lock.setAutoProximityLock(lockModel.isAutoProximityLock());
        lock.setAutoProximityUnlock(lockModel.isAutoProximityUnlock());
        lock.setConnectedDate(lockModel.getConnectedDate());
        lock.setUseDefaultPinCode(lockModel.useDefaultPinCode());

        lock.setSignedMessage(lockModel.getSignedMessage());
        lock.setPublicKey(lockModel.getPublicKey());

        if(lockModel.getVersion()!=null){
            String[] version = lockModel.getVersion().split("\\.");
            lock.setVersion(new Version(Integer.valueOf(version[0]),Integer.valueOf(version[1])));
        }
        return lock;
    }
}
