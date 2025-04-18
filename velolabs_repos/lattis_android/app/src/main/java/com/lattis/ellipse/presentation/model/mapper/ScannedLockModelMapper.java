package com.lattis.ellipse.presentation.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.ScannedLock;
import com.lattis.ellipse.presentation.model.LockModel;

import javax.inject.Inject;

public class ScannedLockModelMapper extends AbstractDataMapper<ScannedLock,LockModel> {

    @Inject
    public ScannedLockModelMapper() {
    }

    @NonNull
    @Override
    public LockModel mapIn(@NonNull ScannedLock lock) {
        LockModel lockModel = new LockModel();
        lockModel.setMacId(lock.getMacId());
        lockModel.setMacAddress(lock.getMacAddress());
        lockModel.setName(lock.getName());
        return lockModel;
    }

    @NonNull
    @Override
    public ScannedLock mapOut(@NonNull LockModel lockModel) {
        Lock lock = new Lock();
        lock.setMacId(lockModel.getMacId());
        lock.setMacAddress(lockModel.getMacAddress());
        lock.setName(lockModel.getName());
        return lock;
    }
}
