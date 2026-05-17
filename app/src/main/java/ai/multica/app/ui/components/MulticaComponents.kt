package ai.multica.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick as semanticsOnClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.multica.app.ui.theme.MulticaColors
import ai.multica.app.ui.theme.LocalMulticaSpacing
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.alexzhirkevich.cupertino.AlertActionStyle
import io.github.alexzhirkevich.cupertino.CupertinoActionSheet
import io.github.alexzhirkevich.cupertino.CupertinoActivityIndicator
import io.github.alexzhirkevich.cupertino.CupertinoAlertDialog
import io.github.alexzhirkevich.cupertino.CupertinoButton
import io.github.alexzhirkevich.cupertino.CupertinoButtonDefaults
import io.github.alexzhirkevich.cupertino.CupertinoButtonSize
import io.github.alexzhirkevich.cupertino.CupertinoBorderedTextField
import io.github.alexzhirkevich.cupertino.CupertinoSwitch
import io.github.alexzhirkevich.cupertino.ExperimentalCupertinoApi

enum class MulticaTab(val label: String, val zhLabel: String, val icon: ImageVector) {
    Inbox("Inbox", "收件箱", Icons.Outlined.Inbox),
    Issues("Issues", "Issues", Icons.Outlined.CheckCircle),
    MyIssues("My Issues", "我的 Issues", Icons.Outlined.AssignmentInd),
    Projects("Projects", "项目", Icons.Outlined.Folder),
    Settings("Settings", "设置", Icons.Outlined.Settings),
}

@Composable
fun MulticaShell(
    selectedTab: MulticaTab,
    workspaceName: String,
    languageLabel: String,
    zh: Boolean = false,
    onLanguageClick: () -> Unit,
    onWorkspaceClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChatClick: () -> Unit,
    onTabClick: (MulticaTab) -> Unit,
    showChrome: Boolean = true,
    content: @Composable () -> Unit,
) {
    val hazeState = rememberHazeState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MulticaColors.Background)
            .hazeSource(hazeState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (showChrome) Modifier.padding(top = 58.dp) else Modifier),
        ) {
            content()
        }
        if (showChrome) {
            MulticaGlobalActionsBar(
                workspaceName = workspaceName,
                hazeState = hazeState,
                onWorkspaceClick = onWorkspaceClick,
                onSearchClick = onSearchClick,
                onChatClick = onChatClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 14.dp, end = 14.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                MulticaBottomNav(
                    selectedTab = selectedTab,
                    zh = zh,
                    hazeState = hazeState,
                    onTabClick = onTabClick,
                )
            }
        }
    }
}

@Composable
private fun MulticaGlobalActionsBar(
    workspaceName: String,
    hazeState: HazeState,
    onWorkspaceClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .widthIn(max = 520.dp)
            .clip(RoundedCornerShape(24.dp))
            .hazeEffect(
                hazeState,
                style = HazeDefaults.style(
                    backgroundColor = MulticaColors.Background.copy(alpha = 0.74f),
                    blurRadius = 22.dp,
                    noiseFactor = 0.07f,
                ),
            )
            .background(MulticaColors.Background.copy(alpha = 0.74f))
            .border(0.5.dp, MulticaColors.Border.copy(alpha = 0.62f), RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .semantics(mergeDescendants = false) {
                contentDescription = "Multica Global Actions Bar Web Sidebar Header Search Chat Workspace"
            },
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MulticaColors.Surface.copy(alpha = 0.82f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .widthIn(min = 86.dp, max = 168.dp)
                .height(36.dp)
                .clickable {
                    performMulticaTapFeedback(haptic)
                    onWorkspaceClick()
                }
                .semantics { contentDescription = "Global Workspace Switcher $workspaceName" },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MulticaColors.Accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = workspaceName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "M",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold),
                        color = MulticaColors.Accent,
                    )
                }
                Text(
                    text = workspaceName.ifBlank { "Multica" },
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = MulticaColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
        MulticaIconPillButton(
            icon = Icons.Outlined.Search,
            contentDescription = "Global Search Trigger",
            onClick = onSearchClick,
            tone = MulticaButtonTone.Ghost,
        )
        MulticaIconPillButton(
            icon = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Global Chat Trigger",
            onClick = onChatClick,
            tone = MulticaButtonTone.Ghost,
        )
    }
}

