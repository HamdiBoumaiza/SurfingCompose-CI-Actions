package com.hb.surfingcompose.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.domain.library.*
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.models.TagsModel
import com.hb.surfingcompose.domain.request.Constants.DEFAULT_GET_RECIPES_SIZE
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UndoFavorite(val token: Long, val removal: FavoriteRemoval)
data class RecipesUiState(
    val recipes: List<RecipeModel> = emptyList(),
    val query: String = "",
    val selectedTag: TagsModel? = null,
    val filters: List<TagsModel> = emptyList(),
    val showingLibrary: Boolean = false,
    val library: LibraryState = LibraryState(isLoaded = false),
    val isLoading: Boolean = true,
    val isAppending: Boolean = false,
    val error: String = "",
    val appendError: String = "",
    val hasMore: Boolean = false,
    val selectedRecipeId: Int? = null,
    val selectedRecipe: RecipeModel? = null,
    val isLoadingRecipe: Boolean = false,
    val recipeError: String = "",
    val completedSteps: Set<Int> = emptySet(),
    val undoFavorite: UndoFavorite? = null,
    val actionError: String? = null
)

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val library: RecipeLibrary = MemoryRecipeLibrary()
) : ViewModel() {
    private val mutableState = MutableStateFlow(RecipesUiState(
        query = savedStateHandle.get<String>(SEARCH_QUERY).orEmpty(),
        selectedTag = savedStateHandle["selected_tag"],
        filters = savedStateHandle.get<ArrayList<TagsModel>>("filters").orEmpty(),
        showingLibrary = savedStateHandle["showing_library"] ?: false,
        selectedRecipeId = savedStateHandle[SELECTED_RECIPE_ID]
    ))
    val uiState: StateFlow<RecipesUiState> = mutableState.asStateFlow()
    private var requestJob: Job? = null
    private var selectionJob: Job? = null
    private var requestId = 0L
    private var nextOffset = 0
    private var undoToken = 0L

    init {
        viewModelScope.launch { library.state.collect { value -> mutableState.update { it.copy(library = value) } } }
        getRecipesFlow()
        uiState.value.selectedRecipeId?.let(::selectRecipe)
    }

    fun showLibrary(show: Boolean) {
        savedStateHandle["showing_library"] = show
        mutableState.update { it.copy(showingLibrary = show) }
    }
    fun searchRecipes(query: String) {
        val normalized = query.trim()
        if (normalized == uiState.value.query) return
        savedStateHandle[SEARCH_QUERY] = normalized
        mutableState.update { it.copy(query = normalized) }
        getRecipesFlow()
    }
    fun filterByTag(tag: TagsModel?) {
        if (tag != null) showLibrary(false)
        if (uiState.value.selectedTag == tag) return
        savedStateHandle["selected_tag"] = tag
        mutableState.update { it.copy(selectedTag = tag) }
        getRecipesFlow()
    }
    fun setFilter(category: String, tag: TagsModel?) {
        val filters = uiState.value.filters.filterNot { it.type == category } + listOfNotNull(tag)
        if (filters == uiState.value.filters) return
        savedStateHandle["filters"] = ArrayList(filters)
        mutableState.update { it.copy(filters = filters) }
        getRecipesFlow()
    }
    fun clearFilters() {
        savedStateHandle["selected_tag"] = null
        savedStateHandle["filters"] = arrayListOf<TagsModel>()
        mutableState.update { it.copy(filters = emptyList(), selectedTag = null) }
        getRecipesFlow()
    }
    fun getRecipesFlow() = fetchPage(append = false)
    fun loadMore() {
        val state = uiState.value
        if (state.isLoading || state.isAppending || !state.hasMore) return
        fetchPage(append = true)
    }
    private fun fetchPage(append: Boolean) {
        requestJob?.cancel()
        val currentRequest = ++requestId
        if (!append) nextOffset = 0
        val snapshot = uiState.value
        val request = RecipesRequest(nextOffset, DEFAULT_GET_RECIPES_SIZE, snapshot.query, snapshot.selectedTag, snapshot.filters)
        mutableState.update {
            it.copy(recipes = if (append) it.recipes else emptyList(), isLoading = !append,
                isAppending = append, error = "", appendError = "", hasMore = if (append) it.hasMore else false)
        }
        requestJob = viewModelScope.launch {
            try {
                getRecipesUseCase(request).collect { result ->
                    if (currentRequest != requestId) return@collect
                    when (result) {
                        is SurfaceResult.Success -> {
                            nextOffset = request.from + result.data.size
                            mutableState.update {
                                it.copy(recipes = ((if (append) it.recipes else emptyList()) + result.data).distinctBy { recipe -> recipe.id },
                                    isLoading = false, isAppending = false, hasMore = result.data.size >= request.size,
                                    error = "", appendError = "")
                            }
                        }
                        is SurfaceResult.Error -> pageError(currentRequest, append, result.exception)
                        SurfaceResult.Loading -> Unit
                    }
                }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { pageError(currentRequest, append, error) }
            finally {
                if (currentRequest == requestId) mutableState.update { it.copy(isLoading = false, isAppending = false) }
            }
        }
    }
    private fun pageError(id: Long, append: Boolean, error: Exception) {
        if (id != requestId) return
        val message = error.message ?: "Unable to load recipes"
        mutableState.update { it.copy(error = if (append) "" else message, appendError = if (append) message else "", isLoading = false, isAppending = false) }
    }

    fun setRecipe(recipe: RecipeModel) {
        selectionJob?.cancel()
        savedStateHandle[SELECTED_RECIPE_ID] = recipe.id
        mutableState.update { it.copy(selectedRecipeId = recipe.id, selectedRecipe = recipe,
            isLoadingRecipe = false, recipeError = "", completedSteps = readProgress(recipe.id)) }
    }
    fun selectRecipe(id: Int) {
        if (uiState.value.selectedRecipe?.id == id) return
        val inMemory = (uiState.value.recipes + uiState.value.library.recipes).firstOrNull { it.id == id }
        if (inMemory != null) { setRecipe(inMemory); return }
        selectionJob?.cancel()
        savedStateHandle[SELECTED_RECIPE_ID] = id
        mutableState.update { it.copy(selectedRecipeId = id, selectedRecipe = null, isLoadingRecipe = true, recipeError = "", completedSteps = readProgress(id)) }
        selectionJob = viewModelScope.launch {
            try {
                val recipe = getRecipesUseCase.recipe(id) ?: library.state.first { it.isLoaded }.recipes.firstOrNull { it.id == id }
                if (uiState.value.selectedRecipeId == id) mutableState.update { it.copy(selectedRecipe = recipe, isLoadingRecipe = false) }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                if (uiState.value.selectedRecipeId == id) mutableState.update { it.copy(isLoadingRecipe = false, recipeError = error.message.orEmpty()) }
            }
        }
    }
    fun getRecipe(): RecipeModel? = uiState.value.selectedRecipe
    private fun readProgress(id: Int): Set<Int> = savedStateHandle.get<ArrayList<Int>>("progress_$id").orEmpty().toSet()
    fun setCompletedSteps(id: Int, positions: Set<Int>) {
        savedStateHandle["progress_$id"] = ArrayList(positions)
        if (id == uiState.value.selectedRecipeId) mutableState.update { it.copy(completedSteps = positions) }
    }

    fun toggleFavorite(recipe: RecipeModel) = libraryAction {
        val removal = library.toggleFavorite(recipe)
        mutableState.update { it.copy(undoFavorite = removal?.let { UndoFavorite(++undoToken, it) }) }
    }
    fun undoFavorite(token: Long) = libraryAction {
        val undo = uiState.value.undoFavorite?.takeIf { it.token == token } ?: return@libraryAction
        library.restoreFavorite(undo.removal)
        dismissUndo(token)
    }
    fun dismissUndo(token: Long) { mutableState.update { if (it.undoFavorite?.token == token) it.copy(undoFavorite = null) else it } }
    suspend fun createCollection(name: String): String? = try { library.createCollection(name) }
        catch (error: CancellationException) { throw error }
        catch (error: Exception) { actionError(error); null }
    suspend fun renameCollection(id: String, name: String): Boolean = try { library.renameCollection(id, name) }
        catch (error: CancellationException) { throw error }
        catch (error: Exception) { actionError(error); false }
    fun setMembership(recipe: RecipeModel, collectionId: String, included: Boolean) = libraryAction { library.setMembership(recipe, collectionId, included) }
    fun deleteCollection(id: String) = libraryAction { library.deleteCollection(id) }
    fun clearActionError() { mutableState.update { it.copy(actionError = null) } }
    private fun libraryAction(action: suspend () -> Unit) = viewModelScope.launch {
        try { action() }
        catch (error: CancellationException) { throw error }
        catch (error: Exception) { actionError(error) }
    }
    private fun actionError(error: Exception) { mutableState.update { it.copy(actionError = error.message ?: "Unable to update saved recipes") } }
    private companion object {
        const val SELECTED_RECIPE_ID = "selected_recipe_id"
        const val SEARCH_QUERY = "search_query"
    }
}
