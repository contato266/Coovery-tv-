package com.streamvault.app.ui.screens.series

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.device.rememberIsTelevisionDevice
import com.streamvault.app.ui.components.shell.AppSectionHeader
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.TvClickableSurface

private const val SERIES_CAROUSEL_CARD_COUNT = 5

internal data class SeriesCategoryCard(
    val key: String,
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val categoryMatchers: List<String>
)

@Composable
internal fun SeriesHeroCarousel(
    modifier: Modifier = Modifier,
    onCardClick: (Int) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    val cardWidth = when {
        screenWidth < 700.dp -> screenWidth * 0.88f
        !isTelevisionDevice && screenWidth < 1280.dp -> screenWidth * 0.82f
        else -> screenWidth * 0.78f
    }
    val cardHeight = cardWidth / 3.2f
    val cardShape = RoundedCornerShape(18.dp)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(SERIES_CAROUSEL_CARD_COUNT, key = { index -> "series_carousel_$index" }) { index ->
            TvClickableSurface(
                onClick = { onCardClick(index) },
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight),
                shape = ClickableSurfaceDefaults.shape(cardShape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Black,
                    focusedContainerColor = AppColors.SurfaceEmphasis
                ),
                border = ClickableSurfaceDefaults.border(
                    border = Border(
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.92f)),
                        shape = cardShape
                    ),
                    focusedBorder = Border(
                        border = BorderStroke(3.dp, AppColors.Focus),
                        shape = cardShape
                    )
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
            ) {
                Image(
                    painter = painterResource(R.drawable.coovery_hero_banner),
                    contentDescription = stringResource(R.string.series_carousel_banner_content_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(cardShape)
                )
            }
        }
    }
}

@Composable
internal fun SeriesCategoryCardsRow(
    visibleCategoryNames: List<String>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = remember { seriesCategoryCards() }
    val resolvedCards = remember(visibleCategoryNames, cards) {
        cards.map { card ->
            val matchedCategory = visibleCategoryNames.firstOrNull { name ->
                card.categoryMatchers.any { matcher ->
                    name.contains(matcher, ignoreCase = true)
                }
            }
            card to matchedCategory
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AppSectionHeader(
            title = stringResource(R.string.series_browse_section_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(resolvedCards, key = { _, (card, _) -> card.key }) { _, (card, matchedCategory) ->
                SeriesCategoryCardItem(
                    card = card,
                    onClick = {
                        matchedCategory?.let(onCategorySelected)
                    }
                )
            }
        }
    }
}

@Composable
private fun SeriesCategoryCardItem(
    card: SeriesCategoryCard,
    onClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    val cardSize = when {
        screenWidth < 700.dp -> 108.dp
        !isTelevisionDevice && screenWidth < 1280.dp -> 118.dp
        else -> 128.dp
    }
    val cardShape = RoundedCornerShape(14.dp)

    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier
            .width(cardSize)
            .height(cardSize),
        shape = ClickableSurfaceDefaults.shape(cardShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = card.backgroundColor,
            focusedContainerColor = card.backgroundColor.copy(alpha = 0.88f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, AppColors.Focus),
                shape = cardShape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = if (card.label.length > 10) 13.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                ),
                color = card.contentColor,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun seriesCategoryCards(): List<SeriesCategoryCard> = listOf(
    SeriesCategoryCard(
        key = "netflix",
        label = "Netflix",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("netflix")
    ),
    SeriesCategoryCard(
        key = "hbo_max",
        label = "HBO Max",
        backgroundColor = Color(0xFF002BE7),
        contentColor = Color.White,
        categoryMatchers = listOf("hbo", "max")
    ),
    SeriesCategoryCard(
        key = "disney_plus",
        label = "Disney+",
        backgroundColor = Color(0xFF113CCF),
        contentColor = Color.White,
        categoryMatchers = listOf("disney")
    ),
    SeriesCategoryCard(
        key = "prime_video",
        label = "Prime Video",
        backgroundColor = Color(0xFF00A8E1),
        contentColor = Color.White,
        categoryMatchers = listOf("prime", "amazon")
    ),
    SeriesCategoryCard(
        key = "discovery",
        label = "Discovery",
        backgroundColor = Color(0xFF0047AB),
        contentColor = Color.White,
        categoryMatchers = listOf("discovery")
    ),
    SeriesCategoryCard(
        key = "paramount_plus",
        label = "Paramount+",
        backgroundColor = Color(0xFF0064FF),
        contentColor = Color.White,
        categoryMatchers = listOf("paramount")
    ),
    SeriesCategoryCard(
        key = "action",
        label = "Ação",
        backgroundColor = Color(0xFF1A1A1A),
        contentColor = Color.White,
        categoryMatchers = listOf("ação", "acao", "action")
    ),
    SeriesCategoryCard(
        key = "drama",
        label = "Drama",
        backgroundColor = Color(0xFF2B1B2E),
        contentColor = Color.White,
        categoryMatchers = listOf("drama")
    ),
    SeriesCategoryCard(
        key = "comedy",
        label = "Comédia",
        backgroundColor = Color(0xFF3D2C1E),
        contentColor = Color.White,
        categoryMatchers = listOf("comédia", "comedia", "comedy")
    ),
    SeriesCategoryCard(
        key = "sci_fi",
        label = "Sci-Fi",
        backgroundColor = Color(0xFF0F1B2D),
        contentColor = Color.White,
        categoryMatchers = listOf("sci", "ficção", "ficcao", "science")
    ),
    SeriesCategoryCard(
        key = "horror",
        label = "Terror",
        backgroundColor = Color(0xFF1F0A0A),
        contentColor = Color.White,
        categoryMatchers = listOf("terror", "horror")
    ),
    SeriesCategoryCard(
        key = "romance",
        label = "Romance",
        backgroundColor = Color(0xFF3A1028),
        contentColor = Color.White,
        categoryMatchers = listOf("romance", "romântico", "romantico")
    ),
    SeriesCategoryCard(
        key = "animation",
        label = "Animação",
        backgroundColor = Color(0xFF1E3A2F),
        contentColor = Color.White,
        categoryMatchers = listOf("animação", "animacao", "anime", "cartoon")
    ),
    SeriesCategoryCard(
        key = "documentary",
        label = "Documentário",
        backgroundColor = Color(0xFF1C2430),
        contentColor = Color.White,
        categoryMatchers = listOf("documentário", "documentario", "documentary", "doc")
    )
)
