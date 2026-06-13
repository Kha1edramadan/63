package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

data class SupplementInfo(
    val name:          String,
    val nameAr:        String,
    val tier:          String,
    val tierColor:     Long,
    val summary:       String,
    val worthIt:       String,
    val dose:          String,
    val timing:        String,
    val whenNeeded:    String,
    val whenNotNeeded: String,
    val fromFood:      String,
    val warnings:      String,
    val buyingGuide:   String = "",
    val cycleGuide:    String = ""
)

private val supplements = listOf(
    SupplementInfo(
        name          = "Creatine Monohydrate",
        nameAr        = "كرياتين",
        tier          = "أساسي",
        tierColor     = 0xFFC3F400,
        summary       = "أكثر مكمل رياضي تم دراسته في التاريخ. يزود الخلايا بطاقة إضافية للتكرارات الأخيرة.",
        worthIt       = "أيوه بقوة. 500+ دراسة علمية تؤكد فعاليته. يرفع القوة والحجم بشكل ملموس خلال 4-6 أسابيع.",
        dose          = "3–5 جرام يومياً، مفيش داعي لـ Loading Phase",
        timing        = "أي وقت خلال اليوم، التوقيت مش مهم",
        whenNeeded    = "لو بتتمرن بانتظام وعايز تحسّن الأداء. من أول يوم في الجيم.",
        whenNotNeeded = "لو مش بتتمرن أو بتلعب رياضة تحمل فقط زي الماراثون.",
        fromFood      = "موجود في اللحمة والسمك لكن بكميات صغيرة جداً، يعني لازم تاكل كيلوز عشان توصل للجرعة.",
        warnings      = "لازم تشرب مياه كفاية. آمن تماماً مع الكلى الطبيعية. لو عندك مشكلة كلوي استشر دكتور.",
        buyingGuide   = "اشتري Creatine Monohydrate فقط. HCL وEthyl Ester وBuffered كلها نفس النتيجة بسعر أغلى.\n\nالسوق المصري (متاحة في كارفور وهايبر وان وأونلاين):\nMyprotein Impact Creatine: الأرخص لكل جرام وجودة مضمونة\nON (Optimum Nutrition) Micronized Creatine: الأشهر والأكثر انتشاراً في مصر\nScitec Creatine 100%: متاح في محلات السوبليمنتس المصرية\nBio-Tech USA 100% Creatine: موثوق وسعره معقول في السوق المصري\nDymatize Creatine Micronized: جودة عالية ومحترمة\n\nابحث على شعار Creapure® على العلبة لأعلى درجة نقاء.",
        cycleGuide    = "مش محتاج تقطعه خالص. خذه كل يوم حتى أيام الراحة. بعض الناس بيوقفوه 4-6 أسابيع كل سنة بس مفيش دليل علمي إن ده ضروري."
    ),
    SupplementInfo(
        name          = "Whey Protein",
        nameAr        = "بروتين مصل اللبن",
        tier          = "حسب الاحتياج",
        tierColor     = 0xFFC3F400,
        summary       = "مصدر بروتين سريع الامتصاص. مش سحر، هو بس بروتين في شكل مريح.",
        worthIt       = "لو صعب توصل للـ Protein Target اليومي من الأكل العادي. لو بتاكل بروتين كافي، مش محتاجه.",
        dose          = "حسب ما تحتاجه من بروتين، 1 سيرفينج ≈ 25-30 جرام بروتين",
        timing        = "أي وقت. بعد التمرين مش إلزامي زي ما الناس بتقول.",
        whenNeeded    = "لما الأكل مش كافي أو وقتك ضيق. طلاب وناس مشغولة.",
        whenNotNeeded = "لو بتاكل لحمة وبيض وبقوليات كافية في يومك.",
        fromFood      = "بيض ودجاج ولحمة وجبنة، كلها بروتين ممتاز. الـ Whey بس أسرع وأسهل.",
        warnings      = "مش للناس اللي عندها حساسية من اللاكتوز، جرب Plant-Based بديل.",
        buyingGuide   = "Whey Concentrate كافي لمعظم الناس وأرخص. Whey Isolate لو عندك حساسية للاكتوز أو في مرحلة تنشيف.\n\nالسوق المصري (متاحة في كارفور وهايبر وان وOlympic Nutrition وأونلاين):\nON Gold Standard Whey: الأشهر والأكثر انتشاراً في مصر، طعم ممتاز\nMyprotein Impact Whey: الأرخص لكل جرام بروتين، يُطلب أونلاين\nScitec 100% Whey Protein: متاح كثيراً في محلات الرياضة المصرية\nDymatize Nutrition Elite Whey: جودة عالية بسعر معقول\nBio-Tech USA Hydro Whey Zero: Isolate متاح ومعقول السعر\nDymatize ISO-100: للمتقدمين وعشان الحساسية من الكتوز\n\nاقرأ الـ Nutrition Label: ابحث على 20g+ بروتين و3g سكر أو أقل لكل سيرفينج.",
        cycleGuide    = "مفيش cycle. هو مجرد أكل. لو بتاكل بروتين كافي من مصادر أخرى توقف عنه في أي وقت."
    ),
    SupplementInfo(
        name          = "Omega-3",
        nameAr        = "أوميجا 3، زيت السمك",
        tier          = "أساسي",
        tierColor     = 0xFFC3F400,
        summary       = "يقلل الالتهاب، يسرع التعافي، ومهم لصحة القلب والمفاصل.",
        worthIt       = "أيوه. خصوصاً لو مش بتاكل سمك بانتظام. فرق ملموس في المفاصل والتعافي.",
        dose          = "2–3 جرام EPA+DHA يومياً",
        timing        = "مع وجبة فيها دهون للامتصاص الأحسن",
        whenNeeded    = "لو بتاكل سمك أقل من مرتين في الأسبوع. لو بتتمرن بشدة.",
        whenNotNeeded = "لو بتاكل سمك دهني 3+ مرات أسبوعياً زي السردين والسلمون.",
        fromFood      = "سردين، سلمون، ماكريل، أفضل مصدر طبيعي بكتير.",
        warnings      = "جرعات عالية ممكن تخفف الدم. استشر دكتور لو بتاخد مميعات.",
        buyingGuide   = "الأهم: ابحث على EPA+DHA combined على العلبة مش إجمالي الأوميجا 3. Fish Oil 1000mg ممكن يكون فيه 300mg EPA+DHA بس، يعني هتاخد 6 كبسولات للجرعة.\n\nاختار منتج يدي 500mg+ EPA+DHA في كل كبسولة.\n\nالسوق المصري (صيدليات وكارفور وهايبر وان):\nOmega Forte من صيدليات مصر: متاح وسعره معقول (تأكد من EPA+DHA على العلبة)\nNow Foods Omega-3 (متاح أونلاين وبعض محلات الصحة)\nSolgar Triple Strength Omega-3 (900mg EPA+DHA): الأفضل، متاح في صيدليات العزبي والنهدي\nMyprotein Omega-3: سعر ممتاز ومتاح أونلاين\nNordic Naturals Ultimate Omega: الأجود والأغلى، للاستيراد\n\nتجنب زيوت السمك الرخيصة بدون ذكر EPA+DHA على العلبة.",
        cycleGuide    = "يومياً بلا توقف. خذه مع وجبة فيها دهون للامتصاص الأحسن."
    ),
    SupplementInfo(
        name          = "Vitamin D3 + K2",
        nameAr        = "فيتامين د + ك2",
        tier          = "أساسي",
        tierColor     = 0xFFC3F400,
        summary       = "40% من الناس عندهم نقص من غير ما يعرفوا. بيأثر على الهرمونات والمناعة والعظام.",
        worthIt       = "أيوه جداً. النقص مرتبط بانخفاض التستوستيرون وضعف الأداء الرياضي.",
        dose          = "2000–5000 IU فيتامين D3 + 100 مكجم K2",
        timing        = "مع وجبة دسمة، فيتامين ذائب في الدهون",
        whenNeeded    = "لو مش بتتعرض للشمس كفاية. معظم الناس محتاجينه خصوصاً في الشتاء.",
        whenNotNeeded = "لو حصلت على تحليل دم وثبت مستوياتك طبيعية.",
        fromFood      = "الشمس هي المصدر الأساسي. سمك السلمون والبيض بكميات محدودة.",
        warnings      = "K2 مهم جداً مع D3 عشان يوجّه الكالسيوم للعظام مش الشرايين.",
        buyingGuide   = "D3 لا D2 (D2 أقل فاعلية). مع K2 MK-7 لا MK-4. D3+K2 مع بعض أفضل من كل واحد لوحده.\n\nالسوق المصري (صيدليات عادية):\nVitamin D3 من Pharco أو Amoun: متاح في كل صيدلية، 2000 IU أو 5000 IU\nVitamino D3+K2 من Memphis: متاح ومعقول السعر\nNow Foods D3+K2: متاح في بعض صيدليات الصحة والأونلاين\nMyprotein Vitamin D3: رخيص ويطلب أونلاين\nSolgar Vitamin D3 (5000 IU): متاح في صيدليات العزبي والنهدي\n\nاعمل تحليل 25-OH Vitamin D قبل البداية وبعد 3 شهور.",
        cycleGuide    = "يومياً بلا توقف. عمل تحليل دم كل 6 شهور تطمن."
    ),
    SupplementInfo(
        name          = "Magnesium Glycinate",
        nameAr        = "مغنيسيوم",
        tier          = "مفيد جداً",
        tierColor     = 0xFFFFB347,
        summary       = "بيتفقد في العرق أثناء التمرين. مهم للنوم والتقلصات العضلية وأكثر من 300 وظيفة.",
        worthIt       = "أيوه لو بتتمرن بشدة أو بتعاني من نوم سيء أو تقلصات.",
        dose          = "200–400 مجم Glycinate أو Bisglycinate",
        timing        = "قبل النوم بـ 30-60 دقيقة",
        whenNeeded    = "لو بتتعرق كتير في التمرين. لو نومك مش كويس. لو بتحس بتقلصات.",
        whenNotNeeded = "لو بتاكل مكسرات وخضار ورقي وبقوليات بكميات كافية.",
        fromFood      = "اللوز، السبانخ، البقوليات، الشوكولاتة الداكنة، مصادر ممتازة.",
        warnings      = "أشكال Oxide و Sulfate أقل امتصاصاً وممكن تعمل إسهال. Glycinate الأفضل.",
        buyingGuide   = "ابحث على Magnesium Glycinate أو Bisglycinate. Citrate خيار تاني جيد. Oxide تجنبه خالص امتصاصه ضعيف جداً.\n\nالسوق المصري (صيدليات وأونلاين):\nMagnesium من Pharco (Citrate): متاح في كل صيدلية مصرية بسعر رخيص\nMagnolin من Sigma Pharma: شائع في الصيدليات المصرية\nNow Foods Magnesium Glycinate: متاح أونلاين وأفضل امتصاصاً\nDoctor's Best High Absorption Magnesium: الأشهر عالمياً، متاح أونلاين\nMyprotein Magnesium Bisglycinate: رخيص ومتاح أونلاين\n\nجرعة مناسبة 200-400mg قبل النوم.",
        cycleGuide    = "يومياً بلا توقف. مكمل أمان عالي."
    ),
    SupplementInfo(
        name          = "Caffeine",
        nameAr        = "كافيين",
        tier          = "مفيد",
        tierColor     = 0xFFFFB347,
        summary       = "محسّن الأداء الأكتر دراسة. يرفع القوة والتركيز ويأخر الإحساس بالتعب.",
        worthIt       = "أيوه كـ Pre-Workout. لكن مفيش داعي تشتري مكمل، قهوة عادية بتعمل نفس الشيء.",
        dose          = "3–6 مجم لكل كيلو من وزنك، كوباية قهوة ≈ 80-100 مجم",
        timing        = "30-60 دقيقة قبل التمرين",
        whenNeeded    = "لو محتاج طاقة ذهنية وجسدية إضافية للتمرين.",
        whenNotNeeded = "لو حساس للكافيين أو عندك قلق أو مشاكل نوم.",
        fromFood      = "قهوة، شاي، شيكولاتة داكنة، مصادر طبيعية ممتازة.",
        warnings      = "خذ فترات راحة أسبوعية عشان متتعمدش مقاومة. لا تستخدم بعد 2-3 ظهر.",
        buyingGuide   = "مش محتاج تشتري مكمل. 2 كوباية قهوة سوداء قبل التمرين بتعمل نفس اللي بيتباع بمئات الجنيهات.\n\nلو عايز مكمل بس:\nCaffeine 200mg tablets (أي ماركة)، أرخص وأبسط\nPre-workout اللي فيه Caffeine + Beta-Alanine + Citrulline زي C4 أو Ghost Pre-workout\n\nتجنب Pre-workouts اللي فيها مكونات كتير مجهولة أو Proprietary Blends.",
        cycleGuide    = "وقّف أسبوع كامل كل 6-8 أسابيع تعيد حساسيتك له. لو بتتمرن بكرة متشربش قهوة بعد 2-3 ظهر."
    ),
    SupplementInfo(
        name          = "Zinc",
        nameAr        = "زنك",
        tier          = "مفيد",
        tierColor     = 0xFFFFB347,
        summary       = "مهم لإنتاج التستوستيرون والمناعة والتئام الجروح. بيتفقد في العرق أثناء التمرين.",
        worthIt       = "أيوه لو بتتمرن بشدة وأكلك مش متنوع. لكن لازم تنتبه لموضوع النحاس مع الاستخدام الطويل.",
        dose          = "15–25 مجم يومياً من Zinc Bisglycinate أو Picolinate",
        timing        = "مع الطعام لتجنب الغثيان، بعيد عن الكالسيوم بساعتين",
        whenNeeded    = "رياضيون وناس بتتمرن بشدة. نباتيون (مصادر نباتية أقل امتصاصاً).",
        whenNotNeeded = "لو بتاكل لحمة وبحريات وكبدة بانتظام، ممكن تكون مش محتاجه.",
        fromFood      = "المحار الأعلى مصدر، بعدين لحمة البقر والكبدة وبذور اليقطين.",
        warnings      = "⚠️ تنبيه مهم: الاستخدام الطويل للزنك بدون نحاس يعمل نقص نحاس في الجسم. الحل: إما تاخد Zinc مع Copper (2mg نحاس لكل 25mg زنك) أو تاخد استراحة شهر كل 3 شهور. لا تتجاوز 40 مجم/يوم.",
        buyingGuide   = "Bisglycinate أو Picolinate هما الأفضل امتصاصاً. Oxide تجنبه تماماً.\n\nتحذير: اكتساب الزنك على حساب النحاس ممكن يسبب نقص نحاس، اشتري منتج يحتوي Copper معه.\n\nالسوق المصري (صيدليات وأونلاين):\nZinc من Pharco أو Amoun: الأرخص والأبسط، متاح في كل صيدلية (تأكد من الشكل)\nNow Foods Zinc Bisglycinate 30mg + Copper: الأفضل والأكثر امتصاصاً، أونلاين\nMyprotein Zinc: رخيص ومعقول، أونلاين\nSolgar Zinc Chelated 22mg: متاح في صيدليات العزبي\nOptimum Nutrition ZMA: Zinc + Magnesium + B6 في كبسولة واحدة",
        cycleGuide    = "لو مع Copper: يومياً بلا توقف. لو بدون Copper: شهر تاخده ثم أسبوع وقفة، أو تعمل تحليل ZnRBC كل 6 شهور."
    ),
    SupplementInfo(
        name          = "Beta-Alanine",
        nameAr        = "بيتا ألانين",
        tier          = "اختياري",
        tierColor     = 0xFF888888,
        summary       = "يأخر حرقة العضلات في التكرارات العالية (8-15). مش للقوة، للتحمل العضلي.",
        worthIt       = "لو تدريبك على High-Reps والـ Hypertrophy. ملوش أهمية كبيرة للـ Strength.",
        dose          = "3.2–6.4 جرام يومياً، يتراكم في الأنسجة",
        timing        = "يومياً مش مرتبط بوقت التمرين",
        whenNeeded    = "متقدمون في التمرين يدوروا على Edge بسيطة في الأداء.",
        whenNotNeeded = "مبتدئون. ناس بتعمل Low-Rep Strength Training بس.",
        fromFood      = "لا يوجد مصدر غذائي عملي، لازم مكمل لو عايز الجرعة الفعالة.",
        warnings      = "التنميل (Paresthesia) طبيعي ومش خطير. خفف الجرعة لو مزعجتك.",
        buyingGuide   = "ابحث على CarnoSyn® Beta-Alanine على العلبة. البودرة الخام أرخص بكتير بنفس الفاعلية.\n\nالسوق المصري:\nMyprotein Beta-Alanine: رخيص ومتاح أونلاين\nScitec Beta-Alanine: متاح في محلات الرياضة المصرية\nNow Foods Beta-Alanine: كبسولات أسهل في الجرعة، أونلاين\nC4 Pre-Workout (Cellucor): موجود في كارفور وهايبر وان ويحتوي بيتا-ألانين\nGhost Pre-Workout: الأشهر عند المتمرنين المصريين حالياً\n\nقسّم الجرعة على 2-3 مرات طول اليوم لتقليل الوخز الجلدي.",
        cycleGuide    = "8-12 أسبوع استخدام ثم 4 أسابيع راحة. بعض الدراسات بتقول ما محتاجش توقف لكن الاستراحة بتعيد حساسيتك له."
    )
)

