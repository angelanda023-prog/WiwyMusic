package com.wiwymusic.utils

import java.net.URI
import java.net.URLDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumLaunchPromoTest {
    @Test
    fun freeAccountSeesPromoOncePerForegroundGeneration() {
        assertTrue(shouldShowPremiumLaunchPromo(true, false, foregroundGeneration = 2, claimedGeneration = 1))
        assertFalse(shouldShowPremiumLaunchPromo(true, false, foregroundGeneration = 2, claimedGeneration = 2))
    }

    @Test
    fun premiumUnknownAndLoggedOutAccountsDoNotSeePromo() {
        assertFalse(shouldShowPremiumLaunchPromo(true, true, foregroundGeneration = 2, claimedGeneration = 1))
        assertFalse(shouldShowPremiumLaunchPromo(true, null, foregroundGeneration = 2, claimedGeneration = 1))
        assertFalse(shouldShowPremiumLaunchPromo(false, false, foregroundGeneration = 2, claimedGeneration = 1))
    }

    @Test
    fun whatsappLinkContainsCorrectMexicanNumberAndMessage() {
        val uri = URI(PremiumContact.WHATSAPP_URL)
        assertEquals("wa.me", uri.host)
        assertEquals("/528136890880", uri.path)
        assertEquals("/${PremiumContact.PHONE_NUMBER}", uri.path)
        val message = uri.rawQuery.substringAfter("text=")
        assertEquals(PremiumContact.MESSAGE, URLDecoder.decode(message, Charsets.UTF_8.name()))
    }
}