@Composable
fun MulticaRootPage(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    pinnedToolbar: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val spacing = LocalMulticaSpacing.current
    val hazeState = rememberHazeState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MulticaColors.Background)
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .hazeSource(hazeState),
            contentPadding = PaddingValues(
                start = spacing.pageHorizontal,
                top = spacing.pageTop,
                end = spacing.pageHorizontal,
                bottom = if (bottomContent == null) 128.dp else 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displaySmall,
                        color = MulticaColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    trailing?.invoke()
                }
            }
            if (pinnedToolbar != null) {
                item { pinnedToolbar() }
            }
            content()
        }
        if (bottomContent != null) {
            MulticaGlassBottomSlot(hazeState, bottomContent)
        }
    }
}

@Composable
fun MulticaDetailPage(
    title: String,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val spacing = LocalMulticaSpacing.current
    val hazeState = rememberHazeState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MulticaColors.Background)
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .hazeSource(hazeState),
            contentPadding = PaddingValues(
                start = spacing.pageHorizontal,
                top = spacing.pageTop,
                end = spacing.pageHorizontal,
                bottom = if (bottomContent == null) 120.dp else 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leading?.invoke()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MulticaColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (leading == null) Modifier else Modifier.padding(start = 10.dp)),
                    )
                    trailing?.invoke()
                }
            }
            content()
        }
        if (bottomContent != null) {
            MulticaGlassBottomSlot(hazeState, bottomContent)
        }
    }
}

@Composable
fun MulticaCupertinoPage(
    modifier: Modifier = Modifier,
    listState: LazyListState? = null,
    pinnedToolbar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val spacing = LocalMulticaSpacing.current
    val hazeState = rememberHazeState()
    val rememberedListState = rememberLazyListState()
    val effectiveListState = listState ?: rememberedListState
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MulticaColors.Background)
            .statusBarsPadding()
            .semantics(mergeDescendants = false) { contentDescription = "Multica Cupertino Page" },
    ) {
        if (pinnedToolbar != null) {
            MulticaCupertinoPinnedHeader(hazeState) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = spacing.pageHorizontal,
                            top = 14.dp,
                            end = spacing.pageHorizontal,
                            bottom = 10.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    header()
                    pinnedToolbar()
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .hazeSource(hazeState),
            state = effectiveListState,
            contentPadding = PaddingValues(
                start = spacing.pageHorizontal,
                top = if (pinnedToolbar == null) 18.dp else 10.dp,
                end = spacing.pageHorizontal,
                bottom = if (bottomBar == null) 148.dp else 220.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (pinnedToolbar == null) {
                item { header() }
            }
            content()
        }
        if (bottomBar != null) {
            MulticaHazeBottomBar(hazeState = hazeState, content = bottomBar)
        }
    }
}

@Composable
private fun MulticaCupertinoPinnedHeader(
    hazeState: HazeState,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .hazeEffect(
                hazeState,
                style = HazeDefaults.style(
                    backgroundColor = MulticaColors.Background.copy(alpha = 0.78f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.08f,
                ),
            )
            .background(MulticaColors.Background.copy(alpha = 0.78f))
            .semantics(mergeDescendants = false) { contentDescription = "Multica Cupertino Pinned Header" },
    ) {
        content()
    }
}

@Composable
private fun MulticaGlassBottomSlot(
    hazeState: HazeState,
    content: @Composable () -> Unit,
) {
    MulticaHazeBottomBar(
        hazeState = hazeState,
        contentDescription = "Multica Glass Bottom Slot",
        content = content,
    )
}

@Composable
fun MulticaHazeBottomBar(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    contentDescription: String = "Multica Haze Bottom Bar",
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .hazeEffect(
                hazeState,
                style = HazeDefaults.style(
                    backgroundColor = MulticaColors.Background.copy(alpha = 0.72f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.08f,
                ),
            )
            .background(MulticaColors.Background.copy(alpha = 0.72f))
            .imePadding()
            .semantics(mergeDescendants = false) { this.contentDescription = contentDescription },
    ) {
        content()
    }
}

@Composable
fun MulticaGroupedList(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MulticaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MulticaColors.Border),
    ) {
        Column(content = content)
    }
}

@Composable
fun MulticaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MulticaColors.TextTertiary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(top = 24.dp, bottom = 8.dp, start = 2.dp),
    )
}

