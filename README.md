# Mohamed Abdelazim – Azan App 🕌

تطبيق إسلامي متكامل مبني بـ **Kotlin + Jetpack Compose**، مرفوع ومبني مباشرة من **GitHub Actions**.

---

## الميزات

| الميزة | الوصف |
|--------|-------|
| 🕌 الأذان | تنبيه بوقت الصلاة – شاشة كاملة + أذان صوتي فوق شاشة القفل |
| 📿 الأذكار | تشغيل صوتي بالتسلسل مع إيقاف تلقائي أثناء المكالمات |
| 📿 السبحة | عداد تسبيح تفاعلي مع اختيار الذكر وحفظ العدد |
| 🏆 تحديات | تحديات إسلامية 30 يوم (فجر – صدقة – حفظ سورة) |
| 📖 القرآن | استماع وقراءة عبر qari.app بالـ WebView |
| 🌦️ الطقس | درجة الحرارة والوصف من Open-Meteo مجاناً |
| 📅 التاريخ | التاريخ الهجري والميلادي تلقائياً |
| 🤲 إهداء | إلى الوالد الكريم محمد عبد العظيم الطويل رحمه الله |

---

## هيكل المشروع

```
app/src/main/java/com/alaa/
├── App.kt
├── MainActivity.kt
├── data/
│   ├── model/         Models.kt (DhikrItem, PrayerData, WeatherData...)
│   ├── prefs/         PrefsManager.kt
│   └── repository/    PrayerRepository.kt, WeatherRepository.kt
├── di/                AppModule.kt (Koin)
├── navigation/        Screen.kt, AppNavHost.kt
├── presentation/
│   ├── azan/          AzanFullScreenActivity.kt
│   ├── challenges/    ChallengesScreen.kt
│   ├── dhikr/         DhikrScreen.kt, DhikrViewModel.kt
│   ├── home/          HomeScreen.kt, HomeViewModel.kt
│   ├── mesbaha/       MesbahaScreen.kt
│   ├── prayer/        PrayerTimesScreen.kt
│   └── quran/         QuranScreen.kt
├── service/           DhikrService.kt, PrayerAlarmService.kt, BootReceiver.kt
├── ui/theme/          Theme.kt
└── utils/             Constants.kt, PrayerScheduler.kt
```

---

## إعداد GitHub Secrets للتوقيع

في Settings → Secrets and variables → Actions أضف:

| Secret | الوصف |
|--------|-------|
| `KEYSTORE_BASE64` | `base64 -w0 your_key.jks` |
| `STORE_PASSWORD`  | كلمة سر الـ keystore |
| `KEY_ALIAS`       | اسم الـ key alias |
| `KEY_PASSWORD`    | كلمة سر الـ key |

---

## إضافة ملفات الأذكار الصوتية

1. أضف ملفات `.mp3` في `app/src/main/res/raw/`
2. عدّل `DhikrData.list` في `Models.kt`:
```kotlin
DhikrItem(0, "سبحان الله", R.raw.subhanallah),
DhikrItem(1, "الحمد لله",  R.raw.alhamdulillah),
// ...
```

---

## إضافة صوت الأذان

في `PrayerAlarmService.kt` استبدل:
```kotlin
val uri = android.provider.Settings.System.DEFAULT_RINGTONE_URI
```
بـ:
```kotlin
setDataSource(applicationContext, "android.resource://${packageName}/${R.raw.azan_makkah}")
```
بعد إضافة الملف في `res/raw/`.

---

## بناء يدوياً

```bash
./gradlew assembleRelease \
  -PSTORE_PASSWORD=xxxx \
  -PKEY_ALIAS=xxxx \
  -PKEY_PASSWORD=xxxx
```

---

## إصدار جديد

```bash
git tag v1.0.0
git push origin v1.0.0
```

سيقوم GitHub Actions تلقائياً ببناء الـ APK ورفعه كـ Release.

---

## الباكج

`com.alaa`

---

> 🤲 اللهم اغفر لمحمد عبد العظيم الطويل وارحمه وعافه واعف عنه
