# RS VPN — خطوات البناء الكاملة

هذا المشروع هيكل Flutter/Kotlin كامل بهوية **RS VPN**، جاهز من ناحية
الواجهة وخدمة النظام، لكنه **لا يحتوي محرك اتصال VPN فعلي بعد**. المحرك
(تشفير WireGuard/AmneziaWG، OpenVPN، tun2socks) كود C++/Go ضخم يحتاج
بيئة تصريف خاصة (Go toolchain + Android NDK + Qt لبعض الأجزاء)، ولم
يُبنَ داخل هذه الجلسة لأنها بلا اتصال إنترنت ولا أدوات تصريف مهيأة لذلك.

اتبع الخطوات التالية على جهازك لإكمال المشروع.

## 0) أكمل سقالة Gradle أولًا (خطوة ضرورية قبل أي بناء)

المشروع بُني يدويًا بالملفات الجوهرية فقط، وينقصه ملفات Gradle
القياسية (`gradlew`, `gradle-wrapper.jar`, `settings.gradle`,
`res/values/styles.xml`...) التي عادة يولّدها أمر `flutter create`.
بدونها يفشل البناء برسالة مضلّلة مثل:

```
Build failed due to use of deleted Android v1 embedding.
```

شغّل هذا الأمر **مرة واحدة** بعد أول `git clone`، من جذر المشروع:

```bash
flutter create . --platforms=android --org=com.rsvpn --project-name=rs_vpn
```

هذا الأمر **آمن**: يضيف فقط الملفات القياسية الناقصة، ولا يلمس
`lib/`, `AndroidManifest.xml`, أو `MainActivity.kt` المخصصة مسبقًا.
نفس الخطوة مضافة تلقائيًا في GitHub Actions (`.github/workflows/build.yml`)
فلا تحتاجها هناك يدويًا.

## 1) جلب الكود المصدري الرسمي لـ Amnezia

المشروع مرخّص GPLv3 (مفتوح المصدر بالكامل)، ويسمح بإعادة التوزيع
بهوية مختلفة شرط الالتزام بشروط الرخصة (أهمها: إتاحة الكود المصدري
لمستخدمي نسختك أيضًا، والإبقاء على إشعار الترخيص).

```bash
git clone https://github.com/amnezia-vpn/amnezia-client.git
git clone https://github.com/amnezia-vpn/amneziawg-android-kotlin.git
```

اقرأ ملفات `LICENSE` و`README.md` في كل مستودع قبل أي تعديل.

## 2) تصريف المكتبات الأصلية (Native libs)

داخل مستودع `amneziawg-android-kotlin` ستجد سكربتات بناء (عادة عبر
Gradle + NDK) تنتج ملفات `.so` لكل معمارية (`arm64-v8a`,
`armeabi-v7a`). بعد التصريف، انسخها إلى:

```
rs_vpn/android/app/src/main/jniLibs/arm64-v8a/
rs_vpn/android/app/src/main/jniLibs/armeabi-v7a/
```

المتطلبات: Android NDK 26+, Go 1.21+, CMake.

## 3) ربط المحرك بخدمة VpnService

في `RsVpnTunnelService.kt` ستجد تعليقات `TODO` في موضعين:
- إنشاء الواجهة الافتراضية (`Builder().establish()`)
- تمرير `tunInterface.fd` إلى دوال JNI التي يوفرها المحرك الذي صرّفته
  في الخطوة 2 (عادة عبر كلاس Kotlin/Java wrapper موجود في مستودع
  amneziawg-android-kotlin نفسه — انسخه إلى مشروعك أو اربطه كموديول).

## 4) إعداد السيرفرات الخاصة بك

لا تضع هنا أي سيرفرات أو مفاتيح مأخوذة من تطبيق آخر. أنشئ ملف
`SERVER_CONFIG.md` (نموذج موجود بجانب هذا الملف) وعبّئه ببيانات
سيرفراتك الخاصة فقط (IP, publicKey, endpoint, DNS resolver).

## 5) الأيقونة والهوية البصرية

ضع أيقونة RS VPN بصيغة PNG بالأحجام القياسية داخل:
```
android/app/src/main/res/mipmap-*/ic_launcher.png
```
(Android Studio يولّدها تلقائيًا من صورة واحدة عبر Image Asset Studio)

## 6) البناء النهائي

```bash
flutter pub get
flutter build apk --release
```

الملف الناتج: `build/app/outputs/flutter-apk/app-release.apk`

## 7) التزام الترخيص (مهم)

بما أن المشروع يعتمد على كود GPLv3:
- أضف صفحة "Open Source Licenses" داخل التطبيق تذكر مشروع Amnezia
  والرخصة.
- إن نشرت RS VPN للعامة، يجب إتاحة الكود المصدري الكامل لنسختك
  (بما فيها تعديلاتك) لمن يطلبها — هذا شرط أساسي في GPLv3، وليس
  اختياريًا.
