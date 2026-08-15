package com.wiwymusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wiwymusic.R
import com.wiwymusic.utils.PremiumContact

@Composable
fun PremiumFeatureDialog(
    featureName: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.lock),
                contentDescription = null,
            )
        },
        title = { Text(stringResource(R.string.premium_feature_title)) },
        text = {
            Text(stringResource(R.string.premium_feature_message, featureName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    PremiumContact.open(context)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.premium_feature_obtain))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.premium_feature_later))
            }
        },
    )
}

@Composable
fun PremiumLockBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(16.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.lock),
                contentDescription = null,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}
