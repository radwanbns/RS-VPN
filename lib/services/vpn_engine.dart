import 'dart:async';
import 'package:flutter/services.dart';

/// هذه الطبقة هي الجسر بين واجهة Flutter ومحرك VPN الأصلي (Native).
///
/// محرك الاتصال الفعلي (WireGuard/AmneziaWG + OpenVPN) **لا يُبنى هنا**،
/// لأنه كود C++/Go يحتاج تصريف عبر Android NDK + Go toolchain خاص بمشروع
/// Amnezia الرسمي. راجع BUILD_STEPS.md لمعرفة كيف تجلبه وتربطه بهذا المشروع.
///
/// القناة المتوقعة في الطرف الأصلي (Kotlin):
///   MethodChannel("rs_vpn/engine")
/// والطرق التي يجب تنفيذها هناك: connect, disconnect, status, addTunnel
class VpnConnectionState {
  final bool isConnected;
  final bool isConnecting;
  final String? tunnelName;
  final Duration sessionTime;

  const VpnConnectionState({
    this.isConnected = false,
    this.isConnecting = false,
    this.tunnelName,
    this.sessionTime = Duration.zero,
  });

  VpnConnectionState copyWith({
    bool? isConnected,
    bool? isConnecting,
    String? tunnelName,
    Duration? sessionTime,
  }) {
    return VpnConnectionState(
      isConnected: isConnected ?? this.isConnected,
      isConnecting: isConnecting ?? this.isConnecting,
      tunnelName: tunnelName ?? this.tunnelName,
      sessionTime: sessionTime ?? this.sessionTime,
    );
  }
}

class VpnEngine {
  VpnEngine._internal();
  static final VpnEngine instance = VpnEngine._internal();

  static const _channel = MethodChannel('rs_vpn/engine');

  final _stateController = StreamController<VpnConnectionState>.broadcast();
  Stream<VpnConnectionState> get stateStream => _stateController.stream;

  VpnConnectionState _current = const VpnConnectionState();
  VpnConnectionState get current => _current;

  /// يبدأ الاتصال بنفق معيّن (config يأتي من خادمك الخاص، وليس مستخرجًا
  /// من أي تطبيق آخر — راجع SERVER_CONFIG.md)
  Future<void> connect({required String tunnelConfigJson}) async {
    _emit(_current.copyWith(isConnecting: true));
    try {
      await _channel.invokeMethod('connect', {'config': tunnelConfigJson});
      _emit(_current.copyWith(isConnected: true, isConnecting: false));
    } on PlatformException catch (e) {
      _emit(_current.copyWith(isConnecting: false, isConnected: false));
      rethrow;
    }
  }

  Future<void> disconnect() async {
    await _channel.invokeMethod('disconnect');
    _emit(_current.copyWith(isConnected: false, isConnecting: false));
  }

  void _emit(VpnConnectionState s) {
    _current = s;
    _stateController.add(s);
  }

  void dispose() => _stateController.close();
}
