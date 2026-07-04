package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.InvestmentEntity
import com.example.data.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Background Layer with Ambient Neon Flows to match the Immersive UI theme
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF090A0C))
                ) {
                    // Accent violet-indigo glowing blurred spot at the top right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 100.dp, y = (-60).dp)
                            .size(320.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x266366F1), // Translucent Glowing Indigo
                                        Color(0x006366F1)
                                    )
                                )
                            )
                    )

                    // Secondary purple glow on the bottom-left area for high-fidelity immersion
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-100).dp, y = 100.dp)
                            .size(300.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x198B5CF6), // Translucent Glowing Purple
                                        Color(0x008B5CF6)
                                    )
                                )
                            )
                    )

                    ZenithFlowApp()
                }
            }
        }
    }
}

// Global Currency Formatter
fun formatRupiah(value: Double): String {
    val formatter = DecimalFormat("#,###")
    val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    formatter.decimalFormatSymbols = symbols
    return "Rp " + formatter.format(value)
}

// Global Epoch to Date Formatter
fun formatTimestamp(epoch: Long): String {
    val date = Date(epoch)
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(date)
}

enum class ActiveTab {
    Dashboard, Ledger, Portfolio
}

@Composable
fun ZenithFlowApp() {
    val viewModel: FinanceViewModel = viewModel()
    val isLocked by viewModel.isLocked
    val unlockError by viewModel.unlockError

    AnimatedContent(
        targetState = isLocked,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        },
        label = "AppLockTransition"
    ) { locked ->
        if (locked) {
            ZenithSecurityGate(
                errorMessage = unlockError,
                onPinAttempt = { viewModel.attemptUnlock(it) },
                onBiometricTap = { viewModel.bypassAuth() },
                onBypass = { viewModel.bypassAuth() }
            )
        } else {
            ZenithMainLayout(viewModel)
        }
    }
}

/**
 * Section 1: Security Lock Screen
 * PIN authentication gate representing custom biometric / passcode security wrapper
 */
