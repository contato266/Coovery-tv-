package com.streamvault.app.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.domain.model.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class AppUpdateDownloadState(
    val status: AppUpdateDownloadStatus = AppUpdateDownloadStatus.Idle,
    val versionName: String? = null,
    val downloadId: Long? = null,
    val installPermissionRequired: Boolean = false
)

enum class AppUpdateDownloadStatus {
    Idle,
    Downloading,
    Downloaded,
    Failed
}

@Singleton
class AppUpdateInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {
    private val _downloadState = MutableStateFlow(AppUpdateDownloadState())
    val downloadState: StateFlow<AppUpdateDownloadState> = _downloadState.asStateFlow()

    suspend fun refreshState(): AppUpdateDownloadState = withContext(Dispatchers.IO) {
        clearLegacyDownloadArtifacts()
        AppUpdateDownloadState().also { _downloadState.value = it }
    }

    suspend fun openReleasePage(releaseUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        val normalized = releaseUrl.trim()
        if (!isHttpsUrl(normalized)) {
            return@withContext Result.error("Release page URL is unavailable")
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            Result.success(Unit)
        } catch (error: ActivityNotFoundException) {
            Result.error("No browser is available to open the release page", error)
        } catch (error: SecurityException) {
            Result.error("The release page could not be opened", error)
        }
    }

    suspend fun startDownload(releaseInfo: GitHubReleaseInfo): Result<Unit> =
        openReleasePage(releaseInfo.releaseUrl)

    suspend fun installDownloadedUpdate(@Suppress("UNUSED_PARAMETER") expectedSha256: String? = null): Result<Unit> {
        val releaseUrl = preferencesRepository.cachedAppUpdateReleaseUrl.first()
        return if (releaseUrl.isNullOrBlank()) {
            Result.error("Release page URL is unavailable")
        } else {
            openReleasePage(releaseUrl)
        }
    }

    fun unregister() = Unit

    private suspend fun clearLegacyDownloadArtifacts() {
        preferencesRepository.setAppUpdateDownloadId(null)
        preferencesRepository.setAppUpdateDownloadVersionName(null)
        preferencesRepository.setDownloadedAppUpdateVersionName(null)
    }

    private fun isHttpsUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return runCatching {
            val parsed = URI(url)
            parsed.scheme.equals("https", ignoreCase = true) && !parsed.host.isNullOrBlank()
        }.getOrDefault(false)
    }
}
