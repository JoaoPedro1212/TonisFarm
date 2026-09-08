package com.example.tonisfarm.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.example.tonisfarm.ui.cowdetail.CowDetailScreen
import com.example.tonisfarm.ui.cowform.CowFormScreen
import com.example.tonisfarm.ui.cowlist.CowListScreen
import com.example.tonisfarm.ui.dashboard.DashboardScreen
import com.example.tonisfarm.ui.events.AllEventsScreen
import com.example.tonisfarm.ui.events.EventFormScreen
import com.example.tonisfarm.ui.events.EventDetailScreen
import com.example.tonisfarm.ui.finance.FinanceScreen
import com.example.tonisfarm.ui.finance.IncomeListScreen
import com.example.tonisfarm.ui.finance.ExpenseListScreen
import com.example.tonisfarm.ui.finance.FinanceHistoryScreen
import com.example.tonisfarm.ui.finance.MonthDetailScreen
import com.example.tonisfarm.ui.finance.FinanceChartsScreen
import com.example.tonisfarm.ui.history.VacinacaoHistoryScreen
import com.example.tonisfarm.ui.history.VacinacaoFormScreen
import com.example.tonisfarm.ui.history.VermifugacaoHistoryScreen
import com.example.tonisfarm.ui.history.VermifugacaoFormScreen
import com.example.tonisfarm.ui.history.PesagemHistoryScreen
import com.example.tonisfarm.ui.history.PesagemFormScreen
import com.example.tonisfarm.ui.history.PartoHistoryScreen
import com.example.tonisfarm.ui.history.PartoFormScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CowList : Screen("cow_list?pregnantOnly={pregnantOnly}") {
        fun createRoute(pregnantOnly: Boolean = false) = "cow_list?pregnantOnly=$pregnantOnly"
    }
    object CowForm : Screen("cow_form/{cowId}") {
        fun createRoute(cowId: Long? = null) = if (cowId != null) "cow_form/$cowId" else "cow_form/-1"
    }
    object CowDetail : Screen("cow_detail/{cowId}") {
        fun createRoute(cowId: Long) = "cow_detail/$cowId"
    }
    object EventForm : Screen("event_form?cowId={cowId}&eventoId={eventoId}") {
        fun createRoute(cowId: Long? = null, eventoId: Long? = null): String {
            val cowIdParam = cowId?.let { "cowId=$it" } ?: ""
            val eventoIdParam = eventoId?.let { "eventoId=$it" } ?: ""
            val params = listOfNotNull(cowIdParam, eventoIdParam).joinToString("&")
            return if (params.isNotEmpty()) "event_form?$params" else "event_form"
        }
    }
    object AllEvents : Screen("all_events")
    object EventDetail : Screen("event_detail/{eventoId}") {
        fun createRoute(eventoId: Long) = "event_detail/$eventoId"
    }
    object Finance : Screen("finance")
    object FinanceIncome : Screen("finance_income")
    object FinanceExpense : Screen("finance_expense")
    object FinanceHistory : Screen("finance_history")
    object FinanceMonthDetail : Screen("finance_month_detail/{monthKey}") {
        fun createRoute(monthKey: String): String {
            // Codificar o monthKey para evitar problemas com "/" na URL
            val encoded = java.net.URLEncoder.encode(monthKey, "UTF-8")
            return "finance_month_detail/$encoded"
        }
    }
    object FinanceCharts : Screen("finance_charts")
    object VacinacaoHistory : Screen("vacinacao_history/{cowId}") {
        fun createRoute(cowId: Long) = "vacinacao_history/$cowId"
    }
    object VacinacaoForm : Screen("vacinacao_form/{cowId}?vacinacaoId={vacinacaoId}") {
        fun createRoute(cowId: Long, vacinacaoId: Long? = null): String {
            return if (vacinacaoId != null) "vacinacao_form/$cowId?vacinacaoId=$vacinacaoId" else "vacinacao_form/$cowId"
        }
    }
    object VermifugacaoHistory : Screen("vermifugacao_history/{cowId}") {
        fun createRoute(cowId: Long) = "vermifugacao_history/$cowId"
    }
    object VermifugacaoForm : Screen("vermifugacao_form/{cowId}?vermifugacaoId={vermifugacaoId}") {
        fun createRoute(cowId: Long, vermifugacaoId: Long? = null): String {
            return if (vermifugacaoId != null) "vermifugacao_form/$cowId?vermifugacaoId=$vermifugacaoId" else "vermifugacao_form/$cowId"
        }
    }
    object PesagemHistory : Screen("pesagem_history/{cowId}") {
        fun createRoute(cowId: Long) = "pesagem_history/$cowId"
    }
    object PesagemForm : Screen("pesagem_form/{cowId}?pesagemId={pesagemId}") {
        fun createRoute(cowId: Long, pesagemId: Long? = null): String {
            return if (pesagemId != null) "pesagem_form/$cowId?pesagemId=$pesagemId" else "pesagem_form/$cowId"
        }
    }
    object PartoHistory : Screen("parto_history/{cowId}") {
        fun createRoute(cowId: Long) = "parto_history/$cowId"
    }
    object PartoForm : Screen("parto_form/{cowId}?partoId={partoId}") {
        fun createRoute(cowId: Long, partoId: Long? = null): String {
            return if (partoId != null) "parto_form/$cowId?partoId=$partoId" else "parto_form/$cowId"
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCowList = { navController.navigate(Screen.CowList.createRoute(false)) },
                onNavigateToPregnantCows = { navController.navigate(Screen.CowList.createRoute(true)) },
                onNavigateToCowDetail = { cowId ->
                    navController.navigate(Screen.CowDetail.createRoute(cowId))
                },
                onNavigateToAllEvents = { navController.navigate(Screen.AllEvents.route) },
                onNavigateToFinance = { navController.navigate(Screen.Finance.route) }
            )
        }

        composable(
            route = Screen.CowList.route,
            arguments = listOf(
                navArgument("pregnantOnly") { type = androidx.navigation.NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val pregnantOnly = backStackEntry.arguments?.getBoolean("pregnantOnly") ?: false
            CowListScreen(
                showPregnantOnly = pregnantOnly,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCowForm = { cowId ->
                    navController.navigate(Screen.CowForm.createRoute(cowId))
                },
                onNavigateToCowDetail = { cowId ->
                    navController.navigate(Screen.CowDetail.createRoute(cowId))
                }
            )
        }

        composable(
            route = "cow_form/{cowId}",
            arguments = listOf(navArgument("cowId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId")
            CowFormScreen(
                cowId = if (cowId != null && cowId != -1L) cowId else null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CowDetail.route,
            arguments = listOf(navArgument("cowId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            CowDetailScreen(
                cowId = cowId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(Screen.CowForm.createRoute(cowId)) },
                onNavigateToEventForm = { eventoId ->
                    navController.navigate(Screen.EventForm.createRoute(cowId = cowId, eventoId = eventoId))
                },
                onNavigateToVacinacaoHistory = { 
                    navController.navigate(Screen.VacinacaoHistory.createRoute(cowId))
                },
                onNavigateToVermifugacaoHistory = { 
                    navController.navigate(Screen.VermifugacaoHistory.createRoute(cowId))
                },
                onNavigateToPesagemHistory = { 
                    navController.navigate(Screen.PesagemHistory.createRoute(cowId))
                },
                onNavigateToPartoHistory = { 
                    navController.navigate(Screen.PartoHistory.createRoute(cowId))
                }
            )
        }

        composable(
            route = "event_form?cowId={cowId}&eventoId={eventoId}",
            arguments = listOf(
                navArgument("cowId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("eventoId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId")?.takeIf { it != -1L }
            val eventoId = backStackEntry.arguments?.getLong("eventoId")?.takeIf { it != -1L }
            EventFormScreen(
                cowId = cowId,
                eventoId = eventoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AllEvents.route) {
            AllEventsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEventForm = { cowId, eventoId ->
                    navController.navigate(Screen.EventForm.createRoute(cowId, eventoId))
                },
                onNavigateToEventDetail = { eventoId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventoId))
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getLong("eventoId") ?: return@composable
            EventDetailScreen(
                eventoId = eventoId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { eventoId ->
                    navController.navigate(Screen.EventForm.createRoute(eventoId = eventoId))
                }
            )
        }

        composable(Screen.Finance.route) {
            FinanceScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDespesas = { navController.navigate(Screen.FinanceExpense.route) },
                onNavigateToReceitas = { navController.navigate(Screen.FinanceIncome.route) },
                onNavigateToMonthDetail = { month -> 
                    navController.navigate(Screen.FinanceHistory.route)
                },
                onNavigateToHistory = { navController.navigate(Screen.FinanceHistory.route) },
                onNavigateToCharts = { navController.navigate(Screen.FinanceCharts.route) }
            )
        }

        composable(Screen.FinanceIncome.route) {
            IncomeListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddIncome = { 
                    // TODO: Navegar para tela de adicionar receita
                }
            )
        }

        composable(Screen.FinanceExpense.route) {
            ExpenseListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = { 
                    // TODO: Navegar para tela de adicionar despesa
                }
            )
        }

        composable(Screen.FinanceHistory.route) {
            FinanceHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMonthDetail = { monthKey ->
                    navController.navigate(Screen.FinanceMonthDetail.createRoute(monthKey))
                }
            )
        }

        composable(
            route = Screen.FinanceMonthDetail.route,
            arguments = listOf(navArgument("monthKey") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedMonthKey = backStackEntry.arguments?.getString("monthKey") ?: return@composable
            val monthKey = try {
                java.net.URLDecoder.decode(encodedMonthKey, "UTF-8")
            } catch (e: Exception) {
                encodedMonthKey
            }
            MonthDetailScreen(
                monthKey = monthKey,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FinanceCharts.route) {
            FinanceChartsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Rotas de histórico
        composable(
            route = Screen.VacinacaoHistory.route,
            arguments = listOf(navArgument("cowId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            VacinacaoHistoryScreen(
                cowId = cowId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { cowId, vacinacaoId ->
                    navController.navigate(Screen.VacinacaoForm.createRoute(cowId, vacinacaoId))
                }
            )
        }

        composable(
            route = "vacinacao_form/{cowId}?vacinacaoId={vacinacaoId}",
            arguments = listOf(
                navArgument("cowId") { type = NavType.LongType },
                navArgument("vacinacaoId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            val vacinacaoId = backStackEntry.arguments?.getLong("vacinacaoId")?.takeIf { it != -1L }
            VacinacaoFormScreen(
                cowId = cowId,
                vacinacaoId = vacinacaoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VermifugacaoHistory.route,
            arguments = listOf(navArgument("cowId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            VermifugacaoHistoryScreen(
                cowId = cowId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { cowId, vermifugacaoId ->
                    navController.navigate(Screen.VermifugacaoForm.createRoute(cowId, vermifugacaoId))
                }
            )
        }

        composable(
            route = "vermifugacao_form/{cowId}?vermifugacaoId={vermifugacaoId}",
            arguments = listOf(
                navArgument("cowId") { type = NavType.LongType },
                navArgument("vermifugacaoId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            val vermifugacaoId = backStackEntry.arguments?.getLong("vermifugacaoId")?.takeIf { it != -1L }
            VermifugacaoFormScreen(
                cowId = cowId,
                vermifugacaoId = vermifugacaoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PesagemHistory.route,
            arguments = listOf(navArgument("cowId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            PesagemHistoryScreen(
                cowId = cowId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { cowId, pesagemId ->
                    navController.navigate(Screen.PesagemForm.createRoute(cowId, pesagemId))
                }
            )
        }

        composable(
            route = "pesagem_form/{cowId}?pesagemId={pesagemId}",
            arguments = listOf(
                navArgument("cowId") { type = NavType.LongType },
                navArgument("pesagemId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            val pesagemId = backStackEntry.arguments?.getLong("pesagemId")?.takeIf { it != -1L }
            PesagemFormScreen(
                cowId = cowId,
                pesagemId = pesagemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PartoHistory.route,
            arguments = listOf(navArgument("cowId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            PartoHistoryScreen(
                cowId = cowId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { cowId, partoId ->
                    navController.navigate(Screen.PartoForm.createRoute(cowId, partoId))
                }
            )
        }

        composable(
            route = "parto_form/{cowId}?partoId={partoId}",
            arguments = listOf(
                navArgument("cowId") { type = NavType.LongType },
                navArgument("partoId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getLong("cowId") ?: return@composable
            val partoId = backStackEntry.arguments?.getLong("partoId")?.takeIf { it != -1L }
            PartoFormScreen(
                cowId = cowId,
                partoId = partoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

