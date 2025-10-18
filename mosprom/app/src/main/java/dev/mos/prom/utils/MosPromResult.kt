package dev.mos.prom.utils

/**
 Используется для отображения состояния загрузки, успеха или ошибки при асинхронных действиях
 */

sealed class MosPromResult {
    data object Loading : MosPromResult()
    data object Success : MosPromResult()
    data object Error : MosPromResult()
}