@Composable
fun MulticaListItem(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
        .then(if (contentDescription.isNullOrBlank()) Modifier else Modifier.semantics(mergeDescendants = true) {
            this.contentDescription = contentDescription
        })
    Column(modifier = rowModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MulticaColors.TextTertiary,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                if (!eyebrow.isNullOrBlank()) {
                    Text(
                        text = eyebrow,
                        style = MaterialTheme.typography.labelMedium,
                        color = MulticaColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MulticaColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MulticaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke()
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (leadingIcon == null) 16.dp else 52.dp)
                    .height(1.dp)
                    .background(MulticaColors.Border),
            )
        }
    }
}

@Composable
fun MulticaCupertinoSection(
    title: String,
    modifier: Modifier = Modifier,
    footer: String? = null,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = false) {
                this.contentDescription = contentDescription ?: "Multica Cupertino Section $title"
            },
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Column(
            modifier = Modifier.padding(start = 4.dp, top = 16.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MulticaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!footer.isNullOrBlank()) {
                Text(
                    text = footer,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                    ),
                    color = MulticaColors.TextTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MulticaColors.Surface.copy(alpha = 0.92f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(0.5.dp, MulticaColors.Border.copy(alpha = 0.72f)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                content = content,
            )
        }
    }
}

@Composable
fun MulticaCupertinoListRow(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    value: String? = null,
    showDivider: Boolean = false,
    destructive: Boolean = false,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    val tint = if (destructive) MulticaColors.Danger else MulticaColors.Muted
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val performClick = {
        performMulticaTapFeedback(haptic)
        onClick()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .multicaSpringPressFeedback(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { performClick() }
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription ?: "Multica Cupertino List Row $title"
                semanticsOnClick(label = title) {
                    performClick()
                    true
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (destructive) {
                    MulticaColors.Danger.copy(alpha = 0.10f)
                } else {
                    MulticaColors.Border.copy(alpha = 0.36f)
                },
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (destructive) MulticaColors.Danger else MulticaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                    ),
                    color = MulticaColors.TextTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!value.isNullOrBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                    ),
                    color = if (destructive) MulticaColors.Danger else MulticaColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp),
                    textAlign = TextAlign.End,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MulticaColors.Muted,
                modifier = Modifier.size(17.dp),
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp)
                    .height(0.5.dp)
                    .background(MulticaColors.Border.copy(alpha = 0.72f)),
            )
        }
    }
}

