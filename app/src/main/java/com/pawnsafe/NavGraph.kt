package com.pawnsafe

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pawnsafe.presentation.export.screens.ExportScreen
import com.pawnsafe.presentation.home.screens.HomeScreen
import com.pawnsafe.presentation.pledge.screens.CapturePhotoScreen
import com.pawnsafe.presentation.pledge.screens.PledgeDetailScreen
import com.pawnsafe.presentation.pledge.screens.PledgeFormScreen
import com.pawnsafe.presentation.pledge.screens.PledgeListScreen
import com.pawnsafe.presentation.pledge.screens.ScanTicketScreen
import com.pawnsafe.presentation.redemption.screens.RedemptionFormScreen
import com.pawnsafe.presentation.redemption.screens.RedemptionListScreen
import com.pawnsafe.presentation.settings.InterestRateScreen
import com.pawnsafe.presentation.auth.SettingsScreen
import com.pawnsafe.presentation.auth.LockScreen
import com.pawnsafe.presentation.auth.PinSetupScreen
import com.pawnsafe.presentation.auth.LockScreen
import com.pawnsafe.presentation.auth.PinSetupScreen
import com.pawnsafe.presentation.auth.SettingsScreen

object Routes {
    const val HOME                   = "home"
    const val PLEDGE_LIST            = "pledge_list"
    const val PLEDGE_FORM            = "pledge_form"
    const val PLEDGE_EDIT            = "pledge_edit/{pledgeId}"
    const val PLEDGE_DETAIL          = "pledge_detail/{pledgeId}"
    const val SCAN_TICKET            = "scan_ticket"
    const val CAPTURE_PHOTO          = "capture_photo"
    const val REDEMPTION_LIST        = "redemption_list"
    const val REDEMPTION_FORM        = "redemption_form"
    const val REDEMPTION_FROM_PLEDGE = "redemption_from_pledge/{pledgeId}"
    const val EXPORT                 = "export"
    const val INTEREST_RATES         = "interest_rates"
    const val DASHBOARD  = "dashboard"
    const val SETTINGS   = "settings"
    const val PIN_SETUP  = "pin_setup"
    const val LOCK       = "lock"

    fun pledgeEdit(pledgeId: Int)           = "pledge_edit/$pledgeId"
    fun pledgeDetail(pledgeId: Int)         = "pledge_detail/$pledgeId"
    fun redemptionFromPledge(pledgeId: Int) = "redemption_from_pledge/$pledgeId"
}

@Composable
fun PawnSafeNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.HOME
) {
    // shared photo uri state — survives across capture → form navigation
    var capturedPhotoUri by remember { mutableStateOf<String?>(null) }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.HOME) {
            HomeScreen(
                onPledgeListClick     = { navController.navigate(Routes.PLEDGE_LIST) },
                onAddPledgeClick      = { navController.navigate(Routes.PLEDGE_FORM) },
                onScanClick           = { navController.navigate(Routes.SCAN_TICKET) },
                onRedemptionListClick = { navController.navigate(Routes.REDEMPTION_LIST) },
                onExportClick         = { navController.navigate(Routes.EXPORT) },
                onInterestRatesClick  = { navController.navigate(Routes.INTEREST_RATES) }
            )
        }

        composable(Routes.PLEDGE_LIST) {
            PledgeListScreen(
                onBack        = { navController.popBackStack() },
                onAddClick    = { capturedPhotoUri = null; navController.navigate(Routes.PLEDGE_FORM) },
                onScanClick   = { navController.navigate(Routes.SCAN_TICKET) },
                onPledgeClick = { id -> navController.navigate(Routes.pledgeDetail(id)) },
                onRedeemClick = { id -> navController.navigate(Routes.redemptionFromPledge(id)) }
            )
        }

        composable(Routes.PLEDGE_FORM) {
            PledgeFormScreen(
                onBack           = { navController.popBackStack() },
                onSaved          = { capturedPhotoUri = null; navController.popBackStack() },
                pledgeId         = 0,
                onCapturePhoto   = { navController.navigate(Routes.CAPTURE_PHOTO) },
                capturedPhotoUri = capturedPhotoUri
            )
        }

        composable(
            route = Routes.PLEDGE_EDIT,
            arguments = listOf(navArgument("pledgeId") { type = NavType.IntType })
        ) { backStackEntry ->
            PledgeFormScreen(
                onBack           = { navController.popBackStack() },
                onSaved          = { capturedPhotoUri = null; navController.popBackStack() },
                pledgeId         = backStackEntry.arguments?.getInt("pledgeId") ?: 0,
                onCapturePhoto   = { navController.navigate(Routes.CAPTURE_PHOTO) },
                capturedPhotoUri = capturedPhotoUri
            )
        }

        composable(
            route = Routes.PLEDGE_DETAIL,
            arguments = listOf(navArgument("pledgeId") { type = NavType.IntType })
        ) { backStackEntry ->
            PledgeDetailScreen(
                pledgeId = backStackEntry.arguments?.getInt("pledgeId") ?: 0,
                onBack   = { navController.popBackStack() },
                onEdit   = { id -> navController.navigate(Routes.pledgeEdit(id)) },
                onRedeem = { id -> navController.navigate(Routes.redemptionFromPledge(id)) }
            )
        }

        composable(Routes.CAPTURE_PHOTO) {
            CapturePhotoScreen(
                onBack = { navController.popBackStack() },
                onPhotoCaptured = { uri ->
                    capturedPhotoUri = uri
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SCAN_TICKET) {
            ScanTicketScreen(
                onBack    = { navController.popBackStack() },
                onScanned = {
                    navController.navigate(Routes.PLEDGE_FORM) {
                        popUpTo(Routes.SCAN_TICKET) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REDEMPTION_LIST) {
            RedemptionListScreen(
            onBack     = { navController.popBackStack() },
            onAddClick = { navController.navigate(Routes.REDEMPTION_FORM) })
        }

        composable(Routes.REDEMPTION_FORM) {
            RedemptionFormScreen(onBack = { navController.popBackStack() }, onSaved = { navController.popBackStack() }, pledgeId = 0)
        }

        composable(
            route = Routes.REDEMPTION_FROM_PLEDGE,
            arguments = listOf(navArgument("pledgeId") { type = NavType.IntType })
        ) { backStackEntry ->
            RedemptionFormScreen(
                onBack   = { navController.popBackStack() },
                onSaved  = { navController.popBackStack() },
                pledgeId = backStackEntry.arguments?.getInt("pledgeId") ?: 0
            )
        }

        composable(Routes.EXPORT) {
            ExportScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.INTEREST_RATES) {
            InterestRateScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack     = { navController.popBackStack() },
                onSetupPin = { navController.navigate(Routes.PIN_SETUP) }
            )
        }
        composable(Routes.PIN_SETUP) {
            PinSetupScreen(
                onBack          = { navController.popBackStack() },
                onSetupComplete = { navController.popBackStack() }
            )
        }
        composable(Routes.LOCK) {
            LockScreen(onUnlocked = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOCK) { inclusive = true }
                }
            })
        }
    }
}
