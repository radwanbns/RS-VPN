package com.rsvpn.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

/**
 * هيكل خدمة النفق. هذا الملف **لا يحتوي محرك اتصال حقيقي بعد** —
 * الجزء الذي يبني الواجهة الافتراضية (TUN) ويمرر الحزم إلى محرك
 * AmneziaWG/OpenVPN يأتي من مكتبات Amnezia الأصلية الرسمية.
 *
 * خطوات الربط موضحة في BUILD_STEPS.md، القسم: "ربط محرك Amnezia الأصلي".
 * باختصار: تستدعي من هنا دوال JNI التي يوفرها libam-go.so / libovpn3.so
 * بعد أن تصرّفها بنفسك من الكود المصدري الرسمي لمشروع Amnezia.
 */
class RsVpnTunnelService : VpnService() {

    private var tunInterface: ParcelFileDescriptor? = null
    private val CHANNEL_ID = "rs_vpn_tunnel_channel"
    private val NOTIF_ID = 42

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())

        val configJson = intent?.getStringExtra("config")

        // TODO: 1) أنشئ الواجهة الافتراضية:
        //   val builder = Builder()
        //       .addAddress("10.8.0.2", 32)
        //       .addRoute("0.0.0.0", 0)
        //       .addDnsServer(/* عنوان الـ Resolver الخاص بك */)
        //   tunInterface = builder.establish()
        //
        // TODO: 2) مرر tunInterface.fd إلى محرك Amnezia الأصلي عبر JNI
        //   NativeEngine.startTunnel(tunInterface!!.fd, configJson)

        return START_STICKY
    }

    override fun onDestroy() {
        // TODO: أوقف محرك Amnezia الأصلي عبر JNI قبل إغلاق الواجهة
        tunInterface?.close()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "RS VPN", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RS VPN")
            .setContentText("الاتصال نشط")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()
    }
}
