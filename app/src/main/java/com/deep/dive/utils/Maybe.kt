package com.deep.dive.utils

sealed interface Maybe<out T> {
    data class Some<out T>(val value: T) : Maybe<T>
    data object None : Maybe<Nothing>
}