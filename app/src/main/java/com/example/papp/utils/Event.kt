package com.example.papp.utils

import java.util.concurrent.atomic.AtomicBoolean

class Event<T>(private val value: T?) {

    private val active = AtomicBoolean(true)

    fun getValue(func: (T?) -> Unit) {
        if (active.compareAndSet(true, false)) {
            func(value)
        }
    }
}