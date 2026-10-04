package com.rogbandroid.rogermote.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt
import com.rogbandroid.rogermote.data.TvApplication
import com.rogbandroid.rogermote.R
import com.rogbandroid.rogermote.tv.ConnectionState
import com.rogbandroid.rogermote.tv.RemoteCommand
import com.rogbandroid.rogermote.tv.displayText
import com.rogbandroid.rogermote.ui.theme.RogermoteTheme
import com.rogbandroid.rogermote.viewmodel.RemoteUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val RemoteBackground = Color(0xFF090B0D)
private val RemoteSurface = Color(0xFF171A1D)
private val RemoteSurfaceStrong = Color(0xFF202529)
private val RemoteAccent = Color(0xFF10BCEB)
private val RemoteAccentText = Color(0xFF00151D)
private val RemoteControlIcon = Color(0xFFF2F5F6)
private val ButtonTop = Color(0xFF30383D)
private val ButtonBottom = Color(0xFF111416)
private val ButtonEdge = Color(0xFF647178).copy(alpha = 0.72f)
private val AccentTop = Color(0xFF37D5F4)
private val AccentBottom = Color(0xFF0787B2)
private val AccentEdge = Color(0xFF8CEBFA).copy(alpha = 0.9f)
private val PowerOffRed = Color(0xFFD32F2F)

private fun Modifier.raisedButton(shape: RoundedCornerShape): Modifier =
    shadow(elevation = 5.dp, shape = shape)
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(listOf(ButtonTop, ButtonBottom)),
            shape = shape,
        )

private fun Modifier.raisedAccent(shape: Shape): Modifier =
    shadow(elevation = 7.dp, shape = shape)
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(listOf(AccentTop, AccentBottom)),
            shape = shape,
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(listOf(AccentEdge, Color(0xFF005B78))),
            shape = shape,
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(listOf(ButtonEdge, Color.Black.copy(alpha = 0.82f))),
            shape = shape,
        )

