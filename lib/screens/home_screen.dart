import 'package:flutter/material.dart';
import '../theme/app_theme.dart';
import '../widgets/info_tile.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  bool _connecting = false;
  bool _connected = false;

  Future<void> _toggleConnection() async {
    setState(() => _connecting = true);
    // TODO: استبدل التأخير بالاستدعاء الحقيقي:
    // await VpnEngine.instance.connect(tunnelConfigJson: myConfig);
    await Future.delayed(const Duration(milliseconds: 900));
    setState(() {
      _connecting = false;
      _connected = !_connected;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: const Icon(Icons.menu, color: AppColors.textPrimary),
        title: const Text('RS VPN'),
        actions: const [
          Padding(
            padding: EdgeInsets.only(right: 16),
            child: Icon(Icons.refresh, color: AppColors.textPrimary),
          ),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 8),
              _StatusCard(connected: _connected),
              const SizedBox(height: 24),
              Text(
                _connected ? 'Connected' : 'Disconnected',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: _connected ? AppColors.success : AppColors.disconnected,
                  fontSize: 26,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'WiFi  ·  192.168.22.8',
                textAlign: TextAlign.center,
                style: TextStyle(color: AppColors.primary, fontSize: 16),
              ),
              const SizedBox(height: 28),
              _ConnectButton(
                connecting: _connecting,
                connected: _connected,
                onTap: _toggleConnection,
              ),
              const SizedBox(height: 24),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('VPN time  ·  00:00:00',
                      style: TextStyle(color: AppColors.primary, fontSize: 16)),
                  OutlinedButton(
                    onPressed: () {},
                    style: OutlinedButton.styleFrom(
                      side: const BorderSide(color: AppColors.primary),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(24)),
                    ),
                    child: const Text('Add Time',
                        style: TextStyle(color: AppColors.primary)),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              const InfoTile(
                label: 'TUNNEL',
                title: '🇩🇪 Tunnel 1 · FastDNS',
                subtitle: 'FastDNS',
              ),
              const SizedBox(height: 14),
              const InfoTile(
                label: 'RESOLVER',
                title: 'Auto DNS',
                subtitle: 'Your own resolver IPs here',
              ),
              const SizedBox(height: 14),
              const InfoTile(
                label: 'COMPRESSION',
                title: 'Light',
                subtitle: 'Less heat',
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}

class _StatusCard extends StatelessWidget {
  final bool connected;
  const _StatusCard({required this.connected});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 170,
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border(
          bottom: BorderSide(
            color: connected ? AppColors.success : AppColors.primary,
            width: 3,
          ),
        ),
      ),
    );
  }
}

class _ConnectButton extends StatelessWidget {
  final bool connecting;
  final bool connected;
  final VoidCallback onTap;

  const _ConnectButton({
    required this.connecting,
    required this.connected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: connecting ? null : onTap,
      child: Container(
        height: 64,
        decoration: BoxDecoration(
          color: AppColors.primary,
          borderRadius: BorderRadius.circular(20),
          boxShadow: [
            BoxShadow(
              color: AppColors.primary.withOpacity(0.5),
              blurRadius: 24,
              spreadRadius: 1,
            ),
          ],
        ),
        child: Center(
          child: connecting
              ? const CircularProgressIndicator(color: Colors.black)
              : Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.power_settings_new, color: Colors.black),
                    const SizedBox(width: 10),
                    Text(
                      connected ? 'Disconnect' : 'Connect',
                      style: const TextStyle(
                        color: Colors.black,
                        fontWeight: FontWeight.w800,
                        fontSize: 20,
                      ),
                    ),
                  ],
                ),
        ),
      ),
    );
  }
}
