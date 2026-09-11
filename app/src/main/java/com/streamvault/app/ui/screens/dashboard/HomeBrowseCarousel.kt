package com.streamvault.app.ui.screens.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
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
import kotlin.math.absoluteValue

private const val HOME_CAROUSEL_CARD_COUNT = 5
private const val HOME_CAROUSEL_AUTO_ADVANCE_MS = 3_000L
private const val COOVERY_BANNER_ASPECT_RATIO = 2000f / 626f
/** Portrait promo card (1200×1600) for handheld home carousel. */
private const val HANDHELD_CAROUSEL_ASPECT_RATIO = 1200f / 1600f
private const val HANDHELD_CAROUSEL_VIRTUAL_PAGE_COUNT = 10_000
private const val HANDHELD_CAROUSEL_CENTERED_WIDTH_FRACTION = 0.72f
private const val HANDHELD_CAROUSEL_SIDE_SCALE = 0.88f
private const val HANDHELD_CAROUSEL_SIDE_ALPHA = 0.78f

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
    val destination: HomeSubscriptionDestination,
    @DrawableRes val imageRes: Int? = null
)

@Composable
internal fun HomeHeroCarousel(
    modifier: Modifier = Modifier,
    onCardClick: (Int) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    if (!isTelevisionDevice && screenWidth < 700.dp) {
        HandheldPortraitHeroCarousel(modifier = modifier, onCardClick = onCardClick)
        return
    }
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
private fun HandheldPortraitHeroCarousel(
    modifier: Modifier = Modifier,
    onCardClick: (Int) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val pageWidth = screenWidth * HANDHELD_CAROUSEL_CENTERED_WIDTH_FRACTION
    val sidePeekPadding = (screenWidth - pageWidth) / 2
    val pageHeight = pageWidth / HANDHELD_CAROUSEL_ASPECT_RATIO
    val pagerViewportHeight = pageHeight * 1.06f
    val cardShape = RoundedCornerShape(18.dp)
    val pageCount = HOME_CAROUSEL_CARD_COUNT
    val virtualPageCount = HANDHELD_CAROUSEL_VIRTUAL_PAGE_COUNT
    val pagerState = rememberPagerState(
        initialPage = virtualPageCount / 2,
        pageCount = { virtualPageCount }
    )
    val activeIndex by remember {
        derivedStateOf { pagerState.currentPage % pageCount }
    }
    val indicatorProgress by remember {
        derivedStateOf {
            val logical = pagerState.currentPage % pageCount
            val fraction = pagerState.currentPageOffsetFraction
            if (fraction >= 0f) {
                (logical + fraction) % pageCount.toFloat()
            } else {
                (logical + fraction + pageCount) % pageCount.toFloat()
            }
        }
    }

    LaunchedEffect(pagerState, pageCount, virtualPageCount) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val buffer = pageCount * 12
                val center = virtualPageCount / 2
                if (page < buffer || page > virtualPageCount - buffer) {
                    val logical = page % pageCount
                    pagerState.scrollToPage(center + logical)
                }
            }
    }

    LaunchedEffect(pagerState) {
        while (true) {
            delay(HOME_CAROUSEL_AUTO_ADVANCE_MS)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(pagerViewportHeight),
            contentPadding = PaddingValues(horizontal = sidePeekPadding),
            pageSpacing = 10.dp,
            beyondViewportPageCount = 2,
            pageSize = PageSize.Fixed(pageWidth),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            val index = page % pageCount
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                .absoluteValue
                .coerceIn(0f, 1f)
            val scale = lerp(1f, HANDHELD_CAROUSEL_SIDE_SCALE, pageOffset)
            val alpha = lerp(1f, HANDHELD_CAROUSEL_SIDE_ALPHA, pageOffset)
            Image(
                painter = painterResource(R.drawable.coovery_handheld_carousel_promo),
                contentDescription = stringResource(R.string.home_carousel_banner_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(pageWidth)
                    .height(pageHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(cardShape)
                    .clickable { onCardClick(index) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HandheldCarouselPageIndicators(
            pageCount = pageCount,
            activeIndex = activeIndex,
            scrollProgress = indicatorProgress
        )
    }
}

@Composable
private fun HandheldCarouselPageIndicators(
    pageCount: Int,
    activeIndex: Int,
    scrollProgress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val distance = minOf(
                (index - scrollProgress).absoluteValue,
                (index - scrollProgress + pageCount).absoluteValue,
                (index - scrollProgress - pageCount).absoluteValue
            )
            val emphasis = (1f - distance).coerceIn(0f, 1f)
            val targetWidth = lerp(6f, 18f, emphasis)
            val animatedWidth by animateDpAsState(
                targetValue = targetWidth.dp,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "handheld_carousel_indicator_width_$index"
            )
            val dotAlpha = lerp(0.35f, 1f, emphasis)
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(animatedWidth)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = dotAlpha))
            )
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
        if (card.imageRes != null) {
            Image(
                painter = painterResource(card.imageRes),
                contentDescription = card.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
            )
        } else {
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
}

private fun homeLiveSubscriptionCards(): List<HomeSubscriptionCard> = listOf(
    HomeSubscriptionCard(
        key = "premiere",
        label = "Premiere",
        backgroundColor = Color(0xFF0B6B3A),
        contentColor = Color.White,
        categoryMatchers = listOf("premiere"),
        destination = HomeSubscriptionDestination.LIVE,
        imageRes = R.drawable.assinatura_card_premiere
    ),
    HomeSubscriptionCard(
        key = "sportynet",
        label = "SportyNet",
        backgroundColor = Color(0xFF111111),
        contentColor = Color.White,
        categoryMatchers = listOf("sportynet", "sporty net", "sporty"),
        destination = HomeSubscriptionDestination.LIVE,
        imageRes = R.drawable.assinatura_card_sportynet
    ),
    HomeSubscriptionCard(
        key = "telecine",
        label = "Telecine",
        backgroundColor = Color(0xFF7B1FA2),
        contentColor = Color.White,
        categoryMatchers = listOf("telecine"),
        destination = HomeSubscriptionDestination.LIVE,
        imageRes = R.drawable.assinatura_card_telecine
    ),
    HomeSubscriptionCard(
        key = "dogtv",
        label = "Dogtv",
        backgroundColor = Color(0xFFE65100),
        contentColor = Color.White,
        categoryMatchers = listOf("dogtv", "dog tv"),
        destination = HomeSubscriptionDestination.LIVE,
        imageRes = R.drawable.assinatura_card_dogtv
    ),
    HomeSubscriptionCard(
        key = "sky",
        label = "Sky",
        backgroundColor = Color(0xFF0072C6),
        contentColor = Color.White,
        categoryMatchers = listOf("sky"),
        destination = HomeSubscriptionDestination.LIVE,
        imageRes = R.drawable.assinatura_card_sky
    )
)

private fun homeSeriesSubscriptionCards(): List<HomeSubscriptionCard> = listOf(
    HomeSubscriptionCard(
        key = "netflix",
        label = "Netflix",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("netflix"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_netflix
    ),
    HomeSubscriptionCard(
        key = "hbo_max",
        label = "HBO Max",
        backgroundColor = Color(0xFF002BE7),
        contentColor = Color.White,
        categoryMatchers = listOf("hbo max", "hbo"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_hbo_max
    ),
    HomeSubscriptionCard(
        key = "prime_video",
        label = "Prime Video",
        backgroundColor = Color(0xFF00A8E1),
        contentColor = Color.White,
        categoryMatchers = listOf("prime video", "prime", "amazon"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_prime_video
    ),
    HomeSubscriptionCard(
        key = "discovery",
        label = "Discovery",
        backgroundColor = Color(0xFF0047AB),
        contentColor = Color.White,
        categoryMatchers = listOf("discovery"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_discovery
    ),
    HomeSubscriptionCard(
        key = "paramount",
        label = "Paramount",
        backgroundColor = Color(0xFF0064FF),
        contentColor = Color.White,
        categoryMatchers = listOf("paramount"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_paramount
    ),
    HomeSubscriptionCard(
        key = "globoplay",
        label = "Globoplay",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("globoplay", "globo play"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_globoplay
    ),
    HomeSubscriptionCard(
        key = "apple_tv_plus",
        label = "Apple tv+",
        backgroundColor = Color(0xFF1C1C1E),
        contentColor = Color.White,
        categoryMatchers = listOf("apple tv", "appletv", "apple+"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_apple_tv
    ),
    HomeSubscriptionCard(
        key = "brasil_paralelo",
        label = "Brasil Paralelo",
        backgroundColor = Color(0xFF1A237E),
        contentColor = Color.White,
        categoryMatchers = listOf("brasil paralelo", "paralelo"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_brasil_paralelo
    ),
    HomeSubscriptionCard(
        key = "onlyfans_privacy",
        label = "Onlyfans/privacy",
        backgroundColor = Color(0xFF00AFF0),
        contentColor = Color.White,
        categoryMatchers = listOf("onlyfans", "privacy", "only fans"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_onlyfans
    ),
    HomeSubscriptionCard(
        key = "amc",
        label = "AMC",
        backgroundColor = Color(0xFF1A1A1A),
        contentColor = Color.White,
        categoryMatchers = listOf("amc"),
        destination = HomeSubscriptionDestination.SERIES,
        imageRes = R.drawable.assinatura_card_amc
    )
)
