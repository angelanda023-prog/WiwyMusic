package com.wiwymusic.ui.screens.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wiwymusic.R
import com.wiwymusic.utils.SupabaseAuth
import com.wiwymusic.utils.UserPrefs
import kotlinx.coroutines.launch

@Composable
fun RedeemCodeCard(
    modifier: Modifier = Modifier,
    onLoginRequested: () -> Unit,
) {
    val session by SupabaseAuth.session.collectAsState()
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<SupabaseAuth.RedeemResult?>(null) }

    WiwySettingsCard(modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF5791F).copy(alpha = 0.14f),
                ) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.code),
                            contentDescription = null,
                            tint = Color(0xFFF5791F),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.redeem_code_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.redeem_code_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedTextField(
                value = code,
                onValueChange = { value ->
                    code = value.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(48)
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
                singleLine = true,
                label = { Text(stringResource(R.string.redeem_code_label)) },
                placeholder = { Text("WIWY-XXXX-XXXX") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                shape = RoundedCornerShape(16.dp),
                isError = error != null,
                supportingText = error?.let { message -> { Text(message) } },
            )

            Button(
                onClick = {
                    if (session == null) {
                        onLoginRequested()
                    } else {
                        scope.launch {
                            loading = true
                            error = null
                            SupabaseAuth.redeemPremiumCode(code)
                                .onSuccess { result ->
                                    code = ""
                                    UserPrefs.refresh()
                                    success = result
                                }
                                .onFailure { error = it.message ?: "No se pudo canjear el código" }
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && (session == null || code.isNotBlank()),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = if (session == null) {
                        stringResource(R.string.redeem_code_login)
                    } else {
                        stringResource(R.string.redeem_code_action)
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    success?.let { result ->
        PremiumCelebrationDialog(
            daysRemaining = result.daysRemaining,
            onDismiss = { success = null },
        )
    }
}

@Composable
private fun PremiumCelebrationDialog(
    daysRemaining: Int,
    onDismiss: () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Box(modifier = Modifier.height(330.dp)) {
                val colors = listOf(
                    Color(0xFFF5791F), Color(0xFFFFC107),
                    Color(0xFF7E57C2), Color(0xFF42A5F5),
                )
                Canvas(Modifier.fillMaxSize()) {
                    repeat(24) { index ->
                        val lane = (index % 8 + 1) / 9f
                        val stagger = (index % 5) * 0.08f
                        val fall = ((progress.value - stagger).coerceIn(0f, 1f))
                        drawCircle(
                            color = colors[index % colors.size].copy(alpha = 1f - fall * 0.35f),
                            radius = if (index % 2 == 0) 7f else 5f,
                            center = Offset(
                                x = size.width * lane,
                                y = -12f + (size.height + 24f) * fall,
                            ),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF5791F).copy(alpha = 0.16f),
                        modifier = Modifier.graphicsLayer {
                            scaleX = 0.65f + progress.value * 0.35f
                            scaleY = 0.65f + progress.value * 0.35f
                            rotationZ = -12f + progress.value * 12f
                        },
                    ) {
                        Box(Modifier.size(82.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.star),
                                contentDescription = null,
                                tint = Color(0xFFF5791F),
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = stringResource(R.string.redeem_code_success_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.redeem_code_success_days, daysRemaining),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.redeem_code_success_action))
                    }
                }
            }
        }
    }
}
