package com.antibudget.app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antibudget.app.data.local.AppDatabase
import com.antibudget.app.data.repository.FinanceRepositoryImpl
import com.antibudget.app.service.BankNotificationListenerService
import com.antibudget.app.ui.dashboard.DashboardScreen
import com.antibudget.app.ui.dashboard.DashboardViewModel
import com.antibudget.app.ui.theme.AntiBudgetTheme
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Gold400
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.Slate900
import com.antibudget.app.ui.theme.Slate950
import com.antibudget.app.ui.wealth.WealthScreen
import com.antibudget.app.ui.wealth.WealthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val repository = FinanceRepositoryImpl(database)

        setContent {
            AntiBudgetTheme {
                MainAppScreen(repository = repository)
            }
        }
    }
}

@Composable
fun MainAppScreen(repository: FinanceRepositoryImpl) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isNotificationAccessGranted by remember {
        mutableStateOf(isNotificationServiceEnabled(context))
    }

    // Yêu cầu quyền POST_NOTIFICATIONS cho Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.provideFactory(repository)
    )
    val wealthViewModel: WealthViewModel = viewModel(
        factory = WealthViewModel.provideFactory(repository)
    )
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Slate900,
        bottomBar = {
            NavigationBar(
                containerColor = Slate850,
                contentColor = Emerald500
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Paid, contentDescription = "Dashboard") },
                    label = { Text("Safe-to-Spend") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = Emerald500,
                        indicatorColor = Emerald500,
                        unselectedIconColor = Slate700,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Tích sản") },
                    label = { Text("Tích sản 3 Lọ") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = Gold400,
                        indicatorColor = Gold400,
                        unselectedIconColor = Slate700,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Banner gợi ý cấp quyền Notification Listener nếu chưa có
            if (!isNotificationAccessGranted) {
                NotificationPermissionBanner(
                    onEnableClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    },
                    onDismiss = {
                        isNotificationAccessGranted = true
                    }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToWealth = { selectedTab = 1 }
                    )
                    1 -> WealthScreen(
                        viewModel = wealthViewModel,
                        isPrivacyMode = dashboardUiState.isPrivacyMode
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationPermissionBanner(
    onEnableClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Slate800
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Emerald500.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Emerald500,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tự động đọc số dư ngân hàng",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Bật quyền thông báo để app tự trừ tiền vào Quỹ tự do.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onEnableClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Bật", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Kiểm tra xem người dùng đã cấp quyền đọc thông báo cho app chưa
 */
private fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    if (!flat.isNullOrEmpty()) {
        val names = flat.split(":").toTypedArray()
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == pkgName) {
                return true
            }
        }
    }
    return false
}
