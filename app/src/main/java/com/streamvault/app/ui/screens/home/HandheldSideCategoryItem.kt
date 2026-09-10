package com.streamvault.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.streamvault.app.ui.interaction.mouseClickable
import com.streamvault.app.ui.theme.OnSurface
import com.streamvault.app.ui.theme.OnSurfaceDim
import com.streamvault.app.ui.theme.Primary
import com.streamvault.domain.model.Category

@Composable
internal fun HandheldSideCategoryItem(
    category: Category,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val select = { onClick() }
    Surface(
        onClick = select,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.18f) else Color.Transparent,
            focusedContainerColor = Primary.copy(alpha = 0.24f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .mouseClickable(onClick = select)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .padding(vertical = 2.dp)
                    .background(
                        color = if (isSelected) Primary else Color.Transparent,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(vertical = 10.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Primary else OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp, end = 4.dp)
            )
            Text(
                text = category.count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim
            )
            if (isLocked) {
                Text(
                    text = "🔒",
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
