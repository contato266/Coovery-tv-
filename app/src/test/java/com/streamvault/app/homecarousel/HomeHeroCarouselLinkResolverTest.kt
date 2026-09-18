package com.streamvault.app.homecarousel

import com.google.common.truth.Truth.assertThat
import com.streamvault.app.navigation.Routes
import org.junit.Test

class HomeHeroCarouselLinkResolverTest {

    @Test
    fun `maps internal shortcuts to app routes`() {
        assertThat(resolveHomeHeroCarouselLink("series"))
            .isEqualTo(HomeHeroCarouselLinkTarget.AppRoute(Routes.SERIES))
        assertThat(resolveHomeHeroCarouselLink("live_tv"))
            .isEqualTo(HomeHeroCarouselLinkTarget.AppRoute(Routes.LIVE_TV))
    }

    @Test
    fun `maps category shortcuts`() {
        assertThat(resolveHomeHeroCarouselLink("series?categoryId=42"))
            .isEqualTo(HomeHeroCarouselLinkTarget.AppRoute("series?categoryId=42"))
        assertThat(resolveHomeHeroCarouselLink("live_tv/99"))
            .isEqualTo(HomeHeroCarouselLinkTarget.AppRoute(Routes.liveTv(99L)))
    }

    @Test
    fun `maps https links to external urls`() {
        assertThat(resolveHomeHeroCarouselLink("https://coovery.com.br/planos"))
            .isEqualTo(HomeHeroCarouselLinkTarget.ExternalUrl("https://coovery.com.br/planos"))
    }

    @Test
    fun `blank links are ignored`() {
        assertThat(resolveHomeHeroCarouselLink("   ")).isEqualTo(HomeHeroCarouselLinkTarget.None)
    }
}
