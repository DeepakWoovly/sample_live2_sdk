package com.live2.media.utils

import androidx.databinding.Observable
import androidx.databinding.ObservableField

class NonNullObservableField<T : Any>(
    value: T,
    vararg dependencies: Observable
) : ObservableField<T>(*dependencies) {
    init {
        set(value)
    }

    override fun get(): T = super.get()!!

    // Only allow non-null `value`.
    override fun set(value: T) = super.set(value)
}