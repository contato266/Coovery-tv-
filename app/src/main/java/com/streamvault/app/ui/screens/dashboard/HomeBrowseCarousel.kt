package com.streamvault.app.ui.screens.dashboard

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
import com.streamvault.domain.model.Category

private const val HOME_CAROUSEL_CARD_COUNT = 5
private const val COOVERY_BANNER_ASPECT_RATIO = 2000f / 626f

internal data class HomeSubscriptionCard(
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

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(HOME_CAROUSEL_CARD_COUNT, key = { index -> "home_carousel_$index" }) { index ->
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
internal fun HomeAssinaturasSection(
    liveCategories: List<Category>,
    seriesCategories: List<Category>,
    onLiveCategorySelected: (Long) -> Unit,
    onSeriesCategorySelected: (Long) -> Unit,
    onNavigateToLiveTv: () -> Unit,
    onNavigateToSeries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveCards = remember { homeLiveSubscriptionCards() }
    val seriesCards = remember { homeSeriesSubscriptionCards() }
    val resolvedLiveCards = remember(liveCategories, liveCards) {
        resolveSubscriptionCards(liveCards, liveCategories)
    }
    val resolvedSeriesCards = remember(seriesCategories, seriesCards) {
        resolveSubscriptionCards(seriesCards, seriesCategories)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AppSectionHeader(
            title = stringResource(R.string.home_assinaturas_section_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp)
        )

        SubscriptionCardsRow(
            resolvedCards = resolvedLiveCards,
            onCardClick = { matchedCategory ->
                if (matchedCategory != null) {
                    onLiveCategorySelected(matchedCategory.id)
                } else {
                    onNavigateToLiveTv()
                }
            }
        )

        SubscriptionCardsRow(
            resolvedCards = resolvedSeriesCards,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            onCardClick = { matchedCategory ->
                if (matchedCategory != null) {
                    onSeriesCategorySelected(matchedCategory.id)
                } else {
                    onNavigateToSeries()
                }
            }
        )
    }
}

@Composable
private fun SubscriptionCardsRow(
    resolvedCards: List<Pair<HomeSubscriptionCard, Category?>>,
    onCardClick: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(resolvedCards, key = { _, (card, _) -> card.key }) { _, (card, matchedCategory) ->
            HomeSubscriptionCardItem(
                card = card,
                onClick = { onCardClick(matchedCategory) }
            )
        }
    }
}

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

private fun resolveSubscriptionCards(
    cards: List<HomeSubscriptionCard>,
    categories: List<Category>
): List<Pair<HomeSubscriptionCard, Category?>> {
    return cards.map { card ->
        val matchedCategory = categories.firstOrNull { category ->
            card.categoryMatchers.any { matcher ->
                category.name.contains(matcher, ignoreCase = true)
            }
        }
        card to matchedCategory
    }
}

private fun homeLiveSubscriptionCards(): List<HomeSubscriptionCard> = listOf(
    HomeSubscriptionCard(
        key = "premiere",
        label = "Premiere",
        backgroundColor = Color(0xFF0B6B3A),
        contentColor = Color.White,
        categoryMatchers = listOf("premiere")
    ),
    HomeSubscriptionCard(
        key = "sportynet",
        label = "SportyNet",
        backgroundColor = Color(0xFF111111),
        contentColor = Color.White,
        categoryMatchers = listOf("sportynet", "sporty net", "sporty")
    ),
    HomeSubscriptionCard(
        key = "telecine",
        label = "Telecine",
        backgroundColor = Color(0xFF7B1FA2),
        contentColor = Color.White,
        categoryMatchers = listOf("telecine")
    ),
    HomeSubscriptionCard(
        key = "dogtv",
        label = "Dogtv",
        backgroundColor = Color(0xFFE65100),
        contentColor = Color.White,
        categoryMatchers = listOf("dogtv", "dog tv")
    ),
    HomeSubscriptionCard(
        key = "sky",
        label = "Sky",
        backgroundColor = Color(0xFF0072C6),
        contentColor = Color.White,
        categoryMatchers = listOf("sky")
    )
)

private fun homeSeriesSubscriptionCards(): List<HomeSubscriptionCard> = listOf(
    HomeSubscriptionCard(
        key = "netflix",
        label = "Netflix",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("netflix")
    ),
    HomeSubscriptionCard(
        key = "hbo_max",
        label = "HBO Max",
        backgroundColor = Color(0xFF002BE7),
        contentColor = Color.White,
        categoryMatchers = listOf("hbo max", "hbo")
    ),
    HomeSubscriptionCard(
        key = "prime_video",
        label = "Prime Video",
        backgroundColor = Color(0xFF00A8E1),
        contentColor = Color.White,
        categoryMatchers = listOf("prime video", "prime", "amazon")
    ),
    HomeSubscriptionCard(
        key = "discovery",
        label = "Discovery",
        backgroundColor = Color(0xFF0047AB),
        contentColor = Color.White,
        categoryMatchers = listOf("discovery")
    ),
    HomeSubscriptionCard(
        key = "paramount",
        label = "Paramount",
        backgroundColor = Color(0xFF0064FF),
        contentColor = Color.White,
        categoryMatchers = listOf("paramount")
    ),
    HomeSubscriptionCard(
        key = "globoplay",
        label = "Globoplay",
        backgroundColor = Color(0xFFE50914),
        contentColor = Color.White,
        categoryMatchers = listOf("globoplay", "globo play")
    ),
    HomeSubscriptionCard(
        key = "apple_tv_plus",
        label = "Apple tv+",
        backgroundColor = Color(0xFF1C1C1E),
        contentColor = Color.White,
        categoryMatchers = listOf("apple tv", "appletv", "apple+")
    ),
    HomeSubscriptionCard(
        key = "brasil_paralelo",
        label = "Brasil Paralelo",
        backgroundColor = Color(0xFF1A237E),
        contentColor = Color.White,
        categoryMatchers = listOf("brasil paralelo", "paralelo")
    ),
    HomeSubscriptionCard(
        key = "onlyfans_privacy",
        label = "Onlyfans/privacy",
        backgroundColor = Color(0xFF00AFF0),
        contentColor = Color.White,
        categoryMatchers = listOf("onlyfans", "privacy", "only fans")
    ),
    HomeSubscriptionCard(
        key = "amc",
        label = "AMC",
        backgroundColor = Color(0xFF1A1A1A),
        contentColor = Color.White,
        categoryMatchers = listOf("amc")
    )
)
