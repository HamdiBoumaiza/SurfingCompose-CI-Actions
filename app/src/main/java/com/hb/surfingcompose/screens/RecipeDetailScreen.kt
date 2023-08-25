package com.hb.surfingcompose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hb.surfingcompose.R
import com.hb.surfingcompose.viewmodel.RecipesViewModel
import com.hb.surfingcompose.widgets.AppBar

@Composable
fun RecipeDetailScreen(navController: NavController, viewModel: RecipesViewModel) {
    val recipe = viewModel.getRecipe() ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),

        ) {
        AppBar(title = stringResource(R.string.details), navController = navController)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_place_holder),
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(MaterialTheme.shapes.large)
                    .height(150.dp),
            )

            Text(
                text = recipe.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp),
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colors.primaryVariant,
                textAlign = TextAlign.Start
            )

        }
    }
}