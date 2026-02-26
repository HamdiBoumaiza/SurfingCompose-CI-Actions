package com.hb.surfingcompose.presentation.widgets

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hb.surfingcompose.R

@Composable
fun VideoPlayer(videoUrl: String) {
    val playWhenReady by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaItem = MediaItem.fromUri(videoUrl)
    val playerView = PlayerView(context)
    val player = provideExoPlayer(context = context, mediaItem = mediaItem)
    playerView.player = player
    LaunchedEffect(player) {
        player.prepare()
        player.playWhenReady = playWhenReady
    }
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .height(dimensionResource(id = R.dimen.dimens_200dp)),
        factory = { playerView }
    )
}

fun provideExoPlayer(context: Context, mediaItem: MediaItem): ExoPlayer {
    val player = ExoPlayer.Builder(context).build()
    player.setMediaItem(mediaItem)
    return player
}