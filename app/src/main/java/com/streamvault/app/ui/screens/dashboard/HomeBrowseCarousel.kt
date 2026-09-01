package com.streamvault.app.ui.screens.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

private const val HOME_CAROUSEL_CARD_COUNT = 5
private const val HOME_CAROUSEL_AUTO_ADVANCE_MS = 3_000L
private const val COOVERY_BANNER_ASPECT_RATIO = 2000f / 626f

internal data class HomeSeriesCategoryCard(
    val key: String,
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val categoryMatchers: List<String>
)

@Composable
internal fun HomeHeroCarousel(
    modifier: Modifier = Modifier,
    onCardClick: (Int) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    val horizontalPadding = when {
        screenWidth < 700.dp -> 16.dp
        !isTelevisionDevice && screenWidth < 1280.dp -> 20.dp
        else -> 24.dp
    }
    val cardWidth = when {
        screenWidth < 700.dp -> screenWidth - horizontalPadding * 2
        !isTelevisionDevice && screenWidth < 1280.dp -> (screenWidth - horizontalPadding * 2) * 0.94f
        else -> (screenWidth - horizontalPadding * 2) * 0.92f
    }
    val cardHeight = (cardWidth / COOVERY_BANNER_ASPECT_RATIO)
        .coerceAtMost(screenHeight * 0.42f)
        .coerceAtLeast(140.dp)
    val cardShape = RoundedCornerShape(20.dp)
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(HOME_CAROUSEL_AUTO_ADVANCE_MS)
            currentIndex = (currentIndex + 1) % HOME_CAROUSEL_CARD_COUNT
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = currentIndex,
            animationSpec = tween(durationMillis = 450),
            label = "home_hero_carousel"
        ) { index ->
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
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f)
            ) {
                Image(
                    painter = painterResource(R.drawable.coovery_hero_banner),
                    contentDescription = stringResource(R.string.home_carousel_banner_content_description),
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
internal fun HomeSeriesCategoryCardsRow(
    visibleCategoryNames: List<String>,
    onCategorySelected: (String) -> Unit,
    onNavigateToSeries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = remember { homeSeriesCategoryCards() }
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
            title = stringResource(R.string.home_subscriptions_section_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(resolvedCards, key = { _, (card, _) -> card.key }) { _, (card, matchedCategory) ->
                HomeSeriesCategoryCardItem(
                    card = card,
                    onClick = {
                        if (matchedCategory != null) {
                            onCategorySelected(matchedCategory)
                        } else {
                            onNavigateToSeries()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeSeriesCategoryCardItem(
    card: HomeSeriesCategoryCard,
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

private fun homeSeriesCategoryCards(): List<HomeSeriesCategoryCard> = listOf(
    HomeSeriesCategoryCard(
        key = "netflix",
        label = "Netflix",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("netflix")
    ),
    HomeSeriesCategoryCard(
        key = "hbo_max",
        label = "HBO Max",
        backgroundColor = Color(0xFF002BE7),
        contentColor = Color.White,
        categoryMatchers = listOf("hbo", "max")
    ),
    HomeSeriesCategoryCard(
        key = "disney_plus",
        label = "Disney+",
        backgroundColor = Color(0xFF113CCF),
        contentColor = Color.White,
        categoryMatchers = listOf("disney")
    ),
    HomeSeriesCategoryCard(
        key = "prime_video",
        label = "Prime Video",
        backgroundColor = Color(0xFF00A8E1),
        contentColor = Color.White,
        categoryMatchers = listOf("prime", "amazon")
    ),
    HomeSeriesCategoryCard(
        key = "discovery",
        label = "Discovery",
        backgroundColor = Color(0xFF0047AB),
        contentColor = Color.White,
        categoryMatchers = listOf("discovery")
    ),
    HomeSeriesCategoryCard(
        key = "paramount_plus",
        label = "Paramount+",
        backgroundColor = Color(0xFF0064FF),
        contentColor = Color.White,
        categoryMatchers = listOf("paramount")
    ),
    HomeSeriesCategoryCard(
        key = "globoplay",
        label = "Globoplay",
        backgroundColor = Color(0xFFE53935),
        contentColor = Color.White,
        categoryMatchers = listOf("globo", "globoplay")
    ),
    HomeSeriesCategoryCard(
        key = "apple_tv_plus",
        label = "Apple TV+",
        backgroundColor = Color(0xFF1C1C1E),
        contentColor = Color.White,
        categoryMatchers = listOf("apple", "appletv")
    ),
    HomeSeriesCategoryCard(
        key = "brasil_paralelo",
        label = "Brasil Paralelo",
        backgroundColor = Color(0xFF0D3B66),
        contentColor = Color.White,
        categoryMatchers = listOf("brasil paralelo", "paralelo")
    ),
    HomeSeriesCategoryCard(
        key = "onlyfans_privacy",
        label = "OnlyFans/Privacy",
        backgroundColor = Color(0xFF00AFF0),
        contentColor = Color.White,
        categoryMatchers = listOf("onlyfans", "privacy")
    ),
    HomeSeriesCategoryCard(
        key = "amc",
        label = "AMC",
        backgroundColor = Color(0xFF7B1E3A),
        contentColor = Color.White,
        categoryMatchers = listOf("amc")
    )
)
