package com.example.weelorumtesttask

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weelorumtesttask.ui.Workout
import com.example.weelorumtesttask.ui.OptionViewModel
import com.example.weelorumtesttask.ui.SelectOptionScreen
import com.example.weelorumtesttask.ui.ChooseBreakfast
import javax.sql.DataSource


enum class SportsScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Breakfast(title = R.string.choose_breakfast),
    Calories(title = R.string.calories),
    Exercises(title = R.string.exercises)
}

@Composable
fun Health(
    currentScreen: SportsScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun WeelorumTestTask(
    viewModel: ChooseBreakfast = viewModel(),
    navController: NavHostController = rememberNavController()
) {

    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = SportsScreen.valueOf(
        backStackEntry?.destination?.route ?: SportsScreen.Start.name
    )

    Scaffold(
        topBar = {
            WeelorumTestTaskAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = SportsScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = SportsScreen.Start.name) {
                ChooseBreakfast(
                    quantityOptions = DataSource.quantityOptions,
                    onNextButtonClicked = {
                        viewModel.setQuantity(it)
                        navController.navigate(SportsScreen.Breakfast.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
            composable(route = SportsScreen.Breakfast.name) {
                val context = LocalContext.current
                SelectOptionScreen(
                    onNextButtonClicked = { navController.navigate(SportsScreen.Calories.name) },
                    onCancelButtonClicked = {
                        cancelAndNavigateToStart(viewModel, navController)
                    },
                    options = DataSource.breakfast.map { id -> context.resources.getString(id) },
                    onSelectionChanged = { viewModel.setBreakfast(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = SportsScreen.Calories.name) {
                OptionViewModel(
                    subtotal = uiState.price,
                    onNextButtonClicked = { navController.navigate(SportsScreen.Exercises.name) },
                    onCancelButtonClicked = {
                        cancelAndNavigateToStart(viewModel, navController)
                    },
                    options = uiState.pickupOptions,
                    onSelectionChanged = { viewModel.setDate(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = SportsScreen.Exercises.name) {
                val context = LocalContext.current
                Workout(
                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        cancelAndNavigateToStart(viewModel, navController)
                    },

                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

@Composable
fun Start(currentScreen: Any?, canNavigateBack: Any?, navigateUp: () -> Unit) {

}
private fun cancelAndNavigateToStart(
    viewModel: SelectOptionScreen,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(SportsScreen.Start.name, inclusive = false)
}