@Composable
fun MulticaCupertinoSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    saving: Boolean,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val performClick = {
        if (!saving) {
            performMulticaTapFeedback(haptic)
            onClick()
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .multicaSpringPressFeedback(interactionSource, enabled = !saving)
            .clickable(
                enabled = !saving,
                interactionSource = interactionSource,
                indication = null,
            ) { performClick() }
            .semantics(mergeDescendants = false) {
                this.contentDescription = "Multica Cupertino Switch Row $title"
                if (!saving) {
                    semanticsOnClick(label = title) {
                        performClick()
                        true
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MulticaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                    ),
                    color = MulticaColors.TextTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (saving) {
                Text(
                    text = "...",
                    style = MaterialTheme.typography.labelMedium,
                    color = MulticaColors.Muted,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                CupertinoSwitch(
                    checked = checked,
                    onCheckedChange = { performClick() },
                    modifier = Modifier.semantics { this.contentDescription = contentDescription },
                )
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MulticaColors.Border.copy(alpha = 0.72f)),
            )
        }
    }
}

@Composable
fun MulticaBottomNav(
    selectedTab: MulticaTab,
    zh: Boolean = false,
    hazeState: HazeState? = null,
    onTabClick: (MulticaTab) -> Unit,
) {
    val personalTabs = listOf(MulticaTab.Inbox, MulticaTab.MyIssues)
    val workspaceTabs = listOf(MulticaTab.Issues, MulticaTab.Projects)
    val configureTabs = listOf(MulticaTab.Settings)
    val orderedTabs = listOf(
        MulticaTab.Inbox,
        MulticaTab.Issues,
        MulticaTab.MyIssues,
        MulticaTab.Projects,
        MulticaTab.Settings,
    )
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics(mergeDescendants = false) {
                contentDescription = "Multica Floating Haze Tab Bar Web Navigation Groups Personal Workspace Configure"
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp)
                .clip(RoundedCornerShape(28.dp))
                .then(
                    if (hazeState == null) {
                        Modifier
                    } else {
                        Modifier.hazeEffect(
                            hazeState,
                            style = HazeDefaults.style(
                                backgroundColor = MulticaColors.SurfaceElevated.copy(alpha = 0.72f),
                                blurRadius = 28.dp,
                                noiseFactor = 0.07f,
                            ),
                        )
                    }
                )
                .background(MulticaColors.SurfaceElevated.copy(alpha = 0.72f))
                .border(0.5.dp, MulticaColors.Border.copy(alpha = 0.62f), RoundedCornerShape(28.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            orderedTabs.forEach { tab ->
                val selected = tab == selectedTab
                val visibleLabel = if (zh) tab.zhLabel else tab.label
                val groupLabel = when (tab) {
                    in personalTabs -> "Personal"
                    in workspaceTabs -> "Workspace"
                    in configureTabs -> "Configure"
                    else -> "Workspace"
                }
                val interactionSource = remember(tab) { MutableInteractionSource() }
                val performClick = {
                    performMulticaTapFeedback(haptic)
                    onTabClick(tab)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .multicaSpringPressFeedback(interactionSource)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selected) MulticaColors.AccentSoft.copy(alpha = 0.86f) else Color.Transparent)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { performClick() }
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Bottom Tab ${tab.label} Web Group $groupLabel"
                            semanticsOnClick(label = visibleLabel) {
                                performClick()
                                true
                            }
                        }
                        .padding(horizontal = 2.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 25.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) MulticaColors.Accent.copy(alpha = 0.14f) else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (selected) MulticaColors.Accent else MulticaColors.Muted,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = visibleLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = if (selected) MulticaColors.Accent else MulticaColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

enum class MulticaButtonTone { Primary, Secondary, Ghost, Destructive }

private fun performMulticaTapFeedback(haptic: HapticFeedback) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
}

@Composable
private fun Modifier.multicaSpringPressFeedback(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "Multica spring press",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

data class MulticaCupertinoActionSheetItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val contentDescription: String? = null,
    val selected: Boolean = false,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaCupertinoActionSheet(
    visible: Boolean,
    title: String,
    message: String? = null,
    items: List<MulticaCupertinoActionSheetItem>,
    cancelText: String,
    onDismissRequest: () -> Unit,
) {
    if (!visible) return
    val haptic = LocalHapticFeedback.current
    CupertinoActionSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MulticaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
        },
        message = {
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MulticaColors.TextTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        },
        buttons = {
            items.forEach { item ->
                action(
                    onClick = {
                        performMulticaTapFeedback(haptic)
                        item.onClick()
                        onDismissRequest()
                    },
                    style = if (item.destructive) AlertActionStyle.Destructive else AlertActionStyle.Default,
                    enabled = item.enabled,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Multica Cupertino Action Sheet ${item.contentDescription ?: item.title}"
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        item.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (item.destructive) MulticaColors.Danger else MulticaColors.TextTertiary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (item.destructive) MulticaColors.Danger else MulticaColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                            if (!item.subtitle.isNullOrBlank()) {
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 13.sp),
                                    color = MulticaColors.TextTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                        if (item.selected) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MulticaColors.Accent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            action(
                onClick = {
                    performMulticaTapFeedback(haptic)
                    onDismissRequest()
                },
                style = AlertActionStyle.Cancel,
                enabled = true,
            ) {
                Text(
                    text = cancelText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = MulticaColors.Accent,
                )
            }
        },
    )
}

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaCupertinoAlertDialog(
    visible: Boolean,
    title: String,
    message: String,
    cancelText: String,
    confirmText: String,
    destructive: Boolean = false,
    contentDescription: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return
    val haptic = LocalHapticFeedback.current
    CupertinoAlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics {
                    this.contentDescription = "Multica Cupertino Alert Dialog $contentDescription"
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MulticaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
        },
        message = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp),
                color = MulticaColors.TextTertiary,
                textAlign = TextAlign.Center,
            )
        },
        buttons = {
            action(
                onClick = {
                    performMulticaTapFeedback(haptic)
                    onDismissRequest()
                },
                style = AlertActionStyle.Cancel,
                enabled = true,
            ) {
                Text(
                    text = cancelText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = MulticaColors.Accent,
                )
            }
            action(
                onClick = {
                    performMulticaTapFeedback(haptic)
                    onConfirm()
                },
                style = if (destructive) AlertActionStyle.Destructive else AlertActionStyle.Default,
                enabled = true,
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = if (destructive) MulticaColors.Danger else MulticaColors.Accent,
                )
            }
        },
    )
}

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaCupertinoSecretDialog(
    visible: Boolean,
    title: String,
    message: String,
    secret: String,
    copied: Boolean,
    copyText: String,
    doneText: String,
    copiedText: String,
    contentDescription: String,
    onDismissRequest: () -> Unit,
    onCopy: () -> Unit,
) {
    if (!visible) return
    val haptic = LocalHapticFeedback.current
    CupertinoAlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics {
                    this.contentDescription = "Multica Cupertino Secret Dialog $contentDescription"
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MulticaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
        },
        message = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp),
                    color = MulticaColors.TextTertiary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = secret,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MulticaColors.Background.copy(alpha = 0.96f))
                        .border(0.5.dp, MulticaColors.Border.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 8.dp)
                        .semantics { this.contentDescription = "Multica Secret Dialog Value" },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MulticaColors.TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
                if (copied) {
                    MulticaSuccessState(
                        copiedText,
                        modifier = Modifier.semantics { this.contentDescription = "Multica Secret Dialog Copy Success Motion" },
                    )
                }
            }
        },
        buttons = {
            action(
                onClick = {
                    performMulticaTapFeedback(haptic)
                    onDismissRequest()
                },
                style = AlertActionStyle.Cancel,
                enabled = true,
            ) {
                Text(
                    text = doneText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = MulticaColors.Accent,
                )
            }
            action(
                onClick = {
                    performMulticaTapFeedback(haptic)
                    onCopy()
                },
                style = AlertActionStyle.Default,
                enabled = true,
            ) {
                Text(
                    text = copyText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = MulticaColors.Accent,
                )
            }
        },
    )
}

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: MulticaButtonTone = MulticaButtonTone.Ghost,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
) {
    val activeBackground = when (tone) {
        MulticaButtonTone.Primary -> MulticaColors.Accent
        MulticaButtonTone.Secondary -> MulticaColors.AccentSoft
        MulticaButtonTone.Ghost -> MulticaColors.SurfaceElevated
        MulticaButtonTone.Destructive -> MulticaColors.Danger.copy(alpha = 0.13f)
    }
    val activeForeground = when (tone) {
        MulticaButtonTone.Primary -> Color.White
        MulticaButtonTone.Secondary -> MulticaColors.Accent
        MulticaButtonTone.Ghost -> MulticaColors.TextPrimary
        MulticaButtonTone.Destructive -> MulticaColors.Danger
    }
    val background = if (enabled) activeBackground else MulticaColors.Surface
    val foreground = if (enabled) activeForeground else MulticaColors.Muted
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val performClick = {
        performMulticaTapFeedback(haptic)
        onClick()
    }
    val describedModifier = if (contentDescription.isNullOrBlank()) {
        modifier
    } else {
        modifier.clearAndSetSemantics {
            this.contentDescription = contentDescription
            if (enabled) {
                semanticsOnClick(label = text) {
                    performClick()
                    true
                }
            }
        }
    }
    CupertinoButton(
        onClick = performClick,
        modifier = describedModifier
            .heightIn(min = 32.dp)
            .multicaSpringPressFeedback(interactionSource, enabled),
        enabled = enabled,
        size = CupertinoButtonSize.Small,
        shape = RoundedCornerShape(10.dp),
        colors = CupertinoButtonDefaults.filledButtonColors(
            contentColor = foreground,
            containerColor = background,
            disabledContentColor = MulticaColors.Muted,
            disabledContainerColor = MulticaColors.Surface,
        ),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = foreground,
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = foreground,
            )
        }
    }
}