@Composable
fun ZenithSecurityGate(
    errorMessage: String,
    onPinAttempt: (String) -> Boolean,
    onBiometricTap: () -> Unit,
    onBypass: () -> Unit
) {
    var pinState by remember { mutableStateOf("") }
    val maxPinLength = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Brand Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(EmergingEmerald, NebulaCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ZENITH FLOW",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "SECURE FINTECH VAULT",
                fontSize = 11.sp,
                color = MutedSlate,
                letterSpacing = 2.sp
            )
        }

        // Middle Input Circles & Error Indicators
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Masukkan PIN Anda",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Pin Circles
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until maxPinLength) {
                    val filled = i < pinState.length
                    val scale by animateFloatAsState(if (filled) 1.2f else 1.0f, label = "circleScale")
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(if (filled) 45f else 0f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (filled) {
                                    Brush.linearGradient(listOf(EmergingEmerald, NebulaCyan))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = ExpenseCrimson,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Gunakan PIN bawaan: 1234 atau klik ikon Biometrik",
                    color = MutedSlate,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Custom Numeric Dialpad
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("FP", "0", "DEL")
            )

            for (row in keys) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    for (key in row) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.8f) // elegant widescreen dial keys
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (key == "DEL" || key == "FP") Color.Transparent
                                    else Color(0xFF1E212E)
                                )
                                .clickable {
                                    when (key) {
                                        "DEL" -> {
                                            if (pinState.isNotEmpty()) pinState = pinState.dropLast(1)
                                        }
                                        "FP" -> {
                                            onPinAttempt("1234") // simulate successful biometric signature
                                        }
                                        else -> {
                                            if (pinState.length < maxPinLength) {
                                                pinState += key
                                                if (pinState.length == maxPinLength) {
                                                    val success = onPinAttempt(pinState)
                                                    if (!success) {
                                                        pinState = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (key) {
                                "DEL" -> {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Delete Icon",
                                        tint = MutedSlate
                                    )
                                }
                                "FP" -> {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "Biometrics Fingerprint",
                                        tint = NebulaCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = key,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onBypass,
                modifier = Modifier.testTag("auth_bypass_btn")
            ) {
                Text("Bypass Keamanan PIN (Pengujian)", color = EmergingEmerald, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Section 2: Main Layout
 * High-end translucent navigation layout housing all core finance features
 */
@Composable
fun ZenithMainLayout(viewModel: FinanceViewModel) {
    var activeTab by remember { mutableStateOf(ActiveTab.Dashboard) }
    var showAuthSheet by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var actionDialogType by remember { mutableStateOf("LEDGER") } // "LEDGER" or "PORTFOLIO"

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Color.Transparent, // Let global gradient shine through
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xE6111318)) // `#111318` at 90% opacity
                    .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(26.dp)) // subtle white/5 border
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(74.dp)
                ) {
                    NavigationBarItem(
                        selected = activeTab == ActiveTab.Dashboard,
                        onClick = { activeTab = ActiveTab.Dashboard },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color(0x26FFFFFF), // Subtle high-end bg-white/15 capsule highlight
                            unselectedIconColor = Color.White.copy(alpha = 0.4f), // 40% opacity corresponding to design
                            unselectedTextColor = Color.White.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("nav_dashboard_tab")
                    )

                    NavigationBarItem(
                        selected = activeTab == ActiveTab.Ledger,
                        onClick = { activeTab = ActiveTab.Ledger },
                        icon = { Icon(Icons.Default.List, contentDescription = "Ledger") },
                        label = { Text("Ledger", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color(0x26FFFFFF), // Dynamic capsule highlight
                            unselectedIconColor = Color.White.copy(alpha = 0.4f),
                            unselectedTextColor = Color.White.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("nav_ledger_tab")
                    )

                    NavigationBarItem(
                        selected = activeTab == ActiveTab.Portfolio,
                        onClick = { activeTab = ActiveTab.Portfolio },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Portfolio") },
                        label = { Text("Portfolio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color(0x26FFFFFF), // Dynamic capsule highlight
                            unselectedIconColor = Color.White.copy(alpha = 0.4f),
                            unselectedTextColor = Color.White.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("nav_portfolio_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Premium Header with User Session & Live stock synchronizer
            ZenithAppHeader(
                viewModel = viewModel,
                onProfileClick = { showAuthSheet = true }
            )

            // Primary screen content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        slideInHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            initialOffsetX = { if (targetState.ordinal > initialState.ordinal) it else -it }
                        ) + fadeIn() togetherWith slideOutHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -it else it }
                        ) + fadeOut()
                    },
                    label = "TabTransition"
                ) { currentTab ->
                    when (currentTab) {
                        ActiveTab.Dashboard -> ScreenDashboard(
                            viewModel = viewModel,
                            onAddLedgerClick = {
                                actionDialogType = "LEDGER"
                                showAddDialog = true
                            },
                            onAddPortfolioClick = {
                                actionDialogType = "PORTFOLIO"
                                showAddDialog = true
                            }
                        )
                        ActiveTab.Ledger -> ScreenLedger(viewModel)
                        ActiveTab.Portfolio -> ScreenPortfolio(viewModel)
                    }
                }
            }
        }

        // Firebase Auth overlay Simulation popup
        if (showAuthSheet) {
            FirebaseSimulatedAuthDialog(
                viewModel = viewModel,
                onDismiss = { showAuthSheet = false }
            )
        }

        // Unified Transaction & Item Placement Dialog
        if (showAddDialog) {
            UnifiedEntryDialog(
                mode = actionDialogType,
                viewModel = viewModel,
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

/**
 * Top branding navigation header displaying connection signatures and syncing status
 */
@Composable
fun ZenithAppHeader(viewModel: FinanceViewModel, onProfileClick: () -> Unit) {
    val isLoggedIn by viewModel.isLoggedIn
    val userEmail by viewModel.userEmail
    val userDisplayName by viewModel.userDisplayName
    val isRefreshingStocks by viewModel.isRefreshingStocks

    val rotationTransition = rememberInfiniteTransition(label = "SyncRotate")
    val rotationAngle by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Branding Avatar Block
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onProfileClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GlowIndigo, GlowPurple)
                        )
                    )
                    .border(1.dp, Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isLoggedIn) {
                    Text(
                        text = userDisplayName.take(2).uppercase(Locale.getDefault()),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        text = "JD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "WELCOME BACK",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (isLoggedIn) userDisplayName else "Zenith Flow",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 140.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLoggedIn) EmergingEmerald else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLoggedIn) "Firebase Active" else "Offline Local",
                        fontSize = 10.sp,
                        color = if (isLoggedIn) EmergingEmerald else MutedSlate,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Live Market Refresh & Relock Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Relock lock secure button
            IconButton(
                onClick = { viewModel.isLocked.value = true },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF161924))
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Application",
                    tint = MutedSlate,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Sync Stocks Pull button
            IconButton(
                onClick = { viewModel.refreshStocks() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF161924)),
                modifier = Modifier.testTag("refresh_stocks_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Market Tickers",
                    tint = if (isRefreshingStocks) NebulaCyan else EmergingEmerald,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isRefreshingStocks) rotationAngle else 0f)
                )
            }
        }
    }
}

