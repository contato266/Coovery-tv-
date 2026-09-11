package com.streamvault.app.ui.components.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.streamvault.app.R
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.mouseClickable
import com.streamvault.app.ui.interaction.rememberTvInteractionSounds

internal val MobileHandheldTopBarHeight = 52.dp
private val MobileHandheldBottomBarContentHeight = 58.dp
/** Extra lift above the system navigation bar (gesture / 3-button). */
private val MobileHandheldBottomBarVerticalMargin = 18.dp
private val MobileHandheldBottomBarHorizontalMargin = 18.dp

/** Space reserved above the system navigation bar (floating pill + margins). */
internal val MobileHandheldBottomBarReservedHeight: Dp
    get() = MobileHandheldBottomBarContentHeight + MobileHandheldBottomBarVerticalMargin * 2

private val HandheldNavPillBackground = Color(0xFF1E1E1E).copy(alpha = 0.94f)
private val HandheldNavSelectedPill = Color.White.copy(alpha = 0.16f)

@Composable
internal fun rememberHandheldChromeContentPadding(
    topBarVisible: Boolean
): Pair<Dp, Dp> {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return remember(topBarVisible, statusBarTop, navigationBarBottom) {
        val bottom = navigationBarBottom + MobileHandheldBottomBarReservedHeight
        if (!topBarVisible) {
            statusBarTop to bottom
        } else {
            // Status bar → header → content (content must not draw under the status bar).
            val top = statusBarTop + MobileHandheldTopBarHeight
            top to bottom
        }
    }
}

/** Total height of the floating mobile header (status bar + toolbar) for optional content padding. */
@Composable
internal fun rememberHandheldHeaderOverlayHeight(): Dp {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return remember(statusBarTop) {
        statusBarTop + MobileHandheldTopBarHeight
    }
}

private data class HandheldBottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val handheldBottomNavItems = listOf(
    HandheldBottomNavItem(Routes.HOME, R.string.nav_handheld_home, Icons.Default.Home),
    HandheldBottomNavItem(Routes.LIVE_TV, R.string.nav_handheld_live_tv, Icons.Default.PlayArrow),
    HandheldBottomNavItem(Routes.MOVIES, R.string.nav_handheld_movies, Icons.Default.Star),
    HandheldBottomNavItem(Routes.SERIES, R.string.nav_handheld_series, Icons.Default.Menu)
)

internal fun isHandheldBottomTabSelected(tabRoute: String, currentRoute: String): Boolean {
    return when (tabRoute) {
        Routes.HOME ->
            currentRoute == Routes.HOME || currentRoute.startsWith("${Routes.HOME}?")
        Routes.LIVE_TV ->
            currentRoute.startsWith(Routes.LIVE_TV)
        Routes.MOVIES ->
            currentRoute.startsWith(Routes.MOVIES) ||
                currentRoute.startsWith("movie_detail") ||
                currentRoute == Routes.VOD ||
                currentRoute.startsWith("${Routes.VOD}?")
        Routes.SERIES ->
            currentRoute.startsWith(Routes.SERIES) ||
                currentRoute.startsWith("series_detail")
        else -> currentRoute.startsWith(tabRoute)
    }
}

@Composable
fun MobileHandheldTopBar(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    additionalActions: (@Composable RowScope.() -> Unit)? = null
) {
    var accountMenuExpanded by remember { mutableStateOf(false) }
    val sounds = rememberTvInteractionSounds()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MobileHandheldTopBarHeight)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(R.drawable.coovery_brand_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.height(30.dp),
                contentScale = ContentScale.Fit
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (additionalActions != null) {
                    additionalActions()
                }
                Box {
                    val openAccountMenu = {
                        sounds.playSelect()
                        accountMenuExpanded = true
                    }
                    val accountContentDescription = stringResource(R.string.nav_handheld_account)
                    Surface(
                        onClick = openAccountMenu,
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = AppColors.Brand,
                            focusedContainerColor = AppColors.Brand.copy(alpha = 0.88f)
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .mouseClickable(onClick = openAccountMenu)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = accountContentDescription,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = accountMenuExpanded,
                        onDismissRequest = { accountMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_handheld_account_settings)) },
                            onClick = {
                                accountMenuExpanded = false
                                onNavigate(Routes.SETTINGS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_handheld_account_guide)) },
                            onClick = {
                                accountMenuExpanded = false
                                onNavigate(Routes.EPG)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_handheld_epg)) },
                            onClick = {
                                accountMenuExpanded = false
                                onNavigate(Routes.EPG)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MobileHandheldBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sounds = rememberTvInteractionSounds()
    val pillShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MobileHandheldBottomBarHorizontalMargin,
                end = MobileHandheldBottomBarHorizontalMargin,
                bottom = MobileHandheldBottomBarVerticalMargin
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = pillShape, clip = false)
                .clip(pillShape)
                .background(HandheldNavPillBackground)
                .height(MobileHandheldBottomBarContentHeight)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            handheldBottomNavItems.forEach { item ->
                val selected = isHandheldBottomTabSelected(item.route, currentRoute)
                val label = stringResource(item.labelRes)
                val navigateToTab = {
                    sounds.playSelect()
                    onNavigate(item.route)
                }
                Surface(
                    onClick = navigateToTab,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (selected) HandheldNavSelectedPill else Color.Transparent,
                        focusedContainerColor = Color.White.copy(alpha = 0.22f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .mouseClickable(onClick = navigateToTab)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label,
                            tint = if (selected) Color.White else Color.White.copy(alpha = 0.58f),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                letterSpacing = 0.1.sp
                            ),
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.58f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
