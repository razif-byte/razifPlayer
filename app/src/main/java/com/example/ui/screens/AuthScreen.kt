package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.ui.components.Watermark3D
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0E17)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App branding header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF9D4EDD), Color(0xFF00E5FF))
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF0D1117)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Razif Player Logo",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Razif Player",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "Lightweight Audio & Video Player",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF00E5FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Watermark preview
                Watermark3D(text = "RazifApps®")
            }

            // Feature highlights
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF131B2A)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureRow(
                        icon = Icons.Default.Videocam,
                        title = "Universal Video & Audio Playback",
                        subtitle = "High performance MP4, MKV, WebM, MP3, FLAC"
                    )
                    FeatureRow(
                        icon = Icons.Default.Speed,
                        title = "Playback Speed Controls",
                        subtitle = "Smooth variable speed from 0.25x up to 3.0x"
                    )
                    FeatureRow(
                        icon = Icons.Default.Sync,
                        title = "Cloud Playlist Sync",
                        subtitle = "Manage & synchronize your media playlists securely"
                    )
                }
            }

            // Authentication actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        signInWithGoogle(
                            context = context,
                            credentialManager = credentialManager,
                            scope = coroutineScope,
                            onSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            },
                            onCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("sign_in_google_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF001524)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF001524),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sign in to synchronize playlists and history with Firebase",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF6B7280)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x2600E5FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF9CA3AF)
                )
            )
        }
    }
}

private fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onSuccess()
            } else {
                onError("Unexpected credential type received")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled: ${e.message}", e)
            onCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Sign in failed")
        }
    }
}
