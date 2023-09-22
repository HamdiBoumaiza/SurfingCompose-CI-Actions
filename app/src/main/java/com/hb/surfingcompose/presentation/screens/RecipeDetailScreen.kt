package com.hb.surfingcompose.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hb.surfingcompose.R
import com.hb.surfingcompose.domain.models.InstructionsModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.presentation.theme.LightGreen
import com.hb.surfingcompose.presentation.theme.LightYellow
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import com.hb.surfingcompose.presentation.widgets.AppBar
import com.hb.surfingcompose.presentation.widgets.VideoPlayer

@Composable
fun RecipeDetailScreen(navController: NavController, viewModel: RecipesViewModel) {
    val recipe = viewModel.getRecipe() ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),

        ) {
        AppBar(title = stringResource(R.string.details), navController = navController)
        LazyColumn(contentPadding = PaddingValues(dimensionResource(id = R.dimen.margin_medium_small))) {
            item {
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
                    error = painterResource(R.drawable.ic_place_holder),
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(top = dimensionResource(id = R.dimen.margin_medium))
                        .clip(MaterialTheme.shapes.large)
                        .height(dimensionResource(id = R.dimen.dimens_150dp)),
                )

                Text(
                    text = recipe.description,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.margin_medium)),
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colors.primaryVariant,
                    textAlign = TextAlign.Start
                )

                TextSubTitle(resourceString = R.string.nutritional_value)
                NutritionValues(recipe)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
                TextSubTitle(resourceString = R.string.instructions_list)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))

            }

            items(items = recipe.instructions) { item -> InstructionRow(item) }

            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
                TextSubTitle(resourceString = R.string.see_how_to)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
                VideoPlayer(recipe.originalVideoUrl)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
                ListTags(recipe)
            }
        }
    }
}

@Composable
fun NutritionValues(recipe: RecipeModel) {
    NutritionView(stringResource(R.string.protein), recipe.nutrition.protein.toString(), backgroundColor = LightGreen)
    NutritionView(stringResource(R.string.fat), recipe.nutrition.fat.toString())
    NutritionView(stringResource(R.string.fiber), recipe.nutrition.fiber.toString(), backgroundColor = LightGreen)
    NutritionView(stringResource(R.string.sugar), recipe.nutrition.sugar.toString())
    NutritionView(stringResource(R.string.carbohydrates), recipe.nutrition.carbohydrates.toString(), backgroundColor = LightGreen)
    NutritionView(stringResource(R.string.calories), recipe.nutrition.calories.toString(), unit = "cal")
}

@Composable
private fun ListTags(recipe: RecipeModel) {
    if (recipe.tags.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.margin_medium_small)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_small))
        ) { items(recipe.tags) { tag -> TagChip(tag.displayName) } }
    }
}

@Composable
private fun InstructionsList(instructions: List<InstructionsModel>) {
    LazyColumn(contentPadding = PaddingValues(dimensionResource(id = R.dimen.margin_tiny))) {
        items(instructions) { instruction -> InstructionRow(instruction) }
    }
}

@Composable
fun InstructionRow(instruction: InstructionsModel) {
    Card(
        elevation = dimensionResource(id = R.dimen.margin_tiny),
        modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_tiny)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.margin_tiny)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            Text(
                text = instruction.position.toString(),
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_tiny)),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )

            Text(
                text = instruction.displayText,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.margin_tiny))
                    .weight(1f),
                color = MaterialTheme.colors.primaryVariant
            )
        }
    }
}


@Composable
fun NutritionView(key: String, value: String, unit: String = "g", backgroundColor: Color = LightYellow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.margin_tiny))
            .background(color = backgroundColor, shape = RoundedCornerShape(dimensionResource(id = R.dimen.margin_small))),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(dimensionResource(id = R.dimen.margin_tiny)),
            text = key
        )
        Text(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_small)), text = "$value $unit")
    }
}

@Composable
fun TagChip(tag: String) {
    SuggestionChip(
        onClick = { Log.d("Suggestion chip", "tag") },
        label = { androidx.compose.material3.Text(tag) }
    )
}

@Composable
fun TextSubTitle(modifier: Modifier = Modifier, fontWeight: FontWeight = FontWeight.Bold, resourceString: Int) {
    Text(
        text = stringResource(resourceString),
        fontSize = 18.sp,
        modifier = modifier.padding(top = dimensionResource(id = R.dimen.margin_medium)),
        fontWeight = fontWeight,
        color = MaterialTheme.colors.onSurface
    )
}