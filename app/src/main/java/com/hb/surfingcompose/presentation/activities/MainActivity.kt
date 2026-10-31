package com.hb.surfingcompose.presentation.activities

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.hb.surfingcompose.R
import com.hb.surfingcompose.presentation.screens.LoadingRecipe

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hb.surfingcompose.presentation.ROUTE_DETAILS_RECIPES
import com.hb.surfingcompose.presentation.ROUTE_LIST_RECIPES
import com.hb.surfingcompose.presentation.ROUTE_COOKING
import com.hb.surfingcompose.presentation.screens.CookingScreen
import com.hb.surfingcompose.presentation.screens.RecipeDetailScreen
import com.hb.surfingcompose.presentation.screens.RecipesListScreen
import com.hb.surfingcompose.presentation.theme.SurfingComposeTheme
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SurfingComposeTheme {
                val viewModel: RecipesViewModel = hiltViewModel()
                val navController = rememberNavController()
                RecipesNavigation(navController, viewModel)
            }
        }
    }
}

@Composable
fun RecipesNavigation(navController: NavHostController, viewModel: RecipesViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val undoMessage = state.undoFavorite?.let { stringResource(R.string.favorite_removed, it.removal.recipe.name) }
    val undoLabel = stringResource(R.string.undo)
    LaunchedEffect(state.undoFavorite?.token, undoMessage) {
        state.undoFavorite?.let { undo ->
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                undoMessage.orEmpty(),
                actionLabel = undoLabel, withDismissAction = true,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoFavorite(undo.token)
            else viewModel.dismissUndo(undo.token)
        }
    }
    LaunchedEffect(state.actionError) {
        state.actionError?.let { snackbar.showSnackbar(it); viewModel.clearActionError() }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_LIST_RECIPES,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
            composable(ROUTE_LIST_RECIPES) { RecipesListScreen(navController, viewModel) }
            composable(ROUTE_COOKING, arguments = listOf(navArgument("recipeId") { type = NavType.IntType })) { entry ->
                val id = entry.arguments!!.getInt("recipeId")
                LaunchedEffect(id) { viewModel.selectRecipe(id) }
                if (state.isLoadingRecipe || state.selectedRecipeId != id) LoadingRecipe()
                else CookingScreen(
                    recipe = state.selectedRecipe?.takeIf { it.id == id },
                    completedSteps = state.completedSteps,
                    onCompletedSteps = { viewModel.setCompletedSteps(id, it) },
                    onExit = { navController.navigateUp() }
                )
            }
            composable(ROUTE_DETAILS_RECIPES, arguments = listOf(navArgument("recipeId") { type = NavType.IntType })) { entry ->
                val id = entry.arguments!!.getInt("recipeId")
                LaunchedEffect(id) { viewModel.selectRecipe(id) }
                BoxWithConstraints {
                    if (maxWidth >= 840.dp) RecipesListScreen(navController, viewModel)
                    else RecipeDetailScreen(navController, viewModel)
                }
            }
        }
    }
}
