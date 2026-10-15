package com.iptvplayer.app.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val exception: Throwable) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> AppResult.Error(exception)
    is AppResult.Loading -> AppResult.Loading
}

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class OfflineModeException(
        message: String = "O modo offline manual está ativado. O tráfego de rede está bloqueado."
    ) : AppException(message)

    class NetworkUnavailableException(
        message: String = "Nenhuma conexão com a internet detectada."
    ) : AppException(message)

    class InvalidPlaylistException(
        message: String = "Formato de playlist inválido ou arquivo corrompido."
    ) : AppException(message)

    class InsufficientStorageException(
        message: String = "Espaço insuficiente em disco para completar o download."
    ) : AppException(message)

    class StreamPlaybackException(
        message: String = "Erro ao reproduzir a fonte de mídia.",
        cause: Throwable? = null
    ) : AppException(message, cause)
}
