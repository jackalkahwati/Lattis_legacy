package com.lattis.data.database.base

import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean

import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.disposables.Disposable
import io.realm.Realm
import io.realm.exceptions.RealmException

internal abstract class OnSubscribeRealm<T> : ObservableOnSubscribe<T> {

    private val emitters = ArrayList<ObservableEmitter<in T>>()

    private val canceled = AtomicBoolean()

    private val lock = Any()

    @Throws(Exception::class)
    override fun subscribe(emitter: ObservableEmitter<T>) {
        synchronized(lock) {
            val canceled = this.canceled.get()
            if (!canceled && !emitters.isEmpty()) {
                emitter.setDisposable(newDisposeAction(emitter))
                emitters.add(emitter)
                return
            } else if (canceled) {
                return
            }
        }
        emitter.setDisposable(newDisposeAction(emitter))
        emitters.add(emitter)
        val realm = Realm.getDefaultInstance()
        var withError = false

        var `object`: T? = null
        try {
            if (!this.canceled.get()) {
                realm.beginTransaction()
                `object` = get(realm)
                if (`object` != null && !this.canceled.get()) {
                    realm.commitTransaction()
                } else {
                    realm.cancelTransaction()
                }
            }
        } catch (e: RuntimeException) {
            realm.cancelTransaction()
            sendOnError(RealmException("Error during transaction.", e))
            withError = true
        } catch (e: Error) {
            realm.cancelTransaction()
            sendOnError(e)
            withError = true
        }

        if (`object` != null && !this.canceled.get() && !withError) {
            sendOnNext(`object`)
        }

        try {
            realm.close()
        } catch (ex: RealmException) {
            sendOnError(ex)
            withError = true
        }

        if (!withError) {
            sendOnCompleted()
        }
        this.canceled.set(false)
    }

    private fun sendOnNext(`object`: T) {
        for (i in emitters.indices) {
            val emitter = emitters[i]
            emitter.onNext(`object`)
        }
    }

    private fun sendOnError(e: Throwable) {
        for (i in emitters.indices) {
            val emitter = emitters[i]
            emitter.onError(e)
        }
    }

    private fun sendOnCompleted() {
        for (i in emitters.indices) {
            val emitter = emitters[i]
            emitter.onComplete()
        }
    }

    private fun newDisposeAction(emitter: ObservableEmitter<in T>): Disposable {
        return object : Disposable {

            private var isDisposed = false

            override fun dispose() {
                synchronized(lock) {
                    emitters.remove(emitter)
                    if (emitters.isEmpty()) {
                        canceled.set(true)
                    }
                    isDisposed = true
                }
            }

            override fun isDisposed(): Boolean {
                return isDisposed
            }
        }
    }

    @Throws(Exception::class)
    abstract operator fun get(realm: Realm): T
}