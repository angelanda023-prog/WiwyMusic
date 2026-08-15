package com.wiwymusic.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

object PremiumContact {
    const val PHONE_NUMBER = "528136899880"
    const val MESSAGE = "Hola, quiero solicitar el plan Premium de WiwyMusic por $30 MXN al mes."
    const val WHATSAPP_URL =
        "https://wa.me/$PHONE_NUMBER?text=Hola%2C%20quiero%20solicitar%20el%20plan%20Premium%20de%20WiwyMusic%20por%20%2430%20MXN%20al%20mes."

    fun open(context: Context): Boolean = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URL))
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}
