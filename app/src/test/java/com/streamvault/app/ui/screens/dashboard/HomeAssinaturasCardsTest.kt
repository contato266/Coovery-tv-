package com.streamvault.app.ui.screens.dashboard

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeAssinaturasCardsTest {

    @Test
    fun disneyAndEspnArePlacedImmediatelyBeforeSky() {
        val cards = homeAssinaturasCards()
        val keys = cards.map { it.key }
        val skyIndex = keys.indexOf("sky")

        assertThat(skyIndex).isGreaterThan(1)
        assertThat(keys[skyIndex - 2]).isEqualTo("disney_plus")
        assertThat(keys[skyIndex - 1]).isEqualTo("espn")

        val disney = cards.first { it.key == "disney_plus" }
        val espn = cards.first { it.key == "espn" }
        assertThat(disney.destination).isEqualTo(HomeSubscriptionDestination.SERIES)
        assertThat(espn.destination).isEqualTo(HomeSubscriptionDestination.LIVE)
        assertThat(disney.categoryMatchers).contains("disney")
        assertThat(espn.categoryMatchers).contains("espn")
    }
}
