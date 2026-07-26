/*
 * WiwyMusic — Fase A: preferencias del usuario (onboarding de artistas) en Supabase.
 */

package com.wiwymusic.utils

import com.wiwymusic.db.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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

    // Foto de perfil: URL de Supabase Storage o "preset:N" (avatar prediseñado)
    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl.asStateFlow()

    fun reset() {
        _onboarded.value = null
        _avatarUrl.value = null
    }

    /**
     * Consulta si el usuario ya hizo el onboarding.
     *
     * Importante: si la petición falla (red, timeout, error del servidor) NO
     * se fuerza `onboarded = false` — se deja el valor tal cual estaba (que
     * puede ser `null` = "aún no sabemos" o un `true` ya confirmado antes).
     * De lo contrario, un hiccup de red podía hacer reaparecer el onboarding
     * en una cuenta que ya lo había completado.
     */
    suspend fun refresh() = withContext(Dispatchers.IO) {
        val session = SupabaseAuth.session.value ?: return@withContext
        runCatching {
            val conn = open(
                "$BASE_URL/rest/v1/profiles?select=onboarded,avatar_url&id=eq.${session.userId}",
                "GET",
                session,
            )
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()
            if (code in 200..299) {
                val arr = JSONArray(text)
                if (arr.length() > 0) {
                    val o = arr.getJSONObject(0)
                    _onboarded.value = o.optBoolean("onboarded", false)
                    _avatarUrl.value = o.optString("avatar_url").takeIf { it.isNotBlank() && it != "null" }
                } else {
                    // No hay fila de perfil todavía: cuenta legítimamente nueva.
                    _onboarded.value = false
                }
            }
            // Código de error HTTP: no tocamos _onboarded, se reintentará en el próximo refresh().
        }
        Unit
    }

    /** Elige un avatar prediseñado (círculo de color). */
    suspend fun setAvatarPreset(index: Int): Result<Unit> = patchAvatar("preset:$index")

    /** Sube una foto (cámara/galería) a Supabase Storage y la fija como avatar. */
    suspend fun uploadAvatar(bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val session = SupabaseAuth.session.value ?: return@withContext Result.failure(IllegalStateException("Sin sesión"))
        runCatching {
            val path = "${session.userId}.jpg"
            val put = open("$BASE_URL/storage/v1/object/avatars/$path", "POST", session).apply {
                setRequestProperty("Content-Type", "image/jpeg")
                setRequestProperty("x-upsert", "true")
                doOutput = true
            }
            put.outputStream.use { it.write(bytes) }
            val code = put.responseCode
            put.disconnect()
            if (code !in 200..299) throw IllegalStateException("No se pudo subir la foto ($code)")
            val publicUrl = "$BASE_URL/storage/v1/object/public/avatars/$path?t=${System.currentTimeMillis()}"
            patchAvatar(publicUrl).getOrThrow()
        }
    }

    private suspend fun patchAvatar(value: String): Result<Unit> = withContext(Dispatchers.IO) {
        val session = SupabaseAuth.session.value ?: return@withContext Result.failure(IllegalStateException("Sin sesión"))
        runCatching {
            val conn = open("$BASE_URL/rest/v1/profiles?id=eq.${session.userId}", "PATCH", session).apply {
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }
            conn.outputStream.use { it.write(JSONObject().put("avatar_url", value).toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            conn.disconnect()
            if (code !in 200..299) throw IllegalStateException("No se pudo actualizar el avatar ($code)")
            _avatarUrl.value = value
        }
    }

    /** Lee los artistas preferidos del usuario (para el inicio personalizado). */
    suspend fun getPreferredArtists(): List<ArtistPick> = withContext(Dispatchers.IO) {
        val session = SupabaseAuth.session.value ?: return@withContext emptyList()
        runCatching {
            val conn = open(
                "$BASE_URL/rest/v1/preferred_artists?select=artist_id,name,thumbnail_url&order=weight.desc,added_at.desc",
                "GET",
                session,
            )
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()
            if (code !in 200..299) return@runCatching emptyList<ArtistPick>()
            val arr = JSONArray(text)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ArtistPick(
                    id = o.optString("artist_id"),
                    name = o.optString("name"),
                    thumbnailUrl = o.optString("thumbnail_url").takeIf { it.isNotBlank() && it != "null" },
                )
            }
        }.getOrDefault(emptyList())
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
            val patchCode = patchConn.responseCode
            patchConn.disconnect()
            if (patchCode !in 200..299) throw IllegalStateException("No se pudo marcar el onboarding como completado ($patchCode)")

            _onboarded.value = true
        }
    }

    /**
     * Fase C — Aprendizaje: cuenta las reproducciones por artista del historial local
     * y ajusta pesos / añade artistas descubiertos (source='learned') en la nube.
     * Así el inicio se adapta a lo que realmente escuchas.
     */
    suspend fun learnFromHistory(database: MusicDatabase) = withContext(Dispatchers.IO) {
        val session = SupabaseAuth.session.value ?: return@withContext
        runCatching {
            val events = database.events().first()
            if (events.isEmpty()) return@runCatching

            val counts = HashMap<String, Int>()
            val names = HashMap<String, String>()
            val thumbs = HashMap<String, String?>()
            events.forEach { ev ->
                ev.song.artists.forEach { ar ->
                    if (ar.id.isBlank()) return@forEach
                    counts[ar.id] = (counts[ar.id] ?: 0) + 1
                    names[ar.id] = ar.name
                    if (thumbs[ar.id] == null) thumbs[ar.id] = ar.thumbnailUrl
                }
            }
            val top = counts.entries
                .filter { it.value >= 2 }
                .sortedByDescending { it.value }
                .take(20)
            if (top.isEmpty()) return@runCatching

            val existingSources = fetchPreferredSources(session)

            val rows = JSONArray()
            top.forEach { (id, count) ->
                rows.put(
                    JSONObject()
                        .put("user_id", session.userId)
                        .put("artist_id", id)
                        .put("name", names[id] ?: "")
                        .put("thumbnail_url", thumbs[id] ?: JSONObject.NULL)
                        .put("source", existingSources[id] ?: "learned")
                        .put("weight", 1.0 + count)
                )
            }
            val conn = open("$BASE_URL/rest/v1/preferred_artists?on_conflict=user_id,artist_id", "POST", session).apply {
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                doOutput = true
            }
            conn.outputStream.use { it.write(rows.toString().toByteArray(Charsets.UTF_8)) }
            conn.responseCode
            conn.disconnect()
        }
        Unit
    }

    private fun fetchPreferredSources(session: SupabaseAuth.Session): Map<String, String> {
        val conn = open("$BASE_URL/rest/v1/preferred_artists?select=artist_id,source", "GET", session)
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        conn.disconnect()
        if (code !in 200..299) return emptyMap()
        val arr = JSONArray(text)
        return (0 until arr.length()).associate { i ->
            val o = arr.getJSONObject(i)
            o.optString("artist_id") to o.optString("source", "onboarding")
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
