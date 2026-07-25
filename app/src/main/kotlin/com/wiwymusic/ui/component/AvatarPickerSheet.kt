/*
 * WiwyMusic — Selector de foto de perfil: cámara, galería o avatar prediseñado.
 */

package com.wiwymusic.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiwymusic.R
import com.wiwymusic.utils.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Paleta de avatares prediseñados (círculos de color). */
val WiwyAvatarPresets = listOf(
    Color(0xFFF5791F), Color(0xFF5C8DF2), Color(0xFF3FBF5F),
    Color(0xFFF25D8E), Color(0xFF9B59F6), Color(0xFF16C0C0),
)

private val WiwyOrange = Color(0xFFF5791F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    fun upload(bytes: ByteArray?) {
        if (bytes == null) return
        busy = true
        scope.launch {
            UserPrefs.uploadAvatar(bytes)
            busy = false
            onDismiss()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            busy = true
            scope.launch {
                val bytes = withContext(Dispatchers.IO) { decodeAndCompress(context, uri) }
                if (bytes != null) UserPrefs.uploadAvatar(bytes)
                busy = false
                onDismiss()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        upload(bitmap?.let { compress(it) })
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text("Foto de perfil", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))

            if (busy) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = WiwyOrange)
                }
            } else {
                OptionRow(R.drawable.photo_camera, "Tomar una foto") { cameraLauncher.launch(null) }
                OptionRow(R.drawable.image, "Elegir de la galería") { galleryLauncher.launch("image/*") }

                Spacer(Modifier.height(16.dp))
                Text("O elige un avatar", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    WiwyAvatarPresets.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    scope.launch {
                                        UserPrefs.setAvatarPreset(index)
                                        onDismiss()
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(R.drawable.person), null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(icon: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(WiwyOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), null, tint = WiwyOrange, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun compress(bitmap: Bitmap): ByteArray {
    val scaled = scaleDown(bitmap, 512)
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
    return out.toByteArray()
}

private fun decodeAndCompress(context: Context, uri: Uri): ByteArray? {
    return runCatching {
        val bmp = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
            ?: return null
        compress(bmp)
    }.getOrNull()
}

private fun scaleDown(bitmap: Bitmap, maxSize: Int): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= maxSize && h <= maxSize) return bitmap
    val ratio = minOf(maxSize.toFloat() / w, maxSize.toFloat() / h)
    return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
}