@Composable
fun MulticaIconPillButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: MulticaButtonTone = MulticaButtonTone.Ghost,
    enabled: Boolean = true,
) {
    val activeBackground = when (tone) {
        MulticaButtonTone.Primary -> MulticaColors.Accent
        MulticaButtonTone.Secondary -> MulticaColors.AccentSoft
        MulticaButtonTone.Ghost -> MulticaColors.SurfaceElevated
        MulticaButtonTone.Destructive -> MulticaColors.Danger.copy(alpha = 0.13f)
    }
    val activeForeground = when (tone) {
        MulticaButtonTone.Primary -> Color.White
        MulticaButtonTone.Secondary -> MulticaColors.Accent
        MulticaButtonTone.Ghost -> MulticaColors.TextPrimary
        MulticaButtonTone.Destructive -> MulticaColors.Danger
    }
    val background = if (enabled) activeBackground else MulticaColors.Surface
    val foreground = if (enabled) activeForeground else MulticaColors.Muted
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val performClick = {
        performMulticaTapFeedback(haptic)
        onClick()
    }
    Box(
        modifier = modifier
            .multicaSpringPressFeedback(interactionSource, enabled)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
            ) { performClick() }
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                if (enabled) {
                    semanticsOnClick(label = contentDescription) {
                        performClick()
                        true
                    }
                }
            }
            .heightIn(min = 36.dp)
            .widthIn(min = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .padding(horizontal = 9.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = foreground,
            )
        }
    }
}

