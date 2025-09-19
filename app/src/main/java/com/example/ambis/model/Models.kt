package com.example.ambis.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ActionItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

data class ServiceItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

sealed interface Dest {
    data object Home : Dest
    data object Profile : Dest
    data object Vault : Dest
    data object More : Dest
    data object Notifications : Dest
    data object ManageAccounts : Dest
    data object ApprovalsPending : Dest
    data object ApprovalsDone : Dest
    data object BillsHistory : Dest
    data class Service(val id: String) : Dest
    data class QuickAction(val id: String) : Dest
}
