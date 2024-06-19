package org.yrovas.linklater.domain

import androidx.compose.runtime.Composable

sealed interface Res<out D, out E : Error> {
    data class Ok<out D, out E : Error>(val data: D) : Res<D, E>
    data class Err<out D, out E : Error>(val error: E) : Res<D, E>
}

typealias Ok<D, E> = Res.Ok<D, E>
typealias Err<D, E> = Res.Err<D, E>

sealed interface Error

val <D, E : Error> Res<D, E>.isOk: Boolean
    get() = when (this) {
        is Res.Ok -> true
        is Res.Err -> false
    }

val <D, E : Error> Res<D, E>.isErr: Boolean
    get() = !isOk

fun <D, E : Error> Res<D, E>.ok(function: (data: D) -> Unit) {
    when (this) {
        is Res.Err -> Unit
        is Res.Ok -> function(data)
    }
}

suspend fun <D, E : Error> Res<D, E>.ifOk(function: suspend (data: D) -> Unit) {
    when (this) {
        is Res.Err -> Unit
        is Res.Ok -> function(data)
    }
}

fun <D, E : Error> Res<D, E>.err(function: (error: E) -> Unit) {
    when (this) {
        is Res.Err -> function(error)
        is Res.Ok -> Unit
    }
}

suspend fun <D, E : Error> Res<D, E>.ifErr(function: suspend (error: E) -> Unit) {
    when (this) {
        is Res.Err -> function(error)
        is Res.Ok -> Unit
    }
}

fun <D, T : Any, E : Error> Res<D, E>.then(ok: (data: D) -> T, err: (error: E) -> T): T {
    return when (this) {
        is Res.Err -> err(error)
        is Res.Ok -> ok(data)
    }
}

fun <D, T, E : Error> Res<T, E>.mapData(mapData: (T) -> D): Res<D, E> {
    return when (this) {
        is Res.Err -> Res.Err(this.error)
        is Res.Ok -> Res.Ok(mapData(this.data))
    }
}

fun <T, E : Error> Res<T, E>.mapError(mapError: (E) -> E): Res<T, E> {
    return when (this) {
        is Res.Err -> Err(mapError(this.error))
        is Res.Ok -> Ok(this.data)
    }
}

fun <D, T : Any, E : Error> Res<D, E>?.then(
    ok: (data: D) -> T,
    err: (error: E) -> T,
    nil: () -> T,
): T {
    return this?.then(ok, err) ?: nil()
}

@Composable
fun <D, T : Any, E : Error> Res<D, E>.apply(
    ok: @Composable (data: D) -> T,
    err: @Composable (error: E) -> T,
): T {
    return when (this) {
        is Res.Err -> err(error)
        is Res.Ok -> ok(data)
    }
}

@Composable
fun <D, T : Any, E : Error> Res<D, E>?.apply(
    ok: @Composable (data: D) -> T,
    err: @Composable (error: E) -> T,
    nil: @Composable () -> T,
): T {
    return this?.apply(ok, err) ?: nil()
}

fun <D, E : Error> Res<D, E>.errorOrNull(): E? {
    return when (this) {
        is Res.Err -> error
        is Res.Ok -> null
    }
}

fun <D, E : Error> Res<D, E>.errorOrThrow(): E {
    return (this as Res.Err).error
}

fun <D, E : Error> Res<D, E>.getOrThrow(): D {
    return (this as Res.Ok).data
}

fun <D, E : Error> Res<D, E>.getOrNull(): D? {
    return when (this) {
        is Res.Err -> null
        is Res.Ok -> data
    }
}

fun <D, E : Error> Res<D, E>?.isNull(): Boolean {
    return when (this) {
        is Res.Err -> false
        is Res.Ok -> false
        null -> true
    }
}

fun <D, E : Error> Res<D, E>?.isNotNull(): Boolean {
    return !isNull()
}

fun <T, E : Error> Result<T>.toRes(withError: E): Res<T, E> {
    this.onFailure {
        return Err(withError)
    }
    this.onSuccess { return Ok(it) }
    assert(false)
    return Err(withError)
}
