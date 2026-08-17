package com.vexel.passport.core.datastore

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesTest {
    @Test fun lock_timeout_options_are_explicit_and_bounded() {
        assertTrue(isSupportedLockTimeoutMinutes(0))
        assertTrue(isSupportedLockTimeoutMinutes(5))
        assertTrue(isSupportedLockTimeoutMinutes(15))
        assertTrue(isSupportedLockTimeoutMinutes(30))
        assertFalse(isSupportedLockTimeoutMinutes(-1))
        assertFalse(isSupportedLockTimeoutMinutes(60))
    }

    @Test fun default_preferences_leave_lock_timeout_disabled() {
        assertTrue(UserPreferences().lockTimeoutMinutes == 0)
    }

    @Test fun default_preferences_do_not_hide_recent_apps_preview() {
        assertFalse(UserPreferences().hideRecentAppsPreview)
    }
}
