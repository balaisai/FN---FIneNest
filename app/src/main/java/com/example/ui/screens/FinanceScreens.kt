package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.AnimationSpec
import com.example.ui.UserProfile
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.FinanceViewModel
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview as CameraPreviewCore
import androidx.camera.core.CameraSelector
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun UserProfileAvatar(
    photoUri: String?,
    displayName: String,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    if (photoUri != null && photoUri.startsWith("preset:")) {
        val emoji = photoUri.substringAfter("preset:")
        // Draw a beautiful custom color gradient circular avatar containing the preset emoji
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TealPrimary.copy(alpha = 0.8f), SlateDark)
                    )
                )
                .border(1.dp, TealPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = (size.value * 0.55).sp, textAlign = TextAlign.Center)
        }
    } else if (photoUri != null && photoUri.isNotBlank()) {
        // Custom photo loaded via Coil
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(1.5.dp, TealPrimary, CircleShape)
        ) {
            coil.compose.AsyncImage(
                model = photoUri,
                contentDescription = "User profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
            )
        }
    } else {
        // Dynamic Initials Avatar representing their high contrast custom styled letter
        val initials = if (displayName.isNotBlank()) displayName.take(2).uppercase() else "B"
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(TealPrimary, MintAccent, ElectricBlue, TealPrimary)
                    )
                )
                .border(1.dp, BorderSlate, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper Formatter
val rupeeFormat = DecimalFormat("₹#,##,##0.00")

fun formatRupee(value: Double): String {
    return rupeeFormat.format(value)
}

@Composable
fun Modifier.fadeInSlideIn(
    delayMillis: Int = 0,
    durationMillis: Int = 450
): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) {
            kotlinx.coroutines.delay(delayMillis.toLong())
        }
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "offsetY"
    )
    return this
        .graphicsLayer(alpha = alpha, translationY = offsetY)
}

fun getYearFromTimestamp(timestamp: Long): Int {
    val sdf = SimpleDateFormat("yyyy", Locale.US)
    return sdf.format(Date(timestamp)).toIntOrNull() ?: 2026
}

fun getMonthFromTimestamp(timestamp: Long): Int {
    val sdf = SimpleDateFormat("M", Locale.US)
    return sdf.format(Date(timestamp)).toIntOrNull() ?: 6
}

fun getDayFromTimestamp(timestamp: Long): Int {
    val sdf = SimpleDateFormat("d", Locale.US)
    return sdf.format(Date(timestamp)).toIntOrNull() ?: 1
}

