package org.yrovas.linklater

sealed interface Res<out D, out E : Error> {
    data class Ok<out D, out E : Error>(val data: D) : Res<D, E>
    data class Err<out D, out E : Error>(val error: E) : Res<D, E>
}

typealias Ok<D, E> = Res.Ok<D, E>
typealias Err<D, E> = Res.Err<D, E>

interface Error

val <D, E : Error> Res<D, E>.isOk: Boolean
    get() = when (this) {
        is Res.Ok -> true
        is Res.Err -> false
    }

val <D, E : Error> Res<D, E>.isErr: Boolean
    get() = !isOk

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

fun <D, E : Error> Res<D, E>.errorOrNull(): E? {
    return when (this) {
        is Res.Err -> error
        is Res.Ok -> null
    }
}

fun <D, E : Error> Res<D, E>.getOrNull(): D? {
    return when (this) {
        is Res.Err -> null
        is Res.Ok -> data
    }
}

fun <D, E : Error> Res<D, E>.errorOrThrow(): E = (this as Res.Err).error

fun <D, E : Error> Res<D, E>.getOrThrow(): D = (this as Res.Ok).data

fun <D, E : Error> Res<D, E>?.isNull(): Boolean {
    return when (this) {
        is Res.Err -> false
        is Res.Ok -> false
        null -> true
    }
}

fun <D, E : Error> Res<D, E>?.isNotNull() = !isNull()

fun <T, E : Error> Result<T>.toRes(withError: E): Res<T, E> {
    this.onFailure { return Err(withError) }
    this.onSuccess { return Ok(it) }

    assert(false)
    return Err(withError)
}
