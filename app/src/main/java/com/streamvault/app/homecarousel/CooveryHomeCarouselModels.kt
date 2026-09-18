package com.streamvault.app.homecarousel

import androidx.annotation.DrawableRes
import com.streamvault.app.R

data class HomeHeroCarouselSlide(
    val id: String,
    val imageUrl: String?,
    @DrawableRes val fallbackBannerRes: Int,
    val linkTarget: HomeHeroCarouselLinkTarget
)

sealed interface HomeHeroCarouselLinkTarget {
    data object None : HomeHeroCarouselLinkTarget
    data class AppRoute(val route: String) : HomeHeroCarouselLinkTarget
    data class ExternalUrl(val url: String) : HomeHeroCarouselLinkTarget
}

internal fun defaultTelevisionHomeHeroSlides(): List<HomeHeroCarouselSlide> =
    List(5) { index ->
        HomeHeroCarouselSlide(
            id = "default-$index",
            imageUrl = null,
            fallbackBannerRes = defaultTelevisionBannerRes(index),
            linkTarget = HomeHeroCarouselLinkTarget.AppRoute("series")
        )
    }

internal fun defaultHandheldHomeHeroSlides(): List<HomeHeroCarouselSlide> =
    List(5) { index ->
        HomeHeroCarouselSlide(
            id = "default-handheld-$index",
            imageUrl = null,
            fallbackBannerRes = defaultHandheldBannerRes(index),
            linkTarget = HomeHeroCarouselLinkTarget.None
        )
    }

@DrawableRes
internal fun defaultHandheldBannerRes(@Suppress("UNUSED_PARAMETER") index: Int): Int =
    R.drawable.coovery_handheld_carousel_promo

@DrawableRes
internal fun defaultTelevisionBannerRes(index: Int): Int = when (index) {
    1 -> R.drawable.coovery_carousel_banner_2
    2 -> R.drawable.coovery_carousel_banner_3
    3 -> R.drawable.coovery_carousel_banner_4
    4 -> R.drawable.coovery_carousel_banner_5
    else -> R.drawable.coovery_hero_banner
}