/**
 * Screen 1 Dashboard Component
 */
@Composable
fun ScreenDashboard(
    viewModel: FinanceViewModel,
    onAddLedgerClick: () -> Unit,
    onAddPortfolioClick: () -> Unit
) {
    val totalWealth by viewModel.totalWealth.collectAsStateWithLifecycle()
    val cashBalance by viewModel.cashBalance.collectAsStateWithLifecycle()
    val portfolioValue by viewModel.portfolioValue.collectAsStateWithLifecycle()
    val portfolioCostBasis by viewModel.portfolioCostBasis.collectAsStateWithLifecycle()

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-end Glassmorphic Wealth Card matching Immersive UI Design Theme
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0x0CFFFFFF)) // Glassmorphic 5-6% translucent white surface
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(32.dp)) // subtle glass border (white/10)
            ) {
                // Top-right ambient glowing violet bloom
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-30).dp)
                        .size(130.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x2E6366F1), // indigo-500/18
                                    Color(0x006366F1)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "TOTAL NET WORTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC7D2FE).copy(alpha = 0.7f), // indigo-200/70
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupiah(totalWealth),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-1.5).sp,
                        modifier = Modifier.testTag("total_wealth_text")
                    )

                    // Compact Trend Badge from design HTML
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x3310B981)) // bg-emerald-500/20
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "+12.4% TOTAL P/L",
                                color = Color(0xFF34D399), // emerald-400
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vs last month",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Asset allocation Split layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NebulaCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kas Ledger", fontSize = 11.sp, color = MutedSlate)
                            }
                            Text(
                                text = formatRupiah(cashBalance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmergingEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Portofolio Saham", fontSize = 11.sp, color = MutedSlate)
                            }
                            Text(
                                text = formatRupiah(portfolioValue),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Proportion Bar
                    val totalSum = (cashBalance + portfolioValue).coerceAtLeast(100.0)
                    val cashFraction = (cashBalance / totalSum).toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x33FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(cashFraction.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(NebulaCyan)
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - cashFraction).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(EmergingEmerald)
                        )
                    }
                }
            }
        }

        // Quick Input triggers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cash transaction ledger quick insert
                Button(
                    onClick = onAddLedgerClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Plus",
                        tint = NebulaCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Transaksi Baru", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Portfolio stock positioning quick asset add
                Button(
                    onClick = onAddPortfolioClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Trending",
                        tint = EmergingEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Beli Saham", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Custom drawn Canvas graphical chart stating simulated future wealth compounding curve
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = GlassCardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PROYEKSI KOMPOSIT ASET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedSlate,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Curva compounding historikal (Simulatif)",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sparkline projection canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    ) {
                        val path = androidx.compose.ui.graphics.Path()
                        val points = listOf(0.1f, 0.25f, 0.2f, 0.45f, 0.38f, 0.65f, 0.85f, 1.0f)
                        val stepX = size.width / (points.size - 1)

                        path.moveTo(0f, size.height * (1f - points[0]))
                        for (i in 1 until points.size) {
                            val targetX = i * stepX
                            val targetY = size.height * (1f - points[i])
                            // draw cubic curve for organic feeling
                            val prevX = (i - 1) * stepX
                            val prevY = size.height * (1f - points[i - 1])
                            path.cubicTo(
                                (prevX + targetX) / 2f, prevY,
                                (prevX + targetX) / 2f, targetY,
                                targetX, targetY
                            )
                        }

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(NebulaCyan, EmergingEmerald)),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw dots at key nodes
                        for (i in points.indices) {
                            val cx = i * stepX
                            val cy = size.height * (1f - points[i])
                            drawCircle(
                                color = if (i == points.size - 1) EmergingEmerald else NebulaCyan,
                                radius = if (i == points.size - 1) 5.dp.toPx() else 3.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(cx, cy)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jan", fontSize = 10.sp, color = MutedSlate)
                        Text("Mar", fontSize = 10.sp, color = MutedSlate)
                        Text("Mei", fontSize = 10.sp, color = MutedSlate)
                        Text("Kemarin", fontSize = 10.sp, color = MutedSlate)
                        Text("Real-time", fontSize = 10.sp, color = EmergingEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent transaction items
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AKTIVITAS TERBARU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedSlate,
                    letterSpacing = 1.5.sp
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada transaksi di Ledger Anda.", color = MutedSlate, fontSize = 13.sp)
                }
            }
        } else {
            items(transactions.take(3)) { tx ->
                DashboardTransactionItem(tx = tx, onDelete = { viewModel.deleteTransaction(tx.id) })
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

/**
 * Screen 2: Ledger transaction notebook
 */
@Composable
fun ScreenLedger(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val cashBalance by viewModel.cashBalance.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("SEMUA") } // "SEMUA", "INCOME", "EXPENSE"

    // Custom Category dialog states
    var showCategoryDialog by remember { mutableStateOf(false) }

    val filteredTransactions = when (selectedFilter) {
        "PEMASUKAN" -> transactions.filter { it.type == "INCOME" }
        "PENGELUARAN" -> transactions.filter { it.type == "EXPENSE" }
        else -> transactions
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            // Embedded customized categorization manager trigger
            ExtendedFloatingActionButton(
                onClick = { showCategoryDialog = true },
                containerColor = Color(0xFF1E212D),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Manage Categories") },
                text = { Text("Kategori", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("manage_categories_btn")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Ledger Title Card with high-fidelity glassmorphism
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassCardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SALDO KAS SAAT INI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedSlate,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupiah(cashBalance),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NebulaCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NebulaCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Ledger logo",
                            tint = NebulaCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtering Tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161922))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val filters = listOf("SEMUA", "PEMASUKAN", "PENGELUARAN")
                for (filt in filters) {
                    val isActive = selectedFilter == filt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) EmergingEmerald else Color.Transparent)
                            .clickable { selectedFilter = filt }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filt,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else MutedSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction items List View
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Empty Ledger",
                            tint = MutedSlate.copy(0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada aktivitas pendaftaran.",
                            color = MutedSlate,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Gunakan tombol (+) Dashboard untuk mendaftar.",
                            color = MutedSlate.copy(0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        LedgerRowItem(
                            tx = tx,
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            }
        }

        // Add customizable categories pop-up editor
        if (showCategoryDialog) {
            CustomizeCategoryDialog(
                viewModel = viewModel,
                onDismiss = { showCategoryDialog = false }
            )
        }
    }
}

/**
 * Screen 3: Stock Portfolio performance engine
 */
@Composable
fun ScreenPortfolio(viewModel: FinanceViewModel) {
    val investments by viewModel.investments.collectAsStateWithLifecycle()
    val portfolioValue by viewModel.portfolioValue.collectAsStateWithLifecycle()
    val costBasis by viewModel.portfolioCostBasis.collectAsStateWithLifecycle()

    val unrealizedPL = portfolioValue - costBasis
    val plPercent = if (costBasis > 0) (unrealizedPL / costBasis) * 100.0 else 0.0
    val isProfit = unrealizedPL >= 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // High fidelity Portfolio Metric header with complete Glassmorphism
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassCardBg),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "VALUASI PORTOFOLIO SAHAM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedSlate,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRupiah(portfolioValue),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmergingEmerald
                )

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = BorderGlass)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Modal Pembelian", fontSize = 11.sp, color = MutedSlate)
                        Text(
                            text = formatRupiah(costBasis),
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Unrealized Profit/Loss", fontSize = 11.sp, color = MutedSlate)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isProfit) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Arrow",
                                tint = if (isProfit) EmergingEmerald else ExpenseCrimson,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isProfit) "+" else ""}${String.format("%.2f", plPercent)}%",
                                fontSize = 14.sp,
                                color = if (isProfit) EmergingEmerald else ExpenseCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ALOKASI SAHAM SAYA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSlate,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Harga Terkini update otomatis via Yahoo API",
                fontSize = 10.sp,
                color = MutedSlate.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (investments.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Empty portfolio",
                        tint = MutedSlate.copy(0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kosong. Belum ada saham terdaftar.",
                        color = MutedSlate,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Gunakan Dashboard untuk membeli saham pertama Anda.",
                        color = MutedSlate.copy(0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(investments, key = { it.id }) { item ->
                    PortfolioRowItem(
                        item = item,
                        onDelete = { viewModel.deleteInvestment(item.id) }
                    )
                }
            }
        }
    }
}

