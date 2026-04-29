package com.privatefamily.broadcastsms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.privatefamily.broadcastsms.AppContainer
import com.privatefamily.broadcastsms.ui.screens.ComposeBroadcastScreen
import com.privatefamily.broadcastsms.ui.screens.ContactPickerScreen
import com.privatefamily.broadcastsms.ui.screens.HistoryScreen
import com.privatefamily.broadcastsms.ui.screens.HomeScreen
import com.privatefamily.broadcastsms.ui.screens.ListDetailScreen
import com.privatefamily.broadcastsms.ui.screens.ListsScreen
import com.privatefamily.broadcastsms.ui.screens.TemplatesScreen
import com.privatefamily.broadcastsms.ui.viewmodel.ContactPickerViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.ContactPickerViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.ListDetailViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.ListDetailViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.ListsViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.ListsViewModelFactory
import com.privatefamily.broadcastsms.ui.viewmodel.TemplatesViewModel
import com.privatefamily.broadcastsms.ui.viewmodel.TemplatesViewModelFactory

@Composable
fun ClanCastNavHost(
    navController: androidx.navigation.NavHostController = rememberNavController(),
    container: AppContainer,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home,
    ) {
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
            ComposeBroadcastScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.History) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
