package com.hb.surfingcompose.widgets

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

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
            .height(200.dp),
        factory = { playerView }
    )
}

fun provideExoPlayer(context: Context, mediaItem: MediaItem): ExoPlayer {
    val player = ExoPlayer.Builder(context).build()
    player.setMediaItem(mediaItem)
    return player
}