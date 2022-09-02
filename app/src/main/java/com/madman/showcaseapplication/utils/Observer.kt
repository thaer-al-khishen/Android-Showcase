package com.madman.showcaseapplication.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class Observer<T>(
    lifecycleOwner: LifecycleOwner,
    private val flow: Flow<T>,
    private val collector: suspend (T) -> Unit
) : DefaultLifecycleObserver {

    var job: Job? = null

    override fun onStart(owner: LifecycleOwner) {
        job = owner.lifecycleScope.launch {
            flow.collect {
                collector(it)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        job?.cancel()
        job = null
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }
}

inline fun <reified T> Flow<T>.observeOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    noinline collector: suspend (T) -> Unit
) = Observer(lifecycleOwner, this, collector)

inline fun <reified T> Flow<T>.observeInLifecycle(
    lifecycleOwner: LifecycleOwner
) = Observer(lifecycleOwner, this, {})
