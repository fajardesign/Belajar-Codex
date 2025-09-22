package com.example.ambis.home

import com.example.ambis.model.ActionItem
import com.example.ambis.model.ServiceItem

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Loaded(
        val company: String,
        val balance: Long,
        val visible: Boolean,
        val pending: Int,
        val quick: List<ActionItem>,
        val services: List<ServiceItem>,
        val hasUnread: Boolean
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState

    data object Offline : HomeUiState
}
