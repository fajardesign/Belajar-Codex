package com.example.ambis.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Wifi
import com.example.ambis.model.ActionItem
import com.example.ambis.model.Dest
import com.example.ambis.model.ServiceItem
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.balancePreferences by preferencesDataStore(name = "balance_visibility")

class HomeViewModel(
    private val repository: HomeRepository,
    private val visibilityRepository: BalanceVisibilityRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _navigation = MutableSharedFlow<Dest>(extraBufferCapacity = 1)
    val navigation = _navigation.asSharedFlow()

    init {
        observeVisibility()
        load()
    }

    fun load() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = HomeUiState.Loading
            fetchData()
        }
    }

    fun refresh(force: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            _refreshing.value = true
            try {
                fetchData(force)
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun toggleBalanceVisibility() {
        val current = _uiState.value
        if (current is HomeUiState.Loaded) {
            viewModelScope.launch(ioDispatcher) {
                visibilityRepository.setVisible(!current.visible)
            }
        }
    }

    fun onNavigate(dest: Dest) {
        _navigation.tryEmit(dest)
    }

    private suspend fun fetchData(force: Boolean = false) {
        try {
            val payload = repository.fetchHomeData(force)
            val visible = visibilityRepository.isVisible()
            _uiState.value = HomeUiState.Loaded(
                company = payload.companyDisplayName,
                balance = payload.totalActiveBalance,
                visible = visible,
                pending = payload.pendingApprovalCount,
                quick = payload.quickActions,
                services = payload.services,
                hasUnread = payload.hasUnreadNotifications
            )
        } catch (io: IOException) {
            _uiState.value = HomeUiState.Offline
        } catch (throwable: Throwable) {
            _uiState.value = HomeUiState.Error(throwable.message ?: "Terjadi kesalahan")
        }
    }

    private fun observeVisibility() {
        viewModelScope.launch {
            visibilityRepository.visibility.collectLatest { visible ->
                val current = _uiState.value
                if (current is HomeUiState.Loaded && current.visible != visible) {
                    _uiState.value = current.copy(visible = visible)
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = HomeRepository()
            val visibilityRepository = BalanceVisibilityRepository(context.balancePreferences)
            return HomeViewModel(repository, visibilityRepository) as T
        }
    }
}

class HomeRepository {
    suspend fun fetchHomeData(force: Boolean): HomePayload {
        // In a real implementation, this would reach the network or database.
        // For now we simply return cached/sample content.
        return HomePayload.sample()
    }
}

data class HomePayload(
    val companyDisplayName: String,
    val totalActiveBalance: Long,
    val pendingApprovalCount: Int,
    val quickActions: List<ActionItem>,
    val services: List<ServiceItem>,
    val hasUnreadNotifications: Boolean,
    val lastUpdatedAt: Instant
) {
    companion object {
        fun sample(): HomePayload {
            return HomePayload(
                companyDisplayName = "PT Sarana Pancing Indonesia",
                totalActiveBalance = 100_000_000_000L,
                pendingApprovalCount = 8,
                quickActions = SampleContent.quickActions,
                services = SampleContent.services,
                hasUnreadNotifications = true,
                lastUpdatedAt = Instant.now()
            )
        }
    }
}

object SampleContent {
    val quickActions: List<ActionItem> = listOf(
        ActionItem(id = "topup", label = "Top Up\nSaldo", icon = Icons.Outlined.AccountBalanceWallet),
        ActionItem(id = "history", label = "Riwayat\nTransaksi", icon = Icons.Outlined.ReceiptLong),
        ActionItem(id = "add-account", label = "Tambah\nRekening", icon = Icons.Outlined.AccountBalance),
        ActionItem(id = "transfer", label = "Transfer", icon = Icons.Outlined.Send)
    )

    val services: List<ServiceItem> = listOf(
        ServiceItem(id = "pln", label = "Listrik PLN", icon = Icons.Outlined.Bolt),
        ServiceItem(id = "internet", label = "Internet", icon = Icons.Outlined.Wifi),
        ServiceItem(id = "bpjs", label = "BPJS", icon = Icons.Outlined.HealthAndSafety)
    )
}

class BalanceVisibilityRepository(private val dataStore: DataStore<Preferences>) {

    private val key = booleanPreferencesKey("is_visible")

    val visibility: kotlinx.coroutines.flow.Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[key] ?: false }

    suspend fun isVisible(): Boolean = visibility.first()

    suspend fun setVisible(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }
}
