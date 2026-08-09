package com.vexel.passport

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File

internal const val SHARE_CACHE_MAX_AGE_MILLIS = 24 * 60 * 60 * 1000L

internal fun clearStaleShareCache(shareCacheDir: File, nowEpochMillis: Long = System.currentTimeMillis()): Int {
    val cutoff = nowEpochMillis - SHARE_CACHE_MAX_AGE_MILLIS
    return shareCacheDir.listFiles().orEmpty().count { file ->
        file.isFile && file.lastModified() < cutoff && file.delete()
    }
}

@HiltAndroidApp
class VexelHealthPassportApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        clearStaleShareCache()
    }

    // Shared documents/exports are copied into cacheDir/shared for FileProvider access
    // (see LocalSecureFileStore.copyToShareCache); they must not accumulate indefinitely
    // since they can contain sensitive health information. Only stale files are removed
    // here, not all of them, so an in-progress share started just before a process restart
    // is not disrupted.
    private fun clearStaleShareCache() {
        val shareCacheDir = File(cacheDir, "shared")
        clearStaleShareCache(shareCacheDir)
    }
}
