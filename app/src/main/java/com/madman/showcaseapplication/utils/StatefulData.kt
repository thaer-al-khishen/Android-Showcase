package com.madman.showcaseapplication.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

sealed class StatefulData<out T : Any> {
    data class Success<T : Any>(val result : T) : StatefulData<T>()
    data class Error(val msg : String) : StatefulData<Nothing>()
    object Loading : StatefulData<Nothing>()

    inline fun <R : Any> map(transform: (T) -> R): StatefulData<R> {
        return when(this) {
            is Loading -> Loading
            is Error -> Error(this.msg)
            is Success -> Success(transform(this.result))
        }
    }

    suspend inline fun <R : Any> suspendMap(crossinline transform: suspend (T) -> R): StatefulData<R> {
        return when(this) {
            is Loading -> Loading
            is Error -> Error(this.msg)
            is Success -> Success(transform(this.result))
        }
    }

}

fun <T:Any> Flow<T>.asStatefulData(): Flow<StatefulData<T>> = wrapWithStatefulData()
    .catch {
        emit(StatefulData.Error(it.message ?: "There was an error"))
    }

fun <T:Any> Flow<T>.wrapWithStatefulData() : Flow<StatefulData<T>> = transform { value ->
    return@transform emit(StatefulData.Success(value))
}

inline fun <T:Any,R:Any> Flow<StatefulData<T>>.mapState(crossinline transform: suspend (value: T) -> R): Flow<StatefulData<R>> = transform { value ->
    return@transform emit(value.suspendMap(transform))
}

inline fun <T:Any> Flow<StatefulData<T>>.onSuccessState(crossinline action: suspend (value: T) -> Unit): Flow<StatefulData<T>> = onEach {
    if (it is StatefulData.Success) action(it.result)
}

inline fun <T:Any> Flow<StatefulData<T>>.onErrorState(crossinline action: suspend (error: String) -> Unit): Flow<StatefulData<T>> = onEach {
    if (it is StatefulData.Error) action(it.msg)
}

inline fun <T:Any> Flow<StatefulData<T>>.onLoadingState(crossinline action: suspend () -> Unit): Flow<StatefulData<T>> = onEach {
    if (it is StatefulData.Loading) action()
}

inline fun <T: Any> statefulFlow(crossinline action: suspend () -> T): Flow<StatefulData<T>> {
    return flow {
        emit(StatefulData.Loading)
        delay(1000L)
        emit(StatefulData.Success(action.invoke()))
    }.catch {
        emit(StatefulData.Error(it.message ?: "Some error occurred"))
    }
}
