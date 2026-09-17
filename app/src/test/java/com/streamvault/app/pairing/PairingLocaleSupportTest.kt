package com.streamvault.app.pairing

import com.google.common.truth.Truth.assertThat
import java.util.Locale
import org.junit.Test

class PairingLocaleSupportTest {

    @Test
    fun resolvePairingLocale_prefersPortugueseFromAcceptLanguage() {
        val locale = resolvePairingLocale("pt-BR,pt;q=0.9,en;q=0.8", Locale.ENGLISH)
        assertThat(locale.language).isEqualTo("pt")
    }

    @Test
    fun resolvePairingLocale_fallsBackToDeviceLocale() {
        val locale = resolvePairingLocale(null, Locale.forLanguageTag("pt-BR"))
        assertThat(locale.language).isEqualTo("pt")
    }
}