/**
 * Recycler component for Dashboard activities (ledger only)
 */
@Composable
fun DashboardTransactionItem(tx: TransactionEntity, onDelete: () -> Unit) {
    val isIncome = tx.type == "INCOME"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassCardBg)
            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIncome) EmergingEmerald.copy(0.2f) else ExpenseCrimson.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Flow Tipe",
                        tint = if (isIncome) EmergingEmerald else ExpenseCrimson
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.notes.ifEmpty { tx.category },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = tx.category,
                        fontSize = 11.sp,
                        color = MutedSlate
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${formatRupiah(tx.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) EmergingEmerald else ExpenseCrimson
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MutedSlate.copy(0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Recycler component for Ledger List Screen
 */
@Composable
fun LedgerRowItem(tx: TransactionEntity, onDelete: () -> Unit) {
    val isIncome = tx.type == "INCOME"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassCardBg)
            .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isIncome) EmergingEmerald.copy(0.15f) else ExpenseCrimson.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.Check else Icons.Default.Delete,
                        contentDescription = "Indicator Ledger",
                        tint = if (isIncome) EmergingEmerald else ExpenseCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.notes.ifEmpty { "Tanpa Deskripsi" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.category,
                            fontSize = 11.sp,
                            color = NebulaCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${formatTimestamp(tx.timestamp)}",
                            fontSize = 10.sp,
                            color = MutedSlate
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${formatRupiah(tx.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) EmergingEmerald else ExpenseCrimson
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Tx",
                        tint = MutedSlate.copy(0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Recycler component for Stock Portfolio lists containing math calculations
 */
@Composable
fun PortfolioRowItem(item: InvestmentEntity, onDelete: () -> Unit) {
    val totalShares = item.lotCount * 100
    val costTotal = totalShares * item.averageBuyPrice
    val marketValue = totalShares * item.lastFetchedPrice
    val gainLoss = marketValue - costTotal
    val pLPercent = if (costTotal > 0) (gainLoss / costTotal) * 100.0 else 0.0
    val isGain = gainLoss >= 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassCardBg)
            .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header: Symbol & Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmergingEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.ticker.take(2).uppercase(),
                            color = EmergingEmerald,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${item.ticker.uppercase()}.JK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${item.lotCount} Lot (${totalShares} lembar)",
                            fontSize = 11.sp,
                            color = MutedSlate
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Stock Position",
                        tint = MutedSlate.copy(0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = BorderGlass.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Details pricing parameters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("HARGA COST (AVG)", fontSize = 10.sp, color = MutedSlate)
                    Text(
                        text = formatRupiah(item.averageBuyPrice),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HARGA PASAR API", fontSize = 10.sp, color = MutedSlate)
                    Text(
                        text = formatRupiah(item.lastFetchedPrice),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebulaCyan
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("NILAI PASAR TOTAL", fontSize = 10.sp, color = MutedSlate)
                    Text(
                        text = formatRupiah(marketValue),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Yield metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isGain) EmergingEmerald.copy(0.12f) else ExpenseCrimson.copy(0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Keuntungan/Kerugian", fontSize = 11.sp, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGain) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "gain arrow",
                        tint = if (isGain) EmergingEmerald else ExpenseCrimson,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatRupiah(gainLoss)} (${if (isGain) "+" else ""}${String.format("%.2f", pLPercent)}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isGain) EmergingEmerald else ExpenseCrimson
                    )
                }
            }
        }
    }
}

/**
 * Sheet modal popup illustrating Firebase Auth flow integrations securely
 */
@Composable
fun FirebaseSimulatedAuthDialog(viewModel: FinanceViewModel, onDismiss: () -> Unit) {
    val isLoggedIn by viewModel.isLoggedIn
    val userEmail by viewModel.userEmail
    val userDisplayName by viewModel.userDisplayName

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }

    var isRegisterState by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131520)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .imePadding()
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmergingEmerald.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Auth Logo",
                        tint = EmergingEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Autentikasi Firebase",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Disinkronisasikan oleh Google Identity Workspace",
                    fontSize = 11.sp,
                    color = MutedSlate
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoggedIn) {
                    // Profile Active state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C1E2B))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("PROFIL SAYA", fontSize = 10.sp, color = MutedSlate, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Nama: $userDisplayName", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Email: $userEmail", color = MutedSlate, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmergingEmerald))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Token Sesi Firebase Aktif", color = EmergingEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.logout()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Logout / Keluar Sesi", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Action input state
                    TextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Alamat Email") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E212E),
                            unfocusedContainerColor = Color(0xFF1E212E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password (Min 6 karakter)") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E212E),
                            unfocusedContainerColor = Color(0xFF1E212E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (isRegisterState) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = displayNameInput,
                            onValueChange = { displayNameInput = it },
                            label = { Text("Nama Lengkap") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E212E),
                                unfocusedContainerColor = Color(0xFF1E212E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.loginWithGoogle(
                                email = emailInput.ifEmpty { "syahrifatih66@gmail.com" },
                                name = if (isRegisterState) displayNameInput.ifEmpty { "Syahri Fatih" } else "Syahri Fatih"
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergingEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (isRegisterState) "Daftar Akun Baru" else "Masuk Akun", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Google button
                    OutlinedButton(
                        onClick = {
                            viewModel.loginWithGoogle("syahrifatih66@gmail.com", "Syahri Fatih")
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "GM Logo", tint = NebulaCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Masuk via Akun Google", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { isRegisterState = !isRegisterState }) {
                        Text(
                            text = if (isRegisterState) "Sudah punya akun? Masuk" else "Masuk sebagai Member baru? Daftar",
                            color = NebulaCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onDismiss) {
                    Text("Tutup", color = MutedSlate, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Custom Customizable categorization Dialog Panel
 */
@Composable
fun CustomizeCategoryDialog(viewModel: FinanceViewModel, onDismiss: () -> Unit) {
    var newCategoryText by remember { mutableStateOf("") }
    val categories = viewModel.customCategories

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131520)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .imePadding()
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Atur Kategori Ledger",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "User-customizable category list",
                    fontSize = 11.sp,
                    color = MutedSlate
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List of active categories
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1E2B))
                        .padding(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(categories.toList()) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NebulaCyan)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(cat, color = Color.White, fontSize = 14.sp)
                                }
                                // Protect default categories from being deleted
                                if (cat !in listOf("Makan", "Investasi", "Gaji", "Lainnya")) {
                                    IconButton(
                                        onClick = { categories.remove(cat) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Del Cat",
                                            tint = ExpenseCrimson,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input to add a category
                TextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    label = { Text("Nama Kategori Baru") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E212E),
                        unfocusedContainerColor = Color(0xFF1E212E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("new_category_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (newCategoryText.trim().isNotEmpty()) {
                            viewModel.addCategory(newCategoryText)
                            newCategoryText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergingEmerald),
                    modifier = Modifier.fillMaxWidth().testTag("add_category_save_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tambahkan Kategori", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Selesai", color = MutedSlate)
                }
            }
        }
    }
}

/**
 * Section 3: Universal Entry dialog representing cohesive insertion trigger
 */
@Composable
fun UnifiedEntryDialog(mode: String, viewModel: FinanceViewModel, onDismiss: () -> Unit) {
    if (mode == "LEDGER") {
        var txType by remember { mutableStateOf("EXPENSE") } // "INCOME" or "EXPENSE"
        var amountStr by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(viewModel.customCategories.firstOrNull() ?: "Lainnya") }
        var notesStr by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131520)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .imePadding()
                    .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Tambah Transaksi Ledger",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Type switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A1C29))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (txType == "EXPENSE") ExpenseCrimson else Color.Transparent)
                                .clickable { txType = "EXPENSE" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Pengeluaran",
                                color = if (txType == "EXPENSE") Color.White else MutedSlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (txType == "INCOME") EmergingEmerald else Color.Transparent)
                                .clickable { txType = "INCOME" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Pemasukan",
                                color = if (txType == "INCOME") Color.White else MutedSlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount Textfield
                    TextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Jumlah (Rupiah)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E212E),
                            unfocusedContainerColor = Color(0xFF1E212E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ledger_amount_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { dropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kategori: $selectedCategory", color = Color.White, fontSize = 14.sp)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = MutedSlate)
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E212E)).fillMaxWidth(0.7f)
                        ) {
                            viewModel.customCategories.toList().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        selectedCategory = cat
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deskripsi Textfield
                    TextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("Deskripsi / Catatan") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E212E),
                            unfocusedContainerColor = Color(0xFF1E212E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ledger_notes_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.addTransaction(txType, amt, selectedCategory, notesStr)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergingEmerald),
                        modifier = Modifier.fillMaxWidth().testTag("ledger_save_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan Transaksi", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Batal", color = MutedSlate)
                    }
                }
            }
        }
    } else {
        // Portfolio acquisition dialog input
        var tickerStr by remember { mutableStateOf("") }
        var lotStr by remember { mutableStateOf("") }
        var avgPriceStr by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131520)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .imePadding()
                    .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Catat Pembelian Saham baru",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Secukupnya masukkan ID emiten (cth: TLKM, BBRI)",
                        fontSize = 11.sp,
                        color = MutedSlate
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stock symbol Input
                    TextField(
                        value = tickerStr,
                        onValueChange = { tickerStr = it },
                        label = { Text("Kode Emiten (cth: BBCA)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E212E),
                            unfocusedContainerColor = Color(0xFF1E212E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("portfolio_ticker_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lot size Input
                    TextField(
                        value = lotStr,
                        onValueChange = { lotStr = it },
                        label = { Text("Jumlah Lot Unit (1 Lot = 100 Lembar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E212E),
                            unfocusedContainerColor = Color(0xFF1E212E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("portfolio_lot_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Avg buy price Input
                    TextField(
                        value = avgPriceStr,
                        onValueChange = { avgPriceStr = it },
                        label = { Text("Harga Beli Rata-Rata per Lembar") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E212E),
                            unfocusedContainerColor = Color(0xFF1E212E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("portfolio_avg_price_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val symbol = tickerStr.trim().uppercase()
                            val lots = lotStr.toIntOrNull() ?: 0
                            val averageP = avgPriceStr.toDoubleOrNull() ?: 0.0

                            if (symbol.isNotEmpty() && lots > 0 && averageP > 0) {
                                viewModel.addInvestment(symbol, lots, averageP)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergingEmerald),
                        modifier = Modifier.fillMaxWidth().testTag("portfolio_save_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan ke Portofolio", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Batal", color = MutedSlate)
                    }
                }
            }
        }
    }
}
