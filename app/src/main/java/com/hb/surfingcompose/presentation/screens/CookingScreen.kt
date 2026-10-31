package com.hb.surfingcompose.presentation.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.hb.surfingcompose.R
import com.hb.surfingcompose.domain.models.RecipeModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CookingScreen(recipe: RecipeModel?, completedSteps: Set<Int>? = null, onCompletedSteps: (Set<Int>) -> Unit = {}, onExit: () -> Unit) {
    if (recipe == null) {
        Column(Modifier.padding(24.dp)) {
            Text(stringResource(R.string.recipe_unavailable))
            TextButton(onClick = onExit) { Text(stringResource(R.string.back)) }
        }
        return
    }
    val instructions = recipe.instructions.sortedBy { it.position }
    var localCompleted by rememberSaveable(recipe.id) { mutableStateOf(emptyList<Int>()) }
    val completed = completedSteps ?: localCompleted.toSet()
    val updateCompleted: (Set<Int>) -> Unit = { if (completedSteps == null) localCompleted = it.toList() else onCompletedSteps(it) }
    var step by rememberSaveable(recipe.id) { mutableIntStateOf(instructions.indexOfFirst { it.position !in completed }.coerceAtLeast(0)) }
    var finished by rememberSaveable(recipe.id) { mutableStateOf(instructions.isNotEmpty() && instructions.all { it.position in completed }) }
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val timerSaver = Saver<CookingTimer, List<Long>>(
        save = { listOf(it.remainingMs, it.deadlineMs ?: -1, if (it.started) 1L else 0L) },
        restore = { CookingTimer(it[0], it[1].takeIf { value -> value >= 0 }, it[2] == 1L) }
    )
    var timer by rememberSaveable(recipe.id, stateSaver = timerSaver) { mutableStateOf(CookingTimer()) }
    var timerAnnounced by rememberSaveable(recipe.id) { mutableStateOf(false) }
    val requestExit = {
        if (timer.deadlineMs != null || (timer.started && timer.remainingMs > 0)) confirmExit = true else onExit()
    }
    BackHandler(onBack = requestExit)
    KeepCookingScreenAwake()
    LaunchedEffect(timer.deadlineMs) {
        while (timer.deadlineMs != null) {
            timer = timer.tick(SystemClock.elapsedRealtime())
            delay(250)
        }
    }
    LaunchedEffect(timer.finished) {
        if (!timer.finished) timerAnnounced = false
        if (timer.finished && !timerAnnounced) {
            timerAnnounced = true
            val tone = runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80) }.getOrNull()
            try {
                tone?.startTone(ToneGenerator.TONE_PROP_ACK, 500)
                delay(600)
            } finally { tone?.release() }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Scaffold(
            modifier = Modifier.widthIn(max = 900.dp).fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CookingTimerControls(timer, onChange = { timer = it })
                        if (instructions.isNotEmpty() && !finished) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { step-- }, enabled = step > 0, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.previous_step)) }
                                Button(onClick = {
                                    val updated = completed + instructions[step].position
                                    updateCompleted(updated)
                                    if (step < instructions.lastIndex) step++
                                    else if (instructions.all { it.position in updated }) finished = true
                                    else step = instructions.indexOfFirst { it.position !in updated }
                                }, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(if (step == instructions.lastIndex) R.string.finish_cooking else R.string.complete_next))
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    TextButton(onClick = requestExit) { Text(stringResource(R.string.exit_cooking)) }
                    Text(recipe.name, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.screen_stays_awake), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (instructions.isEmpty()) item { Text(stringResource(R.string.no_instructions)) }
                else if (finished) item {
                    Text(stringResource(R.string.cooking_complete), style = MaterialTheme.typography.headlineLarge)
                    Text(stringResource(R.string.enjoy_meal))
                    Button(onClick = requestExit) { Text(stringResource(R.string.done)) }
                    TextButton(onClick = { step = 0; updateCompleted(emptySet()); finished = false }) { Text(stringResource(R.string.cook_again)) }
                } else {
                    item {
                        Text(stringResource(R.string.cooking_step, step + 1, instructions.size), style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(progress = { instructions.count { it.position in completed }.toFloat() / instructions.size }, modifier = Modifier.fillMaxWidth())
                        Text(stringResource(R.string.cooking_progress, instructions.count { it.position in completed }, instructions.size), Modifier.padding(top = 8.dp))
                    }
                    item {
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(instructions[step].displayText, Modifier.fillMaxWidth().padding(20.dp), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    item {
                        val position = instructions[step].position
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(position in completed, role = Role.Checkbox) { checked ->
                            updateCompleted(if (checked) completed + position else completed - position)
                        }, verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = position in completed, onCheckedChange = null)
                            Text(stringResource(R.string.mark_step_complete), Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text(stringResource(R.string.stop_timer_title)) },
            text = { Text(stringResource(R.string.stop_timer_message)) },
            confirmButton = { TextButton(onClick = { confirmExit = false; onExit() }) { Text(stringResource(R.string.exit_cooking)) } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text(stringResource(R.string.keep_cooking)) } }
        )
    }
}

@Composable
private fun CookingTimerControls(timer: CookingTimer, onChange: (CookingTimer) -> Unit) {
    val focusManager = LocalFocusManager.current
    var minutes by rememberSaveable { mutableStateOf("5") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val seconds = (timer.remainingMs + 999) / 1000
    val timerText = if (timer.finished) stringResource(R.string.timer_finished) else String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.cooking_timer), style = MaterialTheme.typography.labelLarge)
                if (timer.started) Text(timerText, style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { if (timer.finished) liveRegion = LiveRegionMode.Assertive })
            }
            if (timer.started) {
                if (!timer.finished) TextButton(onClick = { onChange(if (timer.deadlineMs == null) timer.resume(SystemClock.elapsedRealtime()) else timer.pause(SystemClock.elapsedRealtime())) }) {
                    Text(stringResource(if (timer.deadlineMs == null) R.string.resume_timer else R.string.pause_timer))
                }
                TextButton(onClick = { onChange(CookingTimer()); expanded = false }) { Text(stringResource(R.string.reset_timer)) }
            } else TextButton(onClick = { expanded = !expanded }) { Text(stringResource(R.string.timer_settings)) }
        }
        if (expanded && !timer.started) {
            Text(stringResource(R.string.timer_scope), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minutes, onValueChange = { value -> if (value.length <= 3 && value.all(Char::isDigit)) minutes = value },
                    label = { Text(stringResource(R.string.timer_minutes)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }), modifier = Modifier.weight(1f)
                )
                Button(enabled = (minutes.toIntOrNull() ?: 0) in 1..180, onClick = {
                    onChange(timer.start(minutes.toLong() * 60_000, SystemClock.elapsedRealtime())); expanded = false; focusManager.clearFocus()
                }) { Text(stringResource(R.string.start_timer)) }
            }
        }
    }
}

@Composable
private fun KeepCookingScreenAwake() {
    val view = LocalView.current
    val owner = LocalLifecycleOwner.current
    DisposableEffect(view, owner) {
        val previous = view.keepScreenOn
        val observer = LifecycleEventObserver { _, _ ->
            view.keepScreenOn = previous || owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        owner.lifecycle.addObserver(observer)
        view.keepScreenOn = previous || owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            view.keepScreenOn = previous
        }
    }
}
