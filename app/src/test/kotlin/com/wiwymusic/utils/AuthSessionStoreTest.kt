package com.wiwymusic.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionStoreTest {
    @Test
    fun `migrates a legacy session only after an in-place update`() {
        assertTrue(shouldMigrateLegacySession(firstInstallTime = 1_000L, lastUpdateTime = 2_000L))
    }

    @Test
    fun `does not migrate a restored session on a fresh install`() {
        assertFalse(shouldMigrateLegacySession(firstInstallTime = 2_000L, lastUpdateTime = 2_000L))
    }

    @Test
    fun `fails closed for inconsistent package timestamps`() {
        assertFalse(shouldMigrateLegacySession(firstInstallTime = 2_000L, lastUpdateTime = 1_000L))
    }
}
