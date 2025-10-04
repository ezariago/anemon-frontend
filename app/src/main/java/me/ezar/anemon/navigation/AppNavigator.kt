package me.ezar.anemon.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.screens.dashboard.DashboardScreen
import me.ezar.anemon.ui.screens.login.LoginScreen
import me.ezar.anemon.ui.screens.login.LoginViewModel
import me.ezar.anemon.ui.screens.registration.BiodataScreen
import me.ezar.anemon.ui.screens.registration.RegisterScreen
import me.ezar.anemon.ui.screens.registration.RegistrationViewModel
import me.ezar.anemon.ui.screens.registration.VehiclePictureScreen
import me.ezar.anemon.ui.screens.trip.DestinationSelectorScreen

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val startDestination =
        if (LocalStorage.token.isNotEmpty()) AppRoutes.Map.route else AppRoutes.Dashboard.route

    val activity = LocalActivity.current

    Scaffold { paddingValues ->
        Column(
            Modifier
                .imePadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(400)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(400)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(400)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(400)
                    )
                }
            ) {
                composable(AppRoutes.Dashboard.route) {
                    DashboardScreen(
                        onRegister = { navController.navigate(AppRoutes.RegistrationFlow.route) },
                        onLogin = { navController.navigate(AppRoutes.Login.route) },
                        contentPadding = paddingValues
                    )
                }

                composable(AppRoutes.Login.route) {
                    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate(AppRoutes.Map.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = { navController.popBackStack() },
                        contentPadding = paddingValues
                    )
                }

                navigation(
                    startDestination = AppRoutes.RegistrationFlow.Register.route,
                    route = AppRoutes.RegistrationFlow.route
                ) {
                    composable(AppRoutes.RegistrationFlow.Register.route) { navBackStackEntry ->
                        val parentEntry =
                            remember(navBackStackEntry) { navController.getBackStackEntry(AppRoutes.RegistrationFlow.route) }
                        val registrationViewModel: RegistrationViewModel =
                            viewModel(parentEntry, factory = RegistrationViewModel.Factory)

                        RegisterScreen(
                            registrationViewModel = registrationViewModel,
                            onNextButton = { navController.navigate(AppRoutes.RegistrationFlow.Biodata.route) },
                            onBackButton = { navController.popBackStack() },
                            onSelectVehicleButton = { navController.navigate(AppRoutes.RegistrationFlow.VehiclePicture.route) },
                            contentPadding = paddingValues
                        )
                    }

                    composable(AppRoutes.RegistrationFlow.VehiclePicture.route) { navBackStackEntry ->
                        val parentEntry =
                            remember(navBackStackEntry) { navController.getBackStackEntry(AppRoutes.RegistrationFlow.route) }
                        val registrationViewModel: RegistrationViewModel =
                            viewModel(parentEntry, factory = RegistrationViewModel.Factory)

                        VehiclePictureScreen(
                            registrationViewModel = registrationViewModel,
                            onBackButton = { navController.popBackStack() },
                            onPhotoTaken = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoutes.RegistrationFlow.Biodata.route) { navBackStackEntry ->
                        val context = LocalContext.current
                        val parentEntry =
                            remember(navBackStackEntry) { navController.getBackStackEntry(AppRoutes.RegistrationFlow.route) }
                        val registrationViewModel: RegistrationViewModel =
                            viewModel(parentEntry, factory = RegistrationViewModel.Factory)

                        BiodataScreen(
                            registrationViewModel = registrationViewModel,
                            onBackButton = { navController.popBackStack() },
                            onSuccess = {
                                navController.navigate(AppRoutes.Dashboard.route) {
                                    popUpTo(AppRoutes.RegistrationFlow.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                                Toast.makeText(
                                    context,
                                    "Registrasi berhasil! Silakan login.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                            },
                            contentPadding = paddingValues
                        )
                    }
                }

                composable(AppRoutes.Map.route) {
                    AppPermissionWrapper {
                        DestinationSelectorScreen(
                            onLogout = {
                                LocalStorage.logout()
                                navController.navigate(AppRoutes.Dashboard.route) {
                                    popUpTo(AppRoutes.Map.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onExit = {
                                activity?.finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppPermissionWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionsToRequest = remember {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissions.toTypedArray()
    }

    var allPermissionsGranted by remember {
        mutableStateOf(checkIfAllPermissionsGranted(context, permissionsToRequest))
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            allPermissionsGranted = permissionsMap.values.all { it }

            if (!allPermissionsGranted) {
                Toast.makeText(
                    context,
                    "ANEMON butuh beberapa izin buat berfungsi dengan baikk, tolong yaa!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allPermissionsGranted = checkIfAllPermissionsGranted(context, permissionsToRequest)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (!allPermissionsGranted) {
            permissionsLauncher.launch(permissionsToRequest)
        }
    }

    if (allPermissionsGranted) {
        content()
    } else {
        PermissionRationaleScreen(
            onOpenSettings = { openAppSettings(context) }
        )
    }
}

@Composable
private fun PermissionRationaleScreen(
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Izin dibutuhkan",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ANEMON butuh \"Akses Lokasi Sepanjang Waktu\" dan \"Notifikasi\" untuk pastiin lokasi kamu terus up-to-date pas lagi matching, tolong izinin yaa! Data kamu aman kokk, janji deh.\n\nKalau bingung, bisa tanya ke peneliti untuk bantu pasangin.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        }
    }
}

private fun checkIfAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}