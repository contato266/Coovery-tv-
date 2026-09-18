package com.streamvault.app.homecarousel

import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Test

class CooveryHomeCarouselRepositoryTest {

    @Test
    fun `remote payload keeps only https image urls`() {
        val payload = JSONObject()
            .put(
                "slides",
                org.json.JSONArray()
                    .put(
                        JSONObject()
                            .put("id", "a")
                            .put("imageUrl", "http://insecure.example/banner.jpg")
                            .put("linkUrl", "series")
                    )
                    .put(
                        JSONObject()
                            .put("id", "b")
                            .put("imageUrl", "https://coovery.com.br/wp-content/uploads/banner.jpg")
                            .put("linkUrl", "movies")
                    )
            )
            .toString()

        val slides = CooveryHomeCarouselParser.parseRemotePayload(payload)

        assertThat(slides).hasSize(1)
        assertThat(slides.single().id).isEqualTo("b")
        assertThat(slides.single().linkTarget)
            .isEqualTo(HomeHeroCarouselLinkTarget.AppRoute("movies"))
    }
}
