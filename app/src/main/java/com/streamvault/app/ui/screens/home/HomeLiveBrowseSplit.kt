package com.streamvault.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
internal fun HomeLiveBrowseSplit(
    stacked: Boolean,
    sidebarWidth: Dp,
    modifier: Modifier = Modifier,
    claroHandheldLayout: Boolean = false,
    sidebar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (stacked) {
        val sidebarWeight = if (claroHandheldLayout) 0.22f else 0.4f
        val contentWeight = if (claroHandheldLayout) 0.78f else 0.6f
        Column(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(sidebarWeight)
                    .fillMaxHeight()
            ) {
                sidebar()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(contentWeight)
                    .fillMaxHeight()
            ) {
                content()
            }
        }
    } else {
        Row(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(sidebarWidth)
                    .fillMaxHeight()
            ) {
                sidebar()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                content()
            }
        }
    }
}
