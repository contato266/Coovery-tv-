package com.streamvault.app.homecarousel

import com.streamvault.app.navigation.Routes

fun resolveHomeHeroCarouselLink(linkUrl: String?): HomeHeroCarouselLinkTarget {
    val trimmed = linkUrl?.trim().orEmpty()
    if (trimmed.isBlank()) {
        return HomeHeroCarouselLinkTarget.None
    }
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return HomeHeroCarouselLinkTarget.ExternalUrl(trimmed)
    }

    val normalized = trimmed.removePrefix("/")
    return when {
        normalized.equals("home", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.HOME)
        normalized.equals("live", ignoreCase = true) ||
            normalized.equals("live_tv", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.LIVE_TV)
        normalized.startsWith("live_tv?", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(normalized)
        normalized.startsWith("live_tv/", ignoreCase = true) -> {
            val categoryId = normalized.substringAfter("live_tv/").substringBefore('?').toLongOrNull()
            HomeHeroCarouselLinkTarget.AppRoute(Routes.liveTv(categoryId))
        }
        normalized.equals("movies", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.MOVIES)
        normalized.equals("series", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.SERIES)
        normalized.startsWith("series?", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(normalized)
        normalized.startsWith("series/", ignoreCase = true) -> {
            val categoryId = normalized.substringAfter("series/").substringBefore('?').toLongOrNull()
            HomeHeroCarouselLinkTarget.AppRoute(Routes.series(categoryId))
        }
        normalized.equals("settings", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.SETTINGS)
        normalized.equals("search", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.SEARCH)
        normalized.equals("epg", ignoreCase = true) ||
            normalized.equals("guide", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.EPG)
        normalized.equals("downloads", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(Routes.DOWNLOADS)
        normalized.startsWith("movie_detail/", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(normalized)
        normalized.startsWith("series_detail/", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(normalized)
        normalized.startsWith("provider_setup", ignoreCase = true) -> HomeHeroCarouselLinkTarget.AppRoute(normalized)
        else -> HomeHeroCarouselLinkTarget.None
    }
}
