package org.yrovas.linklater.data.models

enum class APIError: Error {
    NO_CONNECTION,
    INCORRECT_ENDPOINT,
    INCORRECT_AUTH,
    NO_AUTH_PROVIDED
}
