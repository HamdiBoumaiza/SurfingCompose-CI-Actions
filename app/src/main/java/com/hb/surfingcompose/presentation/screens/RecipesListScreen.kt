package com.hb.surfingcompose.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.res.pluralStringResource
import com.hb.surfingcompose.presentation.ROUTE_DETAILS_RECIPES
import com.hb.surfingcompose.presentation.ROUTE_LIST_RECIPES
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hb.surfingcompose.R
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.models.TagsModel
import com.hb.surfingcompose.presentation.detailsRoute
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import com.hb.surfingcompose.presentation.widgets.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesListScreen(navController: NavController, viewModel: RecipesViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var collectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var libraryQuery by rememberSaveable { mutableStateOf("") }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var editCollection by rememberSaveable { mutableStateOf(false) }
    var newCollection by rememberSaveable { mutableStateOf(false) }
    val collection = state.library.collections.firstOrNull { it.id == collectionId }
    val favorites = state.library.recipes.filter {
        (collection == null || it.id in collection.recipeIds) && it.name.contains(libraryQuery.trim(), true)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        var previouslyExpanded by remember { mutableStateOf(expanded) }
        LaunchedEffect(expanded) {
            if (previouslyExpanded && !expanded && navController.currentDestination?.route == ROUTE_LIST_RECIPES) {
                state.selectedRecipeId?.let { navController.navigate(detailsRoute(it)) { launchSingleTop = true } }
            }
            previouslyExpanded = expanded
        }
        Row(Modifier.fillMaxSize()) {
            Column(if (expanded) Modifier.weight(1f) else Modifier.fillMaxSize()) {
                PrimaryTabRow(selectedTabIndex = if (state.showingLibrary) 1 else 0) {
                    Tab(selected = !state.showingLibrary, onClick = { viewModel.showLibrary(false) }, text = { Text(stringResource(R.string.discover)) })
                    Tab(selected = state.showingLibrary, onClick = { viewModel.showLibrary(true) }, text = { Text(stringResource(R.string.my_recipes)) })
                }
                if (state.showingLibrary) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        item { TextButton(onClick = { newCollection = true }) { Text(stringResource(R.string.new_collection)) } }
                        item { FilterChip(selected = collection == null, onClick = { collectionId = null }, label = { Text(stringResource(R.string.all_favorites)) }) }
                        items(state.library.collections, key = { it.id }) { item ->
                            FilterChip(selected = item.id == collectionId, onClick = { collectionId = item.id }, label = { Text(item.name) })
                        }
                    }
                    if (collection != null) {
                        Row(Modifier.padding(horizontal = 8.dp)) {
                            TextButton(onClick = { editCollection = true }) { Text(stringResource(R.string.rename_collection)) }
                            TextButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.delete_collection)) }
                        }
                    }
                }
                key(state.showingLibrary) {
                    RecipesContent(
                        recipes = if (state.showingLibrary) favorites else state.recipes,
                        query = if (state.showingLibrary) libraryQuery else state.query,
                        isLoading = if (state.showingLibrary) !state.library.isLoaded else state.isLoading,
                        error = if (state.showingLibrary) state.library.error.orEmpty() else state.error,
                        onSearch = { if (state.showingLibrary) libraryQuery = it else viewModel.searchRecipes(it) },
                        onRetry = viewModel::getRecipesFlow,
                        onRecipeClick = { recipe ->
                            viewModel.setRecipe(recipe)
                            if (!expanded || navController.currentDestination?.route == ROUTE_DETAILS_RECIPES) {
                                navController.navigate(detailsRoute(recipe.id)) {
                                    if (expanded) popUpTo(ROUTE_DETAILS_RECIPES) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        savedIds = state.library.recipes.map { it.id }.toSet(), onFavorite = { viewModel.toggleFavorite(it) },
                        selectedTag = if (state.showingLibrary) null else state.selectedTag,
                        onClearTag = { viewModel.filterByTag(null) }, libraryMode = state.showingLibrary,
                        filters = state.filters, onFilter = viewModel::setFilter, onClearFilters = viewModel::clearFilters,
                        selectedRecipeId = if (expanded) state.selectedRecipeId else null,
                        hasMore = !state.showingLibrary && state.hasMore,
                        isAppending = state.isAppending, appendError = state.appendError, onLoadMore = viewModel::loadMore
                    )
                }
            }
            if (expanded) {
                VerticalDivider()
                Box(Modifier.weight(1f).fillMaxHeight().testTag("detail_pane")) {
                    if (state.selectedRecipeId != null) {
                        SelectedRecipePane(navController, viewModel)
                    } else Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.choose_recipe), style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(R.string.choose_recipe_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (newCollection || (editCollection && collection != null)) {
        CollectionNameDialog(
            initialName = if (newCollection) "" else collection!!.name,
            title = stringResource(if (newCollection) R.string.new_collection else R.string.rename_collection),
            onSave = { name ->
                if (newCollection) viewModel.createCollection(name)?.let { collectionId = it; true } ?: false
                else viewModel.renameCollection(collection!!.id, name)
            },
            onDismiss = { newCollection = false; editCollection = false }
        )
    }
    if (confirmDelete && collection != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false }, title = { Text(stringResource(R.string.delete_collection)) },
            text = { Text(stringResource(R.string.delete_collection_message, collection.name)) },
            confirmButton = { TextButton(onClick = { viewModel.deleteCollection(collection.id); collectionId = null; confirmDelete = false }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
fun RecipesContent(
    recipes: List<RecipeModel>, query: String, isLoading: Boolean, error: String,
    onSearch: (String) -> Unit, onRetry: () -> Unit, onRecipeClick: (RecipeModel) -> Unit,
    savedIds: Set<Int> = emptySet(), onFavorite: ((RecipeModel) -> Unit)? = null,
    selectedTag: TagsModel? = null, onClearTag: () -> Unit = {}, libraryMode: Boolean = false,
    filters: List<TagsModel> = emptyList(), onFilter: (String, TagsModel?) -> Unit = { _, _ -> },
    onClearFilters: () -> Unit = {}, hasMore: Boolean = false, isAppending: Boolean = false,
    appendError: String = "", onLoadMore: () -> Unit = {}, selectedRecipeId: Int? = null
) {
    val shimmer = if (isLoading) rememberRecipeShimmer() else null
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints {
            val compact = maxWidth < 520.dp
            LazyVerticalGrid(
                modifier = Modifier.testTag("recipe_grid"),
                columns = if (compact) GridCells.Fixed(1) else GridCells.Adaptive(240.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(if (libraryMode) R.string.saved_recipes_title else R.string.discover_title), style = MaterialTheme.typography.headlineSmall)
                        SearchBar(hint = stringResource(R.string.search_recipe), initialText = query, onSearch = onSearch)
                        if (!libraryMode) DiscoveryFilters(filters, onFilter)
                    }
                }
                if (!libraryMode && (selectedTag != null || filters.isNotEmpty())) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedTag?.displayName ?: pluralStringResource(R.plurals.filters_applied, filters.size, filters.size), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                            TextButton(onClick = if (filters.isEmpty()) onClearTag else onClearFilters) { Text(stringResource(R.string.clear_filter)) }
                        }
                    }
                }
                if (error.isNotBlank()) item(span = { GridItemSpan(maxLineSpan) }) { RetryView(error, onRetry) }
                if (isLoading || recipes.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(if (isLoading) stringResource(R.string.loading_recipes) else pluralStringResource(if (libraryMode) R.plurals.saved_recipes_count else R.plurals.loaded_recipes_count, recipes.size, recipes.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (shimmer != null) {
                    items(3, key = { "loading_$it" }) { RecipeLoadingCard(progress = shimmer, announceLoading = it == 0, compact = compact) }
                } else if (recipes.isNotEmpty()) {
                    items(recipes, key = { "recipe_${it.id}" }) { recipe ->
                        RecipeCard(recipe, { onRecipeClick(recipe) }, recipe.id in savedIds, onFavorite?.let { { it(recipe) } }, compact, recipe.id == selectedRecipeId)
                    }
                } else if (error.isBlank()) item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(if (libraryMode) R.string.no_saved_recipes else R.string.no_recipes), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(if (libraryMode) R.string.save_recipes_hint else R.string.try_another_search), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (!libraryMode && !isLoading && (hasMore || appendError.isNotBlank())) item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (appendError.isNotBlank()) Text(stringResource(R.string.more_recipes_error), color = MaterialTheme.colorScheme.error)
                        if (isAppending) CircularProgressIndicator(Modifier.padding(12.dp).size(28.dp))
                        else OutlinedButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) { Text(stringResource(if (appendError.isNotBlank()) R.string.retry_more else R.string.load_more)) }
                    }
                }
                if (!libraryMode && !isLoading && !hasMore && recipes.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(stringResource(R.string.all_recipes_loaded), Modifier.fillMaxWidth().padding(8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun DiscoveryFilters(selected: List<TagsModel>, onFilter: (String, TagsModel?) -> Unit) {
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val groups = listOf("cuisine" to R.string.cuisine_filter, "diet" to R.string.diet_filter, "time" to R.string.time_filter)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(groups) { (key, label) ->
            val value = selected.firstOrNull { it.type == key }
            FilterChip(selected = value != null, onClick = { category = key }, label = { Text(value?.displayName ?: stringResource(label)) })
        }
    }
    category?.let { key ->
        val options = when (key) {
            "cuisine" -> listOf(R.string.italian to "italian", R.string.mexican to "mexican", R.string.indian to "indian", R.string.japanese to "japanese", R.string.french to "french")
            "diet" -> listOf(R.string.vegetarian to "vegetarian", R.string.vegan to "vegan", R.string.gluten_free to "gluten_free", R.string.dairy_free to "dairy_free")
            else -> listOf(R.string.under_30_minutes to "under_30_minutes", R.string.under_45_minutes to "under_45_minutes")
        }
        AlertDialog(
            onDismissRequest = { category = null },
            title = { Text(stringResource(groups.first { it.first == key }.second)) },
            text = {
                Column {
                    FilterOption(stringResource(R.string.any_filter), selected.none { it.type == key }) { onFilter(key, null); category = null }
                    options.forEach { (label, apiName) ->
                        val name = stringResource(label)
                        FilterOption(name, selected.any { it.type == key && it.name == apiName }) { onFilter(key, TagsModel(name, key, apiName)); category = null }
                    }
                }
            }, confirmButton = { TextButton(onClick = { category = null }) { Text(stringResource(R.string.done)) } }
        )
    }
}

@Composable
private fun FilterOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).then(Modifier.selectable(selected = selected, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = onClick)).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected, onClick = null)
        Text(text, Modifier.padding(start = 12.dp))
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier, hint: String = "", initialText: String = "", onSearch: (String) -> Unit = {}) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(initialText) { if (text.trim() != initialText) text = initialText }
    text.useDebounce { onSearch(it) }
    OutlinedTextField(
        value = text, onValueChange = { text = it }, modifier = modifier.fillMaxWidth(),
        label = { Text(hint) }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = { if (text.isNotEmpty()) IconButton(onClick = { text = ""; onSearch("") }) { Icon(Icons.Default.Clear, stringResource(R.string.clear_search)) } },
        singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(text); focusManager.clearFocus() })
    )
}

