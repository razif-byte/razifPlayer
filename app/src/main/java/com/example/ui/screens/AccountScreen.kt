package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.player.PlayerController
import com.example.ui.components.ThemeSelectorDialog
import com.example.ui.components.Watermark3D
import com.example.ui.theme.ThemeManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AccountScreen(
    playerController: PlayerController,
    onSignOutComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val user = Firebase.auth.currentUser
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val playerState by playerController.state.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Account & Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Razif Player Configuration",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
            Watermark3D(text = "RazifApps®")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF001524),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user?.displayName ?: "Google User",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = user?.email ?: "Synced with Firebase Auth",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )
                    Text(
                        text = "UID: ${user?.uid?.take(10)}...",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Theme Customizer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "App Theme & Colors",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = currentTheme.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showThemeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("change_theme_button")
                    ) {
                        Text("Customize", color = Color(0xFF001524), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Background Playback Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Background Playback",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Continue audio when app is minimized or screen locked",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                Switch(
                    checked = playerState.isBackgroundPlaybackEnabled,
                    onCheckedChange = {
                        playerController.toggleBackgroundPlayback(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("background_playback_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Watermark Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3D 50% Watermark Overlay",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A 3D extruded watermark with 50% alpha transparency is embedded during audio & video playback.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Watermark3D(text = "RazifApps®")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sign Out Button
        Button(
            onClick = {
                scope.launch {
                    withContext(NonCancellable) {
                        try {
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        } catch (_: Exception) {}
                        Firebase.auth.signOut()
                        onSignOutComplete()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                contentColor = Color(0xFFEF4444)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sign_out_button")
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }
    }

    if (showThemeDialog) {
        ThemeSelectorDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false }
        )
    }
}
