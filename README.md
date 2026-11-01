# RS VPN

تطبيق VPN لأندرويد (Flutter + Kotlin) مبني على محرك
[Amnezia](https://github.com/amnezia-vpn) مفتوح المصدر (GPLv3).

> هذا المستودع يحتوي هيكل التطبيق والواجهة وطبقة الربط مع محرك الاتصال.
> المكتبات الأصلية (native `.so`) **غير مضمّنة** ويجب تصريفها بنفسك —
> راجع [BUILD_STEPS.md](BUILD_STEPS.md).

## الحالة الحالية

- [x] واجهة المستخدم (Flutter)
- [x] هيكل خدمة `VpnService` على أندرويد
- [x] قناة الاتصال بين Flutter والكود الأصلي (MethodChannel)
- [ ] ربط محرك AmneziaWG الفعلي (انظر BUILD_STEPS.md)
- [ ] تعبئة سيرفراتك الخاصة في `SERVER_CONFIG.json`

## البنية

```
lib/
  main.dart
  screens/home_screen.dart
  widgets/info_tile.dart
  services/vpn_engine.dart
  theme/app_theme.dart
android/
  app/src/main/kotlin/com/rsvpn/app/
    MainActivity.kt
    RsVpnTunnelService.kt
  app/src/main/AndroidManifest.xml
  app/build.gradle
BUILD_STEPS.md
SERVER_CONFIG.example.json
```

## البدء السريع

```bash
flutter pub get
flutter run
```

(للتشغيل الفعلي كـ VPN، أكمل أولًا خطوات ربط المحرك في BUILD_STEPS.md)

## الترخيص

هذا المشروع يعتمد على كود مرخّص بموجب **GPLv3** (مشروع Amnezia).
أي نسخة موزَّعة من هذا التطبيق يجب أن تبقى مفتوحة المصدر بنفس الرخصة.
راجع [LICENSE](LICENSE).

## تنويه

لا تضع في هذا المستودع أي بيانات سيرفرات أو مفاتيح مستخرجة من تطبيقات
أخرى. استخدم فقط بنيتك التحتية الخاصة.
