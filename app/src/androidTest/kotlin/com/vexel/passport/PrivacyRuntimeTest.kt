package com.vexel.passport

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyRuntimeTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun runtime_package_and_sharing_policy_remain_private_and_offline() {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS or PackageManager.GET_PROVIDERS,
        )
        val permissions = packageInfo.requestedPermissions.orEmpty().toSet()
        // WorkManager contributes ACCESS_NETWORK_STATE from its library manifest even though every
        // Vexel work request is network-independent. INTERNET is the permission that would make an
        // outbound connection possible, and the repository boundary check separately rejects HTTP
        // clients, analytics SDKs, and direct network calls.
        assertFalse(permissions.contains("android.permission.INTERNET"))

        val provider = packageInfo.providers.orEmpty().first {
            it.authority == "${context.packageName}.files"
        }
        assertFalse("FileProvider must not be exported", provider.exported)
        assertTrue("FileProvider must support scoped URI grants", provider.grantUriPermissions)

        val contentUri = Uri.parse("content://${context.packageName}.files/shared_documents/synthetic")
        val shareIntent = readOnlyShareIntent(context, contentUri, "application/pdf")
        assertEquals(Intent.ACTION_SEND, shareIntent.action)
        assertTrue(shareIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(0, shareIntent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        assertEquals(0, shareIntent.flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        assertEquals(contentUri, shareIntent.clipData?.getItemAt(0)?.uri)
        assertTrue(
            "FileProvider grants must never be persisted",
            context.contentResolver.persistedUriPermissions.none { it.uri.authority == provider.authority },
        )

        val shareCache = File(context.cacheDir, "shared").apply { mkdirs() }
        val stale = File(shareCache, "${UUID.randomUUID()}_stale")
        val fresh = File(shareCache, "${UUID.randomUUID()}_fresh")
        try {
            stale.writeText("synthetic")
            fresh.writeText("synthetic")
            val now = System.currentTimeMillis()
            stale.setLastModified(now - SHARE_CACHE_MAX_AGE_MILLIS - 1_000)
            fresh.setLastModified(now)

            assertEquals(1, clearStaleShareCache(shareCache, now))
            assertFalse(stale.exists())
            assertTrue(fresh.exists())
        } finally {
            stale.delete()
            fresh.delete()
        }
    }

    @Test
    fun full_data_deletion_also_purges_temporary_share_cache_immediately() = kotlinx.coroutines.runBlocking {
        // Full deletion must not depend on the next app-launch age-based sweep
        // (clearStaleShareCache) to remove temporary document copies -- it has to be immediate.
        val fileStore = com.vexel.passport.core.files.LocalSecureFileStore(context)
        val documentBytes = "synthetic document ${UUID.randomUUID()}".toByteArray(Charsets.UTF_8)
        val preserved = fileStore.preserveOriginal(java.io.ByteArrayInputStream(documentBytes), "application/pdf", "synthetic.pdf")
        val sharedCopy = fileStore.copyToShareCache(context, preserved.id, "synthetic.pdf")
        try {
            assertTrue("share-cache copy must exist right after sharing", sharedCopy.exists())
            fileStore.deleteAll()
            assertFalse("share-cache copy must not survive a full data deletion", sharedCopy.exists())
        } finally {
            sharedCopy.delete()
            fileStore.delete(preserved.id)
        }
    }
}
