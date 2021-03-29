package com.harukeyua.fintrack.utils

sealed class Resource<out T : Any> {
    object Loading : Resource<Nothing>()
    class Success<out T : Any>(val data: T) : Resource<T>()
    class ApiError(val errorCode: Int) : Resource<Nothing>()
    class ExceptionError(val e: String) : Resource<Nothing>()
}