# المساهمة في RS VPN

## قواعد أساسية

1. **لا تضع أسرارًا أو إعدادات سيرفر حقيقية في أي commit.**
   `SERVER_CONFIG.json` موجود في `.gitignore` لهذا السبب. استخدم
   `SERVER_CONFIG.example.json` كمرجع فقط.
2. **لا تضمّن ملفات `.so` مصرّفة مسبقًا** من مصادر غير موثوقة أو من
   تطبيقات أخرى. صرّفها بنفسك حسب BUILD_STEPS.md من مصدر Amnezia
   الرسمي حتى يقدر أي شخص يتحقق من الكود.
3. أي تعديل على طبقة الاتصال (`vpn_engine.dart`,
   `RsVpnTunnelService.kt`) يحتاج وصف واضح للتغيير في الـ PR.

## تشغيل المشروع محليًا

```bash
flutter pub get
flutter analyze
flutter test
```