@Composable
fun RemoteScreen(
    uiState: RemoteUiState,
    onCommand: (RemoteCommand) -> Unit,
    onLaunchApplication: (TvApplication) -> Unit,
    onReorderApplications: (List<TvApplication>) -> Unit,
    onDisconnect: () -> Unit,
    onOpenSetup: () -> Unit,
) {
    val controlsEnabled = uiState.isConnected
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = RemoteBackground,
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onCommand(RemoteCommand.PowerOff)
                    },
                    enabled = controlsEnabled,
                    modifier = Modifier.semantics { contentDescription = "Power off" },
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = PowerOffRed)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rogermote", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = uiState.connectionState.displayText(),
                        color = if (uiState.isConnected) RemoteAccent else Color(0xFFAAB4BA),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Row {
                    IconButton(
                        onClick = onDisconnect,
                        enabled = controlsEnabled,
                        modifier = Modifier.semantics { contentDescription = "Disconnect" },
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                    IconButton(onClick = onOpenSetup, modifier = Modifier.semantics {
                        contentDescription = "TV setup and settings"
                    }) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RemoteActionButton(
                    icon = Icons.Default.LiveTv,
                    label = "Source",
                    enabled = controlsEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    onClick = { onCommand(RemoteCommand.Source) },
                    modifier = Modifier.weight(1f),
                )
                RemoteActionButton(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    enabled = controlsEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    onClick = { onCommand(RemoteCommand.Menu) },
                    modifier = Modifier.weight(1f),
                )
                RemoteActionButton(
                    icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                    label = "Guide",
                    enabled = controlsEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    onClick = { onCommand(RemoteCommand.Guide) },
                    modifier = Modifier.weight(1f),
                )
                RemoteActionButton(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    label = "Exit",
                    enabled = controlsEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    onClick = { onCommand(RemoteCommand.Exit) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val navigationSize = minOf(240.dp, maxWidth - 136.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TallIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        description = "Back",
                        enabled = controlsEnabled,
                        hapticsEnabled = uiState.hapticsEnabled,
                        modifier = Modifier.width(60.dp).height(navigationSize),
                    ) {
                        onCommand(RemoteCommand.Back)
                    }
                    NavigationPad(
                        enabled = controlsEnabled,
                        hapticsEnabled = uiState.hapticsEnabled,
                        onCommand = onCommand,
                        modifier = Modifier.size(navigationSize),
                    )
                    TallIconButton(
                        icon = Icons.Default.Home,
                        description = "Home",
                        enabled = controlsEnabled,
                        hapticsEnabled = uiState.hapticsEnabled,
                        modifier = Modifier.width(60.dp).height(navigationSize),
                    ) {
                        onCommand(RemoteCommand.Home)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VerticalRemoteControl(
                    label = "Vol",
                    upDescription = "Volume up",
                    downDescription = "Volume down",
                    upIcon = Icons.AutoMirrored.Filled.VolumeUp,
                    downIcon = Icons.AutoMirrored.Filled.VolumeDown,
                    enabled = controlsEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    onUp = { onCommand(RemoteCommand.VolumeUp) },
                    onDown = { onCommand(RemoteCommand.VolumeDown) },
                    modifier = Modifier.width(60.dp),
                )
                Column(
                    modifier = Modifier.weight(2.4f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.height(52.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SmallIconButton(
                            Icons.AutoMirrored.Filled.VolumeOff,
                            "Mute",
                            controlsEnabled,
                            uiState.hapticsEnabled,
                            modifier = Modifier.weight(1f),
                            fillWidth = true,
                        ) {
                            onCommand(RemoteCommand.Mute)
                        }
                        SmallIconButton(
                            Icons.Default.Info,
                            "Info",
                            controlsEnabled,
                            uiState.hapticsEnabled,
                            modifier = Modifier.weight(1f),
                            fillWidth = true,
                        ) {
                            onCommand(RemoteCommand.Info)
                        }
                    }
                    SmallTextButton(
                        "Prev",
                        "Previous channel",
                        controlsEnabled,
                        uiState.hapticsEnabled,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        onCommand(RemoteCommand.PreviousChannel)
                    }
                }
                VerticalRemoteControl(
                    label = "Ch",
                    upDescription = "Channel up",
                    downDescription = "Channel down",
                    upIcon = Icons.Default.KeyboardArrowUp,
                    downIcon = Icons.Default.KeyboardArrowDown,
                    enabled = controlsEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    onUp = { onCommand(RemoteCommand.ChannelUp) },
                    onDown = { onCommand(RemoteCommand.ChannelDown) },
                    modifier = Modifier.width(60.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF354047), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            ShortcutRow(
                applications = uiState.installedApplications,
                enabled = controlsEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                onLaunchApplication = onLaunchApplication,
                onReorderApplications = onReorderApplications,
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF354047), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            MoreControls(
                modifier = Modifier.weight(1f),
                enabled = controlsEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                onCommand = onCommand,
            )
        }
    }
}

@Composable
private fun NavigationPad(
    enabled: Boolean,
    hapticsEnabled: Boolean,
    onCommand: (RemoteCommand) -> Unit,
    modifier: Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Box(modifier, contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.fillMaxSize().raisedAccent(CircleShape),
        ) {}
        PadIconButton(
            icon = Icons.Default.KeyboardArrowUp,
            description = "Up",
            enabled = enabled,
            hapticsEnabled = hapticsEnabled,
            repeatOnHold = true,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp).offset(y = (-16).dp),
            onClick = { onCommand(RemoteCommand.Up) },
            iconTint = RemoteControlIcon,
        )
        PadIconButton(
            icon = Icons.Default.KeyboardArrowDown,
            description = "Down",
            enabled = enabled,
            hapticsEnabled = hapticsEnabled,
            repeatOnHold = true,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp).offset(y = 16.dp),
            onClick = { onCommand(RemoteCommand.Down) },
            iconTint = RemoteControlIcon,
        )
        PadIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            description = "Left",
            enabled = enabled,
            hapticsEnabled = hapticsEnabled,
            repeatOnHold = true,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp).offset(x = (-16).dp),
            onClick = { onCommand(RemoteCommand.Left) },
            iconTint = RemoteControlIcon,
        )
        PadIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            description = "Right",
            enabled = enabled,
            hapticsEnabled = hapticsEnabled,
            repeatOnHold = true,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).offset(x = 16.dp),
            onClick = { onCommand(RemoteCommand.Right) },
            iconTint = RemoteControlIcon,
        )
        Surface(
            onClick = {
                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onCommand(RemoteCommand.Enter)
            },
            enabled = enabled,
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
            modifier = Modifier.size(92.dp).raisedAccent(CircleShape).semantics { contentDescription = "OK" },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("OK", color = RemoteControlIcon, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun PadIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    repeatOnHold: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    iconTint: Color = RemoteAccentText,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    IconButton(
        onClick = trigger,
        enabled = enabled,
        modifier = modifier
            .size(58.dp)
            .pointerInput(enabled, repeatOnHold) {
                if (!repeatOnHold) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown()
                    var held = false
                    val job = scope.launch {
                        delay(450L)
                        held = true
                        while (isActive) {
                            trigger()
                            delay(140L)
                        }
                    }
                    waitForUpOrCancellation()
                    job.cancel()
                    if (!held) trigger()
                }
            }
            .semantics { contentDescription = description },
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
    }
}