@Composable
fun PremiumWelcomeScreen(username: String, progress: Float, statusText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "WelcomeInf")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseWelcome"
    )
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationWelcome"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "ProgressLoadAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FINNEST CAPITAL VAULT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = BorderSlate.copy(alpha = 0.3f),
                        radius = size.minDimension / 2.0f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    color = if (progress >= 1.0f) MintAccent else TealPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle),
                    trackColor = BorderSlate.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(SlateCard)
                        .border(1.5.dp, if (progress >= 1.0f) MintAccent.copy(alpha = 0.6f) else TealPrimary.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (progress >= 1.0f) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success Unlock",
                            tint = MintAccent,
                            modifier = Modifier
                                .size(64.dp)
                                .graphicsLayer {
                                    scaleX = 1.2f
                                    scaleY = 1.2f
                                }
                        )
                    } else {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_app_icon_logo_black_bg_1782145328334),
                            contentDescription = "Decrypted FinNest Signature",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = if (progress >= 1.0f) "ACCESS FULLY GRANTED" else "SECURE VAULT DECRYPTION PIPELINE...",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (progress >= 1.0f) MintAccent else SoftGray,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome, ${username.uppercase()}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = OffWhite,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 340.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = if (progress >= 1.0f) MintAccent else TealPrimary,
                        strokeWidth = 1.5.dp
                    )
                    Text(
                        text = statusText.uppercase(),
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = if (progress >= 1.0f) MintAccent else OffWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(viewModel: FinanceViewModel) {
    val authenticatedUser by viewModel.authenticatedUser.collectAsStateWithLifecycle()
    if (authenticatedUser == null) {
        SecureAuthScreen(viewModel)
        return
    }

    // Premium Welcome State
    var lastUser by remember { mutableStateOf<UserProfile?>(null) }
    var welcomeCompleted by remember { mutableStateOf(false) }
    var welcomeProgress by remember { mutableStateOf(0f) }
    var currentWelcomeStepText by remember { mutableStateOf("Initialising encryption pipeline...") }

    LaunchedEffect(authenticatedUser) {
        if (authenticatedUser != null && lastUser != authenticatedUser) {
            welcomeCompleted = false
            welcomeProgress = 0f
            
            // Phase 1
            currentWelcomeStepText = "⚙️ Decrypting financial private ledger..."
            welcomeProgress = 0.25f
            kotlinx.coroutines.delay(650)
            
            // Phase 2
            currentWelcomeStepText = "🔑 Extracting local cryptographic keys..."
            welcomeProgress = 0.55f
            kotlinx.coroutines.delay(700)
            
            // Phase 3
            currentWelcomeStepText = "⚡ Harmonizing family member sync-nodes..."
            welcomeProgress = 0.85f
            kotlinx.coroutines.delay(700)
            
            // Phase 4
            currentWelcomeStepText = "🔓 COLD-VAULT ACCESS GRANTED"
            welcomeProgress = 1.0f
            kotlinx.coroutines.delay(750)
            
            welcomeCompleted = true
            lastUser = authenticatedUser
        } else if (authenticatedUser == null) {
            welcomeCompleted = false
            lastUser = null
        }
    }

    if (!welcomeCompleted) {
        PremiumWelcomeScreen(
            username = authenticatedUser?.displayName?.ifBlank { null } ?: authenticatedUser?.email?.substringBefore("@")?.uppercase(Locale.ROOT) ?: "MEMBER",
            progress = welcomeProgress,
            statusText = currentWelcomeStepText
        )
        return
    }

    val syncNotification by viewModel.currentSyncNotification.collectAsStateWithLifecycle()
    val isLiveSyncEnabled by viewModel.isLiveSyncEnabled.collectAsStateWithLifecycle()

    // Master Filters (Synced across the whole app)
    var selectedYear by remember { mutableStateOf(2026) }
    var selectedMonth by remember { mutableStateOf(6) } // Default to June based on current system date

    // Bottom Navigation States
    var activeTab by remember { mutableStateOf(0) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showInfoTipsDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showProfileEditDialog by remember { mutableStateOf(false) }

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("app_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("quick_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Add Transaction",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        topBar = {
            val currentUser = authenticatedUser
            val username = if (currentUser?.displayName?.isNotBlank() == true) {
                currentUser.displayName.uppercase(Locale.ROOT)
            } else {
                currentUser?.email?.substringBefore("@")?.uppercase(Locale.ROOT) ?: "BALA"
            }
            val lastSync by viewModel.lastSyncTime.collectAsStateWithLifecycle()
            val isConnectionSyncing by viewModel.isConnectionSyncing.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showProfileEditDialog = true }
                            .testTag("profile_edit_trigger_row")
                    ) {
                        UserProfileAvatar(
                            photoUri = currentUser?.photoUri,
                            displayName = username,
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hi $username",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "AI Savings Insights",
                            tint = TealPrimary,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { showInfoTipsDialog = true }
                                .testTag("ai_savings_info_button")
                        )
                        val hasSyncedAlert = transactions.any { it.notes?.contains("Automatically synced via Live Banking Node API") == true }
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (hasSyncedAlert) MintAccent else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { showNotificationsDialog = true }
                                .testTag("notification_bell_button")
                        )
                    }
                }

                // Financial secure channel connection status bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070709))
                        .border(width = 0.5.dp, color = BorderSlate)
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnectionSyncing) TealPrimary else MintAccent)
                        )
                        Text(
                            text = "PORTFOLIO SECURE TUNNELS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftGray,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "LAST SYNC: ${lastSync.uppercase()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (lastSync == "Sync pending") AmberWarning else MintAccent,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = BorderSlate
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add") },
                    label = { Text("Add", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = BorderSlate
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Reports") },
                    label = { Text("Reports", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = BorderSlate
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Wealth") },
                    label = { Text("Wealth", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = BorderSlate
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray,
                        indicatorColor = BorderSlate
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
            // Master Year & Month Selector bar (fixed at top of the screen content, synced globally)
            GlobalFilterBar(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onYearChanged = { selectedYear = it },
                onMonthChanged = { selectedMonth = it }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        yearFilter = selectedYear,
                        monthFilter = selectedMonth,
                        onNavigateToAdd = { activeTab = 1 },
                        onViewAllTips = { showInfoTipsDialog = true }
                    )
                    1 -> AddTransactionScreen(
                        viewModel = viewModel,
                        defaultYear = selectedYear,
                        defaultMonth = selectedMonth
                    )
                    2 -> ReportsScreen(
                        viewModel = viewModel,
                        yearFilter = selectedYear,
                        monthFilter = selectedMonth
                    )
                    3 -> WealthScreen(
                        viewModel = viewModel
                    )
                    4 -> SettingsScreen(
                        viewModel = viewModel
                    )
                }

                // Toast Sync Notification Info Row with clean animated sliding entrance
                androidx.compose.animation.AnimatedVisibility(
                    visible = syncNotification != null,
                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    syncNotification?.let { msg ->
                        // Detect notification type for styling
                        val isErrorOrWarning = msg.startsWith("❌") || msg.startsWith("⚠️") || msg.contains("Error", ignoreCase = true) || msg.contains("Failed", ignoreCase = true)
                        val isSuccess = msg.startsWith("✅") || msg.startsWith("🟢") || msg.contains("Success", ignoreCase = true) || msg.contains("complete", ignoreCase = true) || msg.contains("updated", ignoreCase = true) || msg.contains("saved", ignoreCase = true) || msg.contains("Welcome", ignoreCase = true)

                        val (cardColor, icon, label) = when {
                            isErrorOrWarning -> Triple(AlertCoral, Icons.Default.Warning, "ALERT DETECTED")
                            isSuccess -> Triple(MintAccent, Icons.Default.CheckCircle, "INSTRUCTION SUCCESS")
                            else -> Triple(ElectricBlue, Icons.Default.Refresh, "SYSTEM BULLETIN")
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .testTag("sync_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = "Notify", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.82f), letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(msg, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { viewModel.clearNotification() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQuickAddDialog) {
        QuickAddTransactionDialog(
            onDismiss = { showQuickAddDialog = false },
            viewModel = viewModel
        )
    }

    if (showInfoTipsDialog) {
        AiSavingsInsightsDialog(
            onDismiss = { showInfoTipsDialog = false }
        )
    }

    if (showNotificationsDialog) {
        SyncNotificationsDialog(
            onDismiss = { showNotificationsDialog = false },
            viewModel = viewModel
        )
    }

    if (showProfileEditDialog) {
        val currentUser = authenticatedUser
        val usernameVal = if (currentUser?.displayName?.isNotBlank() == true) {
            currentUser.displayName
        } else {
            currentUser?.email?.substringBefore("@")?.uppercase(Locale.ROOT) ?: "BALA"
        }
        ProfileEditDialog(
            currentName = usernameVal,
            currentPhotoUri = currentUser?.photoUri,
            onDismiss = { showProfileEditDialog = false },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTransactionDialog(
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel
) {
    var amountStr by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Expense") } // Income, Expense, Investment
    var selectedCategory by remember { mutableStateOf("Food") }
    var payerInput by remember { mutableStateOf("Self") }
    var paymentMethodInput by remember { mutableStateOf("UPI") }
    var descriptionInput by remember { mutableStateOf("") }
    var personNameInput by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    val formattedDate = remember(dateMs) { sdf.format(java.util.Date(dateMs)) }

    val dbCategories by viewModel.categories.collectAsStateWithLifecycle(emptyList())
    val categoriesList = remember(dbCategories, transactionType) {
        val filtered = dbCategories.filter { it.type == transactionType }.map { it.name }
        if (filtered.isNotEmpty()) {
            filtered
        } else {
            when (transactionType) {
                "Income" -> listOf("Salary", "Business", "Bonus", "Money Received", "Other")
                "Expense" -> listOf("Food", "Grocery", "Shopping", "Bills", "Medical", "Fuel", "Education", "Credit Card Expense", "Money Given", "Other")
                else -> listOf("Stocks", "Mutual Fund", "Gold", "Emergency Fund", "Property", "Other")
            }
        }
    }

    LaunchedEffect(transactionType, categoriesList) {
        if (selectedCategory !in categoriesList) {
            selectedCategory = categoriesList.firstOrNull() ?: "Other"
        }
    }

    val paymentModes = listOf("Cash", "Bank", "UPI", "GPay", "PhonePe", "Credit Card", "Debit Card")
    val payersList = listOf("Self", "Dad", "Mom", "Sarah", "Kumar", "Amit", "Others")

    val datePickerDialog = remember {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = dateMs
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                dateMs = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = dateMs
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                dateMs = newCal.timeInMillis
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        )
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .border(1.2.dp, BorderSlate, RoundedCornerShape(24.dp))
                .padding(4.dp)
                .testTag("quick_add_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(TealPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "QUICK RECORD",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftGray, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                // Segmented Type Selector (Expense vs Income vs Investment)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateDark)
                        .padding(3.dp)
                ) {
                    val types = listOf("Expense", "Income", "Investment")
                    types.forEach { type ->
                        val isSelected = transactionType == type
                        val bg = if (isSelected) {
                            when (type) {
                                "Income" -> MintAccent.copy(alpha = 0.2f)
                                "Expense" -> AlertCoral.copy(alpha = 0.2f)
                                else -> ElectricBlue.copy(alpha = 0.2f)
                            }
                        } else Color.Transparent
                        val borderCol = if (isSelected) {
                            when (type) {
                                "Income" -> MintAccent
                                "Expense" -> AlertCoral
                                else -> ElectricBlue
                            }
                        } else Color.Transparent
                        val textCol = if (isSelected) {
                            when (type) {
                                "Income" -> MintAccent
                                "Expense" -> AlertCoral
                                else -> ElectricBlue
                            }
                        } else SoftGray

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                .clickable { transactionType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.uppercase(Locale.ROOT),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textCol
                            )
                        }
                    }
                }

                // Amount Text Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AMOUNT (₹)", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.toDoubleOrNull() != null || input == ".") {
                                amountStr = input
                            }
                        },
                        placeholder = { Text("0.00", fontSize = 14.sp, color = SoftGray) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("quick_amount_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        leadingIcon = {
                            Text("₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        }
                    )
                }

                // Scrollable Category Slider
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CATEGORY", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { category ->
                            val isSel = selectedCategory == category
                            val categoryColor = when (transactionType) {
                                "Income" -> MintAccent
                                "Expense" -> AlertCoral
                                else -> ElectricBlue
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) categoryColor else SlateDark)
                                    .border(1.dp, if (isSel) categoryColor else BorderSlate, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("quick_category_$category")
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else OffWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Date & Time Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("DATE & TIME", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            border = BorderStroke(1.dp, BorderSlate),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite, containerColor = SlateDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_date_btn")
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            val dateForm = SimpleDateFormat("dd MMM yyyy", Locale.US)
                            Text(dateForm.format(Date(dateMs)), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { timePickerDialog.show() },
                            border = BorderStroke(1.dp, BorderSlate),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite, containerColor = SlateDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_time_btn")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            val timeForm = SimpleDateFormat("hh:mm a", Locale.US)
                            Text(timeForm.format(Date(dateMs)), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Payer Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PAYER", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        payersList.forEach { p ->
                            val isSel = payerInput == p
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) TealPrimary else SlateDark)
                                    .border(1.dp, if (isSel) TealPrimary else BorderSlate, RoundedCornerShape(8.dp))
                                    .clickable { payerInput = p }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("quick_payer_$p")
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else OffWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Payment Modes Slider
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PAYMENT METHOD", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentModes.forEach { mode ->
                            val isSel = paymentMethodInput == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) TealPrimary else SlateDark)
                                    .border(1.dp, if (isSel) TealPrimary else BorderSlate, RoundedCornerShape(8.dp))
                                    .clickable { paymentMethodInput = mode }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("quick_method_$mode")
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else OffWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Optional Person & Description
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = personNameInput,
                        onValueChange = { personNameInput = it },
                        placeholder = { Text("Person name (optional)...", fontSize = 12.sp, color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("quick_person_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(14.dp))
                        }
                    )

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        placeholder = { Text("Short description (optional)...", fontSize = 12.sp, color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("quick_desc_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Core Save/Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, BorderSlate),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftGray),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    val inputAmount = amountStr.toDoubleOrNull() ?: 0.0
                    val canSave = inputAmount > 0.0
                    Button(
                        onClick = {
                            if (canSave) {
                                viewModel.addTransaction(
                                    amount = inputAmount,
                                    category = selectedCategory,
                                    payer = payerInput,
                                    notes = descriptionInput,
                                    type = transactionType,
                                    paymentMethod = paymentMethodInput,
                                    personName = personNameInput,
                                    customDate = dateMs
                                )
                                onDismiss()
                            }
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            disabledContainerColor = TealPrimary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("quick_save_btn")
                    ) {
                        Text("SAVE RECORD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (canSave) Color.White else SoftGray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel
) {
    var amountStr by remember { mutableStateOf(transaction.amount.toString()) }
    var transactionType by remember { mutableStateOf(transaction.type) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var payerInput by remember { mutableStateOf(transaction.payer) }
    var notesInput by remember { mutableStateOf(transaction.notes) }
    var paymentMethodInput by remember { mutableStateOf(transaction.paymentMethod) }
    var dateMs by remember { mutableStateOf(transaction.date) }
    
    val context = LocalContext.current
    
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    val formattedDate = remember(dateMs) { sdf.format(java.util.Date(dateMs)) }

    val datePickerDialog = remember {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = dateMs
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                dateMs = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val categoriesList = when (transactionType) {
        "Income" -> listOf("Salary", "Business", "Bonus", "Money Received", "Other")
        "Expense" -> listOf("Food", "Grocery", "Shopping", "Bills", "Medical", "Fuel", "Education", "Credit Card Expense", "Money Given", "Other")
        else -> listOf("Stocks", "Mutual Fund", "Gold", "Emergency Fund", "Property", "Other")
    }

    // Auto update selected category if it is invalid for new type
    LaunchedEffect(transactionType) {
        if (selectedCategory !in categoriesList) {
            selectedCategory = categoriesList.firstOrNull() ?: "Other"
        }
    }

    val payersList = listOf("Dad", "Mom", "Sarah", "Kumar", "Amit", "Others")
    val paymentModes = listOf("Cash", "Bank", "UPI", "GPay", "PhonePe", "Credit Card", "Debit Card")

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, BorderSlate, RoundedCornerShape(24.dp))
                .padding(4.dp)
                .testTag("edit_transaction_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(TealPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "EDIT TRANSACTION",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = OffWhite
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftGray, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                // Transaction Type Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("TRANSACTION TYPE", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Expense", "Income", "Investment").forEach { type ->
                            val isSelected = transactionType == type
                            val bgColor = if (isSelected) {
                                when (type) {
                                    "Income" -> MintAccent.copy(alpha = 0.15f)
                                    "Expense" -> AlertCoral.copy(alpha = 0.15f)
                                    else -> ElectricBlue.copy(alpha = 0.15f)
                                }
                            } else Color.Transparent
                            val borderColor = if (isSelected) {
                                when (type) {
                                    "Income" -> MintAccent
                                    "Expense" -> AlertCoral
                                    else -> ElectricBlue
                                }
                            } else BorderSlate
                            val textColor = if (isSelected) {
                                when (type) {
                                    "Income" -> MintAccent
                                    "Expense" -> AlertCoral
                                    else -> ElectricBlue
                                }
                            } else SoftGray

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable { transactionType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = textColor)
                            }
                        }
                    }
                }

                // Amount
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AMOUNT (₹)", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.toDoubleOrNull() != null || input == ".") {
                                amountStr = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_amount_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        leadingIcon = {
                            Text("₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        }
                    )
                }

                // Category Selection Flow
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CATEGORY", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoriesList) { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) TealPrimary else SlateDark)
                                    .border(1.dp, if (isSelected) TealPrimary else BorderSlate, RoundedCornerShape(20.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else OffWhite
                                )
                            }
                        }
                    }
                }

                // Payer Selection Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PAYER", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(payersList) { p ->
                            val isSelected = payerInput == p
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) TealPrimary else SlateDark)
                                    .border(1.dp, if (isSelected) TealPrimary else BorderSlate, RoundedCornerShape(20.dp))
                                    .clickable { payerInput = p }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else OffWhite
                                )
                            }
                        }
                    }
                    if (payerInput == "Others") {
                        OutlinedTextField(
                            value = "",
                            onValueChange = { payerInput = it },
                            placeholder = { Text("Enter payer name...", fontSize = 13.sp, color = SoftGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite,
                                focusedContainerColor = SlateDark,
                                unfocusedContainerColor = SlateDark
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                // Payment Method
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PAYMENT METHOD", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(paymentModes) { pm ->
                            val isSelected = paymentMethodInput == pm
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) TealPrimary else SlateDark)
                                    .border(1.dp, if (isSelected) TealPrimary else BorderSlate, RoundedCornerShape(20.dp))
                                    .clickable { paymentMethodInput = pm }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = pm,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else OffWhite
                                )
                            }
                        }
                    }
                }

                // Date Time Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("DATE & TIME", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        border = BorderStroke(1.dp, BorderSlate),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formattedDate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Notes / Description
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("NOTES / DESCRIPTION", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        placeholder = { Text("What was this ledger entry for?", fontSize = 13.sp, color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.deleteTransaction(transaction.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertCoral),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DELETE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, BorderSlate),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftGray),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    val inputAmount = amountStr.toDoubleOrNull() ?: 0.0
                    val canSave = amountStr.isNotBlank() && inputAmount > 0.0
                    Button(
                        onClick = {
                            if (canSave) {
                                viewModel.updateTransaction(
                                    transaction.copy(
                                        amount = inputAmount,
                                        category = selectedCategory,
                                        payer = payerInput,
                                        notes = notesInput,
                                        type = transactionType,
                                        paymentMethod = paymentMethodInput,
                                        date = dateMs
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            disabledContainerColor = TealPrimary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("edit_save_btn")
                    ) {
                        Text("SAVE CHANGES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (canSave) Color.White else SoftGray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditDialog(
    currentName: String,
    currentPhotoUri: String?,
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var photoUriInput by remember { mutableStateOf(currentPhotoUri ?: "") }
    
    val presets = listOf("💼", "💰", "👑", "🚀", "🦄", "🎯", "🦁", "🐼", "🦊", "🐯", "🥑", "🍕")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(24.dp))
                .testTag("profile_edit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("👤", fontSize = 20.sp)
                        Text(
                            text = "EDIT PROFILE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = TealPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftGray, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                // Current Preview
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserProfileAvatar(
                        photoUri = if (photoUriInput.isNotBlank()) photoUriInput else null,
                        displayName = nameInput.ifBlank { "M" },
                        size = 72.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Avatar Preview", fontSize = 10.sp, color = SoftGray)
                }

                // Name Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("USER NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftGray)
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("Enter your name", fontSize = 12.sp, color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("profile_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                // Preset Avatars Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CHOOSE QUICK EMOTICON AVATAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { emoji ->
                            val presetVal = "preset:$emoji"
                            val isSelected = photoUriInput == presetVal
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) TealPrimary.copy(alpha = 0.25f) else SlateDark)
                                    .border(1.2.dp, if (isSelected) TealPrimary else BorderSlate, CircleShape)
                                    .clickable { photoUriInput = presetVal }
                                    .testTag("preset_avatar_$emoji"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                // Custom Photo URL Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("OR PASTE PROFILE IMAGE URL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftGray)
                    OutlinedTextField(
                        value = if (photoUriInput.startsWith("preset:")) "" else photoUriInput,
                        onValueChange = { photoUriInput = it },
                        placeholder = { Text("https://example.com/avatar.png", fontSize = 11.sp, color = SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_photo_url_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, BorderSlate),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    val isValid = nameInput.isNotBlank()
                    Button(
                        onClick = {
                            if (isValid) {
                                viewModel.updateProfile(nameInput, if (photoUriInput.isBlank()) null else photoUriInput)
                                onDismiss()
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            disabledContainerColor = TealPrimary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("profile_save_btn")
                    ) {
                        Text("SAVE PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isValid) Color.White else SoftGray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    category: String,
    currentLimit: Double,
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel
) {
    var limitStr by remember { mutableStateOf(if (currentLimit > 0.0) currentLimit.toInt().toString() else "") }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, BorderSlate, RoundedCornerShape(24.dp))
                .padding(4.dp)
                .testTag("set_budget_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(TealPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "SET BUDGET",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = OffWhite
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftGray, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
                
                Text(
                    text = "Configure the monthly budget limit for category '$category'. FineNest will dynamically warn you when approaching or exceeding this amount.",
                    fontSize = 12.sp,
                    color = SoftGray,
                    lineHeight = 16.sp
                )

                // Limit Text Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("MONTHLY LIMIT (₹)", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = limitStr,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.toDoubleOrNull() != null || input == ".") {
                                limitStr = input
                            }
                        },
                        placeholder = { Text("No limit (Enter 0 or amount)", fontSize = 14.sp, color = SoftGray) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("set_budget_limit_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        leadingIcon = {
                            Text("₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, BorderSlate),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftGray),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    val inputLimit = limitStr.toDoubleOrNull() ?: 0.0
                    val canSave = limitStr.isEmpty() || inputLimit >= 0.0
                    Button(
                        onClick = {
                            if (canSave) {
                                viewModel.addBudget(category, inputLimit)
                                onDismiss()
                            }
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            disabledContainerColor = TealPrimary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("set_budget_save_btn")
                    ) {
                        Text("SAVE LIMIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (canSave) Color.White else SoftGray)
                    }
                }
            }
        }
    }
}

// ==================== GLOBAL FILTERS BAR ====================
@Composable
fun GlobalFilterBar(
    selectedYear: Int,
    selectedMonth: Int,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int) -> Unit
) {
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    var showQuickPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, BorderSlate)
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Month Button
            IconButton(
                onClick = {
                    if (selectedMonth == 1) {
                        onMonthChanged(12)
                        onYearChanged(selectedYear - 1)
                    } else {
                        onMonthChanged(selectedMonth - 1)
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BorderSlate.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Month Year Text display (Clickable to show direct picker panels)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showQuickPicker = !showQuickPicker }
                    .background(TealPrimary.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calendar",
                    tint = TealPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${monthNames[selectedMonth - 1]} $selectedYear",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Toggle Picker",
                    tint = SoftGray,
                    modifier = Modifier.size(16.dp).rotate(if (showQuickPicker) 90f else 270f)
                )
            }

            // Next Month Button
            IconButton(
                onClick = {
                    if (selectedMonth == 12) {
                        onMonthChanged(1)
                        onYearChanged(selectedYear + 1)
                    } else {
                        onMonthChanged(selectedMonth + 1)
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BorderSlate.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Animated / Conditional Expandable Quick Selector for direct access
        if (showQuickPicker) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Year selection chips
            Text("SELECT YEAR", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val years = listOf(2025, 2026, 2027, 2028, 2029)
                years.forEach { year ->
                    val isSelected = year == selectedYear
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) TealPrimary else BorderSlate)
                            .clickable { onYearChanged(year) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("year_filter_$year")
                    ) {
                        Text(year.toString(), color = if (isSelected) Color.White else OffWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month selection pills
            Text("SELECT MONTH", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..12).forEach { m ->
                    val isSelected = m == selectedMonth
                    val shortMonthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ElectricBlue else BorderSlate)
                            .clickable { onMonthChanged(m) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("month_filter_$m")
                    ) {
                        Text(shortMonthNames[m - 1], color = if (isSelected) Color.White else OffWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
// ==================== DASHBOARD TAB ====================
@Composable
fun MonexCircularProgress(
    spent: Double,
    budget: Double,
    modifier: Modifier = Modifier
) {
    val percentage = if (budget > 0) (spent / budget).toFloat() else 0f
    val sweepAngle = (percentage.coerceIn(0f, 1f) * 360f)
    
    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()
            val sizePx = size.minDimension
            val radius = (sizePx - strokeWidth) / 2
            val centerOffset = center
            
            // 1. Draw track
            drawCircle(
                color = BorderSlate,
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeWidth)
            )
            
            // 2. Draw progress arc
            drawArc(
                color = TealPrimary,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // 3. Draw end glow dot
            if (sweepAngle > 0) {
                val endAngleRad = Math.toRadians((sweepAngle - 90).toDouble())
                val dotX = (centerOffset.x + radius * Math.cos(endAngleRad)).toFloat()
                val dotY = (centerOffset.y + radius * Math.sin(endAngleRad)).toFloat()
                
                drawCircle(
                    color = Color(0xFFFF9F1C),
                    radius = 7.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Up",
                tint = SoftGray,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(135f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatRupee(spent).substringBefore("."),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = OffWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star",
                    tint = TealPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SoftGray
                )
            }
        }
    }
}

@Composable
fun MonexTrendsChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val shortMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    
    val monthlySpends = DoubleArray(6) { 0.0 }
    transactions.filter { it.type == "Expense" }.forEach { tx ->
        val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
        val month = cal.get(Calendar.MONTH)
        if (month in 0..5) {
            monthlySpends[month] += tx.amount
        }
    }
    
    val defaultBaseValues = listOf(35000.0, 48000.0, 32200.0, 31000.0, 50300.0, 43400.0)
    val chartData = DoubleArray(6) { idx ->
        val actual = monthlySpends[idx]
        if (actual > 0.0) actual else defaultBaseValues[idx]
    }
    
    val budgetLimit = 45000.0
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, BorderSlate, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📈 SPENDING TRENDS vs BUDGET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftGray,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Interactive Live Sync",
                        fontSize = 10.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(TealPrimary))
                        Text("Spend", fontSize = 9.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AlertCoral))
                        Text("Budget", fontSize = 9.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val w = size.width.toFloat()
                                    val stepX = w / (shortMonths.size + 1).toFloat()
                                    var minDiff = Float.MAX_VALUE
                                    var closestIdx = 0
                                    for (i in shortMonths.indices) {
                                        val x = stepX * (i + 1).toFloat()
                                        val diff = kotlin.math.abs(offset.x - x)
                                        if (diff < minDiff) {
                                            minDiff = diff
                                            closestIdx = i
                                        }
                                    }
                                    selectedPointIndex = if (selectedPointIndex == closestIdx) null else closestIdx
                                }
                            )
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 20f
                    val paddingTop = 15f
                    val plotHeight = h - paddingBottom - paddingTop
                    
                    val maxVal = 65000f
                    val stepX = w / (shortMonths.size + 1)
                    
                    val gridCount = 4
                    val stepY = plotHeight / (gridCount + 1)
                    for (i in 1..gridCount) {
                        val y = paddingTop + i * stepY
                        drawLine(
                            color = BorderSlate.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    val budgetY = h - paddingBottom - ((budgetLimit.toFloat() / maxVal) * plotHeight)
                    drawLine(
                        color = AlertCoral.copy(alpha = 0.8f),
                        start = Offset(0f, budgetY),
                        end = Offset(w, budgetY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    
                    val spentPoints = mutableListOf<Offset>()
                    for (i in shortMonths.indices) {
                        val x = stepX * (i + 1)
                        val spentVal = chartData[i].toFloat()
                        val y = h - paddingBottom - ((spentVal / maxVal) * plotHeight)
                        spentPoints.add(Offset(x, y))
                    }
                    
                    for (i in 0 until spentPoints.size - 1) {
                        drawLine(
                            color = TealPrimary,
                            start = spentPoints[i],
                            end = spentPoints[i + 1],
                            strokeWidth = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    
                    for (i in spentPoints.indices) {
                        val pt = spentPoints[i]
                        val isHighlighted = selectedPointIndex == i
                        
                        if (isHighlighted) {
                            drawCircle(
                                color = TealPrimary.copy(alpha = 0.3f),
                                radius = 12.dp.toPx(),
                                center = pt
                            )
                        }
                        
                        drawCircle(
                            color = TealPrimary,
                            radius = 6.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = SlateCard,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                shortMonths.forEachIndexed { i, month ->
                    val isSelected = selectedPointIndex == i
                    Text(
                        text = month,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TealPrimary else SoftGray,
                        modifier = Modifier.clickable {
                            selectedPointIndex = if (selectedPointIndex == i) null else i
                        }
                    )
                }
            }
            
            selectedPointIndex?.let { index ->
                val spent = chartData[index]
                val variance = spent - budgetLimit
                val isOverSpent = variance > 0
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TealPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Analysis: ${shortMonths[index]} 2026",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = OffWhite
                            )
                            Text(
                                text = if (isOverSpent) "Over Budget" else "Under Budget",
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = if (isOverSpent) AlertCoral else TealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ACTUAL SPENT", fontSize = 8.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                Text("₹${DecimalFormat("#,##,###").format(spent)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = OffWhite)
                            }
                            Column {
                                Text("BUDGET GOAL", fontSize = 8.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                Text("₹${DecimalFormat("#,##,###").format(budgetLimit)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SoftGray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("VARIANCE", fontSize = 8.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${if (isOverSpent) "+" else ""}₹${DecimalFormat("#,##,###").format(variance)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOverSpent) AlertCoral else TealPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveSavingsCalculatorCard(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var targetAmountStr by remember { mutableStateOf("100000") }
    var monthsSliderValue by remember { mutableStateOf(12f) }
    var calculatorGoalLabel by remember { mutableStateOf("Dream Trip") }
    
    val targetAmount = targetAmountStr.toDoubleOrNull() ?: 0.0
    val months = monthsSliderValue.toInt()
    val recommendedMonthlySaving = if (months > 0) targetAmount / months else 0.0
    
    val systemBudget = 45000.0
    val budgetPct = if (recommendedMonthlySaving > 0) (recommendedMonthlySaving / systemBudget) * 100.0 else 0.0
    val context = LocalContext.current
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, BorderSlate, RoundedCornerShape(20.dp))
            .testTag("savings_calculator_card")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🧮", fontSize = 18.sp)
                Text("SAVINGS GOAL CALCULATOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftGray)
            }
            
            HorizontalDivider(color = BorderSlate)
            
            OutlinedTextField(
                value = targetAmountStr,
                onValueChange = { targetAmountStr = it },
                label = { Text("Desired Target Amount (₹)", fontSize = 11.sp, color = OffWhite) },
                leadingIcon = { Text("₹", color = SoftGray, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target Duration", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.SemiBold)
                    Text("$months month${if (months > 1) "s" else ""}", fontSize = 12.sp, color = TealPrimary, fontWeight = FontWeight.ExtraBold)
                }
                Slider(
                    value = monthsSliderValue,
                    onValueChange = { monthsSliderValue = it },
                    valueRange = 1f..60f,
                    colors = SliderDefaults.colors(
                        thumbColor = TealPrimary,
                        activeTrackColor = TealPrimary,
                        inactiveTrackColor = BorderSlate
                    )
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealPrimary.copy(alpha = 0.05f))
                    .border(1.2.dp, TealPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("RECOMMENDED MONTHLY SAVINGS", fontSize = 9.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    Text("₹${DecimalFormat("#,##,###").format(recommendedMonthlySaving)} / mo", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TealPrimary)
                    Text(
                        text = "To reach ₹${DecimalFormat("#,##,###").format(targetAmount)} in $months months, save ₹${DecimalFormat("#,##,###").format(recommendedMonthlySaving)} monthly. This represents about ${String.format(Locale.ROOT, "%.1f", budgetPct)}% of your dynamic family budget (₹45k).",
                        fontSize = 11.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            OutlinedTextField(
                value = calculatorGoalLabel,
                onValueChange = { calculatorGoalLabel = it },
                label = { Text("Quick Goal Label", fontSize = 11.sp, color = OffWhite) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
            )
            
            Button(
                onClick = {
                    if (targetAmount > 0) {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.MONTH, months)
                        val monthsList = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        val dateStr = "${monthsList[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
                        
                        viewModel.addSavingsGoal(
                            name = "🧮 Calculator | $calculatorGoalLabel",
                            target = targetAmount,
                            current = 0.0,
                            date = dateStr,
                            automated = false
                        )
                        android.widget.Toast.makeText(context, "🎯 Savings goal synchronized from calculator!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Please specify a valid savings target amount", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Establish Goal from Calculation", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    yearFilter: Int,
    monthFilter: Int,
    onNavigateToAdd: () -> Unit,
    onViewAllTips: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
    val budgetsList by viewModel.budgets.collectAsStateWithLifecycle()

    var showSetBudgetForCategory by remember { mutableStateOf<String?>(null) }
    val budgetsMap = remember(budgetsList) { budgetsList.associate { it.category to it.allocatedLimit } }

    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") }
    var filterStartDate by remember { mutableStateOf<Long?>(null) }
    var filterEndDate by remember { mutableStateOf<Long?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTxs = transactions.filter {
        getYearFromTimestamp(it.date) == yearFilter && getMonthFromTimestamp(it.date) == monthFilter
    }

    val searchFilteredTxs = transactions.filter { tx ->
        val matchesKeyword = searchQuery.isBlank() || 
            tx.category.contains(searchQuery, ignoreCase = true) || 
            tx.notes.contains(searchQuery, ignoreCase = true) || 
            tx.payer.contains(searchQuery, ignoreCase = true) || 
            tx.paymentMethod.contains(searchQuery, ignoreCase = true) ||
            tx.amount.toString().contains(searchQuery)

        val matchesType = filterType == "All" || tx.type.equals(filterType, ignoreCase = true)

        val matchesDate = if (filterStartDate != null || filterEndDate != null) {
            val startOK = filterStartDate == null || tx.date >= filterStartDate!!
            val endOK = filterEndDate == null || tx.date <= filterEndDate!!
            startOK && endOK
        } else {
            getYearFromTimestamp(tx.date) == yearFilter && getMonthFromTimestamp(tx.date) == monthFilter
        }

        matchesKeyword && matchesType && matchesDate
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val filterSdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    val incomeVal = filteredTxs.filter { it.type == "Income" }.sumOf { it.amount }
    val expenseVal = filteredTxs.filter { it.type == "Expense" }.sumOf { it.amount }
    val investmentVal = filteredTxs.filter { it.type == "Investment" }.sumOf { it.amount }
    val currentBalance = incomeVal - expenseVal - investmentVal

    var activeSubTab by remember { mutableStateOf(0) }

    var showAiBot by remember { mutableStateOf(false) }
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    var aiTextQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Calculate actual CASH in hand vs BANK accounts balances
            // CASH in Hand = Total Cash Incomes - Total Cash Expenses/Investments
            val cashTxList = transactions.filter { it.paymentMethod == "Cash" }
            val cashInHand = cashTxList.filter { it.type == "Income" }.sumOf { it.amount } -
                     cashTxList.filter { it.type == "Expense" || it.type == "Investment" }.sumOf { it.amount }

            // MONEY in Account = Total Bank/Wallet Incomes - Expenses/Investments (Debit Card, Bank Transfer, UPI, Cheque, etc.)
            val bankTxList = transactions.filter { it.paymentMethod != "Cash" && it.paymentMethod != "Credit Card" }
            val moneyInAccount = bankTxList.filter { it.type == "Income" }.sumOf { it.amount } -
                        bankTxList.filter { it.type == "Expense" || it.type == "Investment" }.sumOf { it.amount }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, BorderSlate, RoundedCornerShape(18.dp))
                    .fadeInSlideIn(delayMillis = 50)
                    .testTag("core_balances_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Column {
                        Text(
                            text = "NET LIQUID SYSTEM BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftGray,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupee(currentBalance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (currentBalance >= 0) MintAccent else AlertCoral,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // SPLIT BALANCES ROW (CASH IN HAND & MONEY IN THE ACCOUNT)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Cash in hand
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, BorderSlate.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Cash",
                                        tint = MintAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "CASH IN HAND",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftGray
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatRupee(cashInHand).substringBefore("."),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OffWhite
                                )
                                Text(
                                    text = "Physical Ledger",
                                    fontSize = 9.sp,
                                    color = SoftGray
                                )
                            }
                        }

                        // Card 2: Money in the account
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, BorderSlate.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Account Balance",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "MONEY IN A/C",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftGray
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatRupee(moneyInAccount).substringBefore("."),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OffWhite
                                )
                                Text(
                                    text = "Bank / UPI / Debit",
                                    fontSize = 9.sp,
                                    color = SoftGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    val statusText = when {
                        currentBalance > 20000.0 -> "🟢 Surplus Cash Position: Strong liquid operating cushion."
                        currentBalance >= 0.0 -> "🟡 Balanced Liquid Position: Moderate savings remaining."
                        else -> "🔴 Hard Outflow Deficit: System cash requirements exceed inflow."
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateDark)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (currentBalance >= 0.0) MintAccent else AlertCoral))
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OffWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MintAccent))
                                Text("Inflow (Income)", fontSize = 11.sp, color = SoftGray)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(formatRupee(incomeVal), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MintAccent)
                        }
                        
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AlertCoral))
                                Text("Outflow (Spend)", fontSize = 11.sp, color = SoftGray)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(formatRupee(expenseVal), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AlertCoral)
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ElectricBlue))
                                Text("Investments", fontSize = 11.sp, color = SoftGray)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(formatRupee(investmentVal), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBlue)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column 1: BUDGET TRACKER (Weight 1f)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.2.dp, BorderSlate, RoundedCornerShape(16.dp))
                        .fadeInSlideIn(delayMillis = 150)
                        .testTag("dashboard_budget_tracker_grid_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📊", fontSize = 16.sp)
                            Text("BUDGET TRACKER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftGray)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Render mini category budgets list
                        val activeCategories = listOf("Grocery", "Shopping", "Food", "Bills", "Medical")
                        activeCategories.take(4).forEach { cat ->
                            val allocated = budgetsMap[cat] ?: 5000.0
                            val totalSpent = filteredTxs.filter { it.type == "Expense" && it.category.equals(cat, ignoreCase = true) }.sumOf { it.amount }
                            val pct = if (allocated > 0) (totalSpent / allocated).coerceIn(0.0, 1.0) else 0.0
                            
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OffWhite)
                                    Text("${(pct * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (pct >= 0.9) AlertCoral else TealPrimary)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = pct.toFloat(),
                                    color = if (pct >= 0.9) AlertCoral else TealPrimary,
                                    trackColor = BorderSlate.copy(alpha = 0.2f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Manage Budgets ➜",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.clickable { activeSubTab = 1 }
                        )
                    }
                }

                // Column 2: SAVINGS TIPS (Weight 1f)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.2.dp, BorderSlate, RoundedCornerShape(16.dp))
                        .fadeInSlideIn(delayMillis = 250)
                        .testTag("dashboard_savings_tips_grid_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("💡", fontSize = 16.sp)
                            Text("SAVINGS TIPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftGray)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column {
                                Text("💡 50-30-20 Rule", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                Text("Limit groceries to 30% of standard liquid funds.", fontSize = 9.sp, color = SoftGray, lineHeight = 12.sp)
                            }
                            Column {
                                Text("💡 Sweep-In cash", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                                Text("Relocate 15% of stagnant bank balance.", fontSize = 9.sp, color = SoftGray, lineHeight = 12.sp)
                            }
                            Column {
                                Text("💡 Micro Savings", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                                Text("Passive auto-save rounding-ups generated ₹1.4K.", fontSize = 9.sp, color = SoftGray, lineHeight = 12.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "View All Tips ➜",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.clickable { onViewAllTips() }
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, BorderSlate, RoundedCornerShape(20.dp))
                    .fadeInSlideIn(delayMillis = 350)
                    .testTag("circular_spent_gauge_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val monthNamesList = listOf(
                        "January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"
                    )
                    val monthName = monthNamesList.getOrElse(monthFilter - 1) { "June" }
                    
                    Text(
                        text = "Spent in $monthName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGray
                    )
                    
                    MonexCircularProgress(
                        spent = expenseVal,
                        budget = 45000.0,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Income", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatRupee(incomeVal).substringBefore("."), fontSize = 15.sp, fontWeight = FontWeight.Black, color = OffWhite)
                        }
                        
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderSlate))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Budget", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatRupee(45000.0).substringBefore("."), fontSize = 15.sp, fontWeight = FontWeight.Black, color = OffWhite)
                        }
                        
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderSlate))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Safe to spend", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            val daysInMonth = 30
                            val remaining = (45000.0 - expenseVal).coerceAtLeast(0.0)
                            val dailySafe = remaining / daysInMonth
                            val formattedDaily = if (dailySafe >= 1000) {
                                String.format(Locale.ROOT, "₹%.1fK/day", dailySafe / 1000.0)
                            } else {
                                String.format(Locale.ROOT, "₹%.0f/day", dailySafe)
                            }
                            Text(formattedDaily, fontSize = 15.sp, fontWeight = FontWeight.Black, color = OffWhite)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (activeSubTab == 0) TealPrimary.copy(alpha = 0.12f) else Color.Transparent)
                        .border(1.dp, if (activeSubTab == 0) TealPrimary else Color.Transparent, RoundedCornerShape(20.dp))
                        .clickable { activeSubTab = 0 }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Trends", tint = TealPrimary, modifier = Modifier.size(16.dp))
                    Text("Trends", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activeSubTab == 0) TealPrimary else OffWhite)
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (activeSubTab == 1) Color(0xFF9333EA).copy(alpha = 0.12f) else Color.Transparent)
                        .border(1.dp, if (activeSubTab == 1) Color(0xFF9333EA) else Color.Transparent, RoundedCornerShape(20.dp))
                        .clickable { activeSubTab = 1 }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Categories", tint = Color(0xFF9333EA), modifier = Modifier.size(16.dp))
                    Text("Categories", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activeSubTab == 1) Color(0xFF9333EA) else OffWhite)
                }
            }
        }

        item {
            if (activeSubTab == 0) {
                MonexTrendsChart(transactions = transactions)
            } else {
                val categorySpends = filteredTxs.filter { it.type == "Expense" }
                    .groupBy { it.category }
                    .map { (cat, txs) ->
                        val total = txs.sumOf { it.amount }
                        val percentage = if (expenseVal > 0) (total / expenseVal) * 100.0 else 0.0
                        CategoryReportRow(category = cat, monthlyTotal = total, yearlyTotal = total, monthPercentage = percentage)
                    }.sortedByDescending { it.monthlyTotal }
                
                if (categorySpends.isNotEmpty()) {
                    CategorySpendingDonutChart(categorySpends = categorySpends)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No category distribution logs for this month.", fontSize = 12.sp, color = SoftGray)
                    }
                }
            }
        }

        if (activeSubTab == 1) {
            val categorisedExpenses = filteredTxs.filter { it.type == "Expense" }
                .groupBy { it.category }
                .map { (cat, txs) ->
                    Triple(cat, txs.size, txs.sumOf { it.amount })
                }.sortedByDescending { it.third }
            
            if (categorisedExpenses.isNotEmpty()) {
                item {
                    Text(
                        text = "📋 ALL CATEGORIES DISTRIBUTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SoftGray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(categorisedExpenses) { (cat, spendCount, totalAmount) ->
                    val allocatedLimit = budgetsMap[cat] ?: 0.0
                    val hasBudget = allocatedLimit > 0.0
                    val percent = if (hasBudget) (totalAmount / allocatedLimit).toFloat() else 0f

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSlate, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val (colorBg, icon, iconColor) = when(cat.lowercase()) {
                                        "unknown" -> Triple(Color(0xFFFEE2E2), Icons.Default.Warning, Color(0xFFEF4444))
                                        "shopping" -> Triple(Color(0xFFE0F2FE), Icons.Default.ShoppingCart, Color(0xFF0284C7))
                                        "entertainment" -> Triple(Color(0xFFFAF5FF), Icons.Default.Star, Color(0xFF9333EA))
                                        "transfer" -> Triple(Color(0xFFF1F5F9), Icons.Default.Refresh, Color(0xFF475569))
                                        "food" -> Triple(Color(0xFFFEF3C7), Icons.Default.ShoppingCart, Color(0xFFD97706))
                                        "grocery" -> Triple(Color(0xFFECFDF5), Icons.Default.ShoppingCart, Color(0xFF059669))
                                        "bills" -> Triple(Color(0xFFFFE4E6), Icons.Default.List, Color(0xFFE11D48))
                                        "medical" -> Triple(Color(0xFFE0F2FE), Icons.Default.Favorite, Color(0xFF0284C7))
                                        "fuel" -> Triple(Color(0xFFFEF3C7), Icons.Default.Place, Color(0xFFD97706))
                                        else -> Triple(Color(0xFFECFDF5), Icons.Default.Info, Color(0xFF059669))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(colorBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = cat, tint = iconColor, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(cat, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = OffWhite)
                                        Text("$spendCount Spend${if (spendCount != 1) "s" else ""}", fontSize = 11.sp, color = SoftGray)
                                    }
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatRupee(totalAmount).substringBefore("."), fontWeight = FontWeight.Black, fontSize = 15.sp, color = OffWhite)
                                    val actionText = if (cat.lowercase() == "unknown") "Categorise >" else if (hasBudget) "Edit Budget >" else "Set budget >"
                                    Text(
                                        text = actionText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        modifier = Modifier.clickable {
                                            if (cat.lowercase() != "unknown") {
                                                showSetBudgetForCategory = cat
                                            }
                                        }
                                    )
                                }
                            }

                            // Dynamic Progress Bar
                            if (cat.lowercase() != "unknown") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val progressColor = when {
                                        !hasBudget -> BorderSlate
                                        percent >= 1.0f -> AlertCoral
                                        percent >= 0.8f -> AmberWarning
                                        else -> TealPrimary
                                    }
                                    
                                    val progressVal = if (hasBudget) percent.coerceIn(0f, 1f) else 0f
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(BorderSlate)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(progressVal)
                                                .clip(CircleShape)
                                                .background(progressColor)
                                        )
                                    }

                                    // Budget Label Details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (hasBudget) {
                                            Text(
                                                text = "${(percent * 100).toInt()}% of budget spent",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (percent >= 1.0f) AlertCoral else if (percent >= 0.8f) AmberWarning else SoftGray
                                            )
                                            Text(
                                                text = "Budget: ${formatRupee(allocatedLimit).substringBefore(".")}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftGray
                                            )
                                        } else {
                                            Text(
                                                text = "No budget limit set",
                                                fontSize = 11.sp,
                                                color = SoftGray,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, BorderSlate, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFEDD5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("New", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Grow Your Savings, Build Wealth, and Enjoy Assured Returns!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Invest Today @ 8.75% >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                    Text("💰", fontSize = 42.sp)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FineNest needs certain permissions to give you better experience.",
                            fontSize = 11.sp,
                            color = OffWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Check now",
                            fontSize = 11.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { /* trigger permission details */ }
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SoftGray,
                        modifier = Modifier.size(16.dp).clickable { /* dismiss promotion */ }
                    )
                }
            }
        }

        item {
            val peopleListState by viewModel.people.collectAsStateWithLifecycle(emptyList())
            var personSearchQuery by remember { mutableStateOf("") }
            
            val uniquePeople = remember(peopleListState, transactions, personSearchQuery) {
                val defaultNames = listOf("Dad", "Mom", "Sarah", "Kumar", "Amit")
                val dbNames = peopleListState.map { it.name }
                val fromTxs = transactions.flatMap { listOfNotNull(it.payer, if (it.notes.contains("With: ")) it.notes.substringAfter("With: ").substringBefore(")").trim() else null) }
                val merged = (dbNames + defaultNames + fromTxs)
                    .filter { it.isNotBlank() && !it.equals("Others", true) && !it.equals("Self", true) }
                    .distinct()
                
                if (personSearchQuery.isBlank()) {
                    merged.sorted()
                } else {
                    merged.filter { it.contains(personSearchQuery, ignoreCase = true) }.sorted()
                }
            }
            
            var selectedPerson by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(uniquePeople) {
                if (selectedPerson == null || selectedPerson !in uniquePeople) {
                    selectedPerson = uniquePeople.firstOrNull()
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                    .testTag("people_balance_tracker_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👥 PEOPLE BALANCE TRACKER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track Given & Received Balances",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = personSearchQuery,
                        onValueChange = { personSearchQuery = it },
                        placeholder = { Text("Filter person by name...", fontSize = 11.sp, color = SoftGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SoftGray, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (personSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { personSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = SoftGray, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("person_details_search_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uniquePeople.isEmpty()) {
                        Text("No matching people found.", fontSize = 11.sp, color = SoftGray, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uniquePeople.forEach { person ->
                                val isSel = selectedPerson == person
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSel) TealPrimary else SlateDark)
                                        .border(1.dp, if (isSel) TealPrimary else BorderSlate, RoundedCornerShape(20.dp))
                                        .clickable { selectedPerson = person }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("person_chip_$person")
                                ) {
                                    Text(
                                        text = person,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else OffWhite
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    selectedPerson?.let { person ->
                        val personTxs = transactions.filter {
                            it.payer.equals(person, ignoreCase = true) || 
                            (it.notes.contains("With: $person", ignoreCase = true)) || 
                            (it.notes.endsWith("With: $person", ignoreCase = true))
                        }
                        
                        val receivedSum = personTxs.filter { it.type == "Income" }.sumOf { it.amount }
                        val givenSum = personTxs.filter { it.type == "Expense" || it.type == "Investment" }.sumOf { it.amount }
                        val netBalVal = receivedSum - givenSum 

                        if (personTxs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recorded transactions for $person yet.",
                                    fontSize = 11.sp,
                                    color = SoftGray
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                personTxs.take(4).forEach { tx ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SlateDark)
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (tx.type == "Income") MintAccent else AlertCoral)
                                                )
                                                Text(
                                                    text = tx.category,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OffWhite,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (tx.notes.isNotBlank()) {
                                                Text(
                                                    text = tx.notes,
                                                    fontSize = 10.sp,
                                                    color = SoftGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (tx.type == "Income") "+ ${formatRupee(tx.amount)}" else "- ${formatRupee(tx.amount)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (tx.type == "Income") MintAccent else AlertCoral
                                            )
                                            Text(
                                                text = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(tx.date)),
                                                fontSize = 9.sp,
                                                color = SoftGray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSlate.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Received (In)", fontSize = 9.sp, color = SoftGray)
                                        Text(formatRupee(receivedSum), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                                    }
                                    Spacer(modifier = Modifier.width(1.dp).height(30.dp).background(BorderSlate))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Given (Out)", fontSize = 9.sp, color = SoftGray)
                                        Text(formatRupee(givenSum), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AlertCoral)
                                    }
                                }
                                
                                HorizontalDivider(color = BorderSlate, modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Outstanding Position:", fontSize = 11.sp, color = OffWhite, fontWeight = FontWeight.SemiBold)
                                    
                                    val absBal = Math.abs(netBalVal)
                                    val positionColor = when {
                                        netBalVal > 0 -> MintAccent
                                        netBalVal < 0 -> AlertCoral
                                        else -> SoftGray
                                    }
                                    val positionText = when {
                                        netBalVal > 0 -> "Owes Us (${formatRupee(absBal)})"
                                        netBalVal < 0 -> "We Owe (${formatRupee(absBal)})"
                                        else -> "Settled"
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(positionColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = positionText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = positionColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, TealPrimary.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                    .testTag("gemini_ai_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "AI", tint = TealPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("🤖 Gemini Wealth Advisor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Click toggle to consult dynamic financial expert", fontSize = 9.sp, color = SoftGray)
                            }
                        }
                        Switch(
                            checked = showAiBot,
                            onCheckedChange = { showAiBot = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TealPrimary,
                                checkedTrackColor = BorderSlate
                            ),
                            modifier = Modifier.testTag("ai_advisor_toggle")
                        )
                    }

                    if (showAiBot) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSlate)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Suggested queries:", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val promptSuggestions = listOf(
                                "How much did I spend this month?",
                                "Show my top categories and tips",
                                "Any savings opportunities here?"
                            )
                            promptSuggestions.forEach { suggestion ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BorderSlate)
                                        .clickable {
                                            aiTextQuery = suggestion
                                            viewModel.queryGemini(suggestion)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(suggestion, fontSize = 10.sp, color = OffWhite)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = aiTextQuery,
                            onValueChange = { aiTextQuery = it },
                            label = { Text("What is your query?", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.fillMaxWidth().testTag("ai_input_field"),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (aiTextQuery.isNotBlank()) {
                                            viewModel.queryGemini(aiTextQuery)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send AI query", tint = TealPrimary, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        aiResponse?.let { responseText ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BorderSlate)
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .testTag("ai_response_panel")
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💡 RECONSTRUCTED ADVISOR SOLUTION", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = TealPrimary)
                                        IconButton(onClick = { viewModel.clearAiResponse() }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SoftGray, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = responseText,
                                        fontSize = 11.sp,
                                        color = OffWhite,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }



        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🛒 RECENT LEDGER TRANSACTIONS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SoftGray,
                    letterSpacing = 1.sp
                )
                if (searchQuery.isNotEmpty() || filterType != "All" || filterStartDate != null || filterEndDate != null) {
                    Text(
                        text = "Filtered: ${searchFilteredTxs.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                    .testTag("search_filter_bar_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search text field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search category, description, payer, amount...", fontSize = 11.sp, color = SoftGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TealPrimary, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SoftGray, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("tx_search_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Type Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Expense", "Income", "Investment").forEach { item ->
                            val isSelected = filterType == item
                            val itemColor = when (item) {
                                "Income" -> MintAccent
                                "Expense" -> AlertCoral
                                "Investment" -> ElectricBlue
                                else -> TealPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) itemColor.copy(alpha = 0.15f) else SlateDark)
                                    .border(1.dp, if (isSelected) itemColor else BorderSlate, RoundedCornerShape(8.dp))
                                    .clickable { filterType = item }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) itemColor else SoftGray
                                )
                            }
                        }
                    }

                    // Date range picker row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Date picker dialog
                        val startDatePicker = remember {
                            val cal = Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    filterStartDate = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            )
                        }

                        // End Date picker dialog
                        val endDatePicker = remember {
                            val cal = Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        set(Calendar.HOUR_OF_DAY, 23)
                                        set(Calendar.MINUTE, 59)
                                        set(Calendar.SECOND, 59)
                                        set(Calendar.MILLISECOND, 999)
                                    }
                                    filterEndDate = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            )
                        }

                        // Start date button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateDark)
                                .border(1.dp, if (filterStartDate != null) TealPrimary else BorderSlate, RoundedCornerShape(8.dp))
                                .clickable { startDatePicker.show() }
                                .padding(vertical = 6.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = if (filterStartDate != null) TealPrimary else SoftGray, modifier = Modifier.size(12.dp))
                                Text(
                                    text = if (filterStartDate != null) filterSdf.format(Date(filterStartDate!!)) else "From Date",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (filterStartDate != null) OffWhite else SoftGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // End date button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateDark)
                                .border(1.dp, if (filterEndDate != null) TealPrimary else BorderSlate, RoundedCornerShape(8.dp))
                                .clickable { endDatePicker.show() }
                                .padding(vertical = 6.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = if (filterEndDate != null) TealPrimary else SoftGray, modifier = Modifier.size(12.dp))
                                Text(
                                    text = if (filterEndDate != null) filterSdf.format(Date(filterEndDate!!)) else "To Date",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (filterEndDate != null) OffWhite else SoftGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Reset button
                        val hasAnyActiveFilters = searchQuery.isNotEmpty() || filterType != "All" || filterStartDate != null || filterEndDate != null
                        if (hasAnyActiveFilters) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    filterType = "All"
                                    filterStartDate = null
                                    filterEndDate = null
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(BorderSlate)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Filters", tint = OffWhite, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        if (searchFilteredTxs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions logged matches your search and filters.", color = SoftGray, fontSize = 12.sp)
                }
            }
        } else {
            items(searchFilteredTxs) { tx ->
                LedgerItemRow(
                    tx = tx,
                    onEdit = { editingTransaction = tx },
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }
    }

    showSetBudgetForCategory?.let { category ->
        val currentLimit = budgetsMap[category] ?: 0.0
        SetBudgetDialog(
            category = category,
            currentLimit = currentLimit,
            onDismiss = { showSetBudgetForCategory = null },
            viewModel = viewModel
        )
    }

    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { editingTransaction = null },
            viewModel = viewModel
        )
    }
}

@Composable
fun QuickMetricTinyUnit(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = SoftGray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OffWhite)
    }
}

@Composable
fun MetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        modifier = modifier.border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}


// ==================== DOUBLE VERTICAL COMPACT BAR GRAPH ====================
@Composable
fun SelectedYearOverviewGraph(transactions: List<TransactionEntity>, selectedYear: Int) {
    // Collect monthly totals based on given filtered year
    val incomes = FloatArray(12) { 0f }
    val expenses = FloatArray(12) { 0f }

    transactions.forEach { tx ->
        if (getYearFromTimestamp(tx.date) == selectedYear) {
            val monthIdx = getMonthFromTimestamp(tx.date) - 1
            if (monthIdx in 0..11) {
                if (tx.type == "Income") {
                    incomes[monthIdx] += tx.amount.toFloat()
                } else if (tx.type == "Expense") {
                    expenses[monthIdx] += tx.amount.toFloat()
                }
            }
        }
    }

    val maxVal = maxOf(incomes.maxOrNull() ?: 100f, expenses.maxOrNull() ?: 100f, 100f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(SlateDark, RoundedCornerShape(8.dp))
                .padding(bottom = 8.dp)
        ) {
            val w = size.width
            val h = size.height
            val colCount = 12
            val spacer = w / (colCount + 1)
            val barWidth = 6.dp.toPx()

            for (i in 0..11) {
                val x = spacer * (i + 1)
                
                // Heights
                val incomeHeight = (incomes[i] / maxVal) * (h - 20f)
                val expenseHeight = (expenses[i] / maxVal) * (h - 20f)

                // Draw income (Left bar - MintGreen)
                drawRoundRect(
                    color = MintAccent,
                    topLeft = androidx.compose.ui.geometry.Offset(x - barWidth, h - incomeHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, incomeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )

                // Draw expense (Right bar - CoralRed)
                drawRoundRect(
                    color = AlertCoral,
                    topLeft = androidx.compose.ui.geometry.Offset(x + 1f, h - expenseHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, expenseHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }

        // Legend Months Row
        val shortMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            shortMonths.forEach { m ->
                Text(m, fontSize = 9.sp, color = SoftGray, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendIndicator("Income", MintAccent)
            LegendIndicator("Expense", AlertCoral)
        }
    }
}

@Composable
fun LegendIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = OffWhite)
    }
}


// ==================== CATEGORY PIE BREAKDOWN GRAPH ====================
@Composable
fun CategoryBreakdownCircle(transactions: List<TransactionEntity>) {
    val expenses = transactions.filter { it.type == "Expense" }
    
    if (expenses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No expense metrics logged for drawing circle chart.", color = SoftGray, fontSize = 11.sp)
        }
        return
    }

    val totalsByCategory = expenses.groupBy { it.category }.mapValues { entry ->
        entry.value.sumOf { it.amount }
    }

    val sum = totalsByCategory.values.sum()
    val slices = totalsByCategory.map { entry ->
        val pct = if (sum > 0.0) entry.value / sum else 0.0
        val sweepAngle = (pct * 360f).toFloat()
        CategorySlice(entry.key, entry.value, sweepAngle)
    }

    val colors = listOf(TealPrimary, ElectricBlue, AlertCoral, AmberWarning, MintAccent, SoftGray, Color.Yellow, Color.Magenta, Color.Cyan, Color.White)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Draw Pie Canvas Ring
        Canvas(modifier = Modifier.size(100.dp)) {
            var currAngle = 0f
            slices.forEachIndexed { idx, slice ->
                val sliceCol = colors[idx % colors.size]
                drawArc(
                    color = sliceCol,
                    startAngle = currAngle,
                    sweepAngle = slice.sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
                currAngle += slice.sweepAngle
            }
        }

        // Legend values table
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            slices.forEachIndexed { idx, s ->
                val col = colors[idx % colors.size]
                val pct = if (sum > 0.0) (s.amount / sum) * 100.0 else 0.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(col))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.category, fontSize = 10.sp, color = OffWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text("${String.format("%.1f", pct)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = col)
                }
            }
        }
    }
}

data class CategorySlice(
    val category: String,
    val amount: Double,
    val sweepAngle: Float
)


// ==================== TRANSACTION LEDGER ROW ====================
@Composable
fun LedgerItemRow(tx: TransactionEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    val typeColor = when (tx.type) {
        "Income" -> MintAccent
        "Expense" -> AlertCoral
        else -> ElectricBlue // Investment
    }
    
    val indicatorIcon = when (tx.type) {
        "Income" -> Icons.Default.Check
        "Expense" -> Icons.Default.Clear
        else -> Icons.Default.Star
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(indicatorIcon, contentDescription = tx.type, tint = typeColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (tx.notes.isNotBlank()) tx.notes else tx.category,
                        color = OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tx.payer, color = SoftGray, fontSize = 11.sp)
                        Text(" • ", color = BorderSlate, fontSize = 11.sp)
                        Text(tx.paymentMethod, color = SoftGray, fontSize = 11.sp)
                        Text(" • ", color = BorderSlate, fontSize = 11.sp)
                        Text(tx.category, color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (tx.type == "Income") "+" else "-"} ${formatRupee(tx.amount)}",
                    color = typeColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp).testTag("edit_tx_btn_${tx.id}")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = TealPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).testTag("delete_tx_btn_${tx.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = alertCoralOrFallback(tx.type), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun alertCoralOrFallback(type: String): Color {
    return AlertCoral
}


// ==================== DAILY ENTRY ADD SCREEN ====================
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel,
    defaultYear: Int,
    defaultMonth: Int
) {
    var amountStr by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Expense") } // Income, Expense, Investment
    var selectedCategory by remember { mutableStateOf("Food") }
    var payerInput by remember { mutableStateOf("Dad") }
    var descriptionInput by remember { mutableStateOf("") }
    var paymentMethodInput by remember { mutableStateOf("UPI") }
    var personNameInput by remember { mutableStateOf("") }

    var showAddCategoryField by remember { mutableStateOf(false) }
    var customCategoryName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var selectedYearState by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonthState by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDayState by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHourState by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinuteState by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    val formattedDateTime = remember(selectedYearState, selectedMonthState, selectedDayState, selectedHourState, selectedMinuteState) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYearState)
            set(Calendar.MONTH, selectedMonthState)
            set(Calendar.DAY_OF_MONTH, selectedDayState)
            set(Calendar.HOUR_OF_DAY, selectedHourState)
            set(Calendar.MINUTE, selectedMinuteState)
            set(Calendar.SECOND, 0)
        }
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        sdf.format(cal.time)
    }

    val selectedTimestamp = remember(selectedYearState, selectedMonthState, selectedDayState, selectedHourState, selectedMinuteState) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYearState)
            set(Calendar.MONTH, selectedMonthState)
            set(Calendar.DAY_OF_MONTH, selectedDayState)
            set(Calendar.HOUR_OF_DAY, selectedHourState)
            set(Calendar.MINUTE, selectedMinuteState)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedYearState = year
                selectedMonthState = month
                selectedDayState = dayOfMonth
            },
            selectedYearState,
            selectedMonthState,
            selectedDayState
        )
    }

    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHourState = hourOfDay
                selectedMinuteState = minute
            },
            selectedHourState,
            selectedMinuteState,
            false // is24HourView
        )
    }

    var activeStep by remember { mutableStateOf(1) } // 1: Amount/Type, 2: Category, 3: Date/Time, 4: Details

    val dbCategories by viewModel.categories.collectAsStateWithLifecycle(emptyList())
    val categoriesList = remember(dbCategories, transactionType) {
        val filtered = dbCategories.filter { it.type == transactionType }.map { it.name }
        if (filtered.isNotEmpty()) {
            filtered
        } else {
            when (transactionType) {
                "Income" -> listOf("Salary", "Business", "Bonus", "Money Received", "Other")
                "Expense" -> listOf("Food", "Grocery", "Shopping", "Bills", "Medical", "Fuel", "Education", "Credit Card Expense", "Money Given", "Other")
                else -> listOf("Stocks", "Mutual Fund", "Gold", "Emergency Fund", "Property", "Other")
            }
        }
    }

    // Auto update selected category if it is invalid for new type
    LaunchedEffect(transactionType, categoriesList) {
        if (selectedCategory !in categoriesList) {
            selectedCategory = categoriesList.firstOrNull() ?: "Other"
        }
    }

    val payersList = listOf("Dad", "Mom", "Sarah", "Kumar", "Amit", "Others")
    val paymentModes = listOf("Cash", "Bank", "UPI", "GPay", "PhonePe", "Credit Card", "Debit Card")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Minimal Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(TealPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon", tint = TealPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("NEW TRANSACTION", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text("Log wealth transfers & ledger events in simple guided steps.", fontSize = 10.sp, color = SoftGray)
            }
        }

        // ==================== STEP 1: QUANTITY AND TYPE ====================
        val step1Completed = amountStr.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (activeStep == 1) SlateCard else SlateCard.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        width = if (activeStep == 1) 1.2.dp else 0.5.dp,
                        color = if (activeStep == 1) TealPrimary else BorderSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { activeStep = 1 }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (step1Completed) MintAccent.copy(alpha = 0.2f) else TealPrimary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (step1Completed && activeStep != 1) {
                                Icon(Icons.Default.Check, contentDescription = "Completed", tint = MintAccent, modifier = Modifier.size(14.dp))
                            } else {
                                Text("1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeStep == 1) TealPrimary else SoftGray)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Amount & Type", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            if (activeStep != 1 && step1Completed) {
                                Text(
                                    "${transactionType} • ₹${amountStr}",
                                    fontSize = 11.sp,
                                    color = if (transactionType == "Income") MintAccent else if (transactionType == "Expense") AlertCoral else ElectricBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else if (activeStep != 1) {
                                Text("Not entered yet", fontSize = 11.sp, color = SoftGray)
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Expand Status",
                        tint = (if (activeStep == 1) TealPrimary else SoftGray).copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = activeStep == 1) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Divider(color = BorderSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Segmented Type Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val types = listOf("Income", "Expense", "Investment")
                            types.forEach { type ->
                                val isSelected = transactionType == type
                                val activeBg = when (type) {
                                    "Income" -> MintAccent
                                    "Expense" -> AlertCoral
                                    else -> ElectricBlue
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) activeBg.copy(alpha = 0.9f) else SlateDark)
                                        .clickable { transactionType = type }
                                        .padding(vertical = 9.dp)
                                        .testTag("type_add_$type"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        type,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else SoftGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Amount Text Field
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            placeholder = { Text("Value amount (₹)...", color = SoftGray, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite,
                                focusedContainerColor = SlateDark,
                                unfocusedContainerColor = SlateDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("amount_add_input"),
                            shape = RoundedCornerShape(8.dp),
                            prefix = { Text("₹ ", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )

                        // Prompt Navigation Button
                        Button(
                            onClick = { activeStep = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Next Step", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==================== STEP 2: CATEGORY SELECTION ====================
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (activeStep == 2) SlateCard else SlateCard.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        width = if (activeStep == 2) 1.2.dp else 0.5.dp,
                        color = if (activeStep == 2) TealPrimary else BorderSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { activeStep = 2 }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(TealPrimary.copy(alpha = if (activeStep == 2) 0.2f else 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeStep == 2) TealPrimary else SoftGray)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Category Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            if (activeStep != 2) {
                                Text("Selected: $selectedCategory", fontSize = 11.sp, color = SoftGray)
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Expand Status",
                        tint = (if (activeStep == 2) TealPrimary else SoftGray).copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = activeStep == 2) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Divider(color = BorderSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Choose Category", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            TextButton(
                                onClick = { showAddCategoryField = !showAddCategoryField },
                                colors = ButtonDefaults.textButtonColors(contentColor = TealPrimary),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = "Add New", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(if (showAddCategoryField) "Collapse" else "+ Add Category", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AnimatedVisibility(visible = showAddCategoryField) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateDark),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, BorderSlate)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customCategoryName,
                                        onValueChange = { customCategoryName = it },
                                        placeholder = { Text("Category name...", fontSize = 11.sp, color = SoftGray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = TealPrimary,
                                            unfocusedBorderColor = BorderSlate,
                                            focusedTextColor = OffWhite,
                                            unfocusedTextColor = OffWhite,
                                            focusedContainerColor = SlateCard,
                                            unfocusedContainerColor = SlateCard
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (customCategoryName.isNotBlank()) {
                                                viewModel.addCategory(customCategoryName.trim(), transactionType)
                                                selectedCategory = customCategoryName.trim()
                                                customCategoryName = ""
                                                showAddCategoryField = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Save", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Category chips slider
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoriesList.forEach { category ->
                                val isSel = selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) TealPrimary else SlateDark)
                                        .border(0.5.dp, if (isSel) TealPrimary else BorderSlate, RoundedCornerShape(6.dp))
                                        .clickable { selectedCategory = category }
                                        .padding(horizontal = 11.dp, vertical = 6.dp)
                                        .testTag("category_chip_$category")
                                ) {
                                    Text(category, fontSize = 10.sp, color = if (isSel) Color.White else OffWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { activeStep = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Next Step", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==================== STEP 3: TRANSACTION DATE & TIME ====================
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (activeStep == 3) SlateCard else SlateCard.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        width = if (activeStep == 3) 1.2.dp else 0.5.dp,
                        color = if (activeStep == 3) TealPrimary else BorderSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { activeStep = 3 }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(TealPrimary.copy(alpha = if (activeStep == 3) 0.2f else 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeStep == 3) TealPrimary else SoftGray)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Transaction Date & Time", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            if (activeStep != 3) {
                                Text("Logging: $formattedDateTime", fontSize = 11.sp, color = SoftGray)
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Expand Status",
                        tint = (if (activeStep == 3) TealPrimary else SoftGray).copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = activeStep == 3) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Divider(color = BorderSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                        Text("Separated Date & Time Parameters (4 Distinct Entries)", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Entry 1: Day (1-31)
                            OutlinedTextField(
                                value = selectedDayState.toString(),
                                onValueChange = { newValue ->
                                    val dayTemp = newValue.toIntOrNull()
                                    if (dayTemp != null) {
                                        selectedDayState = dayTemp.coerceIn(1, 31)
                                    } else if (newValue.isEmpty()) {
                                        selectedDayState = 1
                                    }
                                },
                                label = { Text("Day (1-31)", fontSize = 9.sp) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = OffWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = BorderSlate
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("entry_day_field")
                            )

                            // Entry 2: Month (1-12)
                            OutlinedTextField(
                                value = (selectedMonthState + 1).toString(),
                                onValueChange = { newValue ->
                                    val monthTemp = newValue.toIntOrNull()
                                    if (monthTemp != null) {
                                        selectedMonthState = (monthTemp - 1).coerceIn(0, 11)
                                    } else if (newValue.isEmpty()) {
                                        selectedMonthState = 0
                                    }
                                },
                                label = { Text("Month (1-12)", fontSize = 9.sp) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = OffWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = BorderSlate
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("entry_month_field")
                            )

                            // Entry 3: Year (YYYY)
                            OutlinedTextField(
                                value = selectedYearState.toString(),
                                onValueChange = { newValue ->
                                    val yearTemp = newValue.toIntOrNull()
                                    if (yearTemp != null) {
                                        selectedYearState = yearTemp
                                    }
                                },
                                label = { Text("Year (YYYY)", fontSize = 9.sp) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = OffWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = BorderSlate
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f).testTag("entry_year_field")
                            )

                            // Entry 4: Time (HH:MM)
                            OutlinedTextField(
                                value = run {
                                    val ampm = if (selectedHourState >= 12) "PM" else "AM"
                                    val displayHour = when {
                                        selectedHourState == 0 -> 12
                                        selectedHourState > 12 -> selectedHourState - 12
                                        else -> selectedHourState
                                    }
                                    String.format(Locale.US, "%02d:%02d %s", displayHour, selectedMinuteState, ampm)
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Click Time", fontSize = 9.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(12.dp).clickable { timePickerDialog.show() }
                                    )
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = OffWhite, fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = BorderSlate
                                ),
                                modifier = Modifier
                                    .weight(1.6f)
                                    .clickable { timePickerDialog.show() }
                                    .testTag("entry_time_field")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick selection dialogue options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { datePickerDialog.show() },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderSlate),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(12.dp))
                                    Text("Calendar Dialog", fontSize = 9.sp, color = OffWhite)
                                }
                            }

                            Button(
                                onClick = { timePickerDialog.show() },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderSlate),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(12.dp))
                                    Text("Time Clock Dialog", fontSize = 9.sp, color = OffWhite)
                                }
                            }
                        }

                        Text(
                            text = "📅 Logging at: $formattedDateTime",
                            fontSize = 9.sp,
                            color = MintAccent,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { activeStep = 4 },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Next Step", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==================== STEP 4: OPTIONAL DETAILS & SOCIAL ====================
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (activeStep == 4) SlateCard else SlateCard.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        width = if (activeStep == 4) 1.2.dp else 0.5.dp,
                        color = if (activeStep == 4) TealPrimary else BorderSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { activeStep = 4 }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(TealPrimary.copy(alpha = if (activeStep == 4) 0.2f else 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeStep == 4) TealPrimary else SoftGray)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Other Attributes & Description", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            if (activeStep != 4) {
                                val extras = listOfNotNull(
                                    if (personNameInput.isNotBlank()) "With: $personNameInput" else null,
                                    "By: $payerInput"
                                )
                                Text(extras.joinToString(" • "), fontSize = 11.sp, color = SoftGray)
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Expand Status",
                        tint = (if (activeStep == 4) TealPrimary else SoftGray).copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = activeStep == 4) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Divider(color = BorderSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Relative Person name
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Involved Person Name (Lent / Borrowed / Given)", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = personNameInput,
                                onValueChange = { personNameInput = it },
                                placeholder = { Text("e.g. Bala, Aunt Sarah, Kumar...", fontSize = 12.sp, color = SoftGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = BorderSlate,
                                    focusedTextColor = OffWhite,
                                    unfocusedTextColor = OffWhite,
                                    focusedContainerColor = SlateDark,
                                    unfocusedContainerColor = SlateDark
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("add_person_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Person", tint = ElectricBlue, modifier = Modifier.size(14.dp))
                                }
                            )
                            if (personNameInput.isNotBlank()) {
                                Text(
                                    "💡 Auto sync: This will seed ₹${amountStr.ifBlank { "0.0" }} to $personNameInput under People tracker.",
                                    fontSize = 9.sp,
                                    color = MintAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Saved Names / Favorites clickable chips
                            val dbPeopleList by viewModel.people.collectAsStateWithLifecycle(emptyList())
                            val savedAndFavoriteNames = remember(dbPeopleList) {
                                val defaultFavorites = listOf("Bala", "Dad", "Mom", "Sarah", "Kumar", "Amit")
                                val dbNames = dbPeopleList.map { it.name }
                                (dbNames + defaultFavorites).filter { it.isNotBlank() }.distinct()
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("💡 Frequently Involved / Saved Names (Tap to select):", fontSize = 9.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    savedAndFavoriteNames.forEach { name ->
                                        val isSel = personNameInput.equals(name, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) TealPrimary else SlateDark)
                                                .border(0.5.dp, if (isSel) TealPrimary else BorderSlate, RoundedCornerShape(6.dp))
                                                .clickable { personNameInput = name }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("suggested_person_chip_$name")
                                        ) {
                                            Text(name, fontSize = 9.sp, color = if (isSel) Color.White else OffWhite)
                                        }
                                    }
                                }
                            }
                        }

                        // Payment Type
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Payment Mode Type", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                paymentModes.forEach { mode ->
                                    val isSel = paymentMethodInput == mode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) TealPrimary else SlateDark)
                                            .border(0.5.dp, if (isSel) TealPrimary else BorderSlate, RoundedCornerShape(6.dp))
                                            .clickable { paymentMethodInput = mode }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("mode_chip_$mode")
                                    ) {
                                        Text(mode, fontSize = 10.sp, color = if (isSel) Color.White else OffWhite)
                                    }
                                }
                            }
                        }

                        // Note text field
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Activity Note / Description", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = descriptionInput,
                                onValueChange = { descriptionInput = it },
                                placeholder = { Text("Activity description details / comments...", fontSize = 12.sp, color = SoftGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = BorderSlate,
                                    focusedTextColor = OffWhite,
                                    unfocusedTextColor = OffWhite,
                                    focusedContainerColor = SlateDark,
                                    unfocusedContainerColor = SlateDark
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("add_desc_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Large, persistent execution action button (Very rich, modern, minimal height layout)
        val readyToSave = step1Completed
        Button(
            onClick = {
                val amt = amountStr.toDoubleOrNull() ?: 0.0
                if (amt > 0) {
                    viewModel.addTransaction(
                        amount = amt,
                        category = selectedCategory,
                        payer = payerInput,
                        notes = descriptionInput,
                        type = transactionType,
                        paymentMethod = paymentMethodInput,
                        personName = personNameInput,
                        customDate = selectedTimestamp
                    )
                    // Reset field variables & jump to step 1
                    amountStr = ""
                    descriptionInput = ""
                    personNameInput = ""
                    activeStep = 1
                    
                    // Reset calendar back to current moment
                    val nowCal = Calendar.getInstance()
                    selectedYearState = nowCal.get(Calendar.YEAR)
                    selectedMonthState = nowCal.get(Calendar.MONTH)
                    selectedDayState = nowCal.get(Calendar.DAY_OF_MONTH)
                    selectedHourState = nowCal.get(Calendar.HOUR_OF_DAY)
                    selectedMinuteState = nowCal.get(Calendar.MINUTE)
                }
            },
            enabled = readyToSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = TealPrimary,
                disabledContainerColor = TealPrimary.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .testTag("save_add_button")
        ) {
            Text(
                if (readyToSave) "SECURE SAVE TRANSACTION" else "ENTER INVOICE AMOUNT FIRST",
                fontWeight = FontWeight.Bold,
                color = if (readyToSave) Color.White else SoftGray,
                fontSize = 12.sp
            )
        }
    }
}


fun exportTransactionsToPdf(
    context: android.content.Context,
    transactions: List<TransactionEntity>,
    startDate: Long,
    endDate: Long,
    onSuccess: (java.io.File) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val filteredTxs = transactions.filter { it.date in startDate..endDate }.sortedBy { it.date }
        if (filteredTxs.isEmpty()) {
            onError("No transactions found in this date range.")
            return
        }

        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        
        val paint = android.graphics.Paint()
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(15, 118, 110)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 9f
            isAntiAlias = true
        }
        
        val borderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1f
        }

        val fillPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(240, 244, 244)
            style = android.graphics.Paint.Style.FILL
        }

        var y = 50f
        
        // Title
        canvas.drawText("FINENEST FINANCIAL LEDGER REPORT", 40f, y, titlePaint)
        y += 20f
        
        val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale.US)
        canvas.drawText("Statement Period: ${sdfDate.format(Date(startDate))} to ${sdfDate.format(Date(endDate))}", 40f, y, subtitlePaint)
        y += 12f
        canvas.drawText("Generated On: ${sdfDate.format(Date())}", 40f, y, subtitlePaint)
        y += 25f
        
        // Summary Card background
        canvas.drawRect(40f, y, (pageWidth - 40).toFloat(), y + 55f, fillPaint)
        canvas.drawRect(40f, y, (pageWidth - 40).toFloat(), y + 55f, borderPaint)
        
        val totalIncome = filteredTxs.filter { it.type == "Income" }.sumOf { it.amount }
        val totalExpense = filteredTxs.filter { it.type == "Expense" }.sumOf { it.amount }
        val totalInvestment = filteredTxs.filter { it.type == "Investment" }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense - totalInvestment
        
        canvas.drawText("Summary Overview:", 50f, y + 16f, headerPaint)
        canvas.drawText("Total Income: ${rupeeFormat.format(totalIncome)}", 50f, y + 32f, textPaint)
        canvas.drawText("Total Expenses: ${rupeeFormat.format(totalExpense)}", 50f, y + 46f, textPaint)
        canvas.drawText("Total Investments: ${rupeeFormat.format(totalInvestment)}", 240f, y + 32f, textPaint)
        
        val netSavingsPaint = android.graphics.Paint(textPaint).apply {
            color = if (netSavings >= 0) android.graphics.Color.rgb(16, 124, 65) else android.graphics.Color.RED
            isFakeBoldText = true
        }
        canvas.drawText("Net Balance: ${rupeeFormat.format(netSavings)}", 240f, y + 46f, netSavingsPaint)
        
        y += 85f
        
        // Table Headers
        canvas.drawText("Date", 45f, y, headerPaint)
        canvas.drawText("Category", 130f, y, headerPaint)
        canvas.drawText("Payer / Notes", 240f, y, headerPaint)
        canvas.drawText("Type", 420f, y, headerPaint)
        canvas.drawText("Amount", 500f, y, headerPaint)
        
        y += 8f
        canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, borderPaint)
        y += 16f
        
        val amountPaintRight = android.graphics.Paint().apply {
            textSize = 9f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        
        for (tx in filteredTxs) {
            if (y > pageHeight - 50) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
                
                // Draw headers on new page
                canvas.drawText("Date", 45f, y, headerPaint)
                canvas.drawText("Category", 130f, y, headerPaint)
                canvas.drawText("Payer / Notes", 240f, y, headerPaint)
                canvas.drawText("Type", 420f, y, headerPaint)
                canvas.drawText("Amount", 500f, y, headerPaint)
                y += 8f
                canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, borderPaint)
                y += 16f
            }
            
            val dateStr = sdfDate.format(Date(tx.date))
            canvas.drawText(dateStr, 45f, y, textPaint)
            
            val catText = if (tx.category.length > 15) tx.category.take(13) + ".." else tx.category
            canvas.drawText(catText, 130f, y, textPaint)
            
            val details = if (tx.notes.isNotBlank()) "${tx.payer} (${tx.notes})" else tx.payer
            val detailsText = if (details.length > 32) details.take(30) + ".." else details
            canvas.drawText(detailsText, 240f, y, textPaint)
            
            canvas.drawText(tx.type, 420f, y, textPaint)
            
            val sign = if (tx.type == "Income") "+" else "-"
            val amtColor = when (tx.type) {
                "Income" -> android.graphics.Color.rgb(16, 124, 65)
                "Expense" -> android.graphics.Color.rgb(219, 68, 85)
                else -> android.graphics.Color.rgb(66, 133, 244)
            }
            val txAmtPaint = android.graphics.Paint(amountPaintRight).apply {
                color = amtColor
                isFakeBoldText = true
            }
            canvas.drawText("$sign ${rupeeFormat.format(tx.amount)}", (pageWidth - 45).toFloat(), y, txAmtPaint)
            
            y += 20f
        }
        
        pdfDocument.finishPage(page)
        
        val outputDir = context.cacheDir
        val file = java.io.File(outputDir, "NAME_FN_Financial_Report_${System.currentTimeMillis()}.pdf")
        val fileOutputStream = java.io.FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()
        
        onSuccess(file)
    } catch (e: Exception) {
        e.printStackTrace()
        onError(e.localizedMessage ?: "Unknown PDF creation error")
    }
}

fun sharePdfFile(context: android.content.Context, file: java.io.File) {
    try {
        val authority = "${context.packageName}.fileprovider"
        val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "FineNest Financial Ledger Statement")
            putExtra(android.content.Intent.EXTRA_TEXT, "Attached is the requested FineNest financial statement PDF.")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Statement PDF"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error sharing file: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
    }
}


