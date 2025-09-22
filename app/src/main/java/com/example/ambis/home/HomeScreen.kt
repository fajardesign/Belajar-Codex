package com.example.ambis.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ambis.model.ActionItem
import com.example.ambis.model.Dest
import com.example.ambis.model.ServiceItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    state: HomeUiState,
    refreshing: Boolean,
    onToggleVisible: () -> Unit,
    onNavigate: (Dest) -> Unit,
    onRefresh: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            HomeTopBar(
                hasUnreadNotifications = (state as? HomeUiState.Loaded)?.hasUnread == true,
                onNotifications = { onNavigate(Dest.Notifications) }
            )
        },
        bottomBar = {
            HomeBottomBar(current = Dest.Home, onNavigate = onNavigate)
        }
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            isRefreshing = refreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (state) {
                    HomeUiState.Loading -> HomeSkeleton()
                    is HomeUiState.Error -> HomeError(message = state.message, onRetry = onRefresh)
                    HomeUiState.Offline -> OfflineNotice(onRetry = onRefresh)
                    is HomeUiState.Loaded -> HomeContent(
                        state = state,
                        onToggleVisible = onToggleVisible,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Loaded,
    onToggleVisible: () -> Unit,
    onNavigate: (Dest) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BalanceCard(
                company = state.company,
                balance = state.balance,
                visible = state.visible,
                hasAccounts = state.quick.isNotEmpty(),
                quickActions = state.quick,
                onToggleVisible = onToggleVisible,
                onManageAccounts = { onNavigate(Dest.ManageAccounts) },
                onQuickAction = { action -> onNavigate(Dest.QuickAction(action.id)) }
            )
        }
        item {
            ApprovalCard(
                pending = state.pending,
                onAll = { onNavigate(Dest.ApprovalsPending) },
                onDone = { onNavigate(Dest.ApprovalsDone) }
            )
        }
        item {
            BillsSection(
                services = state.services,
                onService = { service -> onNavigate(Dest.Service(service.id)) },
                onHistory = { onNavigate(Dest.BillsHistory) }
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    hasUnreadNotifications: Boolean,
    onNotifications: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.primary) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "amar bank",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = "bisnis",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            BadgedBox(
                badge = {
                    if (hasUnreadNotifications) {
                        Badge(containerColor = MaterialTheme.colorScheme.error)
                    }
                }
            ) {
                IconButton(onClick = onNotifications) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifikasi",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    current: Dest,
    onNavigate: (Dest) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Dest.Home,
            onClick = { },
            enabled = false,
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Beranda") },
            label = { Text("Beranda") }
        )
        NavigationBarItem(
            selected = current == Dest.Profile,
            onClick = { onNavigate(Dest.Profile) },
            icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Profil") },
            label = { Text("Profil") }
        )
        NavigationBarItem(
            selected = current == Dest.Vault,
            onClick = { onNavigate(Dest.Vault) },
            icon = { Icon(Icons.Outlined.CreditCard, contentDescription = "Brankas") },
            label = { Text("Brankas") }
        )
        NavigationBarItem(
            selected = current == Dest.More,
            onClick = { onNavigate(Dest.More) },
            icon = { Icon(Icons.Outlined.Menu, contentDescription = "Lainnya") },
            label = { Text("Lainnya") }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BalanceCard(
    company: String,
    balance: Long,
    visible: Boolean,
    hasAccounts: Boolean,
    quickActions: List<ActionItem>,
    onToggleVisible: () -> Unit,
    onManageAccounts: () -> Unit,
    onQuickAction: (ActionItem) -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Surface(
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        text = company,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Saldo Aktif",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Seluruh dana rekening",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleVisible) {
                    val icon = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                    val desc = if (visible) "Sembunyikan saldo" else "Tampilkan saldo"
                    Icon(imageVector = icon, contentDescription = desc)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedContent(
                targetState = visible,
                transitionSpec = { fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200)) },
                label = "balanceVisibility"
            ) { isVisible ->
                val display = if (isVisible) currencyFormatter.format(balance) else "Rp•••"
                Text(
                    text = display,
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 2,
                    softWrap = true,
                    modifier = Modifier.animateContentSize()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(onClick = onManageAccounts) {
                Text("Atur Semua Rekening")
            }

            if (hasAccounts) {
                Spacer(modifier = Modifier.height(24.dp))
                QuickActionsGrid(actions = quickActions, onQuickAction = onQuickAction)
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                EmptyAccountState(onAddAccount = onManageAccounts)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionsGrid(
    actions: List<ActionItem>,
    onQuickAction: (ActionItem) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 4,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { action ->
            QuickActionTile(action = action, onClick = { onQuickAction(action) })
        }
    }
}

@Composable
private fun QuickActionTile(action: ActionItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .semantics { contentDescription = action.label }
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(72.dp)
                .clickable(onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(
            text = action.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun EmptyAccountState(onAddAccount: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Belum ada rekening",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tambahkan rekening terlebih dahulu untuk melihat saldo dan akses cepat.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onAddAccount) {
                Text("Tambah Rekening")
            }
        }
    }
}

@Composable
private fun ApprovalCard(
    pending: Int,
    onAll: () -> Unit,
    onDone: () -> Unit
) {
    Surface(
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pending.toString(),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = "Aktivitas Butuh Persetujuan Anda",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                ElevatedAssistChip(
                    onClick = onAll,
                    label = { Text("Lihat Semua") }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDone)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lihat Aktivitas Selesai",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Riwayat persetujuan yang sudah selesai",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillsSection(
    services: List<ServiceItem>,
    onService: (ServiceItem) -> Unit,
    onHistory: () -> Unit
) {
    Surface(
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Beli & Bayar",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onHistory) {
                    Text(
                        text = "Riwayat",
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            val tiles = services.take(3)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                tiles.forEach { service ->
                    ServiceTile(
                        service = service,
                        modifier = Modifier.weight(1f),
                        onClick = { onService(service) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceTile(service: ServiceItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp,
        modifier = modifier
            .heightIn(min = 112.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = service.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = service.label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun HomeSkeleton() {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) {
            Surface(
                tonalElevation = 6.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {}
        }
    }
}

@Composable
private fun HomeError(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onRetry) {
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun OfflineNotice(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Mode offline. Menampilkan data terakhir.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onRetry) {
                Text("Muat Ulang")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    HomeContent(
        state = HomeUiState.Loaded(
            company = "PT Sarana Pancing Indonesia",
            balance = 100_000_000_000L,
            visible = true,
            pending = 8,
            quick = SampleContent.quickActions,
            services = SampleContent.services,
            hasUnread = true
        ),
        onToggleVisible = {},
        onNavigate = {}
    )
}
