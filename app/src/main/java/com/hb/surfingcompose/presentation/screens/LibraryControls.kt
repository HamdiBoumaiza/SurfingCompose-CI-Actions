package com.hb.surfingcompose.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hb.surfingcompose.R
import com.hb.surfingcompose.domain.library.LibraryState
import com.hb.surfingcompose.domain.models.RecipeModel

@Composable
fun FavoriteButton(saved: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = stringResource(if (saved) R.string.remove_favorite else R.string.save_favorite),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CollectionDialog(
    recipe: RecipeModel, library: LibraryState,
    onCreate: suspend (String) -> String?, onMembership: (String, Boolean) -> Unit, onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var saving by androidx.compose.runtime.remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_to_collection)) },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (library.collections.isEmpty()) Text(stringResource(R.string.no_collections))
                library.collections.forEach { collection ->
                    val checked = recipe.id in collection.recipeIds
                    Row(
                        Modifier.fillMaxWidth().toggleable(checked, role = Role.Checkbox) { onMembership(collection.id, it) }.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked, onCheckedChange = null)
                        Text(collection.name, Modifier.weight(1f))
                    }
                }
                OutlinedTextField(
                    value = name, onValueChange = { name = it.take(60) }, singleLine = true,
                    label = { Text(stringResource(R.string.collection_name)) }
                )
                TextButton(enabled = name.isNotBlank() && !saving, onClick = {
                    scope.launch {
                        saving = true
                        try { onCreate(name)?.let { onMembership(it, true); name = "" } }
                        finally { saving = false }
                    }
                }) { Text(stringResource(R.string.create_and_add)) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) } }
    )
}

@Composable
fun CollectionNameDialog(initialName: String, title: String, onSave: suspend (String) -> Boolean, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var error by rememberSaveable { mutableStateOf(false) }
    var saving by androidx.compose.runtime.remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() }, title = { Text(title) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it.take(60); error = false },
                label = { Text(stringResource(R.string.collection_name)) }, singleLine = true,
                enabled = !saving, isError = error,
                supportingText = { if (error) Text(stringResource(R.string.collection_name_error)) })
        },
        confirmButton = { TextButton(enabled = name.isNotBlank() && !saving, onClick = {
            scope.launch {
                saving = true
                try { if (onSave(name)) onDismiss() else error = true }
                finally { saving = false }
            }
        }) { Text(stringResource(R.string.save_collection)) } },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
