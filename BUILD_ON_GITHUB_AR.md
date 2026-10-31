# بناء APK من الهاتف باستخدام GitHub Actions

1. أنشئ مستودعاً جديداً في https://github.com/new واجعله Public أو Private.
2. ارفع **محتويات** مجلد `AlmunifProductStudio` بعد فك ضغط ZIP، وليس ملف ZIP نفسه. يجب أن يكون `app/` و`build.gradle` و`.github/` في جذر المستودع.
3. انتظر حتى يبدأ GitHub Actions تلقائياً، أو افتح تبويب Actions وشغّل `Build Almunif APK`.
4. عند نجاح البناء، افتح تشغيل workflow الناجح ثم انزل إلى Artifacts.
5. نزّل `Almunif-Product-Studio-APK.zip`، وفك ضغطه على الهاتف، ثم ثبّت `app-debug.apk`.

ملاحظة: هذه طريقة لبناء APK تجريبي (debug). التطبيق لا يراقب الكاميرا بالخلفية ولا يرد على الزبائن. الصور التي تضيفها من التطبيق تُنسخ إلى Pictures/Almunif/القسم/المنتج. ظهور الألبومات في واتساب يعتمد على إصدار أندرويد وواتساب؛ يجب اختباره على الهاتف.
