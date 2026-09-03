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
import androidx.annotation.DrawableRes
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.device.rememberIsTelevisionDevice
import com.streamvault.app.ui.components.shell.AppSectionHeader
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Category
import kotlinx.coroutines.delay

private const val HOME_CAROUSEL_CARD_COUNT = 5
private const val HOME_CAROUSEL_AUTO_ADVANCE_MS = 3_000L
private const val COOVERY_BANNER_ASPECT_RATIO = 2000f / 626f

@DrawableRes
private fun homeCarouselBannerRes(index: Int): Int = when (index) {
    1 -> R.drawable.coovery_carousel_banner_2
    2 -> R.drawable.coovery_carousel_banner_3
    3 -> R.drawable.coovery_carousel_banner_4
    4 -> R.drawable.coovery_carousel_banner_5
    else -> R.drawable.coovery_hero_banner
}

internal enum class HomeSubscriptionDestination {
    LIVE,
    SERIES
}

internal data class HomeSubscriptionCard(
    val key: String,
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val categoryMatchers: List<String>,
    val destination: HomeSubscriptionDestination
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
                    painter = painterResource(homeCarouselBannerRes(index)),
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
internal fun HomeAssinaturasSection(
    liveCategories: List<Category>,
    seriesCategories: List<Category>,
    onLiveCategorySelected: (Long) -> Unit,
    onSeriesCategorySelected: (Long) -> Unit,
    onNavigateToLiveTv: () -> Unit,
    onNavigateToSeries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = remember { homeAssinaturasCards() }
    val resolvedCards = remember(liveCategories, seriesCategories, cards) {
        cards.map { card ->
            val categories = when (card.destination) {
                HomeSubscriptionDestination.LIVE -> liveCategories
                HomeSubscriptionDestination.SERIES -> seriesCategories
            }
            val matchedCategory = categories.firstOrNull { category ->
                card.categoryMatchers.any { matcher ->
                    category.name.contains(matcher, ignoreCase = true)
                }
            }
            ResolvedSubscriptionCard(card, matchedCategory)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AppSectionHeader(
            title = stringResource(R.string.home_assinaturas_section_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(resolvedCards, key = { _, resolved -> resolved.card.key }) { _, resolved ->
                HomeSubscriptionCardItem(
                    card = resolved.card,
                    onClick = {
                        val matchedCategory = resolved.matchedCategory
                        if (matchedCategory != null) {
                            when (resolved.card.destination) {
                                HomeSubscriptionDestination.LIVE -> onLiveCategorySelected(matchedCategory.id)
                                HomeSubscriptionDestination.SERIES -> onSeriesCategorySelected(matchedCategory.id)
                            }
                        } else {
                            when (resolved.card.destination) {
                                HomeSubscriptionDestination.LIVE -> onNavigateToLiveTv()
                                HomeSubscriptionDestination.SERIES -> onNavigateToSeries()
                            }
                        }
                    }
                )
            }
        }
    }
}

private data class ResolvedSubscriptionCard(
    val card: HomeSubscriptionCard,
    val matchedCategory: Category?
)

private fun homeAssinaturasCards(): List<HomeSubscriptionCard> =
    homeSeriesSubscriptionCards() + homeLiveSubscriptionCards()

@Composable
private fun HomeSubscriptionCardItem(
    card: HomeSubscriptionCard,
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

private fun homeLiveSubscriptionCards(): List<HomeSubscriptionCard> = listOf(
    HomeSubscriptionCard(
        key = "premiere",
        label = "Premiere",
        backgroundColor = Color(0xFF0B6B3A),
        contentColor = Color.White,
        categoryMatchers = listOf("premiere"),
        destination = HomeSubscriptionDestination.LIVE
    ),
    HomeSubscriptionCard(
        key = "sportynet",
        label = "SportyNet",
        backgroundColor = Color(0xFF111111),
        contentColor = Color.White,
        categoryMatchers = listOf("sportynet", "sporty net", "sporty"),
        destination = HomeSubscriptionDestination.LIVE
    ),
    HomeSubscriptionCard(
        key = "telecine",
        label = "Telecine",
        backgroundColor = Color(0xFF7B1FA2),
        contentColor = Color.White,
        categoryMatchers = listOf("telecine"),
        destination = HomeSubscriptionDestination.LIVE
    ),
    HomeSubscriptionCard(
        key = "dogtv",
        label = "Dogtv",
        backgroundColor = Color(0xFFE65100),
        contentColor = Color.White,
        categoryMatchers = listOf("dogtv", "dog tv"),
        destination = HomeSubscriptionDestination.LIVE
    ),
    HomeSubscriptionCard(
        key = "sky",
        label = "Sky",
        backgroundColor = Color(0xFF0072C6),
        contentColor = Color.White,
        categoryMatchers = listOf("sky"),
        destination = HomeSubscriptionDestination.LIVE
    )
)

private fun homeSeriesSubscriptionCards(): List<HomeSubscriptionCard> = listOf(
    HomeSubscriptionCard(
        key = "netflix",
        label = "Netflix",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("netflix"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "hbo_max",
        label = "HBO Max",
        backgroundColor = Color(0xFF002BE7),
        contentColor = Color.White,
        categoryMatchers = listOf("hbo max", "hbo"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "prime_video",
        label = "Prime Video",
        backgroundColor = Color(0xFF00A8E1),
        contentColor = Color.White,
        categoryMatchers = listOf("prime video", "prime", "amazon"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "discovery",
        label = "Discovery",
        backgroundColor = Color(0xFF0047AB),
        contentColor = Color.White,
        categoryMatchers = listOf("discovery"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "paramount",
        label = "Paramount",
        backgroundColor = Color(0xFF0064FF),
        contentColor = Color.White,
        categoryMatchers = listOf("paramount"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "globoplay",
        label = "Globoplay",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("globoplay", "globo play"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "apple_tv_plus",
        label = "Apple tv+",
        backgroundColor = Color(0xFF1C1C1E),
        contentColor = Color.White,
        categoryMatchers = listOf("apple tv", "appletv", "apple+"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "brasil_paralelo",
        label = "Brasil Paralelo",
        backgroundColor = Color(0xFF1A237E),
        contentColor = Color.White,
        categoryMatchers = listOf("brasil paralelo", "paralelo"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "onlyfans_privacy",
        label = "Onlyfans/privacy",
        backgroundColor = Color(0xFF00AFF0),
        contentColor = Color.White,
        categoryMatchers = listOf("onlyfans", "privacy", "only fans"),
        destination = HomeSubscriptionDestination.SERIES
    ),
    HomeSubscriptionCard(
        key = "amc",
        label = "AMC",
        backgroundColor = Color(0xFF1A1A1A),
        contentColor = Color.White,
        categoryMatchers = listOf("amc"),
        destination = HomeSubscriptionDestination.SERIES
    )
)
