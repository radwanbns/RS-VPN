package com.rsvpn.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * حالة اتصال VPN، تُراقَب من الواجهة عبر StateFlow.
 */
data class VpnConnectionState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val tunnelName: String = "Tunnel 1 · FastDNS",
    val sessionSeconds: Long = 0L,
)

/**
 * نقطة الدخول الوحيدة لإدارة حالة الاتصال من واجهة Compose.
 *
 * هذا الكائن **لا يحتوي محرك تشفير فعلي بعد**. الاتصال الحقيقي
 * (AmneziaWG/WireGuard عبر JNI) يُستدعى من داخل [RsVpnTunnelService]
 * — راجع BUILD_STEPS.md، القسم "ربط محرك Amnezia الأصلي".
 */
object VpnEngine {
    private val _state = MutableStateFlow(VpnConnectionState())
    val state: StateFlow<VpnConnectionState> = _state.asStateFlow()

    fun setConnecting() {
        _state.value = _state.value.copy(isConnecting = true)
    }

    fun setConnected(connected: Boolean) {
        _state.value = _state.value.copy(isConnected = connected, isConnecting = false)
    }

    fun updateSessionSeconds(seconds: Long) {
        _state.value = _state.value.copy(sessionSeconds = seconds)
    }
}
