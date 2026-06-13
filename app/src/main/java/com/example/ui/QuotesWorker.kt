package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class QuotesWorker(
    private val ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    companion object {
        const val CHANNEL_ID = "igym_quotes"
        const val WORK_TAG   = "igym_quotes_worker"

        // ── اقتباسات موثقة — مرتبة بالمصدر ──────────────────────────────────

        // الأدب العربي
        private val ARABIC_LIT = listOf(
            "\"الناس يعيشون لأنفسهم، والعظماء يعيشون لغيرهم\" — نجيب محفوظ",
            "\"لا تخف من الخطأ، الخطأ هو الطريق الوحيد للتعلم\" — نجيب محفوظ",
            "\"الحياة ثمينة جداً لنضيعها في ما لا يستحق\" — أحمد خالد توفيق",
            "\"كل ما تخيلته عن الدنيا أكبر منها\" — أحمد خالد توفيق",
            "\"الشك يؤلم، لكنه علامة العقل الحي\" — مصطفى محمود",
            "\"الإنسان الحي هو الذي يشعر بألم الحياة\" — مصطفى محمود",
            "\"الإنسان مجموع عاداته\" — مصطفى محمود",
            "\"اقرأ ثم اقرأ ثم اقرأ\" — طه حسين",
            "\"العمل الجاد لا يخذل صاحبه\" — طه حسين"
        )

        // الفلسفة الرواقية
        private val STOICS = listOf(
            "\"ما لا تستطيع تغييره، تعلّم تقبّله\" — Marcus Aurelius",
            "\"افعل ما تملكه، تقبّل ما لا تملكه\" — Epictetus",
            "\"لا تستعجل المستقبل ولا تتأسف على الماضي — الحاضر كافٍ\" — Seneca",
            "\"الخوف من الألم أشد ضرراً من الألم نفسه\" — Seneca",
            "\"كن متفائلاً للمستقبل، محايداً للحاضر، هادئاً للماضي\" — Seneca",
            "\"ماذا يستفيد الإنسان إذا ربح العالم وخسر نفسه\" — Marcus Aurelius",
            "\"عش كأنك ستموت غداً، تعلّم كأنك ستعيش للأبد\" — Marcus Aurelius",
            "\"التحدي يكشف الشخصية\" — Epictetus",
            "\"الأقوياء يختارون معاناتهم\" — Epictetus"
        )

        // تطوير الذات وريادة الأعمال
        private val SELF_DEV = listOf(
            "\"العادات الصغيرة يومياً = نتائج كبيرة على المدى الطويل\" — James Clear",
            "\"الاتساق أهم من الكمال\" — James Clear",
            "\"لا تحاول أن تكون مثيراً للإعجاب — حاول أن تكون متسقاً\" — James Clear",
            "\"الإرادة عضلة — كلما استخدمتها قويت\" — Ryan Holiday",
            "\"الشهرة لا تصنع العظمة — العمل الصامت يصنعها\" — Ryan Holiday",
            "\"لا تقيس نفسك بالآخرين، قارن نفسك بمن كنت بالأمس\" — Naval Ravikant",
            "\"الوقت هو أغلى ما تملك، وصحتك عاصمتك\" — Naval Ravikant",
            "\"الحرية الحقيقية هي في عدم احتياجك لموافقة أحد\" — Naval Ravikant",
            "\"اصنع ما لا تستطيع الآلة صنعه\" — Naval Ravikant",
            "\"العمل بدون اتجاه واضح مجرد إجهاد\" — Jim Rohn",
            "\"أنت متوسط الخمسة الذين تقضي معهم أغلب وقتك\" — Jim Rohn"
        )

        // الرياضة والصحة
        private val FITNESS = listOf(
            "\"جسمك يتحمل تقريباً أي شيء — عقلك هو الذي تحتاج إقناعه\" — David Goggins",
            "\"الألم مؤقت. الفخر دائم\" — David Goggins",
            "\"من لم يجد وقتاً للصحة سيجد وقتاً للمرض\" — Hippocrates",
            "\"الانضباط يساوي الحرية\" — Jocko Willink",
            "\"إما أن تُسيطر على يومك أو يُسيطر عليك\" — Jocko Willink",
            "\"الفوز يُكسب في التدريب\" — Vince Lombardi",
            "\"البطولة ليست في اللحظة — هي في كل يوم من الإعداد\" — Bobby Knight",
            "\"اهزم نفسك كل يوم — لا أحد آخر يستطيع فعل ذلك\" — David Goggins"
        )

        // علم النفس والفلسفة العامة
        private val PSYCHOLOGY = listOf(
            "\"الإنسان يصبح ما يتكرر فعله\" — أرسطو",
            "\"اعرف نفسك\" — سقراط",
            "\"لا تكن عبداً لعقلك — كن قائده\" — Viktor Frankl",
            "\"بين المثير والاستجابة يوجد فراغ — في هذا الفراغ تكمن حريتنا\" — Viktor Frankl",
            "\"الذي لديه سبب للعيش يتحمل أي ظرف\" — Nietzsche",
            "\"ما لا يقتلك يجعلك أقوى\" — Nietzsche",
            "\"الناجحون يجدون حلولاً، الفاشلون يجدون أعذاراً\" — Brian Tracy",
            "\"كل إنجاز كبير بدأ بقرار صغير\" — Brian Tracy"
        )

        private val ALL_QUOTES = ARABIC_LIT + STOICS + SELF_DEV + FITNESS + PSYCHOLOGY

        // ── الجدولة ──────────────────────────────────────────────────────────
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<QuotesWorker>(
                2, TimeUnit.HOURS,
                20, TimeUnit.MINUTES
            )
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        }

        fun isScheduled(context: Context): Boolean {
            val infos = WorkManager.getInstance(context)
                .getWorkInfosByTag(WORK_TAG).get()
            return infos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
        }
    }

    override fun doWork(): Result {
        createChannel()

        val quote = ALL_QUOTES.random()

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("IGYM")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(9000 + (System.currentTimeMillis() % 50).toInt(), notif)
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "اقتباسات IGYM",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اقتباس كل ساعتين"
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }
    }
}
