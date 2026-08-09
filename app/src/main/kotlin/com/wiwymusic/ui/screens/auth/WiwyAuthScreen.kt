/*
 * WiwyMusic — Pantalla de acceso al backend propio (Supabase).
 * Correo + contraseña. Sin Google ni YouTube.
 */

package com.wiwymusic.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiwymusic.R
import com.wiwymusic.utils.SupabaseAuth
import kotlinx.coroutines.launch

private val WiwyOrange = Color(0xFFF5791F)

private enum class AuthMode { LOGIN, REGISTER, RECOVER }

@Composable
fun WiwyAuthScreen(onAuthenticated: () -> Unit) {
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    val title = when (mode) {
        AuthMode.LOGIN -> "Bienvenido"
        AuthMode.REGISTER -> "Crear cuenta"
        AuthMode.RECOVER -> "Recuperar contraseña"
    }
    val actionText = when (mode) {
        AuthMode.LOGIN -> "Iniciar sesión"
        AuthMode.REGISTER -> "Crear cuenta"
        AuthMode.RECOVER -> "Enviar correo"
    }

    fun submit() {
        error = null; info = null
        if (email.isBlank() || (mode != AuthMode.RECOVER && password.isBlank())) {
            error = "Completa los campos"
            return
        }
        loading = true
        scope.launch {
            when (mode) {
                AuthMode.LOGIN -> SupabaseAuth.signIn(email, password)
                    .onSuccess { loading = false; onAuthenticated() }
                    .onFailure { loading = false; error = it.message }

                AuthMode.REGISTER -> SupabaseAuth.signUp(email, password)
                    .onSuccess { loggedIn ->
                        loading = false
                        if (loggedIn) onAuthenticated()
                        else {
                            mode = AuthMode.LOGIN
                            info = "Cuenta creada. Revisa tu correo para confirmarla y luego inicia sesión."
                        }
                    }
                    .onFailure { loading = false; error = it.message }

                AuthMode.RECOVER -> SupabaseAuth.recover(email)
                    .onSuccess {
                        loading = false
                        mode = AuthMode.LOGIN
                        info = "Te enviamos un correo para restablecer tu contraseña."
                    }
                    .onFailure { loading = false; error = it.message }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Image(
            painter = painterResource(R.drawable.opentune),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "WiwyMusic",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WiwyOrange,
            focusedLabelColor = WiwyOrange,
            cursorColor = WiwyOrange,
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        if (mode != AuthMode.RECOVER) {
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (mode == AuthMode.REGISTER && passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                trailingIcon = if (mode == AuthMode.REGISTER) {
                    {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) R.drawable.visibility_off else R.drawable.visibility,
                                ),
                                contentDescription = if (passwordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                },
                            )
                        }
                    }
                } else {
                    null
                },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
        info?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = WiwyOrange, fontSize = 13.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { submit() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WiwyOrange, contentColor = Color.White),
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text(actionText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        when (mode) {
            AuthMode.LOGIN -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = { mode = AuthMode.REGISTER; error = null; info = null }) {
                    Text("Crear cuenta", color = WiwyOrange, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { mode = AuthMode.RECOVER; error = null; info = null }) {
                    Text("Recuperar contraseña", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            AuthMode.REGISTER, AuthMode.RECOVER -> TextButton(onClick = { mode = AuthMode.LOGIN; error = null; info = null }) {
                Text("Volver a iniciar sesión", color = WiwyOrange, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
