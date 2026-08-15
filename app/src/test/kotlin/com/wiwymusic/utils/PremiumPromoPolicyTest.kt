package com.wiwymusic.utils

import java.net.URI
import java.net.URLDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumPromoPolicyTest {
    @Test
    fun freeAccountSeesPromoOnlyOnANewDay() {
        assertTrue(shouldShowPremiumPromo(false, lastShownEpochDay = 10, todayEpochDay = 11))
        assertFalse(shouldShowPremiumPromo(false, lastShownEpochDay = 11, todayEpochDay = 11))
    }

    @Test
    fun premiumAndUnknownAccountsNeverSeePromo() {
        assertFalse(shouldShowPremiumPromo(true, lastShownEpochDay = 10, todayEpochDay = 11))
        assertFalse(shouldShowPremiumPromo(null, lastShownEpochDay = 10, todayEpochDay = 11))
    }

    @Test
    fun whatsappLinkContainsExpectedNumberAndMessage() {
        val uri = URI(PremiumContact.WHATSAPP_URL)
        assertEquals("wa.me", uri.host)
        assertEquals("/${PremiumContact.PHONE_NUMBER}", uri.path)
        val message = uri.rawQuery.substringAfter("text=")
        assertEquals(PremiumContact.MESSAGE, URLDecoder.decode(message, Charsets.UTF_8.name()))
    }
}
