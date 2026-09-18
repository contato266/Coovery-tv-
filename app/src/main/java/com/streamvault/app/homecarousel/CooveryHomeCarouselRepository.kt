package com.streamvault.app.homecarousel

import android.content.Context
import com.streamvault.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

@Singleton
class CooveryHomeCarouselRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun getTelevisionSlides(forceRefresh: Boolean = false): List<HomeHeroCarouselSlide> =
        withContext(Dispatchers.IO) {
            val cached = readCachedSlides()
            val cacheFresh = cached != null && !forceRefresh && !isCacheExpired()
            if (cacheFresh) {
                return@withContext cached
            }

            val remote = runCatching { fetchRemoteSlides() }.getOrNull()
            if (remote != null && remote.isNotEmpty()) {
                writeCache(remote)
                remote
            } else {
                cached ?: defaultTelevisionHomeHeroSlides()
            }
        }

    private fun isCacheExpired(): Boolean {
        val fetchedAt = preferences.getLong(KEY_FETCHED_AT, 0L)
        if (fetchedAt <= 0L) return true
        return System.currentTimeMillis() - fetchedAt > CACHE_TTL_MS
    }

    private fun readCachedSlides(): List<HomeHeroCarouselSlide>? {
        val raw = preferences.getString(KEY_PAYLOAD, null) ?: return null
        return runCatching { parseSlidesJson(raw) }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun writeCache(slides: List<HomeHeroCarouselSlide>) {
        val array = JSONArray()
        slides.forEach { slide ->
            array.put(
                JSONObject()
                    .put("id", slide.id)
                    .put("imageUrl", slide.imageUrl)
                    .put("fallbackBannerRes", slide.fallbackBannerRes)
                    .put(
                        "linkTarget",
                        when (val target = slide.linkTarget) {
                            HomeHeroCarouselLinkTarget.None -> JSONObject().put("type", "none")
                            is HomeHeroCarouselLinkTarget.AppRoute -> JSONObject()
                                .put("type", "route")
                                .put("route", target.route)
                            is HomeHeroCarouselLinkTarget.ExternalUrl -> JSONObject()
                                .put("type", "url")
                                .put("url", target.url)
                        }
                    )
            )
        }
        preferences.edit()
            .putString(KEY_PAYLOAD, array.toString())
            .putLong(KEY_FETCHED_AT, System.currentTimeMillis())
            .apply()
    }

    @Throws(IOException::class)
    private fun fetchRemoteSlides(): List<HomeHeroCarouselSlide> {
        val request = Request.Builder()
            .url(BuildConfig.COOVERY_HOME_CAROUSEL_API_URL)
            .header("Accept", "application/json")
            .header("User-Agent", "Coovery-Home-Carousel/ ${BuildConfig.VERSION_NAME}")
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Home carousel HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) {
                throw IOException("Home carousel response was empty")
            }
            return CooveryHomeCarouselParser.parseRemotePayload(body)
        }
    }

    private fun parseSlidesJson(raw: String): List<HomeHeroCarouselSlide> {
        val slides = JSONArray(raw)
        val parsed = ArrayList<HomeHeroCarouselSlide>(slides.length())
        for (index in 0 until slides.length()) {
            val slide = slides.optJSONObject(index) ?: continue
            val linkJson = slide.optJSONObject("linkTarget")
            val linkTarget = when (linkJson?.optString("type")) {
                "route" -> HomeHeroCarouselLinkTarget.AppRoute(linkJson.optString("route"))
                "url" -> HomeHeroCarouselLinkTarget.ExternalUrl(linkJson.optString("url"))
                else -> HomeHeroCarouselLinkTarget.None
            }
            parsed += HomeHeroCarouselSlide(
                id = slide.optString("id").ifBlank { "cached-$index" },
                imageUrl = slide.optString("imageUrl").takeIf { it.isNotBlank() },
                fallbackBannerRes = slide.optInt("fallbackBannerRes", defaultTelevisionBannerRes(index)),
                linkTarget = linkTarget
            )
        }
        return parsed
    }

    private companion object {
        private const val PREFS_NAME = "coovery_home_carousel"
        private const val KEY_PAYLOAD = "payload"
        private const val KEY_FETCHED_AT = "fetched_at"
        private const val CACHE_TTL_MS = 15 * 60 * 1000L
    }
}
