package com.vexel.passport

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File

private const val SHARE_CACHE_MAX_AGE_MILLIS = 24 * 60 * 60 * 1000L

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
        val cutoff = System.currentTimeMillis() - SHARE_CACHE_MAX_AGE_MILLIS
        shareCacheDir.listFiles()?.forEach { file -> if (file.lastModified() < cutoff) file.delete() }
    }
}

