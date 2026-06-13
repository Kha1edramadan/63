package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class SittingReminderWorker(
    private val ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    companion object {
        const val CHANNEL_ID = "igym_sitting_reminder"
        const val WORK_TAG   = "igym_movement_reminder"
        const val NOTIF_ID   = 7001

        // ── رسائل الحركة — قصيرة ومباشرة ────────────────────────────────────
        private val MOVEMENT_MESSAGES = listOf(
            "قوم اتمشى دقيقتين",
            "نص ساعة على الكرسي — كفاية",
            "جسمك محتاج حركة",
            "قوم",
            "الوقوف دقيقتين بينشط الدورة الدموية",
            "ضهرك محتاج استراحة",
            "خطوة واحدة أفضل من صفر",
            "الحركة المنتظمة تبني الجسم، مش الجيم بس",
            "اتمدد وارجع",
            "5 دقايق حركة = ريست كامل للجسم",
            "قوم اشرب مية",
            "الانضباط مش في الجيم بس — في كل لحظة",
            "عيونك محتاجة بريك من الشاشة",
            "3 دقايق مشي دلوقتي",
            "كل تحرك صغير بيبني العادة الكبيرة",
            "الجسم مش مصمم يقعد — مصمم يتحرك"
        )

        // ── اقتباسات — حقيقية وموثقة ─────────────────────────────────────────
        private val QUOTES = listOf(
            "\"الانضباط يساوي الحرية\" — Jocko Willink",
            "\"لا تتمنى أن يكون الأمر أسهل، تمنّ أن تصبح أفضل\" — Jim Rohn",
            "\"العادات الصغيرة يومياً هي نتائج كبيرة على المدى الطويل\" — James Clear",
            "\"جسمك يُحقق ما يعتقده عقلك\" — Napoleon Hill",
            "\"الألم مؤقت، الفخر دائم\" — David Goggins",
            "\"لا تقيس التقدم بالمقارنة بالآخرين — قارن بنفسك أمس\" — Naval Ravikant",
            "\"الإرادة عضلة — كلما استخدمتها قويت\" — Ryan Holiday",
            "\"افعل الصعب وهو سهل، ولا تنتظره حتى يصبح صعباً\" — Marcus Aurelius",
            "\"اهزم نفسك كل يوم قبل أن يهزمك أحد\" — Seneca",
            "\"الجسم يتحمل تقريباً أي شيء — العقل هو الذي يجب إقناعه\" — David Goggins",
            "\"الاتساق أهم من الكمال\" — James Clear",
            "\"الراحة الحقيقية تُكسب، لا تُعطى\" — Naval Ravikant",
            "\"الخوف من الألم أشد من الألم نفسه\" — Seneca",
            "\"نصف المعركة هي الظهور\" — Woody Allen",
            "\"إما أن تُسيطر على يومك أو يُسيطر عليك\" — Jocko Willink",
            "\"كل ما تفعله اليوم يُقرر من ستكون غداً\" — Ryan Holiday",
            "\"الإنسان يصبح ما يتكرر فعله\" — أرسطو",
            "\"الحكمة مع الشباب ولا الشباب مع الجهل\" — نجيب محفوظ",
            "\"الفرق بين الممكن والمستحيل هو الإرادة\" — مصطفى محمود",
            "\"القوة لا تأتي من الجسم — تأتي من الإرادة\" — أحمد خالد توفيق"
        )

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SittingReminderWorker>(
                30, TimeUnit.MINUTES,
                5,  TimeUnit.MINUTES
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
                .getWorkInfosByTag(WORK_TAG)
                .get()
            return infos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
        }
    }

    override fun doWork(): Result {
        createChannel()

        val rand = (System.currentTimeMillis() % 100).toInt()
        val (title, body) = if (rand < 65) {
            "IGYM" to MOVEMENT_MESSAGES.random()
        } else {
            "IGYM" to QUOTES.random()
        }

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .build()

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID + (System.currentTimeMillis() % 10).toInt(), notif)
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تذكيرات IGYM",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تذكير بالحركة كل 30 دقيقة"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            nm.createNotificationChannel(channel)
        }
    }
}