@Composable
fun SupplementsScreen(onBack: () -> Unit = {}) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column {
                    Text(
                        "المكملات",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "دليل عملي بدون مبالغة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        item {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Color(0xFF202020), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, null, tint = Primary, modifier = Modifier.size(14.dp))
                        Text(
                            "الغذاء الأساس دايماً. المكملات بس تكمّل النقص.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }

        supplements.forEach { supp ->
            item { SupplementCard(supp) }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SupplementCard(supp: SupplementInfo) {
    var expanded by remember { mutableStateOf(false) }
    var taken    by remember { mutableStateOf(false) }
    val tierColor = Color(supp.tierColor)

    // Animate dose bar 0 → 100% when user marks as taken
    val doseProgress by animateFloatAsState(
        targetValue   = if (taken) 1f else 0f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label         = "doseProgress"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // ── Always-visible header ──────────────────────────────────────
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            supp.nameAr,
                            style      = MaterialTheme.typography.titleMedium,
                            color      = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                supp.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Tier badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tierColor.copy(0.12f))
                                .border(1.dp, tierColor.copy(0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                supp.tier,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = tierColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // ✓ "Taken today" circular toggle
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    if (taken) tierColor.copy(0.18f) else SurfaceContainerHigh
                                )
                                .border(
                                    1.dp,
                                    if (taken) tierColor.copy(0.55f) else Color.White.copy(0.08f),
                                    CircleShape
                                )
                                .clickable { taken = !taken },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (taken) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (taken) "تم الأخذ" else "سجّل بعد الأخذ",
                                tint     = if (taken) tierColor else OnSurfaceVariant.copy(0.55f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint     = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Dose progress bar — always visible below header ───────────
            Spacer(Modifier.height(13.dp))
            DoseProgressBar(
                dose     = supp.dose,
                progress = doseProgress,
                color    = tierColor,
                taken    = taken
            )

            // ── Expanded details ───────────────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        HorizontalDivider(color = Color.White.copy(0.07f))
                        Spacer(Modifier.height(12.dp))

                        Text(
                            supp.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))

                        SupplementRow("هل يستحق؟",         supp.worthIt,       Primary)
                        Spacer(Modifier.height(10.dp))
                        SupplementRow("الجرعة",            supp.dose)
                        Spacer(Modifier.height(10.dp))
                        SupplementRow("التوقيت",           supp.timing)
                        Spacer(Modifier.height(10.dp))
                        SupplementRow("إمتى تحتاجه",      supp.whenNeeded)
                        Spacer(Modifier.height(10.dp))
                        SupplementRow("إمتى ملوش لازمة",  supp.whenNotNeeded)
                        Spacer(Modifier.height(10.dp))
                        SupplementRow("من الأكل",          supp.fromFood)

                        if (supp.warnings.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB347).copy(0.08f))
                                    .border(1.dp, Color(0xFFFFB347).copy(0.25f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment     = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFFB347), modifier = Modifier.size(14.dp))
                                    Text(supp.warnings, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                }
                            }
                        }

                        if (supp.buyingGuide.isNotBlank()) {
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(0.07f))
                            Spacer(Modifier.height(10.dp))
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, null, tint = Primary, modifier = Modifier.size(13.dp))
                                Text("إيه تشتري؟", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(supp.buyingGuide, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }

                        if (supp.cycleGuide.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Primary.copy(0.06f))
                                    .border(1.dp, Primary.copy(0.18f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Refresh, null, tint = Primary, modifier = Modifier.size(13.dp))
                                    Column {
                                        Text("دورة الاستخدام", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(3.dp))
                                        Text(supp.cycleGuide, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Dose Progress Bar ──────────────────────────────────────────────────────────
@Composable
private fun DoseProgressBar(dose: String, progress: Float, color: Color, taken: Boolean) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = if (taken) "✓ تم الأخذ اليوم" else "اضغط ✓ بعد الأخذ",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (taken) color else OnSurfaceVariant.copy(0.65f)
                )
                Text(
                    text       = "${(progress * 100).toInt()}%",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = if (taken) color else OnSurfaceVariant.copy(0.45f),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(7.dp))

            // Track background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(0.06f))
            ) {
                // Gradient fill
                if (progress > 0.005f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(color.copy(0.50f), color, color.copy(0.88f))
                                )
                            )
                    )
                    // Top shine
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(Color.White.copy(0.22f))
                    )
                }
            }

            Spacer(Modifier.height(5.dp))
            Text(
                text       = dose,
                style      = MaterialTheme.typography.bodySmall,
                color      = color.copy(0.72f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SupplementRow(label: String, value: String, labelColor: Color = Primary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Text(
            "$label:",
            style     = MaterialTheme.typography.labelSmall,
            color     = labelColor,
            fontWeight = FontWeight.Bold,
            modifier  = Modifier.width(80.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f))
    }
}