@Composable
fun MulticaListRow(
    eyebrow: String,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    unread: Boolean = false,
    showLeadingIndicator: Boolean = unread,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val clickableModifier = if (onClick == null) {
        modifier
    } else {
        modifier.clickable(onClick = onClick)
    }
    val rowModifier = if (contentDescription.isNullOrBlank()) {
        clickableModifier
    } else {
        clickableModifier.semantics {
            this.contentDescription = contentDescription
            if (onClick != null) {
                semanticsOnClick(label = title) {
                    onClick()
                    true
                }
            }
        }
    }
    Column(
        modifier = rowModifier
            .fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(horizontal = 0.dp, vertical = 12.dp)) {
            if (showLeadingIndicator) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (unread) MulticaColors.Accent else MulticaColors.Muted.copy(alpha = 0.34f))
                )
                Spacer(modifier = Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    color = MulticaColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = MulticaColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MulticaColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier.padding(top = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    trailing()
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (showLeadingIndicator) 21.dp else 0.dp)
                .height(1.dp)
                .background(MulticaColors.Border)
        )
    }
}

@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MulticaColors.TextSecondary,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MulticaColors.SurfaceElevated)
            .padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaCupertinoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: String = label,
    showLabel: Boolean = true,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    val fieldDescription = contentDescription ?: "Multica Cupertino Text Field $label"
    val fieldModifier = Modifier
        .fillMaxWidth()
        .semantics { this.contentDescription = fieldDescription }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (showLabel && label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold),
                color = MulticaColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CupertinoBorderedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier.heightIn(min = if (singleLine) 44.dp else 48.dp),
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(placeholder, color = MulticaColors.TextTertiary)
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 21.sp, color = MulticaColors.TextPrimary),
            shape = RoundedCornerShape(10.dp),
        )
    }
}

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: String = label,
    showLabel: Boolean = true,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    MulticaCupertinoTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        contentDescription = contentDescription,
        placeholder = placeholder,
        showLabel = showLabel,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
    )
}

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaLoadingState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MulticaColors.Background)
            .semantics { contentDescription = "Multica System Loading State" },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                MulticaMotionGlyph(kind = MulticaMotionGlyphKind.Loading, size = 48.dp)
                CupertinoActivityIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { contentDescription = "Multica Cupertino Activity Indicator" },
                )
            }
            Text(
                text = compactSystemStateTitle(message),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
                color = MulticaColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

enum class MulticaMotionGlyphKind {
    Loading,
    Empty,
    Error,
    Success,
}

@Composable
fun MulticaMotionGlyph(
    kind: MulticaMotionGlyphKind,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp,
) {
    val json = when (kind) {
        MulticaMotionGlyphKind.Loading -> multicaLoadingLottieJson
        MulticaMotionGlyphKind.Empty -> multicaEmptyLottieJson
        MulticaMotionGlyphKind.Error -> multicaErrorLottieJson
        MulticaMotionGlyphKind.Success -> multicaSuccessLottieJson
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(json)
    )
    val iterations = when (kind) {
        MulticaMotionGlyphKind.Loading -> LottieConstants.IterateForever
        MulticaMotionGlyphKind.Empty,
        MulticaMotionGlyphKind.Error,
        MulticaMotionGlyphKind.Success -> 1
    }
    val description = when (kind) {
        MulticaMotionGlyphKind.Loading -> "Multica Loading Lottie"
        MulticaMotionGlyphKind.Empty -> "Multica Empty Lottie"
        MulticaMotionGlyphKind.Error -> "Multica Error Lottie"
        MulticaMotionGlyphKind.Success -> "Multica Success Lottie"
    }
    LottieAnimation(
        composition = composition,
        iterations = iterations,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = description },
        safeMode = true,
    )
}

