package com.hb.surfingcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hb.surfingcompose.screens.RecipeDetailScreen
import com.hb.surfingcompose.screens.RecipesListScreen
import com.hb.surfingcompose.ui.theme.SurfingComposeTheme
import com.hb.surfingcompose.viewmodel.RecipesViewModel
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
                    startDestination = ROUTE_LIST_RECIPES
                ) {
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