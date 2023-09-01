package com.hb.surfingcompose.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.hb.surfingcompose.domain.models.InstructionsModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.ui.theme.LightGreen
import com.hb.surfingcompose.ui.theme.LightYellow
import com.hb.surfingcompose.viewmodel.RecipesViewModel
import com.hb.surfingcompose.widgets.AppBar
import com.hb.surfingcompose.widgets.VideoPlayer

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
                error = painterResource(R.drawable.ic_place_holder),
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

            TextSubTitle(resourceString = R.string.nutritional_value)
            NutritionValues(recipe)
            Spacer(modifier = Modifier.height(10.dp))
            TextSubTitle(resourceString = R.string.instructions_list)
            Spacer(modifier = Modifier.height(10.dp))
            InstructionsList(recipe.instructions)
            Spacer(modifier = Modifier.height(10.dp))
            TextSubTitle(resourceString = R.string.see_how_to)
            Spacer(modifier = Modifier.height(8.dp))
            VideoPlayer(recipe.originalVideoUrl)
            Spacer(modifier = Modifier.height(10.dp))
            ListTags(recipe)
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
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) { items(recipe.tags) { tag -> TagChip(tag.displayName) } }
    }
}

@Composable
private fun InstructionsList(instructions: List<InstructionsModel>) {
    LazyColumn(contentPadding = PaddingValues(4.dp)) {
        items(instructions) { instruction -> InstructionRow(instruction) }
    }
}

@Composable
fun InstructionRow(instruction: InstructionsModel) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.padding(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            Text(
                text = instruction.position.toString(),
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(4.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )

            Text(
                text = instruction.displayText,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .padding(4.dp)
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
            .padding(top = 4.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            text = key
        )
        Text(modifier = Modifier.padding(8.dp), text = "$value $unit")
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
        modifier = modifier.padding(top = 16.dp),
        fontWeight = fontWeight,
        color = MaterialTheme.colors.onSurface
    )
}