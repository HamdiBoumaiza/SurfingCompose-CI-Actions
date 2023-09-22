package com.hb.surfingcompose.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hb.surfingcompose.R
import com.hb.surfingcompose.presentation.ROUTE_DETAILS_RECIPES
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import com.hb.surfingcompose.presentation.widgets.AppBar
import com.hb.surfingcompose.presentation.widgets.useDebounce

@Composable
fun RecipesListScreen(
    navController: NavController,
    viewModel: RecipesViewModel
) {
    viewModel.getRecipesFlow()
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppBar(title = stringResource(R.string.home), navController = navController)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
            SearchBar(
                hint = stringResource(R.string.search_recipe),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.margin_medium_small))
            ) { viewModel.getRecipesFlow(RecipesRequest(it)) }
            RecipeList(navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf("") }
    var isHintDisplayed by remember { mutableStateOf(hint != "") }

    text.useDebounce { t -> onSearch(t) }

    Box(modifier = modifier) {
        BasicTextField(
            value = text,
            onValueChange = { searchedText -> text = searchedText },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(dimensionResource(id = R.dimen.margin_tiny), shapes.large)
                .background(Color.White, shapes.large)
                .padding(horizontal = dimensionResource(id = R.dimen.margin_medium_plus), vertical = dimensionResource(id = R.dimen.margin_medium_small))
                .onFocusChanged {
                    isHintDisplayed = it.isFocused != true && text.isEmpty()
                }
        )
        if (isHintDisplayed) {
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.margin_medium_plus), vertical = dimensionResource(id = R.dimen.margin_medium_small))
            )
        }
    }
}

@Composable
fun RecipeList(navController: NavController, viewModel: RecipesViewModel) {

    val recipeList by remember { viewModel.recipeList }
    val errorMessage by remember { viewModel.errorMessage }
    val isLoading by remember { viewModel.isLoading }

    RecipeListView(recipes = recipeList, navController = navController, viewModel = viewModel)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) CircularProgressIndicator(color = MaterialTheme.colors.primary)
        if (errorMessage.isNotEmpty()) RetryView(error = errorMessage) { viewModel.getRecipesFlow() }
    }

}

@Composable
fun RecipeListView(recipes: List<RecipeModel>, navController: NavController, viewModel: RecipesViewModel) {
    LazyColumn(contentPadding = PaddingValues(dimensionResource(id = R.dimen.margin_tiny))) {
        items(recipes) { recipe ->
            RecipeRow(navController = navController, recipe = recipe, viewModel = viewModel)
        }
    }
}

@Composable
fun RecipeRow(navController: NavController, recipe: RecipeModel, viewModel: RecipesViewModel) {
    Card(
        elevation = dimensionResource(id =R.dimen.margin_small) ,
        modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_tiny)),
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.background)
            .clickable {
                viewModel.setRecipe(recipe)
                navController.navigate(ROUTE_DETAILS_RECIPES)
            }
        ) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_place_holder),
                error = painterResource(R.drawable.ic_place_holder),
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_medium_plus)))
                    .height(dimensionResource(id = R.dimen.dimens_150dp))
                    .padding(dimensionResource(id = R.dimen.margin_tiny)),
            )

            Text(
                text = recipe.name,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_tiny)),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.onSurface
            )
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_tiny)),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.primaryVariant
            )
        }
    }
}

@Composable
fun RetryView(error: String, onRetry: () -> Unit) {
    Column {
        Text(error, color = Color.Red, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_medium_small)))
        Button(
            onClick = { onRetry() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}
