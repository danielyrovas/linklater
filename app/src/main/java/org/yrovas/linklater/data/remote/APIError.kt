package org.yrovas.linklater.data.remote

import org.yrovas.linklater.Error

enum class APIError : Error {
    NO_CONNECTION,
    INCORRECT_ENDPOINT,
    INCORRECT_AUTH,
    NO_AUTH_PROVIDED
}
