/*
 * WiwyMusic — Backend propio (Supabase) — Autenticación por email/contraseña.
 * Enfoque ligero por REST (usa HttpURLConnection, sin SDK adicional).
 * Nada relacionado con Google ni YouTube.
 */

package com.wiwymusic.utils

import androidx.datastore.preferences.core.edit
import com.wiwymusic.App
import com.wiwymusic.constants.SupabaseAccessTokenKey
import com.wiwymusic.constants.SupabaseRefreshTokenKey
import com.wiwymusic.constants.SupabaseUserEmailKey
import com.wiwymusic.constants.SupabaseUserIdKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupabaseAuth {

    private const val BASE_URL = "https://yfthptxqqazqyngfjpvp.supabase.co"
    private const val ANON_KEY = "sb_publishable_Y7BpaQliciyuNM3gEJjinw_2v7Xh4Po"

    data class Session(
        val accessToken: String,
        val refreshToken: String,
        val userId: String,
        val email: String,
    )

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()
    val isLoggedIn: Boolean get() = _session.value != null

    // true cuando ya se intentó cargar la sesión guardada (evita parpadeo del login)
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    /** Carga la sesión guardada al iniciar la app. */
    suspend fun loadSession() {
        val ds = App.instance.dataStore
        val at = ds.getAsync(SupabaseAccessTokenKey)
        val rt = ds.getAsync(SupabaseRefreshTokenKey)
        val uid = ds.getAsync(SupabaseUserIdKey)
        val email = ds.getAsync(SupabaseUserEmailKey)
        if (!at.isNullOrBlank() && !rt.isNullOrBlank() && !uid.isNullOrBlank()) {
            _session.value = Session(at, rt, uid, email ?: "")
        }
        _loaded.value = true
    }

    /** Inicia sesión. */
    suspend fun signIn(email: String, password: String): Result<Unit> =
        post(
            "/auth/v1/token?grant_type=password",
            JSONObject().put("email", email.trim()).put("password", password),
        ).mapCatching { persistSession(it) }

    /**
     * Crea la cuenta. Devuelve `true` si además quedó con sesión iniciada
     * (cuando la confirmación por correo está desactivada), o `false` si hay
     * que confirmar el correo antes de entrar.
     */
    suspend fun signUp(email: String, password: String): Result<Boolean> =
        post(
            "/auth/v1/signup",
            JSONObject().put("email", email.trim()).put("password", password),
        ).mapCatching { json ->
            if (json.has("access_token")) {
                persistSession(json)
                true
            } else {
                false
            }
        }

    /** Envía el correo de recuperación de contraseña. */
    suspend fun recover(email: String): Result<Unit> =
        post("/auth/v1/recover", JSONObject().put("email", email.trim())).map { }

    /** Cierra la sesión localmente. */
    suspend fun signOut() {
        _session.value = null
        App.instance.dataStore.edit { prefs ->
            prefs.remove(SupabaseAccessTokenKey)
            prefs.remove(SupabaseRefreshTokenKey)
            prefs.remove(SupabaseUserIdKey)
            prefs.remove(SupabaseUserEmailKey)
        }
    }

    // ── Interno ────────────────────────────────────────────────────────────────

    private suspend fun post(path: String, body: JSONObject): Result<JSONObject> =
        withContext(Dispatchers.IO) {
            runCatching {
                val conn = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $ANON_KEY")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = 20_000
                    readTimeout = 20_000
                }
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                conn.disconnect()

                if (code !in 200..299) {
                    throw Exception(parseError(text, code))
                }
                JSONObject(text)
            }
        }

    private suspend fun persistSession(json: JSONObject) {
        val accessToken = json.getString("access_token")
        val refreshToken = json.getString("refresh_token")
        val user = json.getJSONObject("user")
        val userId = user.getString("id")
        val email = user.optString("email")

        App.instance.dataStore.edit { prefs ->
            prefs[SupabaseAccessTokenKey] = accessToken
            prefs[SupabaseRefreshTokenKey] = refreshToken
            prefs[SupabaseUserIdKey] = userId
            prefs[SupabaseUserEmailKey] = email
        }
        _session.value = Session(accessToken, refreshToken, userId, email)
    }

    private fun parseError(text: String, code: Int): String {
        val raw = runCatching {
            val o = JSONObject(text)
            o.optString("msg")
                .ifBlank { o.optString("error_description") }
                .ifBlank { o.optString("error") }
                .ifBlank { o.optString("message") }
        }.getOrNull().orEmpty()

        return when {
            raw.contains("Invalid login", true) -> "Correo o contraseña incorrectos"
            raw.contains("Email not confirmed", true) -> "Confirma tu correo antes de entrar"
            raw.contains("already registered", true) || raw.contains("already been registered", true) ->
                "Ese correo ya tiene una cuenta"
            raw.contains("Password should be", true) -> "La contraseña es muy corta (mínimo 6)"
            raw.contains("valid email", true) || raw.contains("invalid format", true) ->
                "Correo no válido"
            raw.isNotBlank() -> raw
            else -> "Error de conexión ($code)"
        }
    }
}
