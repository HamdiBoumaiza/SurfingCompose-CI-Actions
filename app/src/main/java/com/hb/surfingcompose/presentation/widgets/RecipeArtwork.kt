package com.hb.surfingcompose.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.hb.surfingcompose.R

@Composable
fun RecipeArtwork(url: String, description: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = description,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { ArtworkPlaceholder() },
        error = { ArtworkPlaceholder() }
    )
}

@Composable
private fun ArtworkPlaceholder() {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_chef_hat), contentDescription = null,
            modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary
        )
    }
}
