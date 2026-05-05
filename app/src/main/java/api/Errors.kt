package api

sealed class AppError : Exception() {
    data class EopError(val messageOverride: String? = null) : AppError() {
        override val message: String = messageOverride ?: "EOP Error"
    }
}