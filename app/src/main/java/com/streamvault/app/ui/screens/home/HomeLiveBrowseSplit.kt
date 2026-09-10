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
    unitvHandheldLayout: Boolean = false,
    topPreview: (@Composable () -> Unit)? = null,
    sidebar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (stacked && unitvHandheldLayout) {
        Column(modifier = modifier.fillMaxSize()) {
            if (topPreview != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.32f)
                ) {
                    topPreview()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.68f)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.34f)
                        .fillMaxHeight()
                ) {
                    sidebar()
                }
                Box(
                    modifier = Modifier
                        .weight(0.66f)
                        .fillMaxHeight()
                ) {
                    content()
                }
            }
        }
    } else if (stacked) {
        Column(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                sidebar()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
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
