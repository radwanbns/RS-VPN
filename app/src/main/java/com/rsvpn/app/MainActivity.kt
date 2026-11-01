package com.rsvpn.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rsvpn.app.ui.HomeScreen
import com.rsvpn.app.ui.theme.BackgroundDark
import com.rsvpn.app.ui.theme.RsVpnTheme

class MainActivity : ComponentActivity() {

    // نتيجة طلب صلاحية VPN من النظام (نافذة "السماح بإنشاء اتصال VPN؟")
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startTunnelService()
        } else {
            VpnEngine.setConnected(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RsVpnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark,
                ) {
                    HomeScreen(onConnectToggle = ::toggleConnection)
                }
            }
        }
    }

    private fun toggleConnection() {
        val connected = VpnEngine.state.value.isConnected
        if (connected) {
            val intent = Intent(this, RsVpnTunnelService::class.java).apply {
                action = RsVpnTunnelService.ACTION_DISCONNECT
            }
            startService(intent)
        } else {
            val prepareIntent = VpnService.prepare(this)
            if (prepareIntent != null) {
                vpnPermissionLauncher.launch(prepareIntent)
            } else {
                startTunnelService()
            }
        }
    }

    private fun startTunnelService() {
        val intent = Intent(this, RsVpnTunnelService::class.java).apply {
            action = RsVpnTunnelService.ACTION_CONNECT
        }
        startService(intent)
    }
}