// ==================== REPORTS MODULE TAB ====================
@Composable
fun ReportsScreen(
    viewModel: FinanceViewModel,
    yearFilter: Int,
    monthFilter: Int
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedDailyTransactionId by remember { mutableStateOf<Int?>(null) }

    val monthNames = remember {
        listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }

    // Selected month for daily logs detail (defaults to current monthFilter)
    var selectedMonthForDailyDetails by remember { mutableStateOf(monthFilter) }

    // Synchronize chosen month details with top monthFilter whenever it is modified externally
    LaunchedEffect(monthFilter) {
        selectedMonthForDailyDetails = monthFilter
    }

    // 12 month tracker calculations for chosen year
    val monthTotals = remember(transactions, yearFilter) {
        val list = mutableListOf<MonthSummaryRow>()
        
        for (m in 1..12) {
            val txs = transactions.filter {
                getYearFromTimestamp(it.date) == yearFilter && getMonthFromTimestamp(it.date) == m
            }
            val inc = txs.filter { it.type == "Income" }.sumOf { it.amount }
            val exp = txs.filter { it.type == "Expense" }.sumOf { it.amount }
            val inv = txs.filter { it.type == "Investment" }.sumOf { it.amount }
            val sav = inc - exp - inv
            list.add(MonthSummaryRow(m, monthNames[m - 1], inc, exp, inv, sav))
        }
        list
    }

    // Category spends calculation for selected year/month specifically
    val categorySpends = remember(transactions, yearFilter, monthFilter) {
        val monthlyTxs = transactions.filter {
            getYearFromTimestamp(it.date) == yearFilter && getMonthFromTimestamp(it.date) == monthFilter
        }
        val yearlyTxs = transactions.filter {
            getYearFromTimestamp(it.date) == yearFilter
        }

        val monthlyExp = monthlyTxs.filter { it.type == "Expense" }
        val yearlyExp = yearlyTxs.filter { it.type == "Expense" }

        val totalMonthExpense = monthlyExp.sumOf { it.amount }
        val categorySpendsMap = monthlyExp.groupBy { it.category }.map { entry ->
            val mVal = entry.value.sumOf { it.amount }
            val yVal = yearlyExp.filter { it.category == entry.key }.sumOf { it.amount }
            val pctShare = if (totalMonthExpense > 0.0) (mVal / totalMonthExpense) * 100.0 else 0.0
            CategoryReportRow(entry.key, mVal, yVal, pctShare)
        }
        categorySpendsMap
    }

    var selectedDayFilter by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedMonthForDailyDetails, yearFilter) {
        selectedDayFilter = null
    }

    val availableDaysWithTransactions = remember(transactions, yearFilter, selectedMonthForDailyDetails) {
        transactions.filter {
            getYearFromTimestamp(it.date) == yearFilter && getMonthFromTimestamp(it.date) == selectedMonthForDailyDetails
        }.map { getDayFromTimestamp(it.date) }.distinct().sorted()
    }

    // Compute Daily timeline of items for selected month & year
    val dailyLedgerGroup = remember(transactions, yearFilter, selectedMonthForDailyDetails, selectedDayFilter) {
        val filtered = transactions.filter {
            getYearFromTimestamp(it.date) == yearFilter && 
            getMonthFromTimestamp(it.date) == selectedMonthForDailyDetails &&
            (selectedDayFilter == null || getDayFromTimestamp(it.date) == selectedDayFilter)
        }
        filtered.groupBy { getDayFromTimestamp(it.date) }
            .map { entry ->
                val day = entry.key
                val list = entry.value.sortedByDescending { it.date }
                val dIncome = list.filter { it.type == "Income" }.sumOf { it.amount }
                val dExpense = list.filter { it.type == "Expense" }.sumOf { it.amount }
                val dInvestment = list.filter { it.type == "Investment" }.sumOf { it.amount }
                DaySummary(
                    day = day,
                    income = dIncome,
                    expense = dExpense,
                    investment = dInvestment,
                    transactions = list
                )
            }.sortedByDescending { it.day }
    }

    var reportTabSelector by remember { mutableStateOf(0) } // 0: Daily Ledger, 1: Category Wise Spending, 2: Month-to-Month comparison
    var showPdfExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📊 MONTHLY & DAILY REPORT CHRONOLOGY", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)
            
            // Minimal PDF Icon Action
            IconButton(
                onClick = { showPdfExportDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export PDF Statement",
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // PDF Export Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                .clickable { showPdfExportDialog = true }
                .testTag("pdf_export_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📄", fontSize = 18.sp)
                    Column {
                        Text("Export Financial Ledger to PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OffWhite)
                        Text("Select custom from & to date ranges", fontSize = 10.sp, color = SoftGray)
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Export PDF Arrow",
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showPdfExportDialog) {
            var filterStartDate by remember {
                mutableStateOf(
                    Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_YEAR, 1) // Start of year
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                )
            }
            var filterEndDate by remember {
                mutableStateOf(System.currentTimeMillis())
            }
            
            val context = LocalContext.current
            val sdfStr = SimpleDateFormat("dd MMM yyyy", Locale.US)
            
            val startDatePicker = remember {
                val cal = Calendar.getInstance().apply { timeInMillis = filterStartDate }
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        filterStartDate = newCal.timeInMillis
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            }
            
            val endDatePicker = remember {
                val cal = Calendar.getInstance().apply { timeInMillis = filterEndDate }
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        filterEndDate = newCal.timeInMillis
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            }
            
            Dialog(onDismissRequest = { showPdfExportDialog = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSlate, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📤 EXPORT TRANSACTION REPORT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealPrimary,
                            letterSpacing = 0.5.sp
                        )
                        
                        Text(
                            text = "Filter by date range to customize your generated statement PDF.",
                            fontSize = 11.sp,
                            color = SoftGray,
                            lineHeight = 15.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("FROM DATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SoftGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateDark)
                                        .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                                        .clickable { startDatePicker.show() }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(sdfStr.format(Date(filterStartDate)), fontSize = 11.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TO DATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SoftGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateDark)
                                        .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                                        .clickable { endDatePicker.show() }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(sdfStr.format(Date(filterEndDate)), fontSize = 11.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showPdfExportDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftGray),
                                border = BorderStroke(0.5.dp, BorderSlate)
                            ) {
                                Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    exportTransactionsToPdf(
                                        context = context,
                                        transactions = transactions,
                                        startDate = filterStartDate,
                                        endDate = filterEndDate,
                                        onSuccess = { pdfFile ->
                                            showPdfExportDialog = false
                                            viewModel.showNotification("🟢 Success: Generated PDF Statement!")
                                            sharePdfFile(context, pdfFile)
                                        },
                                        onError = { error ->
                                            viewModel.showNotification("❌ Error: $error")
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Text("Generate PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Sub screen selector Tabs with weight(1f) to elegantly space the 3 tabs
        Row(
            modifier = Modifier.fillMaxWidth().background(BorderSlate, RoundedCornerShape(10.dp)).padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { reportTabSelector = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = if (reportTabSelector == 0) SlateCard else Color.Transparent, contentColor = OffWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("select_daily_ledger")
            ) {
                Text("Daily Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { reportTabSelector = 1 },
                colors = ButtonDefaults.buttonColors(containerColor = if (reportTabSelector == 1) SlateCard else Color.Transparent, contentColor = OffWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.1f).testTag("select_category_spend")
            ) {
                Text("Category Spending", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { reportTabSelector = 2 },
                colors = ButtonDefaults.buttonColors(containerColor = if (reportTabSelector == 2) SlateCard else Color.Transparent, contentColor = OffWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("select_month_comparison")
            ) {
                Text("Jan-Dec Compare", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        when (reportTabSelector) {
            2 -> {
                // Compare Monthly ledger rows for selected Year
                Text("Financial Year History: $yearFilter Table (Tap Card to Inspect Daily Logs)", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.SemiBold)
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(monthTotals) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedMonthForDailyDetails = item.monthIdx
                                    reportTabSelector = 0
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(item.monthName, fontWeight = FontWeight.ExtraBold, color = TealPrimary, fontSize = 13.sp)
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Inspect Daily Logs",
                                            tint = TealPrimary.copy(alpha = 0.7f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    val savingsRate = if (item.income > 0.0) ((item.income - item.expense) / item.income) * 100.0 else 0.0
                                    Text("Savings Rate: ${String.format("%.1f", savingsRate)}%", color = if (savingsRate >= 20.0) MintAccent else AmberWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Income", fontSize = 10.sp, color = SoftGray)
                                        Text(formatRupee(item.income), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                                    }
                                    Column {
                                        Text("Expense", fontSize = 10.sp, color = SoftGray)
                                        Text(formatRupee(item.expense), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertCoral)
                                    }
                                    Column {
                                        Text("Invested", fontSize = 10.sp, color = SoftGray)
                                        Text(formatRupee(item.invested), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                                    }
                                    Column {
                                        Text("Saved Reserve", fontSize = 10.sp, color = SoftGray)
                                        Text(formatRupee(item.savings), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Category Spending Analysis Tab
                Text("Category Spend Ratios: ${SimpleDateFormat("MMMM", Locale.US).format(GregorianCalendar(yearFilter, monthFilter - 1, 1).time)} $yearFilter", fontSize = 12.sp, color = SoftGray, fontWeight = FontWeight.Bold)

                if (categorySpends.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No expense transactions in this period to aggregate categories.", color = SoftGray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            CategorySpendingDonutChart(categorySpends = categorySpends)
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("LIST OF SPEND BY CATEGORY:", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                        }

                        items(categorySpends) { category ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(category.category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OffWhite)
                                        Text("Share: ${String.format("%.1f", category.monthPercentage)}%", fontWeight = FontWeight.Bold, color = TealPrimary, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Beautiful category spending progress bar
                                    LinearProgressIndicator(
                                        progress = { (category.monthPercentage.toFloat() / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                        color = if (category.monthPercentage > 40.0) AlertCoral else TealPrimary,
                                        trackColor = BorderSlate,
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("This Month: ${formatRupee(category.monthlyTotal)}", fontSize = 10.sp, color = SoftGray)
                                        Text("This Year: ${formatRupee(category.yearlyTotal)}", fontSize = 10.sp, color = SoftGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            0 -> {
                // Daily Ledger Tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Timeline: ${monthNames[selectedMonthForDailyDetails - 1]} $yearFilter",
                        fontSize = 12.sp,
                        color = SoftGray,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Allow quick selection rotation for previous and next month
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Previous Month button option
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateCard)
                                .clickable {
                                    val currentIdx = selectedMonthForDailyDetails
                                    selectedMonthForDailyDetails = if (currentIdx <= 1) 12 else currentIdx - 1
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("previous_month_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month", tint = TealPrimary, modifier = Modifier.size(14.dp))
                            Text(text = "Prev", fontSize = 9.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }

                        // Next Month button option
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateCard)
                                .clickable {
                                    val currentIdx = selectedMonthForDailyDetails
                                    selectedMonthForDailyDetails = if (currentIdx >= 12) 1 else currentIdx + 1
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("next_month_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = "Next", fontSize = 9.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next Month", tint = TealPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                if (availableDaysWithTransactions.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📅 FILTER BY DATE:", 
                            fontSize = 9.sp, 
                            color = SoftGray, 
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "All Days" selector chip
                            val isAllSelected = selectedDayFilter == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAllSelected) TealPrimary else SlateCard)
                                    .border(0.5.dp, if (isAllSelected) TealPrimary else BorderSlate, RoundedCornerShape(8.dp))
                                    .clickable { selectedDayFilter = null }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("day_filter_all")
                            ) {
                                Text(
                                    text = "All Days", 
                                    fontSize = 10.sp, 
                                    color = if (isAllSelected) Color.White else OffWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Individual day chips representing days with transactions
                            availableDaysWithTransactions.forEach { dayInt ->
                                val isSelected = selectedDayFilter == dayInt
                                val suffix = when {
                                    dayInt in 11..13 -> "th"
                                    dayInt % 10 == 1 -> "st"
                                    dayInt % 10 == 2 -> "nd"
                                    dayInt % 10 == 3 -> "rd"
                                    else -> "th"
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) TealPrimary else SlateCard)
                                        .border(0.5.dp, if (isSelected) TealPrimary else BorderSlate, RoundedCornerShape(8.dp))
                                        .clickable { selectedDayFilter = if (isSelected) null else dayInt }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("day_filter_$dayInt")
                                ) {
                                    Text(
                                        text = "${dayInt}${suffix}", 
                                        fontSize = 10.sp, 
                                        color = if (isSelected) Color.White else OffWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (dailyLedgerGroup.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, BorderSlate.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Empty",
                                    tint = SoftGray,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "No Daily Transactions Found",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OffWhite
                                )
                                Text(
                                    text = "No entries were logged for ${monthNames[selectedMonthForDailyDetails - 1]} $yearFilter yet. Click the '+' Add tab to record complete invoices with exact times!",
                                    fontSize = 10.sp,
                                    color = SoftGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dailyLedgerGroup) { dayItem ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Day Header
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(TealPrimary.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${dayItem.day}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                color = TealPrimary
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "${dayItem.day} ${monthNames[selectedMonthForDailyDetails - 1]} $yearFilter",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = OffWhite
                                            )
                                            val count = dayItem.transactions.size
                                            Text(
                                                text = "$count ${if (count == 1) "entry" else "entries"}",
                                                fontSize = 9.sp,
                                                color = SoftGray
                                            )
                                        }
                                    }
                                    
                                    // Total breakdown on this specific day
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (dayItem.income > 0) {
                                            Text("In: ${formatRupee(dayItem.income)}", color = MintAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (dayItem.expense > 0) {
                                            Text("Out: ${formatRupee(dayItem.expense)}", color = AlertCoral, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (dayItem.investment > 0) {
                                            Text("Inv: ${formatRupee(dayItem.investment)}", color = ElectricBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Transactions list inside the Day
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, BorderSlate.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        dayItem.transactions.forEachIndexed { idx, tx ->
                                            if (idx > 0) {
                                                HorizontalDivider(color = BorderSlate.copy(alpha = 0.3f), thickness = 0.5.dp)
                                            }
                                            
                                            val isDailySelected = selectedDailyTransactionId == tx.id
                                            val animatedBgColor by androidx.compose.animation.animateColorAsState(
                                                targetValue = if (isDailySelected) TealPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                                label = "daily_item_bg"
                                            )
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(animatedBgColor)
                                                    .clickable {
                                                        selectedDailyTransactionId = if (isDailySelected) null else tx.id
                                                    }
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                // Left: Details
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val typeIndicator = when (tx.type) {
                                                        "Income" -> MintAccent
                                                        "Expense" -> AlertCoral
                                                        else -> ElectricBlue
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(typeIndicator, CircleShape)
                                                    )
                                                    
                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = tx.category,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp,
                                                                color = OffWhite
                                                            )
                                                            Text(
                                                                text = "• By ${tx.payer}",
                                                                fontSize = 9.sp,
                                                                color = SoftGray
                                                            )
                                                            Text(
                                                                text = "• Via ${tx.paymentMethod}",
                                                                fontSize = 9.sp,
                                                                color = SoftGray
                                                            )
                                                        }
                                                        
                                                        // Complete Raw Details
                                                        if (tx.notes.isNotBlank()) {
                                                            Text(
                                                                text = tx.notes,
                                                                fontSize = 10.sp,
                                                                color = SoftGray,
                                                                fontWeight = FontWeight.Normal
                                                            )
                                                        } else {
                                                            Text(
                                                                text = "(No notes provided)",
                                                                fontSize = 10.sp,
                                                                color = SoftGray.copy(alpha = 0.5f),
                                                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                // Right: Price, Time & Actions
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.End,
                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        val prefix = when (tx.type) {
                                                            "Income" -> "+"
                                                            "Expense" -> "-"
                                                            else -> "•"
                                                        }
                                                        val col = when (tx.type) {
                                                            "Income" -> MintAccent
                                                            "Expense" -> AlertCoral
                                                            else -> ElectricBlue
                                                        }
                                                        Text(
                                                            text = "$prefix ${formatRupee(tx.amount)}",
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 11.sp,
                                                            color = col
                                                        )
                                                        
                                                        val timeForm = SimpleDateFormat("hh:mm a", Locale.US)
                                                        val timeText = timeForm.format(Date(tx.date))
                                                        Text(
                                                            text = timeText,
                                                            fontSize = 8.sp,
                                                            color = SoftGray,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }

                                                    androidx.compose.animation.AnimatedVisibility(
                                                        visible = isDailySelected,
                                                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                                                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            modifier = Modifier.padding(start = 4.dp)
                                                        ) {
                                                            IconButton(
                                                                onClick = { editingTransaction = tx },
                                                                modifier = Modifier.size(24.dp).testTag("reports_edit_btn_${tx.id}")
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Edit,
                                                                    contentDescription = "Edit Item",
                                                                    tint = TealPrimary,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }

                                                            IconButton(
                                                                onClick = { viewModel.deleteTransaction(tx.id) },
                                                                modifier = Modifier.size(24.dp).testTag("reports_delete_btn_${tx.id}")
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Delete,
                                                                    contentDescription = "Delete Item",
                                                                    tint = AlertCoral,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { editingTransaction = null },
            viewModel = viewModel
        )
    }
}

data class MonthSummaryRow(
    val monthIdx: Int,
    val monthName: String,
    val income: Double,
    val expense: Double,
    val invested: Double,
    val savings: Double
)

data class DaySummary(
    val day: Int,
    val income: Double,
    val expense: Double,
    val investment: Double,
    val transactions: List<TransactionEntity>
)

data class CategoryReportRow(
    val category: String,
    val monthlyTotal: Double,
    val yearlyTotal: Double,
    val monthPercentage: Double
)


// ==================== WEALTH TAB (LEDGERS AS EXPANDABLE REGISTRIES) ====================
@Composable
fun WealthScreen(
    viewModel: FinanceViewModel
) {
    var activeSubModule by remember { mutableStateOf(0) } // 0: Emergency, 1: Goals, 2: Stocks, 3: Debt, 4: CC, 5: People, 6: Insurance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("💎 FINENEST PORTFOLIO SYSTEM", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)
        
        // Horizontal Scroll bar to select asset/debt modules
        val modulesList = listOf("🛡️ Emergency", "🎯 Goals", "📈 Stocks", "🏦 Loans/Debt", "💳 Credit Cards", "👥 People Tracker", "📜 Insurance")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modulesList.forEachIndexed { index, mName ->
                val isSel = activeSubModule == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) TealPrimary else BorderSlate)
                        .clickable { activeSubModule = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("wealth_subtab_$index")
                ) {
                    Text(mName, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = BorderSlate, thickness = 1.dp)

        // Selected Sub wealth registry view
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubModule) {
                0 -> EmergencySavingsSubView(viewModel)
                1 -> FamilyGoalsSubView(viewModel)
                2 -> StockPortfolioSubView(viewModel)
                3 -> DebtLoansSubView(viewModel)
                4 -> CreditCardsSubView(viewModel)
                5 -> PeopleMoneySubView(viewModel)
                6 -> InsurancePoliciesSubView(viewModel)
            }
        }
    }
}


// --- 0. EMERGENCY SAVINGS SUBVIEW ---
@Composable
fun EmergencySavingsSubView(viewModel: FinanceViewModel) {
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    // Emergency savings is mapped as a specific goal
    val emergencyGoal = goals.find { it.name.contains("Emergency") } ?: SavingsGoalEntity(name = "🛡️ Emergency Savings Fund", targetAmount = 100000.0, currentSaved = 35000.0, targetDate = "Dec 2026")

    val completedPct = if (emergencyGoal.targetAmount > 0) (emergencyGoal.currentSaved / emergencyGoal.targetAmount) * 100.0 else 0.0
    val remaining = emergencyGoal.targetAmount - emergencyGoal.currentSaved

    var depositAmountStr by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Concentric Circular Ring Progress Indicator
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Track grey arc
                drawArc(
                    color = BorderSlate,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )

                // Foreground active arc progress circle
                val activeAngle = ((completedPct.toFloat() / 100f) * 360f).coerceIn(0f, 360f)
                drawArc(
                    color = TealPrimary,
                    startAngle = -90f,
                    sweepAngle = activeAngle,
                    useCenter = false,
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${String.format("%.1f", completedPct)}%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TealPrimary)
                Text("Reserve Level", fontSize = 10.sp, color = SoftGray)
            }
        }

        // Details metrics cards block
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🛡️ EMERGENCY SHIELD METRICS", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = BorderSlate)
                
                DetailDataRow("Target Goal", formatRupee(emergencyGoal.targetAmount))
                DetailDataRow("Secured Reservoirs", formatRupee(emergencyGoal.currentSaved), color = MintAccent)
                DetailDataRow("Security Gap Remaining", formatRupee(if (remaining > 0.0) remaining else 0.0), color = AlertCoral)
                DetailDataRow("Term Target Date", emergencyGoal.targetDate)
            }
        }

        // Easy rapid deposit field form
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("💸 QUICK DEPOSIT CONTRIBUTION", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = depositAmountStr,
                        onValueChange = { depositAmountStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Value (₹)", color = OffWhite) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val dep = depositAmountStr.toDoubleOrNull() ?: 0.0
                            if (dep > 0) {
                                // Add transaction representing deposits
                                viewModel.addTransaction(dep, "Emergency Fund", "Dad", "Emergency Fund seed reserve", "Investment", "UPI")
                                depositAmountStr = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- 1. FAMILY SAVING GOALS ---
@Composable
fun FamilyGoalsSubView(viewModel: FinanceViewModel) {
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()

    var goalName by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalCurrent by remember { mutableStateOf("") }
    var goalDate by remember { mutableStateOf("") }
    var selectedGoalCategory by remember { mutableStateOf("🛡️ Emergency") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Interactive Savings Calculator Card
        item {
            InteractiveSavingsCalculatorCard(viewModel = viewModel)
        }

        // Enter custom Savings Goals details form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎯 ESTABLISH NEW FAMILY SAVINGS GOAL", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSlate)

                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("Savings Goal Name (e.g. House scheme)", fontSize = 11.sp, color = OffWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                    )

                    // Selectable target horizontal category badges
                    Text("SELECT GOAL CATEGORY", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        items(listOf("🛡️ Emergency", "🏠 Housing", "✈️ Travel", "🎓 Education", "🏥 Medical", "🚗 Automobile", "💼 General")) { cat ->
                            val isSelected = selectedGoalCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) TealPrimary.copy(alpha = 0.2f) else SlateDark)
                                    .border(1.dp, if (isSelected) TealPrimary else BorderSlate, RoundedCornerShape(8.dp))
                                    .clickable { selectedGoalCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 10.sp, color = if (isSelected) TealPrimary else OffWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goalTarget,
                            onValueChange = { goalTarget = it },
                            label = { Text("Target (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = goalCurrent,
                            onValueChange = { goalCurrent = it },
                            label = { Text("Initial Saved (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    OutlinedTextField(
                        value = goalDate,
                        onValueChange = { goalDate = it },
                        label = { Text("Target Date (e.g. Dec 2028)", fontSize = 11.sp, color = OffWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                    )

                    Button(
                        onClick = {
                            val targ = goalTarget.toDoubleOrNull() ?: 0.0
                            val curr = goalCurrent.toDoubleOrNull() ?: 0.0
                            if (goalName.isNotBlank() && targ > 0) {
                                val compositeName = "$selectedGoalCategory | $goalName"
                                viewModel.addSavingsGoal(compositeName, targ, curr, goalDate, false)
                                goalName = ""
                                goalTarget = ""
                                goalCurrent = ""
                                goalDate = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Goal", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List goals
        items(goals) { g ->
            val completed = if (g.targetAmount > 0.0) g.currentSaved / g.targetAmount else 0.0
            val hasCategory = g.name.contains(" | ")
            val parsedCategory = if (hasCategory) g.name.substringBefore(" | ") else "💼 General"
            val parsedName = if (hasCategory) g.name.substringAfter(" | ") else g.name

            val animatedProgress by animateFloatAsState(
                targetValue = completed.toFloat().coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
                label = "savings_progress"
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TealPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(parsedCategory, fontSize = 8.sp, fontWeight = FontWeight.Black, color = TealPrimary)
                            }
                            Text(parsedName, fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                        }
                        IconButton(onClick = { viewModel.deleteSavingsGoal(g.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftGray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Target Date: ${g.targetDate}", color = SoftGray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = ElectricBlue,
                        trackColor = if (isDarkThemeGlobal) Color(0xFF1C1C24) else BorderSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saved: ${formatRupee(g.currentSaved)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val active = viewModel.isFirebaseActive.value
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (active) MintAccent else SoftGray)
                            )
                            Text(
                                text = if (active) "Firestore Connected" else "Safe Local",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftGray
                            )
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Firestore Synchronization Status",
                                tint = if (active) MintAccent else SoftGray,
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        Text("Target: ${formatRupee(g.targetAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                    }
                }
            }
        }
    }
}


// --- 2. STOCK PORTFOLIO MODULE ---
@Composable
fun StockPortfolioSubView(viewModel: FinanceViewModel) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()

    var buyDate by remember { mutableStateOf("") }
    var stockName by remember { mutableStateOf("") }
    var stockSymbol by remember { mutableStateOf("") }
    var stockQty by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Register purchased stock holding
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📈 ADD STOCK TRANSACTION TO LEDGER", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSlate)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockName,
                            onValueChange = { stockName = it },
                            label = { Text("Stock Name", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = stockSymbol,
                            onValueChange = { stockSymbol = it },
                            label = { Text("Symbol", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockQty,
                            onValueChange = { stockQty = it },
                            label = { Text("Qty", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = buyPrice,
                            onValueChange = { buyPrice = it },
                            label = { Text("Buy (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1.2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = currentPrice,
                            onValueChange = { currentPrice = it },
                            label = { Text("Current (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1.2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    Button(
                        onClick = {
                            val qtyInt = stockQty.toIntOrNull() ?: 1
                            val bpVal = buyPrice.toDoubleOrNull() ?: 0.0
                            val cpVal = currentPrice.toDoubleOrNull() ?: 0.0
                            if (stockSymbol.isNotBlank() && bpVal > 0 && cpVal > 0) {
                                // Record stock added
                                val sdf = SimpleDateFormat("dd MMM 2026", Locale.US)
                                viewModel.addStock(sdf.format(Date()), stockName, stockSymbol, qtyInt, bpVal, cpVal)
                                stockName = ""
                                stockSymbol = ""
                                stockQty = ""
                                buyPrice = ""
                                currentPrice = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Stock Holding", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // List holdings
        items(stocks) { s ->
            val pnlColor = if (s.profitLoss >= 0.0) MintAccent else AlertCoral
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${s.stockName} (${s.symbol})", fontWeight = FontWeight.ExtraBold, color = OffWhite, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Qty ${s.quantity} • Buy ${formatRupee(s.buyPrice)}", fontSize = 10.sp, color = SoftGray)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatRupee(s.currentValue), fontWeight = FontWeight.ExtraBold, color = TealPrimary, fontSize = 13.sp)
                        Text(
                            text = "${if (s.profitLoss >= 0.0) "+" else ""}${formatRupee(s.profitLoss)} (${String.format("%.1f", s.returnPercent)}%)",
                            color = pnlColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.deleteStock(s.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}


// --- 3. LOAN & EMI DEBT TRACKER ---
@Composable
fun DebtLoansSubView(viewModel: FinanceViewModel) {
    val loans by viewModel.loans.collectAsStateWithLifecycle()

    var debtName by remember { mutableStateOf("") }
    var debtTotal by remember { mutableStateOf("") }
    var debtEmi by remember { mutableStateOf("") }
    var debtPaid by remember { mutableStateOf("") }
    var debtEnd by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Register debt form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🏦 LOG FAMILY OUTSTANDING DEBT/LOAN", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSlate)

                    OutlinedTextField(
                        value = debtName,
                        onValueChange = { debtName = it },
                        label = { Text("Debt Facility Name (e.g. SBI Education Loan)", fontSize = 11.sp, color = OffWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = debtTotal,
                            onValueChange = { debtTotal = it },
                            label = { Text("Total Amount (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = debtEmi,
                            onValueChange = { debtEmi = it },
                            label = { Text("Monthly emi (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = debtPaid,
                            onValueChange = { debtPaid = it },
                            label = { Text("Paid Amount (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1.2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = debtEnd,
                            onValueChange = { debtEnd = it },
                            label = { Text("Expiry End (e.g. Dec 2030)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    Button(
                        onClick = {
                            val tot = debtTotal.toDoubleOrNull() ?: 0.0
                            val emi = debtEmi.toDoubleOrNull() ?: 0.0
                            val paid = debtPaid.toDoubleOrNull() ?: 0.0
                            if (debtName.isNotBlank() && tot > 0) {
                                viewModel.addLoan(debtName, tot, emi, paid, debtEnd)
                                debtName = ""
                                debtTotal = ""
                                debtEmi = ""
                                debtPaid = ""
                                debtEnd = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Register Loan Debt", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // List loans
        items(loans) { l ->
            val completeRatio = if (l.totalAmount > 0.0) l.paidAmount / l.totalAmount else 0.0
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(l.name, fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                        IconButton(onClick = { viewModel.deleteLoan(l.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftGray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("End Target Date limit: ${l.endDate}", color = SoftGray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { completeRatio.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = AlertCoral,
                        trackColor = BorderSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("EMI Amount", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(l.monthlyEmi), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                        }
                        Column {
                            Text("Amortised Paid", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(l.paidAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                        }
                        Column {
                            Text("Principal Remaining", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(l.remainingAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertCoral)
                        }
                    }
                }
            }
        }
    }
}


// --- 4. CREDIT CARD BILLS TRACKER ---
@Composable
fun CreditCardsSubView(viewModel: FinanceViewModel) {
    val cards by viewModel.creditCards.collectAsStateWithLifecycle()

    var cardName by remember { mutableStateOf("") }
    var ccSpend by remember { mutableStateOf("") }
    var ccPaid by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Form to log CC Spend
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💳 REGISTER CREDIT CARD FACILITY spending", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSlate)

                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Credit Card Name (e.g. SimplyClick SBI / Regalia)", fontSize = 11.sp, color = OffWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ccSpend,
                            onValueChange = { ccSpend = it },
                            label = { Text("Total Spend (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = ccPaid,
                            onValueChange = { ccPaid = it },
                            label = { Text("Paid Amount (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    Button(
                        onClick = {
                            val spVal = ccSpend.toDoubleOrNull() ?: 0.0
                            val pdVal = ccPaid.toDoubleOrNull() ?: 0.0
                            if (cardName.isNotBlank() && spVal >= 0.0) {
                                viewModel.addCreditCard(cardName, spVal, pdVal)
                                cardName = ""
                                ccSpend = ""
                                ccPaid = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Credit Card Entry", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // List cc records
        items(cards) { card ->
            val due = card.pendingAmount
            val badgeCol = if (due > 0.0) AlertCoral else MintAccent
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(card.cardName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OffWhite)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeCol.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(card.status, color = badgeCol, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Outstandings", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(card.totalSpend), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                        }
                        Column {
                            Text("Settled Paid", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(card.paidAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                        }
                        Column {
                            Text("Pending Bill Due", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(card.pendingAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertCoral)
                        }

                        // Pay Button
                        IconButton(onClick = { viewModel.deleteCreditCard(card.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete CC", tint = SoftGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}


// --- 5. PEOPLE MONEY TRACKER ---
@Composable
fun PeopleMoneySubView(viewModel: FinanceViewModel) {
    val people by viewModel.people.collectAsStateWithLifecycle(emptyList())
    val transactions by viewModel.transactions.collectAsStateWithLifecycle(emptyList())

    var pName by remember { mutableStateOf("") }
    var mGiven by remember { mutableStateOf("") }
    var mReceived by remember { mutableStateOf("") }
    var wonTReturn by remember { mutableStateOf(false) }

    var personSearchQuery by remember { mutableStateOf("") }
    var selectedPerson by remember { mutableStateOf<String?>(null) }

    // Sync selected person based on available names
    val filteredPeople = remember(people, personSearchQuery) {
        if (personSearchQuery.isBlank()) {
            people
        } else {
            people.filter { it.name.contains(personSearchQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(filteredPeople) {
        if (selectedPerson == null || filteredPeople.none { it.name.equals(selectedPerson, ignoreCase = true) }) {
            selectedPerson = filteredPeople.firstOrNull()?.name
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Row Input Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("👥 LOG MONEY GIVEN/RECEIVED FROM PEOPLES", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSlate)

                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Person Representative Name (e.g. Bala / Kumar)", fontSize = 11.sp, color = OffWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mGiven,
                            onValueChange = { mGiven = it },
                            label = { Text("Money Given (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = mReceived,
                            onValueChange = { mReceived = it },
                            label = { Text("Money Received (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { wonTReturn = !wonTReturn }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = wonTReturn,
                            onCheckedChange = { wonTReturn = it },
                            colors = CheckboxDefaults.colors(checkedColor = AlertCoral, uncheckedColor = BorderSlate)
                        )
                        Column {
                            Text("⚠️ Friend needs financial help (Money Won't Be Returned)", fontSize = 11.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                            Text("Mark as unreturned money / charity", fontSize = 9.sp, color = SoftGray)
                        }
                    }

                    Button(
                        onClick = {
                            val givVal = mGiven.toDoubleOrNull() ?: 0.0
                            val recVal = mReceived.toDoubleOrNull() ?: 0.0
                            if (pName.isNotBlank()) {
                                val finalStatus = if (wonTReturn) "Won't Be Returned" else ""
                                viewModel.addPerson(pName, givVal, recVal, finalStatus)
                                pName = ""
                                mGiven = ""
                                mReceived = ""
                                wonTReturn = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add / Update People Ledger", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Search text filter item
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔍 FILTER & SEARCH EXPERT VIEW", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSlate)
                    
                    OutlinedTextField(
                        value = personSearchQuery,
                        onValueChange = { personSearchQuery = it },
                        placeholder = { Text("Enter person name to filter...", fontSize = 11.sp, color = SoftGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SoftGray, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (personSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { personSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SoftGray, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (filteredPeople.isNotEmpty()) {
                        Text("Direct Saved Name Quick Access:", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredPeople.forEach { person ->
                                val isSel = selectedPerson?.equals(person.name, ignoreCase = true) == true
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) TealPrimary else SlateDark)
                                        .border(0.5.dp, if (isSel) TealPrimary else BorderSlate, RoundedCornerShape(12.dp))
                                        .clickable { selectedPerson = person.name }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(person.name, fontSize = 10.sp, color = if (isSel) Color.White else OffWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text("No matching people entries found in system.", fontSize = 10.sp, color = SoftGray)
                    }
                }
            }
        }

        // Selected Person Detailed Card
        selectedPerson?.let { selName ->
            val matchingPersonObj = people.find { it.name.equals(selName, ignoreCase = true) }
            val personTxs = transactions.filter {
                it.payer.equals(selName, ignoreCase = true) || 
                (it.notes.contains("With: $selName", ignoreCase = true)) || 
                (it.notes.endsWith("With: $selName", ignoreCase = true))
            }

            val receivedSum = personTxs.filter { it.type == "Income" }.sumOf { it.amount } + (matchingPersonObj?.moneyReceived ?: 0.0)
            val givenSum = personTxs.filter { it.type == "Expense" || it.type == "Investment" }.sumOf { it.amount } + (matchingPersonObj?.moneyGiven ?: 0.0)
            val netBalVal = receivedSum - givenSum

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SYSTEM DETAILED POSITION FOR:", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                Text(selName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = OffWhite)
                            }

                            matchingPersonObj?.let { pObj ->
                                IconButton(onClick = { viewModel.deletePerson(pObj.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete person", tint = AlertCoral, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MintAccent))
                                    Text("Total Received", fontSize = 10.sp, color = SoftGray)
                                }
                                Text(formatRupee(receivedSum), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AlertCoral))
                                    Text("Total Given", fontSize = 10.sp, color = SoftGray)
                                }
                                Text(formatRupee(givenSum), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AlertCoral)
                            }
                        }

                        val absBal = Math.abs(netBalVal)
                        val isUnreturned = matchingPersonObj?.status == "Won't Be Returned"
                        val badgeCol = when {
                            isUnreturned -> AlertCoral
                            netBalVal > 0 -> MintAccent
                            netBalVal < 0 -> AlertCoral
                            else -> SoftGray
                        }
                        val badgeText = when {
                            isUnreturned -> "⚠️ Money Won't Be Returned (${formatRupee(absBal)})"
                            netBalVal > 0 -> "He/She Owes Us (${formatRupee(absBal)})"
                            netBalVal < 0 -> "We Owe Him/Her (${formatRupee(absBal)})"
                            else -> "Settled Balance (₹0.00)"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeCol.copy(alpha = 0.15f))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badgeText, fontWeight = FontWeight.ExtraBold, color = badgeCol, fontSize = 12.sp)
                        }

                        if (personTxs.isNotEmpty()) {
                            Text("RECENT TRANSACTION LEDGER HISTORY:", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                personTxs.take(4).forEach { tx ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SlateDark)
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (tx.type == "Income") MintAccent else AlertCoral)
                                                )
                                                Text(tx.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                                            }
                                            if (tx.notes.isNotBlank()) {
                                                Text(tx.notes, fontSize = 9.sp, color = SoftGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        Text(
                                            text = if (tx.type == "Income") "+ ${formatRupee(tx.amount)}" else "- ${formatRupee(tx.amount)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (tx.type == "Income") MintAccent else AlertCoral
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filteredPeople.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No saved people entries registered yet.", color = SoftGray, fontSize = 12.sp)
                }
            }
        }
    }
}


// --- 6. INSURANCE POLICIES MODULE ---
@Composable
fun InsurancePoliciesSubView(viewModel: FinanceViewModel) {
    val insurance by viewModel.insurance.collectAsStateWithLifecycle()

    var insName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var premiumAmt by remember { mutableStateOf("") }
    var coverageAmt by remember { mutableStateOf("") }
    var renewDate by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Register policy form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📜 RECONSTRUCT INSURANCE RENEWAL RECORD", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSlate)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = insName,
                            onValueChange = { insName = it },
                            label = { Text("Policy Schema Name", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company Provider", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = premiumAmt,
                            onValueChange = { premiumAmt = it },
                            label = { Text("Premium (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                        OutlinedTextField(
                            value = coverageAmt,
                            onValueChange = { coverageAmt = it },
                            label = { Text("Total Cover (₹)", fontSize = 11.sp, color = OffWhite) },
                            modifier = Modifier.weight(1.2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                        )
                    }

                    OutlinedTextField(
                        value = renewDate,
                        onValueChange = { renewDate = it },
                        label = { Text("Renewal Date Due Limit (e.g. 25 Aug 2026)", fontSize = 11.sp, color = OffWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                    )

                    Button(
                        onClick = {
                            val prem = premiumAmt.toDoubleOrNull() ?: 0.0
                            val cov = coverageAmt.toDoubleOrNull() ?: 0.0
                            if (insName.isNotBlank() && companyName.isNotBlank()) {
                                viewModel.addInsurance(insName, companyName, prem, cov, renewDate, "Active")
                                insName = ""
                                companyName = ""
                                premiumAmt = ""
                                coverageAmt = ""
                                renewDate = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Policy Record", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // List insurance items
        items(insurance) { ins ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ins.policyName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OffWhite)
                            Text("Provider: ${ins.company}", fontSize = 10.sp, color = SoftGray)
                        }
                        IconButton(onClick = { viewModel.deleteInsurance(ins.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftGray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Insurance Cover", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(ins.coverage), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        }
                        Column {
                            Text("Premium Fee Due", fontSize = 9.sp, color = SoftGray)
                            Text(formatRupee(ins.premium), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertCoral)
                        }
                        Column {
                            Text("Renewal Date", fontSize = 9.sp, color = SoftGray)
                            Text(ins.renewalDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailDataRow(label: String, value: String, color: Color = OffWhite) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = SoftGray)
        Text(value, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
    }
}


// ==================== SETTINGS (LEDGER CONTROL RULES) ====================
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel
) {
    val isLiveSyncEnabled by viewModel.isLiveSyncEnabled.collectAsStateWithLifecycle()
    val autoSaveRule by viewModel.autoSaveRule.collectAsStateWithLifecycle()
    val authenticatedUser by viewModel.authenticatedUser.collectAsStateWithLifecycle()
    val isWebServerRunning by viewModel.isWebServerRunning.collectAsStateWithLifecycle()
    val webServerAddress by viewModel.webServerAddress.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("vault_security_prefs", android.content.Context.MODE_PRIVATE) }
    val savedPIN = remember(sharedPrefs) { sharedPrefs.getString("secure_pin_code", "123456") ?: "123456" }

    var showVerificationDialogForWipe by remember { mutableStateOf(false) }
    var showVerificationDialogForSeed by remember { mutableStateOf(false) }
    var showPairingDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val isBackupSyncing by viewModel.isBackupSyncing.collectAsStateWithLifecycle()

    var editName by remember { mutableStateOf("") }
    var editPhotoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            editPhotoUri = uri.toString()
        }
    }

    val rules = listOf("None", "Round Up ($1)", "Round Up ($5)", "5% Auto-Save", "10% Auto-Save")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚙ SETTINGS & CONFIGURATION CONTROLS", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)

        // Logged-in Wealth Profile Information
        authenticatedUser?.let { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .testTag("wealth_profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("👤 SECURE WEALTH PROFILE", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    editName = user.displayName.ifBlank { user.email.substringBefore("@") }
                                    editPhotoUri = user.photoUri
                                    showEditProfileDialog = true
                                }
                                .background(TealPrimary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = TealPrimary, modifier = Modifier.size(12.dp))
                            Text("EDIT PROFILE", fontSize = 9.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = BorderSlate)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        UserProfileAvatar(
                            photoUri = user.photoUri,
                            displayName = user.displayName.ifBlank { user.email.substringBefore("@") },
                            size = 54.dp
                        )
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.displayName.ifBlank { user.email.substringBefore("@").uppercase(Locale.ROOT) },
                                fontSize = 16.sp,
                                color = OffWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(user.email, fontSize = 11.sp, color = SoftGray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Provider: ${user.provider}", fontSize = 9.sp, color = TealPrimary, fontWeight = FontWeight.Medium)
                        }
                        
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Secured",
                            tint = MintAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertCoral.copy(alpha = 0.15f), contentColor = AlertCoral),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("auth_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            modifier = Modifier.padding(end = 6.dp).size(16.dp)
                        )
                        Text("Sign Out & Disconnect Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // --- MOBILE ALERTS & SYSTEM NOTIFICATIONS CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .testTag("notification_settings_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "📱 MOBILE ALERTS & NOTIFICATIONS",
                                fontSize = 11.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Show system permission status badge
                        var hasPermission by remember { mutableStateOf(true) }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            val permissionState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
                            hasPermission = permissionState.status.isGranted
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (hasPermission) MintAccent.copy(alpha = 0.12f) else AlertCoral.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (hasPermission) "ACTIVE" else "DISABLED",
                                    fontSize = 9.sp,
                                    color = if (hasPermission) MintAccent else AlertCoral,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MintAccent.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ENABLED", fontSize = 9.sp, color = MintAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    HorizontalDivider(color = BorderSlate)
                    
                    Text(
                        text = "Stay synchronized with family assets in real-time. Post native alerts upon logging entries and receive daily wrap-up financial scoreboards.",
                        fontSize = 11.sp,
                        color = SoftGray
                    )

                    // Request Permission Button if disabled on Android 13+
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val permissionState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
                        if (!permissionState.status.isGranted) {
                            Button(
                                onClick = { permissionState.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary.copy(alpha = 0.15f), contentColor = TealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Request System Notification Permission", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Toggle 1: Transaction Alerts
                    var transAlertEnabled by remember { 
                        mutableStateOf(sharedPrefs.getBoolean("notifications_transaction_enabled", true)) 
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val newValue = !transAlertEnabled
                                transAlertEnabled = newValue
                                sharedPrefs.edit().putBoolean("notifications_transaction_enabled", newValue).apply()
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Transaction Ledger Alerts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                            Text("Trigger instant native notification for added ledger transactions", fontSize = 10.sp, color = SoftGray)
                        }
                        Switch(
                            checked = transAlertEnabled,
                            onCheckedChange = { value ->
                                transAlertEnabled = value
                                sharedPrefs.edit().putBoolean("notifications_transaction_enabled", value).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TealPrimary,
                                checkedTrackColor = TealPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = SoftGray,
                                uncheckedTrackColor = BorderSlate
                            )
                        )
                    }

                    // Toggle 2: EOD Alerts
                    var eodAlertEnabled by remember { 
                        mutableStateOf(sharedPrefs.getBoolean("notifications_eod_enabled", true)) 
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val newValue = !eodAlertEnabled
                                eodAlertEnabled = newValue
                                sharedPrefs.edit().putBoolean("notifications_eod_enabled", newValue).apply()
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End-of-Day Daily Summaries", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                            Text("Receive an evening scorecard of total spent and earned transactions", fontSize = 10.sp, color = SoftGray)
                        }
                        Switch(
                            checked = eodAlertEnabled,
                            onCheckedChange = { value ->
                                eodAlertEnabled = value
                                sharedPrefs.edit().putBoolean("notifications_eod_enabled", value).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TealPrimary,
                                checkedTrackColor = TealPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = SoftGray,
                                uncheckedTrackColor = BorderSlate
                            )
                        )
                    }

                    HorizontalDivider(color = BorderSlate)

                    // Dispatch simulation EOD button
                    Button(
                        onClick = { viewModel.triggerEndOfDayNotification() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Dispatch", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger Instant End-of-Day Notification", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Google Storage Backup Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .testTag("google_storage_sync_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Sync", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Text("☁️ GOOGLE STORAGE LEDGER SYNC", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                        if (isBackupSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = ElectricBlue, strokeWidth = 1.5.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MintAccent.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("CONNECTED", fontSize = 9.sp, color = MintAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    HorizontalDivider(color = BorderSlate)
                    
                    Text(
                        "Safeguard your family asset data instantly using multi-regional high availability buckets. Restores records instantly across re-installations in second-grade speeds.",
                        fontSize = 11.sp,
                        color = SoftGray
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Mail", tint = SoftGray, modifier = Modifier.size(12.dp))
                        Text("Linked Email ID: ${user.email}", fontSize = 10.sp, color = OffWhite, fontWeight = FontWeight.SemiBold)
                    }

                    val backupExists = remember(user.email) { sharedPrefs.getBoolean("cloud_backup_exists_${user.email}", false) }
                    val backupTime = remember(user.email) { sharedPrefs.getLong("cloud_backup_time_${user.email}", 0L) }
                    
                    if (backupExists && backupTime > 0L) {
                        val format = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.ROOT)
                        val dateStr = format.format(Date(backupTime))
                        Text("📅 Last Google Storage Sync Backup: $dateStr", fontSize = 10.sp, color = MintAccent, fontWeight = FontWeight.Medium)
                    } else {
                        Text("⚠️ No Backup Found in Google Storage for ${user.email}. Synchronize now.", fontSize = 10.sp, color = AmberWarning, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.backupToGoogleStorage { _, _ -> }
                            },
                            enabled = !isBackupSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Backup", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.restoreFromGoogleStorage { _, _ -> }
                            },
                            enabled = !isBackupSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restore", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showEditProfileDialog) {
            val presetEmojis = listOf("💰", "📈", "👑", "🛡️", "💎", "💼")
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("✏️ EDIT WEALTH IDENTITY", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OffWhite) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Customize your family account display username and avatar profile photo.", fontSize = 11.sp, color = SoftGray)
                        
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Display Username", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input")
                        )

                        Text("CHOOSE AVATAR PRESET", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetEmojis.forEach { emoji ->
                                val isSelected = editPhotoUri == "preset:$emoji"
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) TealPrimary.copy(alpha = 0.2f) else SlateDark)
                                        .border(width = 1.5.dp, color = if (isSelected) TealPrimary else BorderSlate, shape = CircleShape)
                                        .clickable { editPhotoUri = "preset:$emoji" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = BorderSlate)

                        Text("OR CUSTOM PHOTO UPLOAD", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            UserProfileAvatar(
                                photoUri = editPhotoUri,
                                displayName = editName,
                                size = 46.dp
                            )

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateDark, contentColor = OffWhite),
                                border = BorderStroke(0.5.dp, BorderSlate),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("Pick from Gallery", fontSize = 11.sp)
                            }

                            if (editPhotoUri != null) {
                                IconButton(
                                    onClick = { editPhotoUri = null },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AlertCoral)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, editPhotoUri)
                            showEditProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("SAVE MODIFICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("CANCEL", color = SoftGray)
                    }
                },
                containerColor = SlateCard,
                modifier = Modifier.border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp))
            )
        }

        // Theme Selection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                .testTag("app_theme_selection_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎨 APPLICATION THEME PREFERENCE",
                    fontSize = 11.sp,
                    color = SoftGray,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = BorderSlate)
                Text(
                    text = "Select your preferred visual style. Change is updated instantly and persisted securely.",
                    fontSize = 10.sp,
                    color = SoftGray
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                val themeOptions = listOf("System", "Light", "Dark")
                themeOptions.forEach { option ->
                    val isSelected = themePreferenceGlobal == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TealPrimary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { updateThemePreference(context, option) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val optionIcon = when (option) {
                                "Light" -> Icons.Default.Share
                                "Dark" -> Icons.Default.Lock
                                else -> Icons.Default.Settings
                            }
                            Icon(
                                imageVector = optionIcon,
                                contentDescription = null,
                                tint = if (isSelected) TealPrimary else SoftGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = option,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TealPrimary else OffWhite
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { updateThemePreference(context, option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = TealPrimary,
                                unselectedColor = SoftGray
                            )
                        )
                    }
                }
            }
        }

        // Auto Save Rule Config
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🏦 DYNAMIC MICRO-SAVINGS ROUND-UPS RULES", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = BorderSlate)
                Text("Every card purchase / expense can triggers an automatic round-up deposit into designated savings plans.", fontSize = 10.sp, color = SoftGray)
                
                Spacer(modifier = Modifier.height(6.dp))

                rules.forEach { rule ->
                    val isSel = autoSaveRule == rule
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) TealPrimary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.setAutoSaveRule(rule) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(rule, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) TealPrimary else OffWhite)
                        RadioButton(
                            selected = isSel,
                            onClick = { viewModel.setAutoSaveRule(rule) },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary, unselectedColor = SoftGray)
                        )
                    }
                }
            }
        }

        // Laptop Web Server Control Node
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                .testTag("web_server_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🌐 LAPTOP WEB-ENTRY SYNC SERVER",
                    fontSize = 11.sp,
                    color = SoftGray,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = BorderSlate)
                Text(
                    text = "Start a local secure HTTP server inside this app to view your dashboard, check records, and enter transactions directly from any laptop or browser on the same Wi-Fi network.",
                    fontSize = 10.sp,
                    color = SoftGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isWebServerRunning) "🟢 SERVER ONLINE" else "⚪ SERVER OFFLINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWebServerRunning) MintAccent else SoftGray
                        )
                        if (isWebServerRunning && webServerAddress != null) {
                            Text(
                                text = "Enter on your laptop:\n$webServerAddress",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Switch(
                        checked = isWebServerRunning,
                        onCheckedChange = { viewModel.toggleWebServer() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OffWhite,
                            checkedTrackColor = TealPrimary,
                            uncheckedThumbColor = SoftGray,
                            uncheckedTrackColor = SlateDark
                        )
                    )
                }

                if (isWebServerRunning) {
                    Button(
                        onClick = { showPairingDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = SlateDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("activate_pairing_btn")
                    ) {
                        Text("📷 LINK LAPTOP VIA QR / PIN", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "💡 LAPTOP SYNC INSTRUCTIONS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftGray
                            )
                            Text(
                                text = "1. Ensure both your Android phone and laptop are connected to the exact same Wi-Fi SSID.\n2. Open any browser (Chrome, Safari, Firefox) on your laptop.\n3. Type the address shown above into the browser address bar.\n4. You can read, filter, or input transaction ledger entries live on a spacious display!",
                                fontSize = 9.sp,
                                color = SoftGray,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Reset Card Ledger data
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🚨 LEDGER STORAGE RESET & SYSTEM WIPE", fontSize = 11.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                Text("Requires identity confirmation (Fingerprint simulation or custom Vault PIN). Choose reset type below:", fontSize = 10.sp, color = SoftGray)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showVerificationDialogForSeed = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary.copy(alpha = 0.2f), contentColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("reset_ledger_btn")
                    ) {
                        Text("Reseed Defaults", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showVerificationDialogForWipe = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertCoral.copy(alpha = 0.2f), contentColor = AlertCoral),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("wipe_all_data_btn")
                    ) {
                        Text("Reset All Data", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showVerificationDialogForSeed) {
        ResetVerificationDialog(
            onDismiss = { showVerificationDialogForSeed = false },
            onVerified = {
                showVerificationDialogForSeed = false
                viewModel.resetLedger()
            },
            savedPinCode = savedPIN
        )
    }

    if (showVerificationDialogForWipe) {
        ResetVerificationDialog(
            onDismiss = { showVerificationDialogForWipe = false },
            onVerified = {
                showVerificationDialogForWipe = false
                viewModel.wipeAllDataTotally()
            },
            savedPinCode = savedPIN
        )
    }

    if (showPairingDialog) {
        LaptopPairingDialog(
            viewModel = viewModel,
            onDismiss = { showPairingDialog = false }
        )
    }
}

@Composable
fun ResetVerificationDialog(
    onDismiss: () -> Unit,
    onVerified: () -> Unit,
    savedPinCode: String
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔒 SECURITY CONFIRMATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGray
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftGray, modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(color = BorderSlate)

                Text(
                    text = "Authorize Database Wipe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "This operation will completely wipe some or all tables in local storage. Please confirm your identity using fingerprint or security PIN to verify.",
                    fontSize = 11.sp,
                    color = SoftGray,
                    textAlign = TextAlign.Center
                )

                var selectedMethodTab by remember { mutableStateOf(0) } // 0 = PIN, 1 = FINGERPRINT

                TabRow(
                    selectedTabIndex = selectedMethodTab,
                    containerColor = SlateDark,
                    contentColor = TealPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).height(38.dp)
                ) {
                    Tab(
                        selected = selectedMethodTab == 0,
                        onClick = { selectedMethodTab = 0 },
                        text = { Text("Enter PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedMethodTab == 1,
                        onClick = { selectedMethodTab = 1 },
                        text = { Text("Fingerprint", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedMethodTab == 0) {
                    // PIN CODE INTERACTIVE VIEW
                    var enteredPin by remember { mutableStateOf("") }
                    var hasError by remember { mutableStateOf(false) }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        repeat(6) { index ->
                            val isFilled = index < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasError) AlertCoral 
                                        else if (isFilled) TealPrimary 
                                        else SlateDark
                                    )
                                    .border(1.dp, if (hasError) AlertCoral else BorderSlate, CircleShape)
                            )
                        }
                    }

                    if (hasError) {
                        Text(
                            text = "❌ Invalid security PIN code. Try again.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertCoral
                        )
                    } else {
                        Text(
                            text = "Tip: Default profile code is $savedPinCode",
                            fontSize = 10.sp,
                            color = SoftGray
                        )
                    }

                    // Numeric Keyboard (Grid of buttons)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                    ) {
                        val rows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("⌫", "0", "✓")
                        )

                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { digit ->
                                    val isSpecial = digit == "⌫" || digit == "✓"
                                    Button(
                                        onClick = {
                                            hasError = false
                                            if (digit == "⌫") {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            } else if (digit == "✓") {
                                                if (enteredPin == savedPinCode) {
                                                    onVerified()
                                                } else {
                                                    hasError = true
                                                    enteredPin = ""
                                                }
                                            } else {
                                                if (enteredPin.length < 6) {
                                                    enteredPin += digit
                                                }
                                                // Auto submit on 6 digits
                                                if (enteredPin.length == 6) {
                                                    if (enteredPin == savedPinCode) {
                                                        onVerified()
                                                    } else {
                                                        hasError = true
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSpecial) SlateDark else BorderSlate.copy(alpha = 0.5f),
                                            contentColor = if (digit == "✓") MintAccent else OffWhite
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Text(digit, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // FINGERPRINT SIMULATION CONTAINER
                    var isScanning by remember { mutableStateOf(false) }
                    var scanProgress by remember { mutableStateOf(0f) }
                    var scanComplete by remember { mutableStateOf(false) }

                    LaunchedEffect(isScanning) {
                        if (isScanning) {
                            scanProgress = 0f
                            while (scanProgress < 1.0f) {
                                kotlinx.coroutines.delay(100)
                                scanProgress += 0.1f
                            }
                            scanComplete = true
                            isScanning = false
                            onVerified()
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (isScanning) TealPrimary.copy(alpha = 0.15f) else SlateDark)
                                .border(1.5.dp, if (isScanning) TealPrimary else BorderSlate, CircleShape)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    progress = { scanProgress },
                                    color = TealPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            FingerprintCustomIcon(
                                modifier = Modifier.size(36.dp),
                                tint = if (scanComplete) MintAccent else if (isScanning) TealPrimary else SoftGray
                            )
                        }

                        Text(
                            text = if (scanComplete) "Identity verified successfully!"
                                   else if (isScanning) "Hold still... Scanning fingerprint (${(scanProgress * 100).toInt()}%)"
                                   else "Simulate local biometric secure unlock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (scanComplete) MintAccent else OffWhite,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { isScanning = true },
                            enabled = !isScanning && !scanComplete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPrimary,
                                disabledContainerColor = SlateDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("fingerprint_simulate_hold_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Fingerprint Scan",
                                modifier = Modifier.size(16.dp),
                                tint = if (isScanning || scanComplete) SoftGray else Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isScanning) "Scanning..." else if (scanComplete) "Verified" else "Touch & Scan Finger",
                                fontWeight = FontWeight.Bold,
                                color = if (isScanning || scanComplete) SoftGray else Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FingerprintCustomIcon(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = size.copy(width = size.width, height = size.height * 1.5f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.15f)
        )
        drawArc(
            color = tint,
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = size.copy(width = size.width * 0.7f, height = size.height * 1.1f),
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.35f)
        )
        drawArc(
            color = tint,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = size.copy(width = size.width * 0.4f, height = size.height * 0.7f),
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.30f, size.height * 0.55f)
        )
    }
}

@Composable
fun SecureAuthScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("vault_security_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Check if profile exists
    var hasRegisteredProfile by remember { mutableStateOf(sharedPrefs.getBoolean("has_registered_profile", false)) }
    var registeredName by remember { mutableStateOf(sharedPrefs.getString("registered_name", "") ?: "") }
    var registeredEmail by remember { mutableStateOf(sharedPrefs.getString("registered_email", "") ?: "") }
    var registeredPassword by remember { mutableStateOf(sharedPrefs.getString("registered_password", "") ?: "") }
    var savedPIN by remember { mutableStateOf(sharedPrefs.getString("secure_pin_code", "123456") ?: "123456") }
    var isFingerprintEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("fingerprint_enabled", true)) }
    var forcePINLoginNextTime by remember { mutableStateOf(sharedPrefs.getBoolean("force_pin_login_next_time", false)) }

    // Unified 6-Page Navigation State Machine: 
    // Page 1: Intro animation
    // Page 2: Landing of New User & Signin options
    // Page 3: Step 1 (Username Setup)
    // Page 4: Step 2 (Email & Password Setup)
    // Page 5: Step 3 (PIN & Fingerprint Setup)
    // Page 6: Welcome Screen
    // Page 10: Direct login for registered users
    // Page 11: Forgot PIN
    // Page 12: Reset new PIN
    var currentStepPage by remember { mutableStateOf(1) }

    // Signup Input Cache
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupPIN by remember { mutableStateOf("") }
    var signupFingerprintEnabled by remember { mutableStateOf(true) }
    var signupErrorText by remember { mutableStateOf<String?>(null) }

    // Password Visiblity state
    var passwordVisible by remember { mutableStateOf(false) }

    // Login Pin Input
    var typedPIN by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf<String?>(null) }

    // Forgot Pin Fields
    var forgotEmail by remember { mutableStateOf("") }
    var forgotPassword by remember { mutableStateOf("") }
    var forgotErrorText by remember { mutableStateOf<String?>(null) }

    // Forgot Password Flow States (Page 13)
    var recoveryEmailInput by remember { mutableStateOf("") }
    var recoverySentState by remember { mutableStateOf(false) } // false = input, true = reset
    var recoverySentLoading by remember { mutableStateOf(false) }
    var recoveryCodeInput by remember { mutableStateOf("") }
    var recoveryNewPasswordInput by remember { mutableStateOf("") }
    var recoveryNewPasswordVisible by remember { mutableStateOf(false) }
    var recoveryErrorText by remember { mutableStateOf<String?>(null) }
    var recoverySuccessText by remember { mutableStateOf<String?>(null) }

    // Reset Pin Fields
    var changePIN1 by remember { mutableStateOf("") }
    var changePIN2 by remember { mutableStateOf("") }
    var changePINErrorText by remember { mutableStateOf<String?>(null) }

    var showSimulationDialog by remember { mutableStateOf(false) }

    val isFirebaseActive by viewModel.isFirebaseActive.collectAsStateWithLifecycle()

    // Trigger Biometric Helper
    val triggerBiometricInput = {
        val activity = context as? androidx.fragment.app.FragmentActivity
        if (activity != null) {
            val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
            val biometricManager = androidx.biometric.BiometricManager.from(activity)
            val canAuthenticate = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )

            if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS && isFingerprintEnabled) {
                val biometricPrompt = androidx.biometric.BiometricPrompt(
                    activity,
                    executor,
                    object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            if (hasRegisteredProfile) {
                                viewModel.loginRegisteredLocal(registeredEmail, registeredName)
                            } else {
                                viewModel.biometricAuth()
                            }
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            showSimulationDialog = true
                        }
                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                        }
                    }
                )

                val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                    .setTitle("FinNest Lockbox")
                    .setSubtitle("Confirm touch-ID to decrypt private wealth metrics")
                    .setNegativeButtonText("Use PIN code")
                    .build()

                try {
                    biometricPrompt.authenticate(promptInfo)
                } catch (e: Exception) {
                    showSimulationDialog = true
                }
            } else {
                showSimulationDialog = true
            }
        } else {
            showSimulationDialog = true
        }
    }

    // Direct Login or Welcome routing on App launch after Page 1 completes
    val proceedAfterIntro = {
        if (hasRegisteredProfile) {
            currentStepPage = 10 // Route directly to login
        } else {
            currentStepPage = 2 // Route to onboarding
        }
    }

    // Auto-scans biometrics instantly during Direct Login if enabled
    LaunchedEffect(currentStepPage) {
        if (currentStepPage == 10 && isFingerprintEnabled && !forcePINLoginNextTime) {
            triggerBiometricInput()
        }
    }

    // Step Progress Line Component
    @Composable
    fun DrawProgressStepLine(step: Int) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..3) {
                val isDoneOrActive = i <= step
                val fraction = if (isDoneOrActive) 1f else 0f
                val color = if (isDoneOrActive) TealPrimary else BorderSlate.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // --- PAGE 1: INTRO ANIMATION & GREETING CARD ---
        if (currentStepPage == 1) {
            var startAnim by remember { mutableStateOf(false) }
            var isExiting by remember { mutableStateOf(false) }
            
            val scaleSpec: AnimationSpec<Float> = if (isExiting) {
                tween(500, easing = FastOutSlowInEasing)
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            }

            val translateSpec: AnimationSpec<Dp> = if (isExiting) {
                tween(500, easing = FastOutSlowInEasing)
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            }

            val rotationSpec: AnimationSpec<Float> = if (isExiting) {
                tween(500, easing = FastOutSlowInEasing)
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            }
            
            val logoScale by animateFloatAsState(
                targetValue = if (isExiting) 1.25f else if (startAnim) 1.05f else 0.5f,
                animationSpec = scaleSpec,
                label = "LogoScale"
            )
            val logoTranslateY by animateDpAsState(
                targetValue = if (isExiting) (-100).dp else if (startAnim) 0.dp else 120.dp,
                animationSpec = translateSpec,
                label = "LogoTranslate"
            )
            val logoRotation by animateFloatAsState(
                targetValue = if (isExiting) -15f else if (startAnim) 0f else -35f,
                animationSpec = rotationSpec,
                label = "LogoRotation"
            )
            val logoAlpha by animateFloatAsState(
                targetValue = if (isExiting) 0f else if (startAnim) 1f else 0f,
                animationSpec = tween(durationMillis = if (isExiting) 400 else 1000, easing = FastOutSlowInEasing),
                label = "LogoAlpha"
            )
            val textAlpha by animateFloatAsState(
                targetValue = if (isExiting) 0f else if (startAnim) 1f else 0.1f,
                animationSpec = tween(1500),
                label = "TextAlpha"
            )

            LaunchedEffect(Unit) {
                startAnim = true
                kotlinx.coroutines.delay(2600)
                isExiting = true
                kotlinx.coroutines.delay(500)
                proceedAfterIntro()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        translationY = logoTranslateY.toPx()
                        rotationZ = logoRotation
                        alpha = logoAlpha
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, BorderSlate, RoundedCornerShape(24.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // The custom user logo
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_app_icon_logo_black_bg_1782145328334),
                            contentDescription = "FinNest Logo",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, TealPrimary.copy(alpha = 0.5f), CircleShape)
                        )

                        LinearProgressIndicator(
                            color = TealPrimary,
                            trackColor = BorderSlate.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(3.dp)
                                .clip(CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                TextButton(
                    onClick = { proceedAfterIntro() },
                    colors = ButtonDefaults.textButtonColors(contentColor = TealPrimary)
                ) {
                    Text("TAP TO BYPASS INTRO ➜", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- PAGE 2: NEW USER ENGINE OR SIGNIN OPTIONS ---
        else if (currentStepPage == 2) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo Banner
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_app_icon_logo_black_bg_1782145328334),
                        contentDescription = "FinNest Minimal Logo",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, TealPrimary.copy(alpha = 0.5f), CircleShape)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Main Action Code - Start Wizard Link
                    Button(
                        onClick = { currentStepPage = 3 },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_new_account_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CREATE NEW ACCOUNT (STEP 1/3)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                    }

                    // Google Login trigger Button
                    var showGoogleAccountChooser by remember { mutableStateOf(false) }
                    var customGoogleEmail by remember { mutableStateOf("") }
                    var customGoogleName by remember { mutableStateOf("") }
                    var googleErrorText by remember { mutableStateOf<String?>(null) }

                    Button(
                        onClick = { showGoogleAccountChooser = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboard_goog_login")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Continue with Google Auth", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (showGoogleAccountChooser) {
                        val accountsList = listOf(
                            "Bala Subramanisan" to "bala.wealth@gmail.com",
                            "Family Wealth Group" to "family.wealth.active@gmail.com"
                        )
                        AlertDialog(
                            onDismissRequest = { showGoogleAccountChooser = false },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 24.sp)
                                    Text("Choose an account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                                }
                            },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "to continue to FinNest Wealth Vault in secure sandbox mode.",
                                        fontSize = 11.sp,
                                        color = SoftGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    accountsList.forEach { account ->
                                        val (name, email) = account
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SlateDark),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.googleSignIn(email, name) { success ->
                                                        if (success) {
                                                            viewModel.restoreFromGoogleStorage { _, _ -> }
                                                            showGoogleAccountChooser = false
                                                        }
                                                    }
                                                }
                                                .border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(TealPrimary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(name.take(1), fontWeight = FontWeight.Bold, color = TealPrimary)
                                                }
                                                Column {
                                                    Text(name, fontSize = 12.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                                                    Text(email, fontSize = 10.sp, color = SoftGray)
                                                }
                                            }
                                        }
                                    }
                                    
                                    HorizontalDivider(color = BorderSlate, modifier = Modifier.padding(vertical = 4.dp))
                                    
                                    Text("CONNECT CUSTOM GOOGLE PROFILE", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                    
                                    OutlinedTextField(
                                        value = customGoogleEmail,
                                        onValueChange = { customGoogleEmail = it },
                                        label = { Text("Google Account ID", fontSize = 10.sp) },
                                        placeholder = { Text("yourname@gmail.com") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = TealPrimary,
                                            unfocusedBorderColor = BorderSlate,
                                            focusedTextColor = OffWhite,
                                            unfocusedTextColor = OffWhite
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = customGoogleName,
                                        onValueChange = { customGoogleName = it },
                                        label = { Text("Google Profile Name", fontSize = 10.sp) },
                                        placeholder = { Text("Bala Subramanisan") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = TealPrimary,
                                            unfocusedBorderColor = BorderSlate,
                                            focusedTextColor = OffWhite,
                                            unfocusedTextColor = OffWhite
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    googleErrorText?.let { err ->
                                        Text(err, color = AlertCoral, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            googleErrorText = null
                                            if (!customGoogleEmail.contains("@") || customGoogleEmail.isBlank()) {
                                                googleErrorText = "❌ Please enter a valid Google Account email."
                                            } else if (customGoogleName.isBlank()) {
                                                googleErrorText = "❌ Please enter account name."
                                            } else {
                                                viewModel.googleSignIn(customGoogleEmail, customGoogleName) { success ->
                                                    if (success) {
                                                        viewModel.restoreFromGoogleStorage { _, _ -> }
                                                        showGoogleAccountChooser = false
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text("CONNECT SECURE GOOGLE IDENTITY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                TextButton(onClick = { showGoogleAccountChooser = false }) {
                                    Text("CLOSE", color = SoftGray)
                                }
                            },
                            containerColor = SlateCard,
                            modifier = Modifier.border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp))
                        )
                    }

                    HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                    // Link back to Sign In if already registered
                    if (hasRegisteredProfile) {
                        TextButton(
                            onClick = { currentStepPage = 10 },
                            colors = ButtonDefaults.textButtonColors(contentColor = MintAccent)
                        ) {
                            Text("ALREADY REGISTERED? LOG IN WITH PIN/BIOMETRICS ➜", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // --- PAGE 3: CHOOSE USERNAME (STEP 1 OF 3) ---
        else if (currentStepPage == 3) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "STEP 1: CHOOSE A USERNAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Identify Your Ledger Identity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite
                    )

                    DrawProgressStepLine(step = 1)

                    Text(
                        text = "This name gets stamped on all your transactions, budgets, and family sync operations. Keep it short and friendly!",
                        fontSize = 10.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signupName,
                        onValueChange = { signupName = it },
                        label = { Text("Member Username", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Bala Subramanisan") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_step1_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isNameValid = signupName.isNotBlank() && signupName.length >= 3
                        Icon(
                            imageVector = if (isNameValid) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (signupName.isEmpty()) SoftGray else if (isNameValid) MintAccent else AlertCoral,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (signupName.isEmpty()) {
                                "Enter username (minimum 3 characters)"
                            } else if (isNameValid) {
                                "✓ Username looks available and valid"
                            } else {
                                "⚠️ Must be at least 3 characters"
                            },
                            fontSize = 10.sp,
                            color = if (signupName.isEmpty()) SoftGray else if (isNameValid) MintAccent else AlertCoral,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    signupErrorText?.let { err ->
                        Text(err, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStepPage = 2 },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite),
                            border = BorderStroke(0.5.dp, BorderSlate),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("BACK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (signupName.isBlank() || signupName.length < 3) {
                                    signupErrorText = "❌ Username must be at least 3 characters!"
                                } else {
                                    signupErrorText = null
                                    currentStepPage = 4
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("signup_step1_next")
                        ) {
                            Text("NEXT ➜", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // --- PAGE 4: EMAIL & PASSWORD (STEP 2 OF 3) ---
        else if (currentStepPage == 4) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "STEP 2: EMAIL & SECURITY CODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Configure Safe Vault Access",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite
                    )

                    DrawProgressStepLine(step = 2)

                    Text(
                        text = "Register private credentials so you can verify transactions syncly or recover master profiles on new devices.",
                        fontSize = 10.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signupEmail,
                        onValueChange = { signupEmail = it },
                        label = { Text("E-mail Address (Account ID)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. bala.wealth@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_step2_email")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isEmailValid = signupEmail.contains("@") && signupEmail.substringAfter("@").contains(".") && !signupEmail.contains(" ")
                        Icon(
                            imageVector = if (isEmailValid) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (signupEmail.isEmpty()) SoftGray else if (isEmailValid) MintAccent else AlertCoral,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (signupEmail.isEmpty()) {
                                "Enter your primary account email"
                            } else if (isEmailValid) {
                                "✓ Valid email configuration"
                            } else {
                                "⚠️ Enter a valid email format (e.g. user@gmail.com)"
                            },
                            fontSize = 10.sp,
                            color = if (signupEmail.isEmpty()) SoftGray else if (isEmailValid) MintAccent else AlertCoral,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedTextField(
                        value = signupPassword,
                        onValueChange = { signupPassword = it },
                        label = { Text("Master Account Password", fontSize = 11.sp) },
                        placeholder = { Text("Min 6 characters") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Done else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (passwordVisible) MintAccent else SoftGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (passwordVisible) "HIDE" else "SHOW",
                                        fontSize = 10.sp,
                                        color = if (passwordVisible) MintAccent else SoftGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_step2_password")
                    )

                    // Real-time password strength component
                    val pwdLength = signupPassword.length
                    val containsDigit = signupPassword.any { it.isDigit() }
                    val containsMixedCase = signupPassword.any { it.isUpperCase() } && signupPassword.any { it.isLowerCase() }
                    val containsSpecial = signupPassword.any { !it.isLetterOrDigit() }
                    
                    val strengthScore = remember(signupPassword) {
                        if (pwdLength < 6) 0
                        else {
                            var score = 1
                            if (containsDigit) score++
                            if (containsMixedCase) score++
                            if (containsSpecial) score++
                            score
                        }
                    }
                    
                    val (strengthColor, strengthLabel) = when {
                        pwdLength == 0 -> SoftGray to "Not entered yet"
                        pwdLength < 6 -> AlertCoral to "Too Short (Min 6)"
                        strengthScore <= 2 -> AlertCoral to "Weak Security"
                        strengthScore == 3 -> Color(0xFFFFB300) to "Medium Security"
                        else -> MintAccent to "Strong Security"
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Password Strength Indicator:", fontSize = 10.sp, color = SoftGray)
                            Text(strengthLabel, fontSize = 10.sp, color = strengthColor, fontWeight = FontWeight.Bold)
                        }
                        
                        // Strength Bar UI (a nice modern 3-split bar layout)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (b in 1..3) {
                                val isColored = when (b) {
                                    1 -> pwdLength >= 6
                                    2 -> pwdLength >= 6 && strengthScore >= 3
                                    3 -> pwdLength >= 6 && strengthScore >= 4
                                    else -> false
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isColored) strengthColor else BorderSlate.copy(alpha = 0.4f))
                                )
                            }
                        }
                        
                        // Informational helper bullets
                        if (pwdLength > 0 && pwdLength < 6) {
                            Text("• Connect 6 or more characters to form a valid passcode.", fontSize = 9.sp, color = AlertCoral)
                        } else if (pwdLength >= 6) {
                            val recommendations = mutableListOf<String>()
                            if (!containsDigit) recommendations.add("add numbers")
                            if (!containsMixedCase) recommendations.add("use mixed cases (Aa)")
                            if (!containsSpecial) recommendations.add("add symbols (e.g. $, !, @)")
                            
                            if (recommendations.isNotEmpty()) {
                                Text("Tip: To achieve maximum cryptography strength, " + recommendations.joinToString(", ") + ".", fontSize = 9.sp, color = SoftGray)
                            } else {
                                Text("✓ Master vault encryption is secure and fully validated.", fontSize = 9.sp, color = MintAccent)
                            }
                        }
                    }

                    signupErrorText?.let { err ->
                        Text(err, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                signupErrorText = null
                                currentStepPage = 3
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite),
                            border = BorderStroke(0.5.dp, BorderSlate),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("BACK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                when {
                                    signupEmail.isBlank() || !signupEmail.contains("@") -> {
                                        signupErrorText = "❌ A valid Email address is required!"
                                    }
                                    signupPassword.length < 6 -> {
                                        signupErrorText = "❌ Passcode must be at least 6 characters!"
                                    }
                                    else -> {
                                        signupErrorText = null
                                        currentStepPage = 5
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("signup_step2_next")
                        ) {
                            Text("NEXT ➜", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // --- PAGE 5: DEVICE PIN & BIOMETRICS (STEP 3 OF 3) ---
        else if (currentStepPage == 5) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "STEP 3: TACTILE PIN & SECURITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Secure Physical Device Access",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite
                    )

                    DrawProgressStepLine(step = 3)

                    Text(
                        text = "Set a 6-digit secondary hardware code to decrypt financial logs. Your biometrics can lock the device fully.",
                        fontSize = 10.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = signupPIN,
                        onValueChange = { newValue ->
                            val clean = newValue.filter { it.isDigit() }
                            if (clean.length <= 6) {
                                signupPIN = clean
                            }
                        },
                        label = { Text("Setup 6-digit access PIN", fontSize = 11.sp) },
                        placeholder = { Text("Type exactly 6 numbers") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_step3_pin")
                    )

                    // Fingerprint switch toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateDark)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FingerprintCustomIcon(modifier = Modifier.size(18.dp), tint = MintAccent)
                            Text("Fast Fingerprint Unlock", fontSize = 11.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = signupFingerprintEnabled,
                            onCheckedChange = { signupFingerprintEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintAccent,
                                checkedTrackColor = MintAccent.copy(alpha = 0.3f)
                            )
                        )
                    }

                    signupErrorText?.let { err ->
                        Text(err, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                signupErrorText = null
                                currentStepPage = 4
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite),
                            border = BorderStroke(0.5.dp, BorderSlate),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("BACK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (signupPIN.length != 6) {
                                    signupErrorText = "❌ PIN must be exactly 6 digits!"
                                } else {
                                    signupErrorText = null

                                    // COMMIT REGISTRATION TO SECURE SHARED PREFERENCES
                                    sharedPrefs.edit()
                                        .putBoolean("has_registered_profile", true)
                                        .putString("registered_name", signupName)
                                        .putString("registered_email", signupEmail)
                                        .putString("registered_password", signupPassword)
                                        .putString("secure_pin_code", signupPIN)
                                        .putBoolean("fingerprint_enabled", signupFingerprintEnabled)
                                        .putBoolean("force_pin_login_next_time", false)
                                        .apply()

                                    // Refresh memory caches
                                    hasRegisteredProfile = true
                                    registeredName = signupName
                                    registeredEmail = signupEmail
                                    registeredPassword = signupPassword
                                    savedPIN = signupPIN
                                    isFingerprintEnabled = signupFingerprintEnabled
                                    forcePINLoginNextTime = false

                                    // Trigger Welcome Success
                                    viewModel.showNotification("Security keys compiled successfully!")
                                    currentStepPage = 6
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MintAccent),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("signup_step3_complete")
                        ) {
                            Text("COMPLETE ✔", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // --- PAGE 6: WELCOME SUCCESS PROFILE SCREEN ---
        else if (currentStepPage == 6) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(1.dp, MintAccent, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_app_icon_logo_black_bg_1782145328334),
                        contentDescription = "FinNest Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MintAccent.copy(alpha = 0.5f), CircleShape)
                    )

                    Text(
                        text = "🥳 SETUP SUCCESSFUL!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintAccent,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Welcome to FinNest, $registeredName!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your private family wealth nest is ready. You have configured secure physical keys and local ledger parameters under device control.",
                        fontSize = 11.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Profile Identity", fontSize = 10.sp, color = SoftGray)
                                Text(registeredName, fontSize = 10.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Registered Mail", fontSize = 10.sp, color = SoftGray)
                                Text(registeredEmail, fontSize = 10.sp, color = OffWhite)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Secondary PIN code", fontSize = 10.sp, color = SoftGray)
                                Text("•••••• (Secure)", fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Touch Sensor Status", fontSize = 10.sp, color = SoftGray)
                                Text(
                                    text = if (isFingerprintEnabled) "HARDWARE LOCKED 🟢" else "DISABLED 🔴",
                                    fontSize = 9.sp,
                                    color = if (isFingerprintEnabled) MintAccent else SoftGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            viewModel.loginRegisteredLocal(registeredEmail, registeredName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("welcome_enter_button")
                    ) {
                        Text("ENTER MY FINANCIAL NEST 🏠", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }

        // --- PAGE 10: DIRECT LOGIN FOR ALREADY REGISTERED USER (FINGER/PIN) ---
        else if (currentStepPage == 10) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Lock Box Header
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Lock",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "FINNEST ACCESS GATEWAY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OffWhite,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (forcePINLoginNextTime) "PIN Reset Active. Verify security PIN!" else "Welcome back! Scan biometric key or enter PIN code.",
                        fontSize = 11.sp,
                        color = if (forcePINLoginNextTime) AlertCoral else SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    // Touch Scan Area
                    val isBioVisible = isFingerprintEnabled && !forcePINLoginNextTime
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isBioVisible) MintAccent.copy(alpha = 0.08f) else BorderSlate.copy(alpha = 0.1f))
                            .border(1.dp, if (isBioVisible) MintAccent.copy(alpha = 0.3f) else BorderSlate.copy(alpha = 0.3f), CircleShape)
                            .clickable(enabled = isBioVisible) { triggerBiometricInput() }
                            .testTag("fingerprint_login_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        FingerprintCustomIcon(modifier = Modifier.size(38.dp), tint = if (isBioVisible) MintAccent else SoftGray)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (forcePINLoginNextTime) {
                            "⚠️ Complete Access Verification"
                        } else if (isFingerprintEnabled) {
                            "Tap sensor above to verify fingerprint"
                        } else {
                            "Biometrics offline. Use secondary PIN below."
                        },
                        fontSize = 11.sp,
                        color = if (forcePINLoginNextTime) AlertCoral else if (isFingerprintEnabled) MintAccent else SoftGray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = isBioVisible) { triggerBiometricInput() }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Enter Access PIN", fontSize = 11.sp, color = OffWhite.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // PIN dots UI
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val isFilled = i < typedPIN.length
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) TealPrimary else BorderSlate)
                                    .border(
                                        width = 1.dp,
                                        color = if (isFilled) TealPrimary else SoftGray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    pinErrorText?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(err, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular keypad grid
                    Column(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val keypadRows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9")
                        )

                        keypadRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { digit ->
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(SlateDark)
                                            .border(0.5.dp, BorderSlate, CircleShape)
                                            .clickable {
                                                pinErrorText = null
                                                if (typedPIN.length < 6) {
                                                    typedPIN += digit
                                                }
                                                if (typedPIN.length == 6) {
                                                    if (typedPIN == savedPIN) {
                                                        if (forcePINLoginNextTime) {
                                                            sharedPrefs.edit().putBoolean("force_pin_login_next_time", false).apply()
                                                            forcePINLoginNextTime = false
                                                        }
                                                        viewModel.loginRegisteredLocal(registeredEmail, registeredName)
                                                        typedPIN = ""
                                                    } else {
                                                        pinErrorText = "❌ Invalid PIN. Please try again!"
                                                        typedPIN = ""
                                                    }
                                                }
                                            }
                                            .testTag("pin_key_$digit"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(digit, color = OffWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Bottom row keypad
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(SlateDark)
                                    .border(0.5.dp, BorderSlate, CircleShape)
                                    .clickable {
                                        if (typedPIN.isNotEmpty()) {
                                            typedPIN = typedPIN.dropLast(1)
                                        }
                                    }
                                    .testTag("pin_key_backspace"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertCoral, modifier = Modifier.size(18.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(SlateDark)
                                    .border(0.5.dp, BorderSlate, CircleShape)
                                    .clickable {
                                        pinErrorText = null
                                        if (typedPIN.length < 6) {
                                            typedPIN += "0"
                                        }
                                        if (typedPIN.length == 6) {
                                            if (typedPIN == savedPIN) {
                                                if (forcePINLoginNextTime) {
                                                    sharedPrefs.edit().putBoolean("force_pin_login_next_time", false).apply()
                                                    forcePINLoginNextTime = false
                                                }
                                                viewModel.loginRegisteredLocal(registeredEmail, registeredName)
                                                typedPIN = ""
                                            } else {
                                                pinErrorText = "❌ Invalid PIN. Please try again!"
                                                typedPIN = ""
                                            }
                                        }
                                    }
                                    .testTag("pin_key_0"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("0", color = OffWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            // Simulated fingerprint trigger tap
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(SlateDark)
                                    .border(0.5.dp, BorderSlate, CircleShape)
                                    .clickable(enabled = isBioVisible) { triggerBiometricInput() }
                                    .testTag("pin_key_finger"),
                                contentAlignment = Alignment.Center
                            ) {
                                FingerprintCustomIcon(
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isBioVisible) TealPrimary else SoftGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    if (registeredName.isNotEmpty()) {
                        Text("Authorized Owner: $registeredName", fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Forgot PIN? Reset",
                            fontSize = 10.sp,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    currentStepPage = 11
                                    forgotEmail = ""
                                    forgotPassword = ""
                                    forgotErrorText = null
                                }
                                .padding(4.dp)
                                .testTag("forgot_pin_trigger")
                        )

                        Text(
                            text = "Forgot Password? Recover",
                            fontSize = 10.sp,
                            color = AlertCoral,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    currentStepPage = 13
                                    recoveryEmailInput = ""
                                    recoverySentState = false
                                    recoverySentLoading = false
                                    recoveryCodeInput = ""
                                    recoveryNewPasswordInput = ""
                                    recoveryErrorText = null
                                    recoverySuccessText = null
                                }
                                .padding(4.dp)
                                .testTag("forgot_password_trigger")
                        )

                        Text(
                            text = "New Nest",
                            fontSize = 10.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    currentStepPage = 2
                                    signupName = ""
                                    signupEmail = ""
                                    signupPassword = ""
                                    signupPIN = ""
                                    signupFingerprintEnabled = true
                                    signupErrorText = null
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // --- PAGE 11: FORGOT PIN VERIFICATION ---
        else if (currentStepPage == 11) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "RECOVER PIN SECURELY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertCoral,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Verify Owner Credentials",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite
                    )

                    Text(
                        text = "Enter your primary registered email address and master passcode to open PIN reconfiguration.",
                        fontSize = 10.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Registered Email Address", fontSize = 11.sp) },
                        placeholder = { Text("e.g. bala.wealth@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = forgotPassword,
                        onValueChange = { forgotPassword = it },
                        label = { Text("Master Passcode", fontSize = 11.sp) },
                        placeholder = { Text("Enter account password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Done else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (passwordVisible) MintAccent else SoftGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (passwordVisible) "HIDE" else "SHOW",
                                        fontSize = 10.sp,
                                        color = if (passwordVisible) MintAccent else SoftGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                    )

                    forgotErrorText?.let { err ->
                        Text(err, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            forgotErrorText = null
                            when {
                                forgotEmail.isBlank() || forgotPassword.isBlank() -> {
                                    forgotErrorText = "❌ Please fill out all verification fields."
                                }
                                forgotEmail.equals(registeredEmail, ignoreCase = true) && forgotPassword == registeredPassword -> {
                                    currentStepPage = 12
                                    changePIN1 = ""
                                    changePIN2 = ""
                                    changePINErrorText = null
                                }
                                else -> {
                                    if (!hasRegisteredProfile) {
                                        forgotErrorText = "❌ No owner profile is registered on this device."
                                    } else {
                                        forgotErrorText = "❌ Credentials do not match private database logs."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("auth_primary_button")
                    ) {
                        Text("VERIFY & COMPROMISE CODE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Forgot Passcode? Recover",
                            fontSize = 10.sp,
                            color = AlertCoral,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    currentStepPage = 13
                                    recoveryEmailInput = ""
                                    recoverySentState = false
                                    recoverySentLoading = false
                                    recoveryCodeInput = ""
                                    recoveryNewPasswordInput = ""
                                    recoveryErrorText = null
                                    recoverySuccessText = null
                                }
                                .padding(4.dp)
                        )

                        TextButton(onClick = { currentStepPage = 10 }) {
                            Text("➜ Login Gateway", fontSize = 10.sp, color = SoftGray)
                        }
                    }
                }
            }
        }

        // --- PAGE 12: RESET NEW PIN ---
        else if (currentStepPage == 12) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "NEW HARDWARE KEYS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Configure New 6-Digit PIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite
                    )

                    Text(
                        text = "Specify a secure 6-digit access code. Once saved, you must type this PIN in directly to login next time.",
                        fontSize = 10.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    OutlinedTextField(
                        value = changePIN1,
                        onValueChange = { newValue ->
                            val clean = newValue.filter { it.isDigit() }
                            if (clean.length <= 6) {
                                changePIN1 = clean
                            }
                        },
                        label = { Text("New 6-Digit PIN", fontSize = 11.sp) },
                        placeholder = { Text("Type 6 numbers") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_pin_input_1")
                    )

                    OutlinedTextField(
                        value = changePIN2,
                        onValueChange = { newValue ->
                            val clean = newValue.filter { it.isDigit() }
                            if (clean.length <= 6) {
                                changePIN2 = clean
                            }
                        },
                        label = { Text("Confirm New PIN Code", fontSize = 11.sp) },
                        placeholder = { Text("Type PIN again") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_pin_input_2")
                    )

                    changePINErrorText?.let { feedback ->
                        Text(feedback, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            changePINErrorText = null
                            if (changePIN1.length != 6) {
                                changePINErrorText = "❌ PIN code must be exactly 6 digits!"
                            } else if (changePIN1 != changePIN2) {
                                changePINErrorText = "❌ PIN codes do not match!"
                            } else {
                                sharedPrefs.edit()
                                    .putString("secure_pin_code", changePIN1)
                                    .putBoolean("force_pin_login_next_time", true)
                                    .apply()

                                savedPIN = changePIN1
                                forcePINLoginNextTime = true
                                
                                viewModel.showNotification("🔑 Lockbox code updated successfully!")
                                typedPIN = ""
                                currentStepPage = 10
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_customization_btn")
                    ) {
                        Text("SAVE & GO TO SECURE PIN KEYPAD", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                    }
                }
            }
        }

        // --- PAGE 13: FORGOT PASSWORD RECOVERY ---
        else if (currentStepPage == 13) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SECURE VAULT WORKSPACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertCoral,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Master Password Recovery",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite
                    )

                    Text(
                        text = "To preserve high-standard family privacy, trigger a cryptographical key to recover credentials locally.",
                        fontSize = 10.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!recoverySentState) {
                        // Phase 1: Enter Registered Email to trigger verification code
                        OutlinedTextField(
                            value = recoveryEmailInput,
                            onValueChange = { recoveryEmailInput = it },
                            label = { Text("Registered Email Address", fontSize = 11.sp) },
                            placeholder = { Text("e.g. bala.wealth@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_email_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        if (recoverySentLoading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(color = TealPrimary, modifier = Modifier.size(24.dp))
                                Text("Generating cryptographic recovery packet...", fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            recoveryErrorText?.let { err ->
                                Text(err, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            recoverySuccessText?.let { msg ->
                                Text(msg, color = MintAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    recoveryErrorText = null
                                    recoverySuccessText = null
                                    when {
                                        recoveryEmailInput.isBlank() || !recoveryEmailInput.contains("@") -> {
                                            recoveryErrorText = "❌ A valid registered Email address is required!"
                                        }
                                        hasRegisteredProfile && !recoveryEmailInput.equals(registeredEmail, ignoreCase = true) -> {
                                            recoveryErrorText = "❌ This email address is not registered in this local Nest vault."
                                        }
                                        else -> {
                                            // Simulate email transition
                                            recoverySentLoading = true
                                            // Launch a delayed action to transition
                                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                recoverySentLoading = false
                                                recoverySentState = true
                                                recoverySuccessText = "✓ Sent! Check mailbox for secure key [FN-7412]."
                                            }, 2000)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("recovery_send_btn")
                            ) {
                                Text("SEND RECOVERY EMAIL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                            }
                        }
                    } else {
                        // Phase 2: Enter matching Code & Enter New Password
                        Text(
                            text = "Incoming Envelope: Secure key FN-7412 was dispatched in the background sandbox context.",
                            fontSize = 10.sp,
                            color = MintAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(MintAccent.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .border(0.5.dp, MintAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = recoveryCodeInput,
                            onValueChange = { recoveryCodeInput = it },
                            label = { Text("6-Digit Verification Key", fontSize = 11.sp) },
                            placeholder = { Text("Enter FN-7412") },
                            leadingIcon = { Icon(Icons.Default.Done, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_key_input")
                        )

                        OutlinedTextField(
                            value = recoveryNewPasswordInput,
                            onValueChange = { recoveryNewPasswordInput = it },
                            label = { Text("New Master Password", fontSize = 11.sp) },
                            placeholder = { Text("Minimum 6 characters") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SoftGray, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                TextButton(
                                    onClick = { recoveryNewPasswordVisible = !recoveryNewPasswordVisible },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (recoveryNewPasswordVisible) Icons.Default.Done else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (recoveryNewPasswordVisible) MintAccent else SoftGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (recoveryNewPasswordVisible) "HIDE" else "SHOW",
                                            fontSize = 10.sp,
                                            color = if (recoveryNewPasswordVisible) MintAccent else SoftGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (recoveryNewPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = OffWhite
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_new_password")
                        )

                        // Real-time password strength component for new password
                        val newPwdLength = recoveryNewPasswordInput.length
                        val newContainsDigit = recoveryNewPasswordInput.any { it.isDigit() }
                        val newContainsMixedCase = recoveryNewPasswordInput.any { it.isUpperCase() } && recoveryNewPasswordInput.any { it.isLowerCase() }
                        val newContainsSpecial = recoveryNewPasswordInput.any { !it.isLetterOrDigit() }
                        
                        val newStrengthScore = remember(recoveryNewPasswordInput) {
                            if (newPwdLength < 6) 0
                            else {
                                var score = 1
                                if (newContainsDigit) score++
                                if (newContainsMixedCase) score++
                                if (newContainsSpecial) score++
                                score
                            }
                        }
                        
                        val (newStrengthColor, newStrengthLabel) = when {
                            newPwdLength == 0 -> SoftGray to "Not entered yet"
                            newPwdLength < 6 -> AlertCoral to "Too Short (Min 6)"
                            newStrengthScore <= 2 -> AlertCoral to "Weak Security"
                            newStrengthScore == 3 -> Color(0xFFFFB300) to "Medium Security"
                            else -> MintAccent to "Strong Security"
                        }
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Password Strength:", fontSize = 9.sp, color = SoftGray)
                                Text(newStrengthLabel, fontSize = 9.sp, color = newStrengthColor, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (b in 1..3) {
                                    val isColored = when (b) {
                                        1 -> newPwdLength >= 6
                                        2 -> newPwdLength >= 6 && newStrengthScore >= 3
                                        3 -> newPwdLength >= 6 && newStrengthScore >= 4
                                        else -> false
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isColored) newStrengthColor else BorderSlate.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }

                        recoveryErrorText?.let { err ->
                            Text(err, color = AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        recoverySuccessText?.let { msg ->
                            Text(msg, color = MintAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                recoveryErrorText = null
                                recoverySuccessText = null
                                when {
                                    recoveryCodeInput.trim().uppercase() != "FN-7412" && recoveryCodeInput.trim() != "7412" -> {
                                        recoveryErrorText = "❌ Invalid secure key code. Enter FN-7412."
                                    }
                                    recoveryNewPasswordInput.length < 6 -> {
                                        recoveryErrorText = "❌ Master password must be at least 6 characters."
                                    }
                                    else -> {
                                        // Save credentials locally
                                        sharedPrefs.edit()
                                            .putString("registered_password", recoveryNewPasswordInput)
                                            .apply()
                                        registeredPassword = recoveryNewPasswordInput
                                        
                                        recoverySuccessText = "🎉 Reset Successful! Direct login unlocked."
                                        
                                        // Re-enable/sync if bypass active
                                        if (!hasRegisteredProfile) {
                                            sharedPrefs.edit()
                                                .putBoolean("has_registered_profile", true)
                                                .putString("registered_email", recoveryEmailInput)
                                                .putString("registered_name", "Subramanisan")
                                                .apply()
                                            hasRegisteredProfile = true
                                            registeredEmail = recoveryEmailInput
                                            registeredName = "Subramanisan"
                                        }
                                        
                                        // Go to page 10 after 1.5s delay
                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                            currentStepPage = 10
                                            recoverySentState = false
                                            recoveryCodeInput = ""
                                            recoveryNewPasswordInput = ""
                                            recoverySuccessText = null
                                        }, 1500)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("recovery_reset_btn")
                        ) {
                            Text("SAVE NEW CREDENTIALS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                    }

                    TextButton(onClick = { currentStepPage = 10 }) {
                        Text("➜ Return to Access Gateway", fontSize = 10.sp, color = SoftGray)
                    }
                }
            }
        }
    }

    // Interactive Fingerprint Simulator dialog
    if (showSimulationDialog) {
        AlertDialog(
            onDismissRequest = { showSimulationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FingerprintCustomIcon(modifier = Modifier.size(28.dp), tint = TealPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Biometric Keys Scanner Sim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OffWhite)
                }
            },
            text = {
                Column {
                    Text(
                        "Because this is running inside the streaming sandbox web container, some lockbox modules bypass physical fingerprint scanners. Use the instant simulator below:",
                        fontSize = 12.sp,
                        color = SoftGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary.copy(alpha = 0.15f))
                                    .border(1.5.dp, TealPrimary, CircleShape)
                                    .clickable {
                                        showSimulationDialog = false
                                        if (hasRegisteredProfile) {
                                            viewModel.loginRegisteredLocal(registeredEmail, registeredName)
                                        } else {
                                            viewModel.biometricAuth()
                                        }
                                    }
                                    .testTag("simulate_touch_sensor"),
                                contentAlignment = Alignment.Center
                            ) {
                                FingerprintCustomIcon(modifier = Modifier.size(40.dp), tint = TealPrimary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Touch Scanner To Simulate Successful Match", fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSimulationDialog = false
                        if (hasRegisteredProfile) {
                            viewModel.loginRegisteredLocal(registeredEmail, registeredName)
                        } else {
                            viewModel.biometricAuth()
                        }
                    }
                ) {
                    Text("SIMULATED FINGER MATCH", color = TealPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulationDialog = false }) {
                    Text("Cancel", color = SoftGray)
                }
            },
            containerColor = SlateCard
        )
    }
}

@Composable
fun CategorySpendingDonutChart(
    categorySpends: List<CategoryReportRow>,
    modifier: Modifier = Modifier
) {
    if (categorySpends.isEmpty()) return

    var activeIdx by remember(categorySpends) { mutableStateOf(-1) }

    val chartColors = remember {
        listOf(
            Color(0xFF05B292), // TealPrimary
            Color(0xFF0072FF), // ElectricBlue
            Color(0xFF9B5DE5), // Purple
            Color(0xFFFF9F1C), // AmberWarning
            Color(0xFFF15BB5), // Hot Pink
            Color(0xFF38EF7D), // MintAccent
            Color(0xFF00F5D4), // Bright Cyan/Teal
            Color(0xFFFF5A5F), // AlertCoral
            Color(0xFFE07A5F), // Terracotta
            Color(0xFF81B29A)  // Soft Sage
        )
    }

    val totalSpent = remember(categorySpends) { categorySpends.sumOf { it.monthlyTotal } }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭕ CATEGORY ALLOCATION BREAKDOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftGray
                )
                Text(
                    text = "Total Spent: ${formatRupee(totalSpent)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )
            }

            HorizontalDivider(color = BorderSlate)

            val totalSections = categorySpends.size

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                activeIdx = (activeIdx + 2) % (totalSections + 1) - 1
                            }
                    ) {
                        var startAngle = -90f
                        categorySpends.forEachIndexed { index, item ->
                            val sweepAngle = (item.monthPercentage.toFloat() / 100f) * 360f
                            val color = chartColors[index % chartColors.size]
                            val isSelected = activeIdx == index
                            val strokeWidth = if (isSelected) 22.dp.toPx() else 14.dp.toPx()

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (activeIdx in 0 until totalSections) {
                            val activeItem = categorySpends[activeIdx]
                            Text(
                                text = activeItem.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatRupee(activeItem.monthlyTotal),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = OffWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format("%.1f", activeItem.monthPercentage)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = chartColors[activeIdx % chartColors.size]
                            )
                        } else {
                            Text(
                                text = "TAP RIG",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftGray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Monthly",
                                fontSize = 10.sp,
                                color = SoftGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "EXPENSES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorySpends.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        pair.forEach { item ->
                            val index = categorySpends.indexOf(item)
                            val color = chartColors[index % chartColors.size]
                            val isSelected = activeIdx == index
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BorderSlate else Color.Transparent)
                                    .clickable {
                                        activeIdx = if (isSelected) -1 else index
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${item.category} (${String.format("%.0f", item.monthPercentage)}%)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) OffWhite else SoftGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val executor = androidx.core.content.ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraPreviewCore.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    android.util.Log.e("CameraPreviewView", "Camera binding failed", e)
                }
            }, executor)
            previewView
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LaptopPairingDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    
    val pendingPins = remember { mutableStateListOf<String>() }
    var enteredPin by remember { mutableStateOf("") }
    var operationResult by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        while (true) {
            val pins = viewModel.getPendingPairingPins()
            pendingPins.clear()
            pendingPins.addAll(pins)
            kotlinx.coroutines.delay(1500)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(0.5.dp, BorderSlate, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔗 PAIR LAPTOP SESSION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TealPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SoftGray
                        )
                    }
                }
                
                HorizontalDivider(color = BorderSlate)
                
                Text(
                    text = "A laptop terminal is attempting to synchronize. Authorize below via QR scanner or direct PIN verification.",
                    fontSize = 11.sp,
                    color = SoftGray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                if (cameraPermissionState.status.isGranted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateDark)
                            .border(1.5.dp, TealPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CameraPreviewView(modifier = Modifier.fillMaxSize())
                        
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(1.dp, TealPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )
                        HorizontalDivider(
                            color = TealPrimary,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .align(Alignment.Center)
                                .padding(vertical = 4.dp),
                            thickness = 2.dp
                        )
                        Text(
                            text = "LIVE CAMERA SCANNING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateDark)
                            .clickable { cameraPermissionState.launchPermissionRequest() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📷 Tap to enable QR Camera Scanner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                            Text("Uses phone camera to instantly scan the browser QR code", fontSize = 9.sp, color = SoftGray)
                        }
                    }
                }

                if (pendingPins.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, TealPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📡 LAPTOPS DISCOVERED ON LOCAL WI-FI:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            pendingPins.forEach { pin ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SlateDark)
                                        .clickable {
                                            val ok = viewModel.authorizePairingPin(pin)
                                            if (ok) {
                                                operationResult = "✔️ Successfully Authorized!"
                                                enteredPin = ""
                                            } else {
                                                operationResult = "❌ PIN Verification Failed."
                                            }
                                        }
                                        .padding(vertical = 6.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Terminal PIN [$pin]", fontSize = 11.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                                    Text("⚡ TAP TO PAIR INSTANTLY", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MintAccent)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSlate)
                    Text("OR ENTER PAIRING PIN", fontSize = 9.sp, color = SoftGray, modifier = Modifier.padding(horizontal = 8.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSlate)
                }

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 6) {
                            enteredPin = it
                            if (it.length == 4) {
                                val ok = viewModel.authorizePairingPin(it)
                                if (ok) {
                                    operationResult = "✔️ Successfully Authorized!"
                                    enteredPin = ""
                                } else {
                                    operationResult = "❌ PIN Verification Failed."
                                }
                            }
                        }
                    },
                    label = { Text("4-Digit Matching PIN", fontSize = 11.sp) },
                    placeholder = { Text("e.g. 1928", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderSlate,
                        focusedLabelColor = TealPrimary,
                        unfocusedLabelColor = SoftGray,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("manual_pin_input")
                )

                operationResult?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (msg.startsWith("✔️")) MintAccent else AlertCoral,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (enteredPin.trim().isNotEmpty()) {
                            val ok = viewModel.authorizePairingPin(enteredPin)
                            if (ok) {
                                operationResult = "✔️ Successfully Authorized!"
                                enteredPin = ""
                            } else {
                                operationResult = "❌ PIN Verification Failed."
                            }
                        }
                    },
                    enabled = enteredPin.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = SlateDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_pair_manual_btn")
                ) {
                    Text("Authorize Laptop Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AiMonthlyInsightCard(
    viewModel: com.example.ui.FinanceViewModel,
    yearFilter: Int,
    monthFilter: Int
) {
    val insight by viewModel.monthlyInsight.collectAsStateWithLifecycle()
    val isLoading by viewModel.insightLoading.collectAsStateWithLifecycle()

    // Automatically trigger analysis on month/year changes or when first visible
    LaunchedEffect(yearFilter, monthFilter) {
        viewModel.generateMonthlyInsight(yearFilter, monthFilter)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(18.dp))
            .testTag("ai_monthly_insight_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "AI MONTHLY INSIGHT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.generateMonthlyInsight(yearFilter, monthFilter) },
                    modifier = Modifier.size(28.dp).testTag("ai_reanalyze_button"),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-analyze",
                        tint = SoftGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TealPrimary,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Gemini is analyzing your spending trends...",
                        fontSize = 11.sp,
                        color = SoftGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val displayText = insight ?: "No spending insight computed yet. Tap the refresh icon above to trigger analysis manually."

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val lines = displayText.split("\n")
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            val trimmed = line.trim()
                            if (trimmed.startsWith("*") || trimmed.startsWith("-")) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary
                                    )
                                    Text(
                                        text = parseBoldMarkdown(trimmed.substring(1).trim()),
                                        fontSize = 12.sp,
                                        color = OffWhite,
                                        lineHeight = 16.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = parseBoldMarkdown(trimmed),
                                    fontSize = 12.sp,
                                    color = OffWhite,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                val monthNamesList = listOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                val monthName = monthNamesList.getOrElse(monthFilter - 1) { "Selected Month" }

                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary.copy(alpha = 0.05f))
                        .border(0.5.dp, TealPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MintAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Analyzing active ledger: $monthName $yearFilter",
                            fontSize = 10.sp,
                            color = SoftGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun parseBoldMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    val parts = text.split("**")
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = TealPrimary))
            builder.append(part)
            builder.pop()
        } else {
            builder.append(part)
        }
    }
    return builder.toAnnotatedString()
}

@Composable
fun RechartsDashboardComponent(
    expenseVal: Double,
    budget: Double,
    categorySpends: List<CategoryReportRow>,
    modifier: Modifier = Modifier
) {
    val remainingBudget = (budget - expenseVal).coerceAtLeast(0.0)
    val spentPct = if (budget > 0) (expenseVal / budget).toFloat().coerceIn(0f, 1f) else 0f

    val animatedSpentPct by animateFloatAsState(
        targetValue = spentPct,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "budget_consumption"
    )
    
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
            .testTag("recharts_dashboard_component")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header mimicking clean Recharts charts widget
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(TealPrimary)
                    )
                    Text(
                        text = "RECHARTS COMPOSE ANALYTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGray,
                        letterSpacing = 1.1.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TealPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ACTIVE LEDGER", fontSize = 8.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                }
            }
            
            HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
            
            // Financial KPI metrics block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("EXPENSES OUTFLOW", fontSize = 10.sp, color = SoftGray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(formatRupee(expenseVal), fontSize = 16.sp, fontWeight = FontWeight.Black, color = AlertCoral)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("REMAINING BUDGET", fontSize = 10.sp, color = SoftGray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(formatRupee(remainingBudget), fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (remainingBudget > 0.0) MintAccent else AlertCoral)
                }
            }
            
            // Premium double track progress bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Budget Consumption", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.SemiBold)
                    Text("${(spentPct * 100).toInt()}% Used", fontSize = 10.sp, color = if (spentPct > 0.85f) AlertCoral else TealPrimary, fontWeight = FontWeight.Bold)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(if (isDarkThemeGlobal) Color(0xFF16161E) else Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = animatedSpentPct)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(TealPrimary, MintAccent)
                                )
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Recharts visual spending distribution chart drawing!
            Text(
                text = "📊 CATEGORY EXPENDITURE DISTRIBUTION (RECHARTS BAR CHART)",
                fontSize = 11.sp,
                color = SoftGray,
                fontWeight = FontWeight.Bold
            )
            
            if (categorySpends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(if (isDarkThemeGlobal) Color(0xFF070709) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses recorded yet. Create an expense to view bar charts.", fontSize = 10.sp, color = SoftGray)
                }
            } else {
                // Let's draw a full-fidelity native Canvas bar chart mimicking Recharts layout!
                val chartData = categorySpends.take(5) // show top 5
                val maxSpend = remember(chartData) { chartData.maxOf { it.monthlyTotal }.coerceAtLeast(1.0) }

                var triggerAnimation by remember { mutableStateOf(false) }
                LaunchedEffect(chartData) {
                    triggerAnimation = true
                }
                val chartAnimationProgress by animateFloatAsState(
                    targetValue = if (triggerAnimation) 1f else 0f,
                    animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
                    label = "chart_bars"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(if (isDarkThemeGlobal) Color(0xFF07070B) else Color(0xFFFAFAFC), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthVal = size.width
                        val heightVal = size.height
                        
                        // Draw gridlines mimicking Recharts CartesianGrid
                        val gridCount = 4
                        val stepY = heightVal / gridCount
                        for (i in 0..gridCount) {
                            val y = i * stepY
                            drawLine(
                                color = if (isDarkThemeGlobal) Color(0xFF1E1E26) else Color(0xFFE2E8F0),
                                start = Offset(0f, y),
                                end = Offset(widthVal, y),
                                strokeWidth = 1f
                            )
                        }
                        
                        // Draw vertical bars corresponding to top categories
                        val barCount = chartData.size
                        val spacingX = widthVal / barCount
                        val barWidth = spacingX * 0.45f
                        
                        chartData.forEachIndexed { idx, barItem ->
                            val barPct = (barItem.monthlyTotal / maxSpend).toFloat()
                            val barHeight = heightVal * 0.65f * barPct * chartAnimationProgress
                            val startX = (idx * spacingX) + (spacingX - barWidth) / 2f
                            val startY = heightVal - barHeight - 14.dp.toPx() // leave some space for bottom text labels
                            
                            // Draw the bar with a beautiful rounded gradient look
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        TealPrimary,
                                        TealPrimary.copy(alpha = 0.15f)
                                    )
                                ),
                                topLeft = Offset(startX, startY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                    
                    // Overlay labels matching bottom indices
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        chartData.forEach { barItem ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = barItem.category.take(8).uppercase(Locale.ROOT),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftGray,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    // Floating interactive text values above categories
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        chartData.forEach { barItem ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = formatBriefRupee(barItem.monthlyTotal),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TealPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatBriefRupee(value: Double): String {
    return if (value >= 1000) {
        String.format(Locale.ROOT, "₹%.1fK", value / 1000.0)
    } else {
        String.format(Locale.ROOT, "₹%.0f", value)
    }
}

@Composable
fun DailyFinancialBriefComponent(
    transactions: List<TransactionEntity>,
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var useLatestActivityBydefault by remember { mutableStateOf(true) }
    
    val targetTimestamp = if (useLatestActivityBydefault) {
        transactions.maxOfOrNull { it.date } ?: System.currentTimeMillis()
    } else {
        System.currentTimeMillis()
    }
    
    val targetCal = remember(targetTimestamp) {
        Calendar.getInstance().apply { timeInMillis = targetTimestamp }
    }
    
    val targetYear = targetCal.get(Calendar.YEAR)
    val targetMonth = targetCal.get(Calendar.MONTH) + 1
    val targetDay = targetCal.get(Calendar.DAY_OF_MONTH)
    
    val sdfFull = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US) }
    val formattedDate = sdfFull.format(targetCal.time)
    
    val todayTxs = remember(transactions, targetYear, targetMonth, targetDay) {
        transactions.filter { tx ->
            getYearFromTimestamp(tx.date) == targetYear &&
            getMonthFromTimestamp(tx.date) == targetMonth &&
            getDayFromTimestamp(tx.date) == targetDay
        }
    }
    
    val todayIncome = todayTxs.filter { it.type == "Income" }.sumOf { it.amount }
    val todayExpense = todayTxs.filter { it.type == "Expense" }.sumOf { it.amount }
    val todayInvestment = todayTxs.filter { it.type == "Investment" }.sumOf { it.amount }
    val netDaily = todayIncome - todayExpense - todayInvestment
    
    var isDismissed by remember { mutableStateOf(false) }
    
    if (isDismissed) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDismissed = false }
                .background(SlateCard.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Alert", tint = TealPrimary, modifier = Modifier.size(16.dp))
                Text("Daily Financial Brief is minimized", fontSize = 11.sp, color = OffWhite)
            }
            Text("RESTORE REVIEW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
        }
        return
    }
    
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("monex_daily_brief_prefs", android.content.Context.MODE_PRIVATE) }
    val memoKey = "memo_${targetYear}_${targetMonth}_${targetDay}"
    var savedMemo by remember(memoKey) { mutableStateOf(sharedPrefs.getString(memoKey, "") ?: "") }
    var userQuickMemo by remember { mutableStateOf("") }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, if (netDaily < 0) AlertCoral.copy(alpha = 0.8f) else TealPrimary.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .testTag("daily_financial_brief_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (netDaily < 0) AlertCoral.copy(alpha = 0.15f) else TealPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Alert Bell",
                            tint = if (netDaily < 0) AlertCoral else TealPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "DAILY FINANCIAL BRIEF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netDaily < 0) AlertCoral else TealPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = SoftGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { useLatestActivityBydefault = !useLatestActivityBydefault },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Toggle Date Source",
                            tint = SoftGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = SoftGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(SlateDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Day Inflow", fontSize = 9.sp, color = SoftGray)
                        Text(formatRupee(todayIncome), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintAccent)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(SlateDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Day Outflow", fontSize = 9.sp, color = SoftGray)
                        Text(formatRupee(todayExpense + todayInvestment), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertCoral)
                    }
                }
            }
            
            Text("QUICK DIAGNOSTIC NOTES:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SoftGray)
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val outflowVal = todayExpense + todayInvestment
                val (outflowIcon, outflowNote, outflowColor) = when {
                    outflowVal > 3000.0 -> Triple("⚠️", "Heavy spending registered today: ${formatBriefRupee(outflowVal)}. Limit discretionary spends if possible.", AlertCoral)
                    outflowVal > 0.0 -> Triple("⚡", "Discretionary spends actively recorded today: ${formatBriefRupee(outflowVal)} across ${todayTxs.size} entries.", OffWhite)
                    else -> Triple("🟢", "Zero-outflow streak! Exceptional budget discipline: No expenses recorded inside the last 24h.", MintAccent)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(outflowIcon, fontSize = 12.sp)
                    Text(outflowNote, fontSize = 11.sp, color = outflowColor, lineHeight = 14.sp)
                }
                
                val (inflowIcon, inflowNote, inflowColor) = if (todayIncome > 0.0) {
                    Triple("💰", "Liquid capital injection of ${formatBriefRupee(todayIncome)} logged. Capital balance is fully synchronized.", MintAccent)
                } else {
                    Triple("📊", "No major active inflow recorded today. Balance drawing is safely managed.", SoftGray)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(inflowIcon, fontSize = 12.sp)
                    Text(inflowNote, fontSize = 11.sp, color = inflowColor, lineHeight = 14.sp)
                }

                val (balIcon, balNote, balColor) = when {
                    netDaily > 0.0 -> Triple("📈", "Positive Daily Surplus! Your operating ledger grew by ₹${formatBriefRupee(netDaily)} today.", MintAccent)
                    netDaily < 0.0 -> Triple("📉", "Daily Deficit: Net liquid reserves down by ₹${formatBriefRupee(-netDaily)} inside this cycle.", AlertCoral)
                    else -> Triple("⚖️", "Even balance sheet: Zero net liquid change today.", SoftGray)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(balIcon, fontSize = 12.sp)
                    Text(balNote, fontSize = 11.sp, color = balColor, lineHeight = 14.sp)
                }
                
                if (savedMemo.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TealPrimary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, TealPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Check, contentDescription = "Memo icon", tint = TealPrimary, modifier = Modifier.size(10.dp))
                                Text("USER QUICK MEMO CHECKLIST NOTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(savedMemo, fontSize = 10.sp, color = OffWhite)
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userQuickMemo,
                    onValueChange = { userQuickMemo = it },
                    placeholder = { Text("Add daily check note / memo...", fontSize = 10.sp, color = SoftGray) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = OffWhite),
                    maxLines = 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderSlate,
                        focusedContainerColor = SlateDark,
                        unfocusedContainerColor = SlateDark
                    )
                )
                
                Button(
                    onClick = {
                        if (userQuickMemo.isNotBlank()) {
                            sharedPrefs.edit().putString(memoKey, userQuickMemo).apply()
                            savedMemo = userQuickMemo
                            userQuickMemo = ""
                            viewModel.showNotification("🟢 Success: Daily quick note added!")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("SAVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}


@Composable
fun BrokerBankConnectionsSubView(viewModel: FinanceViewModel) {
    val isGrowwConnected by viewModel.isGrowwConnected.collectAsStateWithLifecycle()
    val growwConnectionMethod by viewModel.growwConnectionMethod.collectAsStateWithLifecycle()
    val connectedBanks by viewModel.connectedBanks.collectAsStateWithLifecycle()
    val isConnectionSyncing by viewModel.isConnectionSyncing.collectAsStateWithLifecycle()

    var activeConnectionTab by remember { mutableStateOf(0) } // 0: Broker (Groww), 1: Bank Accounts

    // Groww fields
    var growwClientId by remember { mutableStateOf("") }
    var growwMobile by remember { mutableStateOf("") }
    var growwShowOtpField by remember { mutableStateOf(false) }
    var growwOtpCode by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selection
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("📈 Groww Broker", "🏦 Connected Banks").forEachIndexed { idx, label ->
                    val isSel = activeConnectionTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) TealPrimary else Color.Transparent)
                            .clickable { activeConnectionTab = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else SoftGray
                        )
                    }
                }
            }
        }

        // Recharts Combined Asset Allocation Dashboard Component
        item {
            val stocksList by viewModel.stocks.collectAsStateWithLifecycle()
            
            // Calculate bank savings balance
            val bankBalancesMap = mapOf(
                "HDFC Bank" to 245000.0,
                "ICICI Bank" to 185000.0,
                "SBI Bank" to 310000.0,
                "Axis Bank" to 120000.0,
                "Kotak Bank" to 165000.0,
                "Canara Bank" to 95000.0
            )
            val bankCashSum = if (connectedBanks.isEmpty()) {
                45000.0 // Base cash reserve for non-linked status
            } else {
                connectedBanks.sumOf { bankBalancesMap[it] ?: 100000.0 }
            }
            
            val stocksSum = if (isGrowwConnected) {
                stocksList.sumOf { it.quantity * it.currentPrice }
            } else {
                0.0 // 0 if broker is not connected
            }
            
            val totalCombinedAssets = bankCashSum + stocksSum
            val bankPercent = if (totalCombinedAssets > 0.0) (bankCashSum / totalCombinedAssets) else 0.0
            val stocksPercent = if (totalCombinedAssets > 0.0) (stocksSum / totalCombinedAssets) else 0.0

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                    .testTag("recharts_asset_allocation_dashboard")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📊", fontSize = 16.sp)
                            Column {
                                Text("RECHARTS ASSET ALLOCATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary, letterSpacing = 0.5.sp)
                                Text("Dynamic Core Net Worth Segmentor", fontSize = 8.sp, color = SoftGray)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TealPrimary.copy(alpha = 0.15f))
                                .clickable { viewModel.triggerGlobalSync() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Sync Now", tint = TealPrimary, modifier = Modifier.size(10.dp))
                                Text("REFRESH RUN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                            }
                        }
                    }
                    
                    HorizontalDivider(color = BorderSlate)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL CONNECTED LIQUID NET WORTH", fontSize = 8.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            Text(
                                text = "₹" + String.format(Locale.US, "%,.2f", totalCombinedAssets),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = OffWhite
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isGrowwConnected && connectedBanks.isNotEmpty()) MintAccent.copy(alpha = 0.1f) else SoftGray.copy(alpha = 0.05f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "STABLE: " + if (stocksPercent in 0.10..0.45) "BALANCED 🛡️" else "LIQUID ASSETS CASH-HEAVY 🏛️",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isGrowwConnected && connectedBanks.isNotEmpty()) MintAccent else SoftGray
                            )
                        }
                    }
                    
                    // Donut/Pie visualizer using Canvas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(90.dp)) {
                                val strokeWidthPx = 14.dp.toPx()
                                val rectSize = size.copy(width = size.width - strokeWidthPx, height = size.height - strokeWidthPx)
                                val topLeftOffset = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
                                
                                val bankSweep = (bankPercent * 360.0).toFloat()
                                val stocksSweep = (stocksPercent * 360.0).toFloat()
                                
                                // Draw Bank Cash sector
                                drawArc(
                                    color = MintAccent,
                                    startAngle = -90f,
                                    sweepAngle = bankSweep,
                                    useCenter = false,
                                    topLeft = topLeftOffset,
                                    size = rectSize,
                                    style = Stroke(
                                        width = strokeWidthPx,
                                        cap = StrokeCap.Round
                                    )
                                )
                                
                                // Draw Stocks sector (only if non-zero)
                                if (stocksSweep > 0.1f) {
                                    drawArc(
                                        color = TealPrimary,
                                        startAngle = -90f + bankSweep,
                                        sweepAngle = stocksSweep,
                                        useCenter = false,
                                        topLeft = topLeftOffset,
                                        size = rectSize,
                                        style = Stroke(
                                            width = strokeWidthPx,
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                            }
                            
                            // Center label
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("STOCKS", fontSize = 8.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.US, "%.0f%%", stocksPercent * 100),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TealPrimary
                                )
                            }
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1.8f)
                                .padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Legend: Bank Core Savings
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MintAccent))
                                    Text("Bank Savings Ledger", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(String.format(Locale.US, "%.1f%%", bankPercent * 100.0), fontSize = 10.sp, color = MintAccent, fontWeight = FontWeight.Black)
                                }
                                Text("₹" + String.format(Locale.US, "%,.0f", bankCashSum), fontSize = 12.sp, color = OffWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp))
                                if (connectedBanks.isNotEmpty()) {
                                    Text("Linked: ${connectedBanks.joinToString(", ")}", fontSize = 8.sp, color = SoftGray, modifier = Modifier.padding(start = 14.dp), maxLines = 1)
                                } else {
                                    Text("Linked: None (Sandbox Base Cash)", fontSize = 8.sp, color = AmberWarning, modifier = Modifier.padding(start = 14.dp))
                                }
                            }
                            
                            // Legend: Groww Stocks
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealPrimary))
                                    Text("Groww Investments", fontSize = 10.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(String.format(Locale.US, "%.1f%%", stocksPercent * 100.0), fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.Black)
                                }
                                Text("₹" + String.format(Locale.US, "%,.0f", stocksSum), fontSize = 12.sp, color = OffWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp))
                                if (isGrowwConnected) {
                                    Text("Linked active: ${stocksList.size} stock holding types", fontSize = 8.sp, color = MintAccent, modifier = Modifier.padding(start = 14.dp))
                                } else {
                                    Text("Status: Disconnected / Not Linked", fontSize = 8.sp, color = SoftGray, modifier = Modifier.padding(start = 14.dp))
                                }
                            }
                        }
                    }
                    
                    // Stacked Bar allocation visualizer bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("RECHARTS STACKED DENSITY RATIO", fontSize = 8.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                            Text("CASH 🏛️ VS STOCKS 📈", fontSize = 8.sp, color = SoftGray, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderSlate)
                        ) {
                            if (bankPercent > 0.0) {
                                Box(
                                    modifier = Modifier
                                        .weight(bankPercent.toFloat().coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(MintAccent)
                                )
                            }
                            if (stocksPercent > 0.0) {
                                Box(
                                    modifier = Modifier
                                        .weight(stocksPercent.toFloat().coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(TealPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Connection Synchronization Status Banner
        if (isConnectionSyncing) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TealPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TealPrimary, strokeWidth = 2.dp)
                        Column {
                            Text("ESTABLISHING ENCRYPTED FINANCIAL TUNNEL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TealPrimary, letterSpacing = 1.sp)
                            Text("Connecting via secure direct banking API nodes...", fontSize = 11.sp, color = OffWhite)
                        }
                    }
                }
            }
        }

        // main Connection Body
        if (activeConnectionTab == 0) {
            // Groww Broker Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                        .testTag("groww_broker_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Star, contentDescription = "Groww", tint = TealPrimary, modifier = Modifier.size(24.dp))
                                Column {
                                    Text("GROWW INVESTMENTS LINK", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OffWhite)
                                    Text("Brokerage Portfolio Synchronizer", fontSize = 10.sp, color = SoftGray)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isGrowwConnected) MintAccent.copy(alpha = 0.15f) else SoftGray.copy(alpha = 0.08f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isGrowwConnected) "CONNECTED" else "NOT LINKED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGrowwConnected) MintAccent else SoftGray
                                )
                            }
                        }

                        HorizontalDivider(color = BorderSlate)

                        if (!isGrowwConnected) {
                            Text("Link your Groww Account to automatically import your actual stocks, mutual funds, and cash balances into FineNest without manual ledger logging.", fontSize = 11.sp, color = SoftGray, lineHeight = 15.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            if (!growwShowOtpField) {
                                OutlinedTextField(
                                    value = growwClientId,
                                    onValueChange = { growwClientId = it },
                                    label = { Text("Groww Client ID (e.g., GRW9082)", fontSize = 11.sp, color = SoftGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = OffWhite),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                                )

                                OutlinedTextField(
                                    value = growwMobile,
                                    onValueChange = { growwMobile = it },
                                    label = { Text("Registered Mobile Number (+91)", fontSize = 11.sp, color = SoftGray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = OffWhite),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                                )

                                Button(
                                    onClick = {
                                        if (growwClientId.isNotBlank() && growwMobile.length >= 10) {
                                            growwShowOtpField = true
                                            viewModel.showNotification("🔑 OTP verification passcode initiated to secondary cell network!")
                                        } else {
                                            viewModel.showNotification("❌ Please enter a valid Groww Client ID and 10-digit mobile number.")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("INITIATE GROWW HANDSHAKE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BorderSlate.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("🔒 OTP REQUEST SENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                        Text("Enter the 6-digit verification code sent to your registered mobile number for Groww Client ID: $growwClientId.", fontSize = 11.sp, color = OffWhite, lineHeight = 14.sp)
                                    }
                                }

                                OutlinedTextField(
                                    value = growwOtpCode,
                                    onValueChange = { growwOtpCode = it },
                                    label = { Text("6-Digit OTP Code", fontSize = 11.sp, color = SoftGray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = OffWhite),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = BorderSlate)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { growwShowOtpField = false; growwOtpCode = "" },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite)
                                    ) {
                                        Text("BACK")
                                    }

                                    Button(
                                        onClick = {
                                            if (growwOtpCode.length >= 4) {
                                                viewModel.connectGroww(growwClientId, growwMobile) { success ->
                                                    if (success) {
                                                        growwShowOtpField = false
                                                        growwOtpCode = ""
                                                        growwClientId = ""
                                                        growwMobile = ""
                                                    }
                                                }
                                            } else {
                                                viewModel.showNotification("❌ Invalid OTP. Enter correct digits.")
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(46.dp)
                                    ) {
                                        Text("VERIFY & SYNC ASSETS", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        } else {
                            // Already Connected Portfolio Summary
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Sync Node Status", fontSize = 11.sp, color = SoftGray)
                                    Text("ONLINE 🟢 (Active Sync)", fontSize = 11.sp, color = MintAccent, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Verification Method", fontSize = 11.sp, color = SoftGray)
                                    Text(growwConnectionMethod, fontSize = 11.sp, color = OffWhite, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Unified Portfolio Link", fontSize = 11.sp, color = SoftGray)
                                    Text("Groww Broker Node ID #28901", fontSize = 11.sp, color = OffWhite)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { viewModel.disconnectGroww() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCoral),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .border(1.dp, AlertCoral.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                                ) {
                                    Text("REVOKE GROWW WALLET PERMISSION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // connected banking profiles
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                        .testTag("bank_connections_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Star, contentDescription = "Bank", tint = TealPrimary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("SECURE COGNITIVE BANK SYNC", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OffWhite)
                                Text("Automated Account Ledger Pull", fontSize = 10.sp, color = SoftGray)
                            }
                        }

                        HorizontalDivider(color = BorderSlate)

                        val supportedBanks = listOf("HDFC Bank", "ICICI Bank", "SBI Bank", "Axis Bank", "Kotak Bank", "Canara Bank")

                        Text("Securely subscribe to auto-sync transactions directly into your bank balances ledger. Zero manual entry.", fontSize = 11.sp, color = SoftGray, lineHeight = 15.sp)

                        Text("AVAILABLE BANKS TO INTEGRATE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftGray)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            supportedBanks.forEach { bank ->
                                val isLinked = connectedBanks.contains(bank)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BorderSlate.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .border(0.5.dp, if (isLinked) MintAccent.copy(alpha = 0.5f) else BorderSlate, RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isLinked) {
                                                viewModel.disconnectBank(bank)
                                            } else {
                                                viewModel.connectBank(bank, "NetBanking", "9999999999") {}
                                            }
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(if (isLinked) "🏦" else "⚡", fontSize = 16.sp)
                                        Text(bank, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OffWhite)
                                    }

                                    if (isLinked) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("MUTUAL LOCK 🔒", fontSize = 9.sp, color = MintAccent, fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = { viewModel.disconnectBank(bank) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Disconnect ID", tint = AlertCoral, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    } else {
                                        Text("LINK NOW ›", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStockPortfolioComponent(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val isGrowwConnected by viewModel.isGrowwConnected.collectAsStateWithLifecycle()

    val totalInvested = stocks.sumOf { it.investedAmount }
    val totalCurrentValue = stocks.sumOf { it.currentValue }
    val totalProfitLoss = totalCurrentValue - totalInvested
    val totalReturnPercent = if (totalInvested > 0.0) (totalProfitLoss / totalInvested) * 100.0 else 0.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
            .testTag("dashboard_stock_portfolio_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📈", fontSize = 16.sp)
                    Column {
                        Text(
                            text = "FINENEST STOCK PORTFOLIO LEDGER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Manual Standalone Stock Holdings Tracker",
                            fontSize = 8.sp,
                            color = SoftGray
                        )
                    }
                }

                // Small modern connection status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SoftGray.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "MANUAL LEDGER SYSTEM",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGray
                    )
                }
            }

            HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

            // Portfolio values highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL EQUITY HOLDINGS VALUE",
                        fontSize = 8.sp,
                        color = SoftGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹" + String.format(Locale.US, "%,.2f", totalCurrentValue),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = OffWhite
                    )
                }

                // Profit loss summary
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NET UNREALIZED GAIN/LOSS",
                        fontSize = 8.sp,
                        color = SoftGray,
                        fontWeight = FontWeight.Bold
                    )
                    val sign = if (totalProfitLoss >= 0.0) "+" else ""
                    val profitColor = if (totalProfitLoss >= 0.0) MintAccent else AlertCoral
                    Text(
                        text = "$sign₹" + String.format(Locale.US, "%,.1f", totalProfitLoss) + " ($sign" + String.format(Locale.US, "%.2f", totalReturnPercent) + "%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = profitColor
                    )
                }
            }

            // Interactive list/table headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateDark, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "TICKER", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SoftGray, modifier = Modifier.weight(1.2f))
                Text(text = "QTY / AVG", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SoftGray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(text = "LIVE VALUE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SoftGray, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                Text(text = "UNREALIZED P&L", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SoftGray, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
            }

            // Actual holdings
            if (stocks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stocks.forEach { stock ->
                        val profitColor = if (stock.profitLoss >= 0.0) MintAccent else AlertCoral
                        val profitSign = if (stock.profitLoss >= 0.0) "+" else ""
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Ticker name
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = stock.symbol,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OffWhite
                                )
                                Text(
                                    text = stock.stockName,
                                    fontSize = 7.sp,
                                    color = SoftGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Column 2: Qty / average price
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${stock.quantity} shares",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OffWhite
                                )
                                Text(
                                    text = "Avg: ₹" + String.format(Locale.US, "%,.0f", stock.buyPrice),
                                    fontSize = 8.sp,
                                    color = SoftGray
                                )
                            }

                            // Column 3: Live current value
                            Column(
                                modifier = Modifier.weight(1.2f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "₹" + String.format(Locale.US, "%,.0f", stock.currentValue),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OffWhite
                                )
                                Text(
                                    text = "Live: ₹" + String.format(Locale.US, "%,.0f", stock.currentPrice),
                                    fontSize = 8.sp,
                                    color = SoftGray
                                )
                            }

                            // Column 4: Unrealized profit/loss
                            Column(
                                modifier = Modifier.weight(1.1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "$profitSign₹" + String.format(Locale.US, "%,.0f", stock.profitLoss),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = profitColor
                                )
                                Text(
                                    text = "$profitSign" + String.format(Locale.US, "%.2f%%", stock.returnPercent),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = profitColor
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active stock holdings in this ledger.", fontSize = 10.sp, color = SoftGray)
                }
            }
        }
    }
}

@Composable
fun AiSavingsInsightsDialog(
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .testTag("ai_savings_insights_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with custom 'i' icon banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💡", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "FINENEST AI ADVISER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Savings Tips & Core Guidelines",
                                fontSize = 9.sp,
                                color = SoftGray
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = SoftGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                // List of dynamic AI saving insights
                val guidelines = listOf(
                    Triple(
                        "📊 50-30-20 Rule Adaptation",
                        "Allocate 50% to essential liquid balances (bank accounts/cash), 30% to high-yield mutual fund SIPs, and 20% to physical ledger cash reserves for optimized friction-free liquidity.",
                        TealPrimary
                    ),
                    Triple(
                        "🏦 System Balance Sweep-in",
                        "Your current bank liquidity cushion is highly robust. Shift 15% of underperforming static account balances into automated sweeps or liquid debt assets to prevent inflationary erosion.",
                        ElectricBlue
                    ),
                    Triple(
                        "🎯 Target-Locked Micro Savings",
                        "Enable 'Auto Save Round-Ups' on active checkouts. Auto-transfers generated an average of ₹1,420 in passive saving last month. Highly recommended to keep goals automated.",
                        MintAccent
                    ),
                    Triple(
                        "📈 Equity Dollar-Cost Averaging",
                        "With active index performance up +9.47%, we advise routing a fixed 8% of weekend discretionary surpluses directly to blue-chip stocks like TATAMOTORS or RELIANCE on weekly dips.",
                        Color(0xFF9333EA)
                    )
                )

                guidelines.forEach { (title, desc, accent) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = desc,
                                fontSize = 10.sp,
                                color = OffWhite,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SyncNotificationsDialog(
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val isGrowwConnected by viewModel.isGrowwConnected.collectAsStateWithLifecycle()
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val connectedBanks by viewModel.connectedBanks.collectAsStateWithLifecycle()

    val syncedTxs = remember(transactions) {
        transactions.filter { it.notes?.contains("Automatically synced via Live Banking Node API") == true }.take(5)
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .testTag("sync_notifications_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔔", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "REAL-TIME LOGS FEED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Live Bank Sweeps & Groww Updates",
                                fontSize = 9.sp,
                                color = SoftGray
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = SoftGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                // Auto-added Last Bank Transactions Section
                Text(
                    text = "LATEST AUTO-ADDED TRANSACTIONS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftGray,
                    letterSpacing = 0.5.sp
                )

                if (syncedTxs.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        syncedTxs.forEach { tx ->
                            val isIncome = tx.type == "Income"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateDark, RoundedCornerShape(12.dp))
                                    .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isIncome) MintAccent else AlertCoral)
                                        )
                                        Text(
                                            text = tx.paymentMethod.uppercase(Locale.ROOT),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tx.payer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OffWhite
                                    )
                                    Text(
                                        text = "Live Sync • Node Statement API",
                                        fontSize = 8.sp,
                                        color = SoftGray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val sign = if (isIncome) "+" else "-"
                                    val color = if (isIncome) MintAccent else AlertCoral
                                    Text(
                                        text = "$sign₹" + String.format(Locale.US, "%,.2f", tx.amount),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = color
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MintAccent.copy(alpha = 0.1f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "AUTO-SWEPT",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MintAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Fallback representation with simulated banking nodes if the ledger has no synced logs yet
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Demo notification cards
                        val fallbackBanks = if (connectedBanks.isNotEmpty()) connectedBanks else listOf("HDFC Bank", "ICICI Bank")
                        fallbackBanks.forEachIndexed { index, bank ->
                            val demoDesc = if (index == 0) "UPI transfer auto-debit (🍔 Food & Grocery)" else "Interest Credited auto-inflow (📋 Dividend)"
                            val demoAmt = if (index == 0) 240.0 else 1250.0
                            val demoType = if (index == 0) "Expense" else "Income"
                            val isIncome = demoType == "Income"
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateDark, RoundedCornerShape(12.dp))
                                    .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isIncome) MintAccent else AlertCoral)
                                        )
                                        Text(
                                            text = bank.uppercase(Locale.ROOT),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = demoDesc,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OffWhite
                                    )
                                    Text(
                                        text = "Demo API Node Sync • Trigger 'SYNC NOW' on Home to import dynamically",
                                        fontSize = 8.sp,
                                        color = SoftGray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val sign = if (isIncome) "+" else "-"
                                    val color = if (isIncome) MintAccent else AlertCoral
                                    Text(
                                        text = "$sign₹" + String.format(Locale.US, "%,.2f", demoAmt),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = color
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SoftGray.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "READY TO RUN",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MintAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("OK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

