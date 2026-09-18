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
        getSlides(
            apiUrl = BuildConfig.COOVERY_HOME_CAROUSEL_API_URL,
            payloadKey = KEY_TV_PAYLOAD,
            fetchedAtKey = KEY_TV_FETCHED_AT,
            forceRefresh = forceRefresh,
            fallbackDefaults = ::defaultTelevisionHomeHeroSlides,
            fallbackBannerResForIndex = ::defaultTelevisionBannerRes
        )

    suspend fun getMobileSlides(forceRefresh: Boolean = false): List<HomeHeroCarouselSlide> =
        getSlides(
            apiUrl = BuildConfig.COOVERY_HOME_CAROUSEL_MOBILE_API_URL,
            payloadKey = KEY_MOBILE_PAYLOAD,
            fetchedAtKey = KEY_MOBILE_FETCHED_AT,
            forceRefresh = forceRefresh,
            fallbackDefaults = ::defaultHandheldHomeHeroSlides,
            fallbackBannerResForIndex = ::defaultHandheldBannerRes
        )

    private suspend fun getSlides(
        apiUrl: String,
        payloadKey: String,
        fetchedAtKey: String,
        forceRefresh: Boolean,
        fallbackDefaults: () -> List<HomeHeroCarouselSlide>,
        fallbackBannerResForIndex: (Int) -> Int
    ): List<HomeHeroCarouselSlide> = withContext(Dispatchers.IO) {
        val cached = readCachedSlides(payloadKey, fallbackBannerResForIndex)
        val cacheFresh = cached != null && !forceRefresh && !isCacheExpired(fetchedAtKey)
        if (cacheFresh) {
            return@withContext cached
        }

        val remote = runCatching { fetchRemoteSlides(apiUrl, fallbackBannerResForIndex) }.getOrNull()
        if (remote != null && remote.isNotEmpty()) {
            writeCache(payloadKey, fetchedAtKey, remote)
            remote
        } else {
            cached ?: fallbackDefaults()
        }
    }

    private fun isCacheExpired(fetchedAtKey: String): Boolean {
        val fetchedAt = preferences.getLong(fetchedAtKey, 0L)
        if (fetchedAt <= 0L) return true
        return System.currentTimeMillis() - fetchedAt > CACHE_TTL_MS
    }

    private fun readCachedSlides(
        payloadKey: String,
        fallbackBannerResForIndex: (Int) -> Int
    ): List<HomeHeroCarouselSlide>? {
        val raw = preferences.getString(payloadKey, null) ?: return null
        return runCatching {
            parseSlidesJson(raw, fallbackBannerResForIndex)
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun writeCache(
        payloadKey: String,
        fetchedAtKey: String,
        slides: List<HomeHeroCarouselSlide>
    ) {
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
            .putString(payloadKey, array.toString())
            .putLong(fetchedAtKey, System.currentTimeMillis())
            .apply()
    }

    @Throws(IOException::class)
    private fun fetchRemoteSlides(
        apiUrl: String,
        fallbackBannerResForIndex: (Int) -> Int
    ): List<HomeHeroCarouselSlide> {
        val request = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/json")
            .header("User-Agent", "Coovery-Home-Carousel/${BuildConfig.VERSION_NAME}")
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
            return CooveryHomeCarouselParser.parseRemotePayload(body, fallbackBannerResForIndex)
        }
    }

    private fun parseSlidesJson(
        raw: String,
        fallbackBannerResForIndex: (Int) -> Int
    ): List<HomeHeroCarouselSlide> {
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
                fallbackBannerRes = slide.optInt(
                    "fallbackBannerRes",
                    fallbackBannerResForIndex(index)
                ),
                linkTarget = linkTarget
            )
        }
        return parsed
    }

    private companion object {
        private const val PREFS_NAME = "coovery_home_carousel"
        private const val KEY_TV_PAYLOAD = "tv_payload"
        private const val KEY_TV_FETCHED_AT = "tv_fetched_at"
        private const val KEY_MOBILE_PAYLOAD = "mobile_payload"
        private const val KEY_MOBILE_FETCHED_AT = "mobile_fetched_at"
        private const val CACHE_TTL_MS = 15 * 60 * 1000L
    }
}