private val multicaLoadingLottieJson = """
{
  "v":"5.7.4",
  "fr":60,
  "ip":0,
  "op":90,
  "w":120,
  "h":120,
  "nm":"multica-loading",
  "ddd":0,
  "assets":[],
  "layers":[
    {
      "ddd":0,
      "ind":1,
      "ty":4,
      "nm":"orbit-dot",
      "sr":1,
      "ks":{
        "o":{"a":0,"k":100},
        "r":{"a":1,"k":[{"t":0,"s":[0],"e":[360]},{"t":90,"s":[360]}]},
        "p":{"a":0,"k":[60,60,0]},
        "a":{"a":0,"k":[0,0,0]},
        "s":{"a":0,"k":[100,100,100]}
      },
      "shapes":[
        {
          "ty":"gr",
          "it":[
            {"ty":"el","p":{"a":0,"k":[0,-38]},"s":{"a":0,"k":[16,16]}},
            {"ty":"fl","c":{"a":0,"k":[0.145,0.388,0.922,1]},"o":{"a":0,"k":100}},
            {"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}
          ]
        }
      ],
      "ip":0,
      "op":90,
      "st":0,
      "bm":0
    }
  ]
}
""".trimIndent()

@Composable
fun MulticaEmptyState(title: String, description: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MulticaMotionGlyph(kind = MulticaMotionGlyphKind.Empty, size = 54.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MulticaColors.TextPrimary)
        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MulticaColors.TextSecondary)
        }
    }
}

