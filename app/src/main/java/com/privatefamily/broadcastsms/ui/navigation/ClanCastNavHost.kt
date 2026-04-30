package com.privatefamily.broadcastsms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.privatefamily.broadcastsms.AppContainer
import com.privatefamily.broadcastsms.ui.screens.ComposeBroadcastScreen
import com.privatefamily.broadcastsms.ui.screens.ContactPickerScreen
import com.privatefamily.broadcastsms.ui.screens.BroadcastDetailScreen
import com.privatefamily.broadcastsms.ui.screens.HistoryScreen
import com.privatefamily.broadcastsms.ui.screens.HomeScreen
import com.privatefamily.broadcastsms.ui.screens.ListDetailScreen
import com.privatefamily.broadcastsms.ui.screens.ListsScreen
import com.privatefamily.broadcastsms.ui.screens.PreflightScreen
import com.privatefamily.broadcastsms.ui.screens.SplashScreen
import com.privatefamily.broadcastsms.ui.screens.TemplatesScreen
import com.privatefamily.broadcastsms.ui.viewmodel.ContactPickerViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.ContactPickerViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.BroadcastDetailViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.BroadcastDetailViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.ComposeBroadcastViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.ComposeBroadcastViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.HistoryViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.HistoryViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.ListDetailViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.ListDetailViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.ListsViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.ListsViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.PreflightViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.PreflightViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.TemplatesViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.TemplatesViewModelFactory

@Composable
fun ClanCastNavHost(
    navController: androidx.navigation.NavHostController = rememberNavController(),
    container: AppContainer,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onContinue = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                onOpenLists = { navController.navigate(Routes.Lists) },
                onOpenTemplates = { navController.navigate(Routes.Templates) },
                onOpenCompose = { navController.navigate(Routes.ComposeBroadcast) },
                onOpenHistory = { navController.navigate(Routes.History) },
            )
        }
        composable(Routes.Lists) {
            val viewModel: ListsViewModel = viewModel(
                factory = ListsViewModelFactory(container.distributionListRepository),
            )
            ListsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onListSelected = { navController.navigate(Routes.listDetail(it)) },
            )
        }
        composable(
            route = "${Routes.ListDetail}/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId").orEmpty()
            val viewModel: ListDetailViewModel = viewModel(
                factory = ListDetailViewModelFactory(
                    listId = listId,
                    repository = container.distributionListRepository,
                ),
            )
            ListDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddMember = { navController.navigate(Routes.contactPicker(listId)) },
            )
        }
        composable(
            route = "${Routes.ContactPicker}/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId").orEmpty()
            val viewModel: ContactPickerViewModel = viewModel(
                factory = ContactPickerViewModelFactory(
                    listId = listId,
                    distributionListRepository = container.distributionListRepository,
                    contactRepository = container.contactRepository,
                ),
            )
            ContactPickerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Templates) {
            val viewModel: TemplatesViewModel = viewModel(
                factory = TemplatesViewModelFactory(container.templateRepository),
            )
            TemplatesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.ComposeBroadcast) {
            val viewModel: ComposeBroadcastViewModel = viewModel(
                factory = ComposeBroadcastViewModelFactory(
                    distributionListRepository = container.distributionListRepository,
                    templateRepository = container.templateRepository,
                    draftStore = container.broadcastDraftStore,
                ),
            )
            ComposeBroadcastScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinueToPreflight = { navController.navigate(Routes.Preflight) },
            )
        }
        composable(Routes.Preflight) {
            val viewModel: PreflightViewModel = viewModel(
                factory = PreflightViewModelFactory(
                    draftStore = container.broadcastDraftStore,
                    distributionListRepository = container.distributionListRepository,
                    broadcastRepository = container.broadcastRepository,
                ),
            )
            PreflightScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onBroadcastCreated = { navController.navigate(Routes.broadcastDetail(it)) },
            )
        }
        composable(Routes.History) {
            val viewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModelFactory(container.broadcastRepository),
            )
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onBroadcastSelected = { navController.navigate(Routes.broadcastDetail(it)) },
            )
        }
        composable(
            route = "${Routes.BroadcastDetail}/{broadcastId}",
            arguments = listOf(navArgument("broadcastId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val broadcastId = backStackEntry.arguments?.getString("broadcastId").orEmpty()
            val viewModel: BroadcastDetailViewModel = viewModel(
                factory = BroadcastDetailViewModelFactory(
                    broadcastId = broadcastId,
                    repository = container.broadcastRepository,
                ),
            )
            BroadcastDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
