package com.hb.surfingcompose.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.hb.surfingcompose.R
import com.hb.surfingcompose.presentation.ROUTE_LIST_RECIPES
import kotlinx.coroutines.delay


private const val DELAY_SPLASH_SCREEN = 2000L

@Composable
fun SplashScreen(navController: NavController) {

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {

        LaunchedEffect(key1 = true, block = {
            delay(DELAY_SPLASH_SCREEN)
            navController.navigate(ROUTE_LIST_RECIPES)
        })

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.recipe),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(id = R.dimen.dimens_150dp)),
            )
        }
    }
}