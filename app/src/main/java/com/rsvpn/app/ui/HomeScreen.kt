package com.rsvpn.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rsvpn.app.VpnEngine
import com.rsvpn.app.ui.theme.BackgroundDark
import com.rsvpn.app.ui.theme.DisconnectedGray
import com.rsvpn.app.ui.theme.PrimaryCyan
import com.rsvpn.app.ui.theme.RsVpnTheme
import com.rsvpn.app.ui.theme.SuccessGreen
import com.rsvpn.app.ui.theme.TextPrimary

private data class TileData(val label: String, val title: String, val subtitle: String)

private val tiles = listOf(
    TileData("TUNNEL", "🇩🇪 Tunnel 1 · FastDNS", "FastDNS"),
    TileData("RESOLVER", "Auto DNS", "ضع عناوين resolver الخاصة بك هنا"),
    TileData("COMPRESSION", "Light", "Less heat"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onConnectToggle: () -> Unit = {},
) {
    val state by VpnEngine.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "RS VPN",
                        color = PrimaryCyan,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Menu, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark),
            )
        },
        containerColor = BackgroundDark,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                StatusCard(connected = state.isConnected)
            }
            item {
                Text(
                    text = if (state.isConnected) "Connected" else "Disconnected",
                    color = if (state.isConnected) SuccessGreen else DisconnectedGray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            item {
                Text(
                    text = "WiFi  ·  192.168.22.8",
                    color = PrimaryCyan,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            item {
                ConnectButton(
                    connecting = state.isConnecting,
                    connected = state.isConnected,
                    onClick = onConnectToggle,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("VPN time  ·  00:00:00", color = PrimaryCyan)
                    OutlinedButton(onClick = {}) {
                        Text("Add Time", color = PrimaryCyan)
                    }
                }
            }
            items(tiles) { tile ->
                InfoTile(label = tile.label, title = tile.title, subtitle = tile.subtitle)
            }
        }
    }
}

@Composable
private fun StatusCard(connected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        // بطاقة زخرفية بسيطة بلون يعكس حالة الاتصال
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = com.rsvpn.app.ui.theme.SurfaceDark,
            ),
        ) {}
    }
}

@Composable
private fun ConnectButton(
    connecting: Boolean,
    connected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !connecting,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryCyan,
            contentColor = Color.Black,
        ),
    ) {
        if (connecting) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
        } else {
            Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = Color.Black)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = if (connected) "Disconnect" else "Connect",
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
private fun HomeScreenPreview() {
    RsVpnTheme {
        HomeScreen()
    }
}
