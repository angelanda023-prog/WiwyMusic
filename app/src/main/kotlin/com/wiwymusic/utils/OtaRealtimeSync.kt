/*
 * WiwyMusic — Aviso instantáneo de nuevas versiones OTA vía Supabase Realtime.
 *
 * Al publicar un release en GitHub, un Action actualiza la fila
 * app_config.latest_version en Supabase. Esta clase se suscribe a esa fila
 * (WebSocket / Phoenix Channels, igual que UserPrefs.startPremiumRealtimeSync)
 * para enterarse al instante, en vez de esperar el chequeo periódico de
 * UpdateNotificationManager (cada 6h). La tabla es de lectura pública
 * (RLS: select para todos), así que no requiere sesión de usuario.
 */
package com.wiwymusic.utils

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

object OtaRealtimeSync {
    private const val BASE_URL = "https://yfthptxqqazqyngfjpvp.supabase.co"
    private const val ANON_KEY = "sb_publishable_Y7BpaQliciyuNM3gEJjinw_2v7Xh4Po"

    private val client by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(15, TimeUnit.SECONDS)
                    readTimeout(30, TimeUnit.SECONDS)
                    writeTimeout(15, TimeUnit.SECONDS)
                    retryOnConnectionFailure(true)
                }
            }
            install(WebSockets)
        }
    }

    private var job: Job? = null

    fun start(context: Context, scope: CoroutineScope) {
        if (job?.isActive == true) return
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                runCatching {
                    val wsUrl = "wss://${BASE_URL.removePrefix("https://")}/realtime/v1/websocket" +
                        "?apikey=$ANON_KEY&vsn=1.0.0"
                    client.webSocket(wsUrl) {
                        send(
                            JSONObject()
                                .put("topic", "realtime:public:app_config")
                                .put("event", "phx_join")
                                .put(
                                    "payload",
                                    JSONObject().put(
                                        "config",
                                        JSONObject().put(
                                            "postgres_changes",
                                            JSONArray().put(
                                                JSONObject()
                                                    .put("event", "UPDATE")
                                                    .put("schema", "public")
                                                    .put("table", "app_config"),
                                            ),
                                        ),
                                    ),
                                )
                                .put("ref", "1")
                                .toString(),
                        )

                        val heartbeatJob = launch {
                            var ref = 2
                            while (isActive) {
                                delay(30_000)
                                runCatching {
                                    send(
                                        JSONObject()
                                            .put("topic", "phoenix")
                                            .put("event", "heartbeat")
                                            .put("payload", JSONObject())
                                            .put("ref", (ref++).toString())
                                            .toString(),
                                    )
                                }
                            }
                        }

                        try {
                            for (frame in incoming) {
                                if (frame !is Frame.Text) continue
                                handleMessage(context, frame.readText())
                            }
                        } finally {
                            heartbeatJob.cancel()
                        }
                    }
                }.onFailure { e ->
                    Timber.w(e, "OtaRealtimeSync: conexión perdida, reintentando")
                }
                if (isActive) delay(5_000)
            }
        }
    }

    private suspend fun handleMessage(context: Context, text: String) {
        runCatching {
            val msg = JSONObject(text)
            if (msg.optString("event") != "postgres_changes") return
            val changes = msg.optJSONObject("payload")?.optJSONArray("data") ?: return
            for (i in 0 until changes.length()) {
                val record = changes.optJSONObject(i)?.optJSONObject("record") ?: continue
                if (record.optString("key") != "latest_version") continue
                val newVersion = record.optString("value").takeIf { it.isNotBlank() } ?: continue
                val notificationsEnabled = context.dataStore.data
                    .map { it[com.wiwymusic.constants.EnableUpdateNotificationKey] ?: false }
                    .first()
                if (notificationsEnabled && !Updater.isSameVersion(newVersion, com.wiwymusic.BuildConfig.VERSION_NAME)) {
                    UpdateNotificationManager.notifyIfNewVersion(context, newVersion)
                }
            }
        }
    }
}
