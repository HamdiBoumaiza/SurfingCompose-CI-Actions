package com.hb.surfingcompose.presentation.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.dp
import com.hb.surfingcompose.R

@Composable
fun rememberRecipeShimmer(): State<Float> {
    val transition = rememberInfiniteTransition(label = "Recipe loading shimmer")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Shimmer position"
    )
}

@Composable
fun RecipeLoadingCard(progress: State<Float>, announceLoading: Boolean = false, compact: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    val loadingDescription = stringResource(R.string.loading_recipes)
    val placeholder = Modifier
        .clip(MaterialTheme.shapes.small)
        .shimmer(progress, colors.surfaceVariant, colors.surface)
    Card(
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics {
            if (announceLoading) {
                contentDescription = loadingDescription
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                liveRegion = LiveRegionMode.Polite
            }
        },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        if (compact) Row {
            Spacer(Modifier.width(112.dp).height(136.dp).shimmer(progress, colors.surfaceVariant, colors.surface))
            Column(Modifier.weight(1f).padding(12.dp)) {
                Spacer(Modifier.fillMaxWidth().height(24.dp).then(placeholder))
                Spacer(Modifier.height(12.dp))
                Spacer(Modifier.fillMaxWidth(0.8f).height(16.dp).then(placeholder))
                Spacer(Modifier.height(24.dp))
                Spacer(Modifier.fillMaxWidth(0.6f).height(16.dp).then(placeholder))
            }
        } else Column {
            Spacer(Modifier.fillMaxWidth().height(150.dp).shimmer(progress, colors.surfaceVariant, colors.surface))
            Column(Modifier.padding(12.dp)) {
                Spacer(Modifier.fillMaxWidth(0.75f).height(26.dp).then(placeholder))
                Spacer(Modifier.height(12.dp))
                Spacer(Modifier.fillMaxWidth().height(16.dp).then(placeholder))
                Spacer(Modifier.height(8.dp))
                Spacer(Modifier.fillMaxWidth(0.85f).height(16.dp).then(placeholder))
                Spacer(Modifier.height(14.dp))
                Spacer(Modifier.fillMaxWidth(0.3f).height(20.dp).then(placeholder))
            }
        }
    }
}

private fun Modifier.shimmer(progress: State<Float>, base: Color, highlight: Color): Modifier =
    drawWithCache {
        val bandWidth = size.width * 0.8f
        val brush = Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset.Zero,
            end = Offset(bandWidth, 0f)
        )
        onDrawBehind {
            drawRect(base)
            // Read the animated state during drawing so animation does not recompose the list.
            translate(left = -bandWidth + (size.width + bandWidth) * progress.value) {
                drawRect(brush, size = Size(bandWidth, size.height))
            }
        }
    }
