package com.lattis.ellipse.presentation.dagger.module;

import android.content.Context;

import com.lattis.ellipse.bluetooth.BluetoothLockMapper;
import com.lattis.ellipse.bluetooth.BluetoothStateMapper;
import com.lattis.ellipse.bluetooth.HardwareStateMapper;
import com.lattis.ellipse.bluetooth.LattisBluetoothRepository;
import com.lattis.ellipse.bluetooth.LockPositionMapper;
import com.lattis.ellipse.domain.repository.BluetoothRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lattis.ellipse.sdk.manager.EllipseManager;
import io.lattis.ellipse.sdk.manager.IEllipseManager;

@Module
public class BluetoothModule {

    Context context;

    public BluetoothModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    IEllipseManager provideEllipseManager(){
        return EllipseManager.newInstance(context);
    }

    @Provides
    @Singleton
    BluetoothRepository provideBluetoothRepository(IEllipseManager ellipseManager,
                                                   BluetoothLockMapper lockMapper,
                                                   BluetoothStateMapper bluetoothStateMapper,
                                                   LockPositionMapper lockPositionMapper,
                                                   HardwareStateMapper hardwareStateMapper){
        return new LattisBluetoothRepository(ellipseManager, lockMapper,bluetoothStateMapper,lockPositionMapper,hardwareStateMapper);
    }
}
