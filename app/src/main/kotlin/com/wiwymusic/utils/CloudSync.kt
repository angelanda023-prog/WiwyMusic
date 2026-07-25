/*
 * WiwyMusic — Fase 2: sincronización de datos del usuario con el backend propio (Supabase).
 * FAVORITOS en TIEMPO REAL: observa los "Me gusta" locales y sube/borra cada cambio al instante.
 */

package com.wiwymusic.utils

import com.wiwymusic.constants.SongSortType
import com.wiwymusic.db.MusicDatabase
import com.wiwymusic.db.entities.Song
import com.wiwymusic.db.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime

object CloudSync {

    private const val BASE_URL = "https://yfthptxqqazqyngfjpvp.supabase.co"
    private const val ANON_KEY = "sb_publishable_Y7BpaQliciyuNM3gEJjinw_2v7Xh4Po"

    /**
     * Observa los favoritos locales mientras haya sesión y sincroniza cada cambio
     * con la nube en tiempo real. Suspende hasta que se cancele (logout / cierre).
     */
    suspend fun observeFavorites(database: MusicDatabase) {
        val session = SupabaseAuth.session.value ?: return

        // Estado en memoria de lo que hay en la nube (para calcular diffs)
        val cloudIds = withContext(Dispatchers.IO) {
            runCatching { fetchFavorites(session).map { it.songId }.toMutableSet() }
                .getOrDefault(mutableSetOf())
        }

        // Fusión inicial: bajar de la nube lo que no esté marcado localmente
        withContext(Dispatchers.IO) {
            runCatching {
                val localFirst = database.likedSongs(SongSortType.CREATE_DATE, true, false).first()
                val localIds = localFirst.map { it.id }.toSet()
                fetchFavorites(session).forEach { fav ->
                    if (fav.songId in localIds) return@forEach
                    val existing = database.song(fav.songId).first()
                    if (existing == null) {
                        database.insert(
                            SongEntity(
                                id = fav.songId,
                                title = fav.title.ifBlank { "—" },
                                thumbnailUrl = fav.thumbnailUrl,
                                liked = true,
                                likedDate = LocalDateTime.now(),
                                inLibrary = LocalDateTime.now(),
                            )
                        )
                    } else if (!existing.song.liked) {
                        database.update(
                            existing.song.copy(
                                liked = true,
                                likedDate = LocalDateTime.now(),
                                inLibrary = existing.song.inLibrary ?: LocalDateTime.now(),
                            )
                        )
                    }
                }
            }
        }

        // Observación continua: cada cambio local se refleja en la nube
        database.likedSongs(SongSortType.CREATE_DATE, true, false).collect { local ->
            val localIds = local.map { it.id }.toSet()
            val toAdd = local.filter { it.id !in cloudIds }
            val toRemove = cloudIds - localIds
            if (toAdd.isEmpty() && toRemove.isEmpty()) return@collect

            withContext(Dispatchers.IO) {
                if (toAdd.isNotEmpty()) {
                    runCatching { pushFavorites(session, toAdd) }
                        .onSuccess { cloudIds += toAdd.map { it.id } }
                }
                if (toRemove.isNotEmpty()) {
                    runCatching { deleteFavorites(session, toRemove) }
                        .onSuccess { cloudIds -= toRemove }
                }
            }
        }
    }

    // ── REST ─────────────────────────────────────────────────────────────────────

    private data class CloudFav(val songId: String, val title: String, val thumbnailUrl: String?)

    private fun fetchFavorites(session: SupabaseAuth.Session): List<CloudFav> {
        val conn = open("$BASE_URL/rest/v1/favorites?select=song_id,title,thumbnail_url", "GET", session)
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        conn.disconnect()
        if (code !in 200..299) return emptyList()
        val arr = JSONArray(text)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CloudFav(
                songId = o.optString("song_id"),
                title = o.optString("title"),
                thumbnailUrl = o.optString("thumbnail_url").takeIf { it.isNotBlank() && it != "null" },
            )
        }
    }

    private fun pushFavorites(session: SupabaseAuth.Session, songs: List<Song>) {
        val rows = JSONArray()
        songs.forEach { song ->
            rows.put(
                JSONObject()
                    .put("user_id", session.userId)
                    .put("song_id", song.id)
                    .put("title", song.song.title)
                    .put("artists", song.artists.joinToString { it.name })
                    .put("thumbnail_url", song.song.thumbnailUrl ?: JSONObject.NULL)
            )
        }
        val conn = open("$BASE_URL/rest/v1/favorites?on_conflict=user_id,song_id", "POST", session).apply {
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "resolution=merge-duplicates")
            doOutput = true
        }
        conn.outputStream.use { it.write(rows.toString().toByteArray(Charsets.UTF_8)) }
        conn.responseCode
        conn.disconnect()
    }

    private fun deleteFavorites(session: SupabaseAuth.Session, songIds: Set<String>) {
        val list = songIds.joinToString(",") { "\"" + URLEncoder.encode(it, "UTF-8") + "\"" }
        val conn = open(
            "$BASE_URL/rest/v1/favorites?user_id=eq.${session.userId}&song_id=in.($list)",
            "DELETE",
            session,
        )
        conn.responseCode
        conn.disconnect()
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
