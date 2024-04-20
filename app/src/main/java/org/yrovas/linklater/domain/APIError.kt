package org.yrovas.linklater.domain

enum class APIError : Error {
    NO_CONNECTION,
    INCORRECT_ENDPOINT,
    INCORRECT_AUTH,
    NO_AUTH_PROVIDED
}
