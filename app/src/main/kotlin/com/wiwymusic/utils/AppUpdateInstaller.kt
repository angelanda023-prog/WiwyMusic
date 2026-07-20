/*
 * WiwyMusic — Actualización OTA in-app con progreso.
 * Descarga el APK dentro de la app mostrando el % y lanza el instalador
 * de Android (nunca abre el navegador).
 */

package com.wiwymusic.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

object AppUpdateInstaller {

    sealed interface State {
        data object Idle : State
        /** [progress] 0f..1f, o -1f si el tamaño es desconocido (indeterminado). */
        data class Downloading(val progress: Float) : State
        data object Installing : State
        data class Failed(val message: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    /** Cierra el diálogo (solo si no hay una descarga en curso). */
    fun dismiss() {
        if (_state.value !is State.Downloading) _state.value = State.Idle
    }

    fun cancel() {
        job?.cancel()
        _state.value = State.Idle
    }

    /** Descarga el APK [url] mostrando progreso y lanza el instalador. */
    fun downloadAndInstall(context: Context, url: String) {
        if (_state.value is State.Downloading) return
        val appContext = context.applicationContext
        _state.value = State.Downloading(0f)
        job = scope.launch {
            try {
                val apk = downloadApk(appContext, url)
                _state.value = State.Installing
                launchInstaller(appContext, apk)
                delay(1200)
                _state.value = State.Idle
            } catch (e: CancellationException) {
                _state.value = State.Idle
                throw e
            } catch (e: Exception) {
                _state.value = State.Failed(e.message ?: "No se pudo descargar la actualización")
            }
        }
    }

    private suspend fun downloadApk(context: Context, url: String): File {
        val dir = File(context.cacheDir, "update").apply { mkdirs() }
        val outFile = File(dir, "WiwyMusic.apk")
        if (outFile.exists()) outFile.delete()

        var current = url
        var connection: HttpURLConnection
        var redirects = 0
        while (true) {
            connection = (URL(current).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 30_000
                readTimeout = 30_000
                setRequestProperty("User-Agent", "WiwyMusic")
                connect()
            }
            val code = connection.responseCode
            if (code in intArrayOf(301, 302, 303, 307, 308) && redirects < 5) {
                val loc = connection.getHeaderField("Location")
                connection.disconnect()
                if (loc.isNullOrBlank()) throw IllegalStateException("Redirección inválida")
                current = loc
                redirects++
                continue
            }
            if (code !in 200..299) {
                connection.disconnect()
                throw IllegalStateException("Servidor respondió $code")
            }
            break
        }

        val total = connection.contentLengthLong
        connection.inputStream.use { input ->
            outFile.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                var downloaded = 0L
                while (true) {
                    coroutineContext.ensureActive()
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    val progress = if (total > 0) (downloaded.toFloat() / total).coerceIn(0f, 1f) else -1f
                    _state.value = State.Downloading(progress)
                }
                output.flush()
            }
        }
        connection.disconnect()

        if (outFile.length() <= 0L) throw IllegalStateException("Descarga vacía")
        return outFile
    }

    private fun launchInstaller(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