@Composable
fun MulticaSuccessState(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MulticaColors.Success.copy(alpha = 0.08f))
            .border(1.dp, MulticaColors.Success.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .semantics { contentDescription = "Multica Success Row" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MulticaMotionGlyph(kind = MulticaMotionGlyphKind.Success, size = 28.dp)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MulticaColors.Success,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val multicaSuccessLottieJson = """
{
  "v":"5.7.4",
  "fr":60,
  "ip":0,
  "op":60,
  "w":120,
  "h":120,
  "nm":"multica-success-state",
  "ddd":0,
  "assets":[],
  "layers":[
    {
      "ddd":0,
      "ind":1,
      "ty":4,
      "nm":"success-ring",
      "sr":1,
      "ks":{
        "o":{"a":1,"k":[{"t":0,"s":[0],"e":[100]},{"t":12,"s":[100],"e":[100]},{"t":60,"s":[100]}]},
        "r":{"a":0,"k":0},
        "p":{"a":0,"k":[60,60,0]},
        "a":{"a":0,"k":[0,0,0]},
        "s":{"a":1,"k":[{"t":0,"s":[72,72,100],"e":[112,112,100]},{"t":18,"s":[112,112,100],"e":[100,100,100]},{"t":34,"s":[100,100,100],"e":[100,100,100]},{"t":60,"s":[100,100,100]}]}
      },
      "shapes":[
        {
          "ty":"gr",
          "it":[
            {"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[78,78]}},
            {"ty":"st","c":{"a":0,"k":[0.086,0.639,0.290,1]},"o":{"a":0,"k":100},"w":{"a":0,"k":6},"lc":2,"lj":2},
            {"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}
          ]
        }
      ],
      "ip":0,
      "op":60,
      "st":0,
      "bm":0
    },
    {
      "ddd":0,
      "ind":2,
      "ty":4,
      "nm":"success-check",
      "sr":1,
      "ks":{
        "o":{"a":1,"k":[{"t":10,"s":[0],"e":[100]},{"t":20,"s":[100],"e":[100]},{"t":60,"s":[100]}]},
        "r":{"a":0,"k":0},
        "p":{"a":0,"k":[60,60,0]},
        "a":{"a":0,"k":[0,0,0]},
        "s":{"a":1,"k":[{"t":10,"s":[84,84,100],"e":[104,104,100]},{"t":28,"s":[104,104,100],"e":[100,100,100]},{"t":60,"s":[100,100,100]}]}
      },
      "shapes":[
        {
          "ty":"gr",
          "it":[
            {"ty":"sh","ks":{"a":0,"k":{"i":[[0,0],[0,0],[0,0]],"o":[[0,0],[0,0],[0,0]],"v":[[-21,-1],[-6,15],[24,-19]],"c":false}}},
            {"ty":"st","c":{"a":0,"k":[0.086,0.639,0.290,1]},"o":{"a":0,"k":100},"w":{"a":0,"k":8},"lc":2,"lj":2},
            {"ty":"tr","p":{"a":0,"k":[0,2]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}
          ]
        }
      ],
      "ip":10,
      "op":60,
      "st":0,
      "bm":0
    }
  ]
}
""".trimIndent()

private val multicaEmptyLottieJson = """
{
  "v":"5.7.4",
  "fr":60,
  "ip":0,
  "op":72,
  "w":120,
  "h":120,
  "nm":"multica-empty-state",
  "ddd":0,
  "assets":[],
  "layers":[
    {
      "ddd":0,
      "ind":1,
      "ty":4,
      "nm":"soft-ring",
      "sr":1,
      "ks":{
        "o":{"a":1,"k":[{"t":0,"s":[0],"e":[46]},{"t":18,"s":[46],"e":[46]},{"t":72,"s":[46]}]},
        "r":{"a":0,"k":0},
        "p":{"a":0,"k":[60,60,0]},
        "a":{"a":0,"k":[0,0,0]},
        "s":{"a":1,"k":[{"t":0,"s":[88,88,100],"e":[100,100,100]},{"t":24,"s":[100,100,100],"e":[100,100,100]},{"t":72,"s":[100,100,100]}]}
      },
      "shapes":[
        {
          "ty":"gr",
          "it":[
            {"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[74,74]}},
            {"ty":"st","c":{"a":0,"k":[0.145,0.388,0.922,1]},"o":{"a":0,"k":100},"w":{"a":0,"k":4}},
            {"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}
          ]
        }
      ],
      "ip":0,
      "op":72,
      "st":0,
      "bm":0
    },
    {
      "ddd":0,
      "ind":2,
      "ty":4,
      "nm":"center-dot",
      "sr":1,
      "ks":{
        "o":{"a":1,"k":[{"t":0,"s":[0],"e":[100]},{"t":14,"s":[100],"e":[100]},{"t":72,"s":[100]}]},
        "r":{"a":0,"k":0},
        "p":{"a":0,"k":[60,60,0]},
        "a":{"a":0,"k":[0,0,0]},
        "s":{"a":1,"k":[{"t":0,"s":[62,62,100],"e":[100,100,100]},{"t":22,"s":[100,100,100],"e":[100,100,100]},{"t":72,"s":[100,100,100]}]}
      },
      "shapes":[
        {
          "ty":"gr",
          "it":[
            {"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[18,18]}},
            {"ty":"fl","c":{"a":0,"k":[0.145,0.388,0.922,1]},"o":{"a":0,"k":100}},
            {"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}
          ]
        }
      ],
      "ip":0,
      "op":72,
      "st":0,
      "bm":0
    }
  ]
}
""".trimIndent()

private val multicaErrorLottieJson = multicaEmptyLottieJson

@Composable
fun MulticaErrorState(
    message: String,
    onRetry: () -> Unit,
    fullScreen: Boolean = false,
) {
    val title = compactSystemStateTitle(message)
    val detail = compactSystemStateDetail(message)
    Column(
        modifier = (if (fullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
            .then(if (fullScreen) Modifier.background(MulticaColors.Background) else Modifier)
            .padding(horizontal = 24.dp, vertical = 40.dp)
            .semantics { contentDescription = "Multica System Error State" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (fullScreen) Arrangement.Center else Arrangement.Top,
    ) {
        Box(contentAlignment = Alignment.Center) {
            MulticaMotionGlyph(kind = MulticaMotionGlyphKind.Error, size = 42.dp)
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .semantics { contentDescription = "Multica Error Visible Glyph" },
                shape = CircleShape,
                color = MulticaColors.Danger.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, MulticaColors.Danger.copy(alpha = 0.18f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MulticaColors.Danger,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
            color = MulticaColors.Danger,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (detail.isNotBlank()) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 17.sp),
                color = MulticaColors.Muted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        MulticaPillButton(
            text = "Retry",
            onClick = onRetry,
            tone = MulticaButtonTone.Primary,
            contentDescription = "Global Retry Primary Action",
        )
    }
}

private fun compactSystemStateTitle(message: String): String =
    message.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { "Loading" }

private fun compactSystemStateDetail(message: String): String =
    message.lineSequence().drop(1).joinToString(" ").trim()
