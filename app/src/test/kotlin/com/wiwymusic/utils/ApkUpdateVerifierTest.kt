package com.wiwymusic.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkUpdateVerifierTest {
    @Test
    fun acceptsMatchingSignerSets() {
        val first = byteArrayOf(1, 2, 3)
        val second = byteArrayOf(4, 5, 6)

        assertTrue(
            ApkUpdateVerifier.sameSignerDigests(
                installedCertificates = listOf(first, second),
                candidateCertificates = listOf(second, first),
            ),
        )
    }

    @Test
    fun rejectsDifferentSigner() {
        assertFalse(
            ApkUpdateVerifier.sameSignerDigests(
                installedCertificates = listOf(byteArrayOf(1, 2, 3)),
                candidateCertificates = listOf(byteArrayOf(9, 8, 7)),
            ),
        )
    }

    @Test
    fun rejectsMissingSignerInformation() {
        assertFalse(
            ApkUpdateVerifier.sameSignerDigests(
                installedCertificates = emptyList(),
                candidateCertificates = listOf(byteArrayOf(1)),
            ),
        )
    }

}
