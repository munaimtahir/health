package com.vexel.passport

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.datastore.UserPreferences
import com.vexel.passport.core.security.PinVerifier
import com.vexel.passport.core.security.PinMaterialCipher
import com.vexel.passport.core.notifications.ReminderScheduler
import com.vexel.passport.core.designsystem.VexelHealthPassportTheme
import com.vexel.passport.feature.onboarding.OnboardingScreen
import com.vexel.passport.feature.dashboard.HomeScreen
import com.vexel.passport.feature.timeline.TimelineScreen
import com.vexel.passport.feature.reminders.RemindersScreen
import com.vexel.passport.feature.records.DocumentsScreen
import com.vexel.passport.feature.profile.ProfileScreen

private data class Destination(val route: String, val label: String, val icon: ImageVector)
private val destinations = listOf(
    Destination(Routes.HOME, "Home", Icons.Outlined.Home),
    Destination(Routes.RECORDS, "Records", Icons.Outlined.Event),
    Destination(Routes.PLAN, "Plan", Icons.Outlined.Schedule),
    Destination(Routes.VAULT, "Vault", Icons.Outlined.Folder),
    Destination(Routes.PROFILE, "Profile", Icons.Outlined.Person),
)
internal val primaryDestinationLabels: List<String> = destinations.map { it.label }

/** Centralized, stable string routes for the primary bottom-navigation destinations. */
internal object Routes {
    const val HOME = "home"
    const val RECORDS = "records"
    const val PLAN = "plan"
    const val VAULT = "vault"
    const val PROFILE = "profile"
}

@HiltViewModel
class PassportViewModel @Inject constructor(
    private val preferences: PreferencesStore,
    private val pinMaterialCipher: PinMaterialCipher,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    init {
        viewModelScope.launch { reminderScheduler.reconcile() }
    }
    private val pinVerifier = PinVerifier()
    val settings = preferences.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())
    
    private val _operationError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val operationError: kotlinx.coroutines.flow.StateFlow<String?> = _operationError
    
    fun dismissOperationError() { _operationError.value = null }
    fun completeOnboarding() = viewModelScope.launch { preferences.setOnboardingComplete(true) }

    fun verifyPin(pin: String, prefs: UserPreferences): Boolean {
        if (!prefs.lockEnabled) return true
        return runCatching { pinVerifier.matches(pin.toCharArray(), pinMaterialCipher.decrypt(prefs.pinMaterial)) }.getOrDefault(false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VexelHealthPassportApp(viewModel: PassportViewModel = hiltViewModel()) {
    val prefs by viewModel.settings.collectAsState()
    val currentWindow = (androidx.compose.ui.platform.LocalView.current.context as? android.app.Activity)?.window
    LaunchedEffect(currentWindow, prefs.hideRecentAppsPreview) {
        if (prefs.hideRecentAppsPreview) {
            currentWindow?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            currentWindow?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    VexelHealthPassportTheme(darkTheme = prefs.darkTheme) {
        if (!prefs.onboardingComplete) {
            OnboardingScreen(onComplete = viewModel::completeOnboarding)
        } else {
            LockGate(prefs, viewModel) {
                val fontScale = LocalDensity.current.fontScale
                val navigationLabelStyle = if (fontScale >= 1.8f) {
                    MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, lineHeight = 10.sp)
                } else {
                    MaterialTheme.typography.labelMedium
                }
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
                val currentLabel = destinations.firstOrNull { it.route == currentRoute }?.label ?: destinations.first().label
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    topBar = { TopAppBar(title = { Text(currentLabel) }) },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = { NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) { destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = destination.route == currentRoute,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label, maxLines = 1, style = navigationLabelStyle) },
                        )
                    } } },
                ) { padding ->
                    NavHost(navController = navController, startDestination = Routes.HOME) {
                        composable(Routes.HOME) {
                            HomeScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.RECORDS) {
                            TimelineScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.PLAN) {
                            RemindersScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.VAULT) {
                            DocumentsScreen(modifier = Modifier.padding(padding))
                        }
                        composable(Routes.PROFILE) {
                            ProfileScreen(modifier = Modifier.padding(padding))
                        }
                    }
                }
                val operationError by viewModel.operationError.collectAsState()
                operationError?.let { message ->
                    AlertDialog(
                        onDismissRequest = viewModel::dismissOperationError,
                        title = { Text("Something went wrong") },
                        text = { Text(message) },
                        confirmButton = { TextButton(viewModel::dismissOperationError) { Text("OK") } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LockGate(prefs: UserPreferences, vm: PassportViewModel, content: @Composable () -> Unit) {
    var unlocked by rememberSaveable(prefs.lockEnabled) { mutableStateOf(!prefs.lockEnabled) }
    var unlockedAt by rememberSaveable(prefs.lockEnabled) { mutableStateOf<Long?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, prefs.lockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && prefs.lockEnabled) {
                unlocked = false
                unlockedAt = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(unlocked, prefs.lockEnabled, prefs.lockTimeoutMinutes) {
        while (unlocked && prefs.lockEnabled && prefs.lockTimeoutMinutes > 0) {
            delay(1_000)
            val started = unlockedAt ?: continue
            if (System.currentTimeMillis() - started >= prefs.lockTimeoutMinutes * 60_000L) {
                unlocked = false
                unlockedAt = null
            }
        }
    }
    if (unlocked || !prefs.lockEnabled) content() else PinUnlockDialog(prefs, vm) {
        unlocked = true
        unlockedAt = System.currentTimeMillis()
    }
}

@Composable
private fun PinUnlockDialog(prefs: UserPreferences, vm: PassportViewModel, onUnlocked: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    val activity = LocalContext.current as? FragmentActivity
    val canUseBiometric = activity != null && BiometricManager.from(activity).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS
    val biometricPrompt = remember(activity) {
        activity?.let { host ->
            BiometricPrompt(host, ContextCompat.getMainExecutor(host), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onUnlocked()
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { error = false }
            })
        }
    }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vexel Health Passport")
            .setSubtitle("Authenticate to view your private health information")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Unlock Vexel Health Passport") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(12); error = false },
                label = { Text("PIN") },
                isError = error,
                supportingText = { if (error) Text("Incorrect PIN") }
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canUseBiometric) {
                    TextButton({ biometricPrompt?.authenticate(promptInfo) }) {
                        Text("Use device authentication")
                    }
                }
                Button({
                    if (vm.verifyPin(pin, prefs)) onUnlocked() else error = true
                }) {
                    Text("Unlock")
                }
            }
        }
    )
}
