package com.rsvpn.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

/**
 * خدمة النفق. الهيكل جاهز لإنشاء واجهة TUN افتراضية وتشغيلها في
 * المقدمة (foreground)، لكن محرك التشفير الفعلي (AmneziaWG/WireGuard)
 * غير مربوط بعد — راجع BUILD_STEPS.md، القسم "ربط محرك Amnezia الأصلي".
 */
class RsVpnTunnelService : VpnService() {

    private var tunInterface: ParcelFileDescriptor? = null

    companion object {
        const val ACTION_CONNECT = "com.rsvpn.app.action.CONNECT"
        const val ACTION_DISCONNECT = "com.rsvpn.app.action.DISCONNECT"
        private const val CHANNEL_ID = "rs_vpn_tunnel_channel"
        private const val NOTIF_ID = 42
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                stopTunnel()
                return START_NOT_STICKY
            }
            else -> startTunnel()
        }
        return START_STICKY
    }

    private fun startTunnel() {
        startForeground(NOTIF_ID, buildNotification())
        VpnEngine.setConnecting()

        // TODO: 1) أنشئ الواجهة الافتراضية بإعداداتك الخاصة:
        //   tunInterface = Builder()
        //       .addAddress("10.8.0.2", 32)
        //       .addRoute("0.0.0.0", 0)
        //       .addDnsServer("YOUR_RESOLVER_IP")
        //       .setSession(getString(R.string.app_name))
        //       .establish()
        //
        // TODO: 2) مرّر tunInterface?.fd إلى محرك Amnezia الأصلي عبر JNI
        //   NativeEngine.startTunnel(tunInterface!!.fd, tunnelConfigJson)

        VpnEngine.setConnected(true)
    }

    private fun stopTunnel() {
        // TODO: أوقف محرك Amnezia الأصلي عبر JNI قبل إغلاق الواجهة
        tunInterface?.close()
        tunInterface = null
        VpnEngine.setConnected(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        tunInterface?.close()
        super.onDestroy()
    }

    override fun onRevoke() {
        // النظام ألغى صلاحية VPN (مثلاً تطبيق VPN آخر أخذ السيطرة)
        stopTunnel()
        super.onRevoke()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "RS VPN", NotificationManager.IMPORTANCE_LOW,
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, RsVpnTunnelService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RS VPN")
            .setContentText("الاتصال نشط")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .addAction(0, "قطع الاتصال", stopPendingIntent)
            .build()
    }
}
