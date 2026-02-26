package com.hb.surfingcompose.presentation.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hb.surfingcompose.presentation.ROUTE_DETAILS_RECIPES
import com.hb.surfingcompose.presentation.ROUTE_LIST_RECIPES
import com.hb.surfingcompose.presentation.ROUTE_SPLASH_SCREEN
import com.hb.surfingcompose.presentation.screens.RecipeDetailScreen
import com.hb.surfingcompose.presentation.screens.RecipesListScreen
import com.hb.surfingcompose.presentation.screens.SplashScreen
import com.hb.surfingcompose.presentation.theme.SurfingComposeTheme
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SurfingComposeTheme {
                val viewModel: RecipesViewModel = hiltViewModel()
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_SPLASH_SCREEN
                ) {
                    composable(ROUTE_SPLASH_SCREEN) {
                        SplashScreen(navController = navController)
                    }
                    composable(ROUTE_LIST_RECIPES) {
                        RecipesListScreen(navController = navController, viewModel = viewModel)
                    }
                    composable(ROUTE_DETAILS_RECIPES) {
                        RecipeDetailScreen(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }
}