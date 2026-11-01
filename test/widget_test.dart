import 'package:flutter_test/flutter_test.dart';
import 'package:rs_vpn/main.dart';

void main() {
  testWidgets('RS VPN app launches and shows home screen', (tester) async {
    await tester.pumpWidget(const RsVpnApp());
    expect(find.text('RS VPN'), findsOneWidget);
    expect(find.text('Connect'), findsOneWidget);
  });
}
