package com.streamvault.app.ui.components.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.streamvault.app.R
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.rememberTvInteractionSounds

internal val MobileHandheldTopBarHeight = 52.dp
internal val MobileHandheldBottomBarHeight = 64.dp

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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MobileHandheldTopBarHeight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.coovery_brand_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.height(28.dp),
            contentScale = ContentScale.Fit
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (additionalActions != null) {
                additionalActions()
            }
            Box {
                Surface(
                    onClick = {
                        sounds.playSelect()
                        accountMenuExpanded = true
                    },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(999.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.nav_handheld_account),
                        style = MaterialTheme.typography.labelLarge,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
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

@Composable
fun MobileHandheldBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sounds = rememberTvInteractionSounds()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF141414))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MobileHandheldBottomBarHeight)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            handheldBottomNavItems.forEach { item ->
                val selected = isHandheldBottomTabSelected(item.route, currentRoute)
                val label = stringResource(item.labelRes)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        onClick = {
                            sounds.playSelect()
                            if (!selected) onNavigate(item.route)
                        },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(22.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (selected) Color.White.copy(alpha = 0.14f) else Color.Transparent,
                            focusedContainerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = label,
                                tint = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    letterSpacing = 0.2.sp
                                ),
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
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
}
