# RS VPN (Kotlin native — بدون Flutter)

تطبيق أندرويد بلغة Kotlin بحتة (Jetpack Compose) بدل النسخة السابقة
المبنية على Flutter، بنفس واجهة RS VPN ومحرك VPN مبني على
[Amnezia](https://github.com/amnezia-vpn) مفتوح المصدر (GPLv3).

## الفتح والتشغيل

1. افتح مجلد المشروع في **Android Studio** (Koala أو أحدث).
2. انتظر Gradle Sync — Android Studio يولّد تلقائيًا `gradlew` و
   `gradle-wrapper.jar` الناقصين (هذه الملفات ثنائية ولا يمكن تضمينها
   يدويًا في أرشيف نصي، لهذا لم تُرفق).
3. شغّل التطبيق على جهاز أو محاكي (Run ▶).

البناء من سطر الأوامر ممكن أيضًا بعد أول فتح في Android Studio (لأنه
يُنشئ gradlew تلقائيًا):
```bash
./gradlew assembleDebug
```

## البنية

```
app/src/main/java/com/rsvpn/app/
  MainActivity.kt          # نقطة الدخول + طلب صلاحية VPN من النظام
  VpnEngine.kt              # إدارة حالة الاتصال (StateFlow)
  RsVpnTunnelService.kt     # هيكل VpnService (محرك التشفير غير مربوط بعد)
  ui/HomeScreen.kt          # الشاشة الرئيسية (Compose)
  ui/InfoTile.kt            # بطاقات Tunnel/Resolver/Compression
  ui/theme/                 # الألوان والخطوط والثيم
```

## ما الناقص لتشغيل اتصال VPN فعلي؟

محرك التشفير نفسه (AmneziaWG/WireGuard). راجع نفس خطوات
`BUILD_STEPS.md` في نسخة Flutter السابقة — المبدأ نفسه: صرّف مكتبات
Amnezia الأصلية وضعها في `app/src/main/jniLibs/`، ثم نفّذ التعليقات
`TODO` الموجودة داخل `RsVpnTunnelService.kt`.

## الترخيص

مبني على كود GPLv3 (مشروع Amnezia) — أي نسخة توزّعها يجب أن تبقى
مفتوحة المصدر بنفس الرخصة. حمّل النص الرسمي من
https://www.gnu.org/licenses/gpl-3.0.txt قبل النشر.
