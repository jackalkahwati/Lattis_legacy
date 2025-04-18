package io.lattis.ellipse.sdk.model;

public enum  Rssi {

    ANDROID, LOCK;

    int value;

    public Rssi withValue(int value) {
        this.value = value;
        return this;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Rssi{" + name()+
                " value=" + value +
                '}';
    }
}
