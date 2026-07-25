/*
 * WiwyMusic — Fase A: preferencias del usuario (onboarding de artistas) en Supabase.
 */

package com.wiwymusic.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UserPrefs {

    private const val BASE_URL = "https://yfthptxqqazqyngfjpvp.supabase.co"
    private const val ANON_KEY = "sb_publishable_Y7BpaQliciyuNM3gEJjinw_2v7Xh4Po"

    data class ArtistPick(val id: String, val name: String, val thumbnailUrl: String?)

    // null = desconocido/aún no consultado
    private val _onboarded = MutableStateFlow<Boolean?>(null)
    val onboarded: StateFlow<Boolean?> = _onboarded.asStateFlow()

    fun reset() {
        _onboarded.value = null
    }

    /** Consulta si el usuario ya hizo el onboarding. */
    suspend fun refresh() = withContext(Dispatchers.IO) {
        val session = SupabaseAuth.session.value ?: return@withContext
        runCatching {
            val conn = open("$BASE_URL/rest/v1/profiles?select=onboarded", "GET", session)
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()
            if (code in 200..299) {
                val arr = JSONArray(text)
                val value = if (arr.length() > 0) arr.getJSONObject(0).optBoolean("onboarded", false) else false
                _onboarded.value = value
            } else {
                _onboarded.value = false
            }
        }.onFailure { _onboarded.value = false }
        Unit
    }

    /** Guarda los artistas elegidos y marca el onboarding como completado. */
    suspend fun completeOnboarding(artists: List<ArtistPick>): Result<Unit> = withContext(Dispatchers.IO) {
        val session = SupabaseAuth.session.value ?: return@withContext Result.failure(IllegalStateException("Sin sesión"))
        runCatching {
            // 1) Guardar artistas preferidos (upsert)
            val rows = JSONArray()
            artists.forEach { a ->
                rows.put(
                    JSONObject()
                        .put("user_id", session.userId)
                        .put("artist_id", a.id)
                        .put("name", a.name)
                        .put("thumbnail_url", a.thumbnailUrl ?: JSONObject.NULL)
                        .put("source", "onboarding")
                )
            }
            val postConn = open("$BASE_URL/rest/v1/preferred_artists?on_conflict=user_id,artist_id", "POST", session).apply {
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                doOutput = true
            }
            postConn.outputStream.use { it.write(rows.toString().toByteArray(Charsets.UTF_8)) }
            val postCode = postConn.responseCode
            postConn.disconnect()
            if (postCode !in 200..299) throw IllegalStateException("No se pudieron guardar los artistas ($postCode)")

            // 2) Marcar onboarded = true
            val patchConn = open("$BASE_URL/rest/v1/profiles?id=eq.${session.userId}", "PATCH", session).apply {
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }
            patchConn.outputStream.use { it.write(JSONObject().put("onboarded", true).toString().toByteArray(Charsets.UTF_8)) }
            patchConn.responseCode
            patchConn.disconnect()

            _onboarded.value = true
        }
    }

    private fun open(url: String, method: String, session: SupabaseAuth.Session): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("apikey", ANON_KEY)
            setRequestProperty("Authorization", "Bearer ${session.accessToken}")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 20_000
            readTimeout = 20_000
        }
}
