package com.hb.surfingcompose.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SuggestionChip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import com.hb.surfingcompose.presentation.cookingRoute
import com.hb.surfingcompose.domain.models.TagsModel
import com.hb.surfingcompose.presentation.ROUTE_LIST_RECIPES
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hb.surfingcompose.R
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import com.hb.surfingcompose.presentation.widgets.AppBar
import com.hb.surfingcompose.presentation.widgets.RecipeArtwork
import com.hb.surfingcompose.presentation.widgets.FullScreenVideoPlayer

@Composable
fun RecipeDetailScreen(navController: NavController, viewModel: RecipesViewModel) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            AppBar(stringResource(R.string.details), navController)
            SelectedRecipePane(navController, viewModel)
        }
    }
}

@Composable
fun LoadingRecipe() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun SelectedRecipePane(navController: NavController, viewModel: RecipesViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val recipe = state.selectedRecipe
    var showCollections by rememberSaveable(recipe?.id) { mutableStateOf(false) }
    when {
        state.isLoadingRecipe -> LoadingRecipe()
        recipe != null -> key(recipe.id) {
            RecipeDetailContent(
                recipe, saved = state.library.recipes.any { it.id == recipe.id },
                onFavorite = { viewModel.toggleFavorite(recipe) }, onCollections = { showCollections = true },
                onCook = { navController.navigate(cookingRoute(recipe.id)) { launchSingleTop = true } },
                onTag = { tag -> viewModel.filterByTag(tag); navController.popBackStack(ROUTE_LIST_RECIPES, false) },
                completedSteps = state.completedSteps,
                onCompletedSteps = { viewModel.setCompletedSteps(recipe.id, it) }
            )
        }
        else -> Column(Modifier.padding(24.dp)) {
            Text(stringResource(R.string.recipe_unavailable))
            if (state.recipeError.isNotBlank()) Text(state.recipeError)
            TextButton(onClick = { state.selectedRecipeId?.let(viewModel::selectRecipe) }) { Text(stringResource(R.string.retry)) }
        }
    }
    if (showCollections && recipe != null) CollectionDialog(
        recipe, state.library, viewModel::createCollection,
        onMembership = { id, included -> viewModel.setMembership(recipe, id, included) }, onDismiss = { showCollections = false }
    )
}

@Composable
fun RecipeDetailContent(
    recipe: RecipeModel, saved: Boolean = false, onFavorite: (() -> Unit)? = null,
    onCollections: (() -> Unit)? = null, onCook: (() -> Unit)? = null,
    onTag: (TagsModel) -> Unit = {},
    completedSteps: Set<Int>? = null, onCompletedSteps: (Set<Int>) -> Unit = {}
) {
    var localCompleted by rememberSaveable(recipe.id) { mutableStateOf(emptyList<Int>()) }
    val completed = completedSteps ?: localCompleted.toSet()
    val updateCompleted: (Set<Int>) -> Unit = { if (completedSteps == null) localCompleted = it.toList() else onCompletedSteps(it) }
    var showVideo by rememberSaveable(recipe.id) { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.testTag("recipe_detail"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            RecipeArtwork(
                recipe.thumbnailUrl, recipe.name,
                Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.large)
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(recipe.name, style = MaterialTheme.typography.headlineSmall)
                RecipeMetadata(recipe)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (onFavorite != null) FavoriteButton(saved, onFavorite)
                    if (onCollections != null) TextButton(onClick = onCollections) { Text(stringResource(R.string.add_to_collection)) }
                }
                RecipeTags(recipe, onTag)
                if (onCook != null && recipe.instructions.isNotEmpty()) {
                    Button(onClick = onCook, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.start_cooking)) }
                }
                if (recipe.description.isNotBlank()) {
                    Text(recipe.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.nutritional_value), style = MaterialTheme.typography.titleLarge)
                NutritionValues(recipe)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.instructions_list), style = MaterialTheme.typography.titleLarge)
                if (recipe.instructions.isNotEmpty()) {
                    Text(
                        pluralStringResource(R.plurals.steps_completed, recipe.instructions.size, completed.size, recipe.instructions.size),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(stringResource(R.string.no_instructions), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        items(recipe.instructions, key = { it.position }) { instruction ->
            val checked = instruction.position in completed
            Surface(shape = MaterialTheme.shapes.medium, color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier.fillMaxWidth().toggleable(value = checked, role = Role.Checkbox) {
                        updateCompleted(if (it) completed + instruction.position else completed - instruction.position)
                    }.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(top = 2.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.step_number, instruction.position), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(instruction.displayText, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        if (recipe.originalVideoUrl.isNotBlank()) {
            item {
                Button(onClick = { showVideo = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text(stringResource(R.string.see_how_to), Modifier.padding(start = 8.dp))
                }
            }
        }
    }
    if (showVideo && recipe.originalVideoUrl.isNotBlank()) {
        FullScreenVideoPlayer(recipe.originalVideoUrl, onDismiss = { showVideo = false })
    }
}

@Composable
fun NutritionValues(recipe: RecipeModel) {
    val nutrition = recipe.nutrition
    val entries = listOf(
        R.string.calories to stringResource(R.string.energy_value, nutrition.calories),
        R.string.protein to stringResource(R.string.grams_value, nutrition.protein),
        R.string.carbohydrates to stringResource(R.string.grams_value, nutrition.carbohydrates),
        R.string.fat to stringResource(R.string.grams_value, nutrition.fat),
        R.string.fiber to stringResource(R.string.grams_value, nutrition.fiber),
        R.string.sugar to stringResource(R.string.grams_value, nutrition.sugar)
    )
    BoxWithConstraints {
        // Two columns leave room for translated labels and large accessibility fonts.
        val fontScale = LocalDensity.current.fontScale
        val columnCount = if (maxWidth < 300.dp || fontScale > 1.3f) 2 else 3
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            entries.chunked(columnCount).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (label, value) ->
                        Surface(Modifier.weight(1f), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(value, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    repeat(columnCount - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun RecipeTags(recipe: RecipeModel, onTag: (TagsModel) -> Unit) {
    val tags = recipe.tags.filter { it.displayName.isNotBlank() }.distinctBy { it.displayName }
    if (tags.isEmpty()) return
    var expanded by rememberSaveable(recipe.id) { mutableStateOf(false) }
    Column {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (if (expanded) tags else tags.take(3)).forEach { tag ->
                SuggestionChip(onClick = { onTag(tag) }, label = { Text(tag.displayName) })
            }
        }
        if (tags.size > 3) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) stringResource(R.string.fewer_tags) else stringResource(R.string.all_tags, tags.size))
            }
        }
    }
}
