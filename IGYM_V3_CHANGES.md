# IGYM V3 — تغييرات هذا الإصدار

## ✅ الملفات المعدّلة

### 1. `app/build.gradle.kts`
- **compileSdk**: إصلاح syntax من `release(36) { minorApiLevel = 1 }` إلى `compileSdk = 36` — كان يسبب فشل في CI
- **applicationId**: تحديث من `com.aistudio.kinetic.zxyabc` إلى `com.igym.fitness`
- **versionCode**: 1 → 3 | **versionName**: 1.0 → 3.0.0
- **حذف**: Firebase AI dependency (كانت commented out وغير مستخدمة)

### 2. `.github/workflows/build.yml`
- تنظيف كامل وإزالة التكرار (حذف `main.yml`)
- إضافة `cache: gradle` في Java setup
- إضافة `--stacktrace` للـ build لتشخيص أي خطأ بسهولة
- تحديث اسم الـ artifact إلى `igym-v3-debug-apk`
- إضافة build summary خطوة

### 3. `app/src/main/java/com/example/ui/screens/SplashScreen.kt`
- **إصلاح Animation**: كان `targetValue = 1f` من البداية (لا animation)، الآن `0f → 1f`
- إضافة tagline "حياة أفضل، جسم أقوى" مع Brush gradient
- إضافة ambient glow pulse animation خلف اللوجو

### 4. `app/src/main/java/com/example/ui/SittingReminderWorker.kt`
- توسيع من 10 رسالة إلى **60+ رسالة** في 7 فئات:
  - تذكير لطيف / علمية بسيطة / تحفيزية / فكاهية / هدف / صحة عامة / ريلاكس
- إضافة **15 اقتباسات صحية موثوقة** مع المصادر
- إضافة **6 نصائح تغذية** كنوع ثالث من الإشعارات
- توزيع ذكي: 60% حركة، 25% اقتباسات، 15% تغذية
- تحسين أسماء الـ Tags و Channel ID للوضوح

### 5. `app/src/main/java/com/example/ui/screens/WorkoutScreen.kt`
- إعادة بناء كاملة بـ RTL layout صحيح
- إضافة `WeekProgressBanner` — عداد التقدم الأسبوعي مع نقاط
- تحسين `PlanCategoryCard` مع animated card background وEmoji للخطط
- إضافة `ChevronLeft` بدل `ChevronRight` لـ RTL correctness
- Background gradient متجانس مع باقي الشاشات

### 6. `app/src/main/java/com/example/ui/screens/NutritionScreen.kt`
- رفع حد البحث من **18 → 30 نتيجة** للبحث الرئيسي
- رفع حد البحث السريع من **8 → 12 نتيجة**

### 7. `settings.gradle.kts`
- `rootProject.name` = "My Application" → "IGYM"
