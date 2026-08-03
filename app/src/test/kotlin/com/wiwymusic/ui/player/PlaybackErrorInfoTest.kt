package com.wiwymusic.ui.player

import androidx.media3.common.PlaybackException
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackErrorInfoTest {
    @Test
    fun wrappedNetworkErrorIsShownAsNoInternet() {
        val networkError =
            TestPlaybackException(
                "Sin conexión a Internet",
                null,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            )
        val wrappedError =
            TestPlaybackException(
                "Source error",
                networkError,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
            )

        assertEquals(PlaybackErrorKind.NoInternet, wrappedError.toPlaybackErrorInfo().kind)
    }

    @Test
    fun wrappedTimeoutIsShownAsTimeout() {
        val timeoutError =
            TestPlaybackException(
                "Timeout",
                null,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            )
        val wrappedError =
            TestPlaybackException(
                "Source error",
                timeoutError,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
            )

        assertEquals(PlaybackErrorKind.Timeout, wrappedError.toPlaybackErrorInfo().kind)
    }

    private class TestPlaybackException(
        message: String,
        cause: Throwable?,
        errorCode: Int,
    ) : PlaybackException(message, cause, errorCode, Bundle.EMPTY, 0L)
}