@Composable
fun RecipeCard(recipe: RecipeModel, onClick: () -> Unit, saved: Boolean = false, onFavorite: (() -> Unit)? = null, compact: Boolean = true, selected: Boolean = false) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("recipe_card_${recipe.id}").semantics { this.selected = selected }, shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
        if (compact) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RecipeArtwork(recipe.thumbnailUrl, recipe.name, Modifier.width(112.dp).height(136.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(recipe.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                RecipeMetadata(recipe)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.view_recipe), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    if (onFavorite != null) FavoriteButton(saved, onFavorite)
                }
            }
        } else {
            RecipeArtwork(recipe.thumbnailUrl, recipe.name, Modifier.fillMaxWidth().height(150.dp))
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(recipe.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                RecipeMetadata(recipe)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.view_recipe), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    if (onFavorite != null) FavoriteButton(saved, onFavorite)
                }
            }
        }
    }
}

@Composable
fun RecipeMetadata(recipe: RecipeModel) {
    val entries = buildList {
        recipe.prepTimeMinutes?.takeIf { it > 0 }?.let { add(stringResource(R.string.prep_minutes, it)) }
            ?: recipe.totalTimeMinutes?.takeIf { it > 0 }?.let { add(stringResource(R.string.total_minutes, it)) }
        recipe.servings?.takeIf { it > 0 }?.let { add(stringResource(R.string.servings_value, it)) }
    }
    Text(if (entries.isEmpty()) pluralStringResource(R.plurals.recipe_steps, recipe.instructions.size, recipe.instructions.size) else entries.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun RetryView(error: String, onRetry: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.errorContainer) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.load_error), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}
