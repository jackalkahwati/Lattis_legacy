package io.lattis.ellipse.sdk.callback;

import android.bluetooth.BluetoothGattCallback;

import io.reactivex.ObservableEmitter;

abstract class AbstractBluetoothCallBack<T> extends BluetoothGattCallback {

    ObservableEmitter<? super T> emitter;

    AbstractBluetoothCallBack(ObservableEmitter<? super T> emitter) {
        this.emitter = emitter;
    }

    public void setEmitter(ObservableEmitter<? super T> emitter) {
        this.emitter = emitter;
    }
}
