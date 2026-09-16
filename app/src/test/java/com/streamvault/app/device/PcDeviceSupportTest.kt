package com.streamvault.app.device

import com.google.common.truth.Truth.assertThat
import com.streamvault.app.BuildConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PcDeviceSupportTest {

    @Test
    fun standardBuildDoesNotIdentifyAsPc() {
        val context = RuntimeEnvironment.getApplication()
        assertThat(BuildConfig.IS_PC_DISTRIBUTION).isFalse()
        assertThat(context.isPcDevice()).isFalse()
    }
}
