package com.streamvault.app.homecarousel

import org.json.JSONArray
import org.json.JSONObject

internal object CooveryHomeCarouselParser {
    fun parseRemotePayload(body: String): List<HomeHeroCarouselSlide> {
        val root = JSONObject(body)
        val slides = root.optJSONArray("slides") ?: JSONArray()
        val parsed = ArrayList<HomeHeroCarouselSlide>(slides.length())
        for (index in 0 until slides.length()) {
            val slide = slides.optJSONObject(index) ?: continue
            val imageUrl = slide.optString("imageUrl").trim().takeIf { it.isNotBlank() } ?: continue
            if (!imageUrl.startsWith("https://", ignoreCase = true)) {
                continue
            }
            val id = slide.optString("id").trim().ifBlank { "remote-$index" }
            parsed += HomeHeroCarouselSlide(
                id = id,
                imageUrl = imageUrl,
                fallbackBannerRes = defaultTelevisionBannerRes(index),
                linkTarget = resolveHomeHeroCarouselLink(slide.optString("linkUrl"))
            )
        }
        return parsed
    }
}