@Composable
private fun VerticalRemoteControl(
    label: String,
    upDescription: String,
    downDescription: String,
    upIcon: ImageVector,
    downIcon: ImageVector,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.height(140.dp).raisedButton(RoundedCornerShape(16.dp)),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PadIconButton(upIcon, upDescription, enabled, hapticsEnabled, true, Modifier, onUp, iconTint = Color.White)
            Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
            PadIconButton(downIcon, downDescription, enabled, hapticsEnabled, true, Modifier, onDown, iconTint = Color.White)
        }
    }
}

@Composable
private fun RemoteActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.height(54.dp).raisedButton(RoundedCornerShape(16.dp)),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (enabled) Color.White else Color(0xFF657077), modifier = Modifier.size(20.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SmallIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier
            .then(if (fillWidth) Modifier.height(52.dp).fillMaxWidth() else Modifier.size(52.dp))
            .raisedButton(RoundedCornerShape(16.dp))
            .semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun SmallTextButton(
    text: String,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.height(52.dp).fillMaxWidth().raisedButton(RoundedCornerShape(16.dp)).semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) { Text(text, color = Color.White) }
    }
}

@Composable
private fun TallIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier
            .raisedButton(RoundedCornerShape(16.dp))
            .semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutRow(
    applications: List<TvApplication>,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    onLaunchApplication: (TvApplication) -> Unit,
    onReorderApplications: (List<TvApplication>) -> Unit,
) {
    val listState = rememberLazyListState()
    val autoScrollScope = rememberCoroutineScope()
    val orderedApplications = remember { mutableStateListOf<TvApplication>() }
    var draggedApplicationId by remember { mutableStateOf<String?>(null) }
    var draggedCenter by remember { mutableStateOf(0f) }
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }
    var autoScrollDirection by remember { mutableStateOf(0) }

    LaunchedEffect(applications) {
        if (draggedApplicationId == null) {
            orderedApplications.clear()
            orderedApplications.addAll(applications)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (orderedApplications.isEmpty()) {
            items(4) { index ->
                ShortcutPlaceholder(index)
            }
        } else {
            items(orderedApplications, key = { it.id }) { application ->
                val isDragging = draggedApplicationId == application.id
                val itemInfo = listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.key == application.id }
                Box(
                    modifier = Modifier
                        .animateItem()
                        .graphicsLayer {
                            translationX = if (isDragging && itemInfo != null) {
                                draggedCenter - itemInfo.offset - itemInfo.size / 2f
                            } else {
                                0f
                            }
                        }
                        .pointerInput(application.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    val info = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.key == application.id }
                                    if (info != null) {
                                        draggedApplicationId = application.id
                                        draggedCenter = info.offset + info.size / 2f
                                    }
                                },
                                onDragCancel = {
                                    autoScrollJob?.cancel()
                                    autoScrollJob = null
                                    autoScrollDirection = 0
                                    draggedApplicationId = null
                                    draggedCenter = 0f
                                },
                                onDragEnd = {
                                    autoScrollJob?.cancel()
                                    autoScrollJob = null
                                    autoScrollDirection = 0
                                    onReorderApplications(orderedApplications.toList())
                                    draggedApplicationId = null
                                    draggedCenter = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    draggedCenter += dragAmount.x

                                    val draggedInfo = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.key == application.id }
                                    if (draggedInfo != null) {
                                        val targetInfo = listState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { item ->
                                                item.key != application.id &&
                                                    draggedCenter > item.offset &&
                                                    draggedCenter < item.offset + item.size
                                            }
                                        if (targetInfo != null) {
                                            val fromIndex = orderedApplications.indexOfFirst { it.id == application.id }
                                            val targetIndex = orderedApplications.indexOfFirst { it.id == targetInfo.key }
                                            if (fromIndex >= 0 && targetIndex >= 0 && fromIndex != targetIndex) {
                                                val moved = orderedApplications.removeAt(fromIndex)
                                                orderedApplications.add(targetIndex, moved)
                                            }
                                        }

                                        val edgeDistance = 56f
                                        val viewportStart = listState.layoutInfo.viewportStartOffset.toFloat()
                                        val viewportEnd = listState.layoutInfo.viewportEndOffset.toFloat()
                                        val scrollDirection = when {
                                            draggedCenter < viewportStart + edgeDistance -> -1
                                            draggedCenter > viewportEnd - edgeDistance -> 1
                                            else -> 0
                                        }
                                        if (scrollDirection != autoScrollDirection) {
                                            autoScrollJob?.cancel()
                                            autoScrollJob = null
                                            autoScrollDirection = scrollDirection
                                            if (scrollDirection != 0) {
                                                autoScrollJob = autoScrollScope.launch {
                                                    while (isActive && autoScrollDirection == scrollDirection) {
                                                        listState.dispatchRawDelta(scrollDirection * 18f)
                                                        delay(16L)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            )
                        },
                ) {
                    TvApplicationButton(
                        application = application,
                        enabled = enabled,
                        hapticsEnabled = hapticsEnabled,
                        onClick = { onLaunchApplication(application) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortcutPlaceholder(index: Int) {
    Box(
        modifier = Modifier
            .size(width = 84.dp, height = 36.dp)
            .raisedButton(RoundedCornerShape(16.dp))
            .semantics { contentDescription = "Shortcut slot ${index + 1}" },
    )
}

@Composable
private fun TvApplicationButton(
    application: TvApplication,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val bundledIcon = remember(application.id, application.name) {
        bundledApplicationIcon(application)
    }
    val iconBitmap = remember(application.iconData) {
        application.iconData?.let(::decodeApplicationIcon)
    }
    Surface(
        onClick = {
            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = Modifier
            .size(width = 112.dp, height = 36.dp)
            .raisedButton(RoundedCornerShape(16.dp))
            .semantics { contentDescription = "Launch ${application.name}" },
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (bundledIcon != null) {
                Image(
                    painter = painterResource(bundledIcon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            } else if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(
                text = application.name,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun bundledApplicationIcon(application: TvApplication): Int? {
    val name = application.name.trim().lowercase()
    val identity = "$name ${application.id}".lowercase()
    return when {
        "youtube" in identity -> R.drawable.app_youtube
        "netflix" in identity -> R.drawable.app_netflix
        "spotify" in identity -> R.drawable.app_spotify
        "plex" in identity -> R.drawable.app_plex
        "apple" in identity && "tv" in identity -> R.drawable.app_appletv
        "paramount" in identity -> R.drawable.app_paramountplus
        "crunchyroll" in identity -> R.drawable.app_crunchyroll
        "twitch" in identity -> R.drawable.app_twitch
        "max" in identity || "hbo" in identity -> R.drawable.app_max
        "prime" in identity && "video" in identity -> R.drawable.app_primevideo
        "disney" in identity -> R.drawable.app_disneyplus
        "abc" in identity && "iview" in identity -> R.drawable.app_abciview
        "7plus" in identity || "7 plus" in identity -> R.drawable.app_7plus
        "10play" in identity || "10 play" in identity -> R.drawable.app_10play
        "kayo" in identity -> R.drawable.app_kayo
        "binge" in identity -> R.drawable.app_binge
        "stan" in identity -> R.drawable.app_stan
        "9now" in identity || "9-now" in identity || "nine now" in identity -> R.drawable.app_9now
        "nfl" in identity -> R.drawable.app_nfl
        "foxtel" in identity -> R.drawable.app_foxtel
        "google tv" in identity || "googletv" in identity -> R.drawable.app_googletv
        "google" in identity -> R.drawable.app_google
        "tubi" in identity -> R.drawable.app_tubi
        "telstra" in identity -> R.drawable.app_telstra
        "calm" in identity -> R.drawable.app_calm
        "sbs" in identity -> R.drawable.app_sbs
        name == "10" || name == "channel 10" || name == "network 10" -> R.drawable.app_channel10
        "channel 10" in identity || "channel10" in identity || "network 10" in identity || "network10" in identity -> R.drawable.app_channel10
        identity == "10" || identity.endsWith(".10") || identity.endsWith("_10") -> R.drawable.app_channel10
        "universal" in identity -> R.drawable.app_universal
        "britbox" in identity || "brit box" in identity -> R.drawable.app_britbox
        "internet" in identity || "browser" in identity || "web" in identity -> R.drawable.app_internet
        else -> null
    }
}

private fun decodeApplicationIcon(iconData: String) = runCatching {
    val encoded = iconData.substringAfter("base64,", iconData)
    BitmapFactory.decodeByteArray(
        Base64.decode(encoded, Base64.DEFAULT),
        0,
        Base64.decode(encoded, Base64.DEFAULT).size,
    )?.asImageBitmap()
}.getOrNull()

@Composable
private fun MoreControls(
    modifier: Modifier,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    onCommand: (RemoteCommand) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
                val numberRows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
                numberRows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { number ->
                            SheetButton(number.toString(), "Number $number", enabled, hapticsEnabled, Modifier.weight(1f)) {
                                onCommand(numberCommand(number))
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(modifier = Modifier.weight(1f))
                    SheetButton("0", "Number 0", enabled, hapticsEnabled, Modifier.weight(1f)) {
                        onCommand(RemoteCommand.Number0)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SheetIconButton(Icons.Default.FastRewind, "Rewind", enabled, hapticsEnabled, Modifier.weight(1f)) { onCommand(RemoteCommand.Rewind) }
                    SheetIconButton(Icons.Default.PlayArrow, "Play", enabled, hapticsEnabled, Modifier.weight(1f)) { onCommand(RemoteCommand.Play) }
                    SheetIconButton(Icons.Default.Pause, "Pause", enabled, hapticsEnabled, Modifier.weight(1f)) { onCommand(RemoteCommand.Pause) }
                    SheetIconButton(Icons.Default.Stop, "Stop", enabled, hapticsEnabled, Modifier.weight(1f)) { onCommand(RemoteCommand.Stop) }
                    SheetIconButton(Icons.Default.FastForward, "Fast forward", enabled, hapticsEnabled, Modifier.weight(1f)) { onCommand(RemoteCommand.FastForward) }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LargeSheetIconButton(Icons.Default.ClosedCaption, "Subtitles", enabled, hapticsEnabled, Modifier.weight(1f)) {
                        onCommand(RemoteCommand.Subtitles)
                    }
                    ColoredFunctionButton(Color(0xFFFF8A80), Color(0xFFD32F2F), "Red function", enabled, hapticsEnabled, Modifier.weight(1f)) {
                        onCommand(RemoteCommand.Red)
                    }
                    ColoredFunctionButton(Color(0xFF81C784), Color(0xFF388E3C), "Green function", enabled, hapticsEnabled, Modifier.weight(1f)) {
                        onCommand(RemoteCommand.Green)
                    }
                    ColoredFunctionButton(Color(0xFFFFF176), Color(0xFFF9A825), "Yellow function", enabled, hapticsEnabled, Modifier.weight(1f)) {
                        onCommand(RemoteCommand.Yellow)
                    }
                    ColoredFunctionButton(Color(0xFF64B5F6), Color(0xFF1976D2), "Blue function", enabled, hapticsEnabled, Modifier.weight(1f)) {
                        onCommand(RemoteCommand.Blue)
                    }
                }
    }
}

@Composable
private fun SheetButton(
    text: String,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxHeight().raisedButton(RoundedCornerShape(16.dp)).semantics { contentDescription = description },
    ) {
        val textColor = when (text) {
            "RED" -> Color(0xFFFF5252)
            "GREEN" -> Color(0xFF69D36B)
            "YELLOW" -> Color(0xFFFFD740)
            "BLUE" -> Color(0xFF42A5F5)
            else -> Color.White
        }
        Box(contentAlignment = Alignment.Center) { Text(text, color = textColor) }
    }
}

@Composable
private fun SheetIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxHeight().raisedButton(RoundedCornerShape(16.dp)).semantics { contentDescription = description },
    ) { Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = Color.White) } }
}

@Composable
private fun LargeSheetIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxHeight().raisedButton(RoundedCornerShape(16.dp))
            .semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun ColoredFunctionButton(
    topColor: Color,
    bottomColor: Color,
    description: String,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val trigger = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
    Surface(
        onClick = trigger,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxHeight().raisedButton(RoundedCornerShape(16.dp))
            .semantics { contentDescription = description },
    ) {
        Box(
            modifier = Modifier
                .requiredSize(16.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (enabled) topColor else topColor.copy(alpha = 0.35f),
                            if (enabled) bottomColor else bottomColor.copy(alpha = 0.35f),
                        ),
                    ),
                    CircleShape,
                )
                .border(1.dp, Color.White.copy(alpha = if (enabled) 0.45f else 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {}
    }
}

@Composable
private fun TextButtonLike(text: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(32.dp),
    ) { Box(contentAlignment = Alignment.Center) { Text(text, color = Color(0xFFAAB4BA), style = MaterialTheme.typography.labelSmall) } }
}

private fun numberCommand(number: Int): RemoteCommand = when (number) {
    1 -> RemoteCommand.Number1
    2 -> RemoteCommand.Number2
    3 -> RemoteCommand.Number3
    4 -> RemoteCommand.Number4
    5 -> RemoteCommand.Number5
    6 -> RemoteCommand.Number6
    7 -> RemoteCommand.Number7
    8 -> RemoteCommand.Number8
    9 -> RemoteCommand.Number9
    else -> RemoteCommand.Number0
}

@Preview(showBackground = true)
@Composable
private fun RemoteScreenPreview() {
    RogermoteTheme {
        RemoteScreen(
            uiState = RemoteUiState(connectionState = ConnectionState.Connected),
            onCommand = {},
            onLaunchApplication = {},
            onReorderApplications = {},
            onDisconnect = {},
            onOpenSetup = {},
        )
    }
}
