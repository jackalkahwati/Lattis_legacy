package com.lattis.ellipse.data.database.base;

import io.realm.RealmObject;

public class RealmInt extends RealmObject {

    private int value;

    public RealmInt() {
    }

    public RealmInt(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}