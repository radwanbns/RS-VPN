package com.rsvpn.app

import android.content.Intent
import android.net.VpnService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "rs_vpn/engine"
    private val VPN_REQUEST_CODE = 1001

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "connect" -> {
                        val configJson = call.argument<String>("config")
                        val prepareIntent = VpnService.prepare(this)
                        if (prepareIntent != null) {
                            // المستخدم لم يوافق بعد على إذن VPN للنظام
                            startActivityForResult(prepareIntent, VPN_REQUEST_CODE)
                        } else {
                            startTunnel(configJson)
                        }
                        result.success(null)
                    }
                    "disconnect" -> {
                        stopService(Intent(this, RsVpnTunnelService::class.java))
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
    }

    private fun startTunnel(configJson: String?) {
        val intent = Intent(this, RsVpnTunnelService::class.java)
        intent.putExtra("config", configJson)
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startTunnel(null)
        }
    }
}
