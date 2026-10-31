package com.hb.surfingcompose.presentation.widgets

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.hb.surfingcompose.R

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember(context, videoUrl) {
        provideExoPlayer(context, MediaItem.fromUri(videoUrl))
    }
    DisposableEffect(player) {
        player.prepare()
        onDispose { player.release() }
    }
    DisposableEffect(player, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.dimens_200dp)),
        factory = { viewContext -> PlayerView(viewContext).apply { this.player = player } },
        update = { it.player = player },
        onReset = null,
        onRelease = { it.player = null }
    )
}

fun provideExoPlayer(context: Context, mediaItem: MediaItem): ExoPlayer {
    val player = ExoPlayer.Builder(context).build()
    player.setMediaItem(mediaItem)
    return player
}
