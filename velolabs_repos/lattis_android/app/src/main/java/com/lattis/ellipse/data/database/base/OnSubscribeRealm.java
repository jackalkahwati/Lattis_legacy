
package com.lattis.ellipse.data.database.base;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;
import io.realm.exceptions.RealmException;

abstract class OnSubscribeRealm<T> implements ObservableOnSubscribe<T> {

    private final List<ObservableEmitter<? super T>> emitters = new ArrayList<>();

    private final AtomicBoolean canceled = new AtomicBoolean();

    private final Object lock = new Object();

    @Override
    public void subscribe(ObservableEmitter<T> emitter) throws Exception {
        synchronized (lock) {
            boolean canceled = this.canceled.get();
            if (!canceled && !emitters.isEmpty()) {
                emitter.setDisposable(newDisposeAction(emitter));
                emitters.add(emitter);
                return;
            } else if (canceled) {
                return;
            }
        }
        emitter.setDisposable(newDisposeAction(emitter));
        emitters.add(emitter);
        Realm realm = Realm.getDefaultInstance();
        boolean withError = false;

        T object = null;
        try {
            if (!this.canceled.get()) {
                realm.beginTransaction();
                object = get(realm);
                if (object != null && !this.canceled.get()) {
                    realm.commitTransaction();
                } else {
                    realm.cancelTransaction();
                }
            }
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            sendOnError(new RealmException("Error during transaction.", e));
            withError = true;
        } catch (Error e) {
            realm.cancelTransaction();
            sendOnError(e);
            withError = true;
        }
        if (object != null && !this.canceled.get() && !withError) {
            sendOnNext(object);
        }

        try {
            realm.close();
        } catch (RealmException ex) {
            sendOnError(ex);
            withError = true;
        }
        if (!withError) {
            sendOnCompleted();
        }
        this.canceled.set(false);
    }

    private void sendOnNext(T object) {
        for (int i = 0; i < emitters.size(); i++) {
            ObservableEmitter<? super T> emitter = emitters.get(i);
            emitter.onNext(object);
        }
    }

    private void sendOnError(Throwable e) {
        for (int i = 0; i < emitters.size(); i++) {
            ObservableEmitter<? super T> emitter = emitters.get(i);
            emitter.onError(e);
        }
    }

    private void sendOnCompleted() {
        for (int i = 0; i < emitters.size(); i++) {
            ObservableEmitter<? super T> emitter = emitters.get(i);
            emitter.onComplete();
        }
    }

    @NonNull
    private Disposable newDisposeAction(final ObservableEmitter<? super T> emitter) {
        return new Disposable() {

            private  boolean isDisposed = false;

            @Override
            public void dispose() {
                synchronized (lock) {
                    emitters.remove(emitter);
                    if (emitters.isEmpty()) {
                        canceled.set(true);
                    }
                    isDisposed = true;
                }
            }

            @Override
            public boolean isDisposed() {
                return isDisposed;
            }
        };
    }

    public abstract T get(Realm realm) throws Exception;
}