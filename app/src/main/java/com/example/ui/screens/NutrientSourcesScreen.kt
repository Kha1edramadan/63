package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

data class NutrientData(
    val name:         String,
    val nameAr:       String,
    val normalNeed:   String,
    val athleteNeed:  String,
    val highIntensity:String,
    val defSigns:     String,
    val performance:  String,
    val bestSources:  List<Pair<String, String>>,
    val supplement:   String
)

private val nutrients = listOf(
    NutrientData(
        name          = "Magnesium",
        nameAr        = "ماغنيسيوم",
        normalNeed    = "310–420 مجم/يوم للبالغين",
        athleteNeed   = "400–500 مجم/يوم، الرياضي بيحتاج أكتر",
        highIntensity = "أيوه، بيتفقد كميات كبيرة في العرق أثناء التمرين الشديد",
        defSigns      = "تقلصات ليلية، نوم سيء، تعب مستمر، قلق، ضعف التركيز",
        performance   = "نقصه بيأثر مباشرة على قوة العضلات وسرعة التعافي والنوم، والنوم أهم عامل في البناء",
        bestSources   = listOf(
            "اللوز (محمص)" to "270 مجم/100ج||64",
            "بذور اليقطين" to "592 مجم/100ج||141",
            "السبانخ (مطهي)" to "87 مجم/100ج||21",
            "الكاكاو الداكن" to "228 مجم/100ج||54",
            "الفاصوليا السوداء" to "70 مجم/100ج||17",
            "الأفوكادو" to "29 مجم/100ج||7"
        ),
        supplement    = "Glycinate أو Bisglycinate قبل النوم، الأفضل امتصاصاً بدون إسهال"
    ),
    NutrientData(
        name          = "Zinc",
        nameAr        = "زنك",
        normalNeed    = "8–11 مجم/يوم",
        athleteNeed   = "15–25 مجم/يوم، بيتفقد في العرق",
        highIntensity = "أيوه، التمرين الشديد بيزود الفقد بشكل ملحوظ",
        defSigns      = "مناعة ضعيفة، جروح بطيئة الالتئام، فقدان الشهية، ضعف الشم والتذوق",
        performance   = "ضروري لإنتاج التستوستيرون. نقصه = مستويات هرمونية أقل = بناء عضلي أبطأ",
        bestSources   = listOf(
            "المحار (مطهي)" to "78 مجم/100ج||520",
            "كبدة البقر" to "12 مجم/100ج||80",
            "لحم البقر (صافي)" to "7 مجم/100ج||47",
            "بذور اليقطين" to "7.6 مجم/100ج||51",
            "جبنة شيدر" to "3.1 مجم/100ج||21",
            "البيض" to "1.3 مجم/100ج||9"
        ),
        supplement    = "Bisglycinate أو Picolinate مع الأكل، لا تتجاوز 40 مجم/يوم"
    ),
    NutrientData(
        name          = "Potassium",
        nameAr        = "بوتاسيوم",
        normalNeed    = "2600–3400 مجم/يوم",
        athleteNeed   = "3500–5000 مجم/يوم مع تمارين شديدة",
        highIntensity = "أيوه، بيتفقد بكميات كبيرة في العرق مع الصوديوم",
        defSigns      = "تقلصات عضلية، ضعف، خفقان قلب، إمساك، تعب شديد",
        performance   = "حاسم لانقباض العضلات وضخ الدم. نقصه يعمل كرامب أثناء التمرين",
        bestSources   = listOf(
            "الموز" to "358 مجم/100ج||10",
            "البطاطس المشوية" to "535 مجم/100ج||15",
            "السبانخ" to "558 مجم/100ج||16",
            "الأفوكادو" to "485 مجم/100ج||14",
            "اللحمة البقري" to "318 مجم/100ج||9",
            "السلمون" to "490 مجم/100ج||14"
        ),
        supplement    = "مش محتاج مكمل لو أكلك متنوع. الأكل الطبيعي كافي تماماً"
    ),
    NutrientData(
        name          = "Iron",
        nameAr        = "حديد",
        normalNeed    = "8 مجم (رجل) / 18 مجم (مرأة)",
        athleteNeed   = "بنفس القدر لكن الامتصاص أهم من الكمية",
        highIntensity = "لو تمارين تحمل (جري/سباحة) الاحتياج بيزيد، لو رفع أوزان مش تغيير كبير",
        defSigns      = "تعب مزمن، شحوب، ضيق تنفس، دوخة، تركيز ضعيف",
        performance   = "الحديد بينقل الأوكسجين للعضلات، نقصه = طاقة أقل وتعافي أبطأ",
        bestSources   = listOf(
            "كبدة البقر" to "6.5 مجم/100ج||81",
            "بذور السمسم" to "14.6 مجم/100ج||183",
            "بذور اليقطين (محمص)" to "8.8 مجم/100ج||110",
            "فاصوليا بيضاء" to "3.7 مجم/100ج||46",
            "اللحمة البقري" to "2.6 مجم/100ج||33",
            "السبانخ" to "2.7 مجم/100ج||34"
        ),
        supplement    = "بس لو ثبت نقص بتحليل دم. الجرعة الزيادة ضارة. الحديد الهيمي من اللحمة أفضل امتصاصاً"
    ),
    NutrientData(
        name          = "Calcium",
        nameAr        = "كالسيوم",
        normalNeed    = "1000 مجم/يوم",
        athleteNeed   = "1000–1200 مجم/يوم، خصوصاً رياضيات",
        highIntensity = "التمرين الشديد يرفع الكثافة العظمية، الكالسيوم الكافي ضروري لحماية العظام",
        defSigns      = "تشنجات، هشاشة عظام على المدى البعيد، تنميل في الأطراف",
        performance   = "ضروري لانقباض العضلات والأعصاب. نقصه مش واضح فوري لكن على المدى البعيد خطر",
        bestSources   = listOf(
            "جبنة رومي" to "800 مجم/100ج||80",
            "سردين (بالعظم)" to "382 مجم/100ج||38",
            "اللبن الزبادي" to "120 مجم/100ج||12",
            "السبانخ" to "99 مجم/100ج||10",
            "اللوز" to "264 مجم/100ج||26",
            "التوفو" to "350 مجم/100ج||35"
        ),
        supplement    = "Calcium Citrate الأفضل امتصاصاً. لا تاخد مع الحديد، بيتعاركوا في الامتصاص"
    ),
    NutrientData(
        name          = "Vitamin D",
        nameAr        = "فيتامين د",
        normalNeed    = "600–800 IU/يوم",
        athleteNeed   = "2000–5000 IU/يوم، خصوصاً في مناطق قليلة الشمس",
        highIntensity = "أيوه، الرياضيون محتاجين أكتر لأن نقصه بيأثر على القوة والهرمونات",
        defSigns      = "تعب، ضعف عضلي، مزاج سيء، ضعف المناعة، ألام العظام",
        performance   = "مرتبط بمستويات التستوستيرون. نقصه بيخفض الأداء ويضعف المناعة بشكل ملحوظ",
        bestSources   = listOf(
            "الشمس المباشرة (20 دقيقة)" to "1000+ IU",
            "سمك السلمون" to "526 IU/100ج||26",
            "السردين" to "193 IU/100ج||10",
            "صفار البيض" to "37 IU/بيضة||2",
            "الفطر (مُعرَّض للشمس)" to "متغير"
        ),
        supplement    = "D3 مع K2 (100 مكجم) عشان الكالسيوم يروح للعظام مش الشرايين"
    ),
    NutrientData(
        name          = "Omega-3",
        nameAr        = "أوميجا 3",
        normalNeed    = "250–500 مجم EPA+DHA/يوم",
        athleteNeed   = "2000–3000 مجم EPA+DHA/يوم للتعافي والالتهاب",
        highIntensity = "أيوه، التمرين الشديد بيزيد الالتهاب ومحتاج أوميجا 3 للتوازن",
        defSigns      = "التهاب مزمن، جفاف الجلد، تعافي بطيء، مزاج غير مستقر",
        performance   = "يسرع التعافي بشكل ملموس. يقلل ألام العضلات بعد التمرين. مهم لصحة المفاصل طويل المدى",
        bestSources   = listOf(
            "السردين" to "1480 مجم EPA+DHA/100ج||74",
            "السلمون" to "2260 مجم EPA+DHA/100ج||113",
            "الماكريل" to "5134 مجم EPA+DHA/100ج||257",
            "بذور الكتان (ALA)" to "22800 مجم/100ج",
            "الجوز (ALA)" to "9080 مجم/100ج"
        ),
        supplement    = "Fish Oil أو Algae Oil (للنباتيين). ابحث عن 2-3 جرام EPA+DHA، مش الأوميجا الكلي"
    ),
    NutrientData(
        name          = "Sodium",
        nameAr        = "صوديوم",
        normalNeed    = "1500–2300 مجم/يوم",
        athleteNeed   = "2300–4000 مجم/يوم، بيتفقد بشدة في التمارين الطويلة",
        highIntensity = "أيوه جداً، الصوديوم هو أول معدن بيتفقد في العرق",
        defSigns      = "دوخة، غثيان، تشنجات، ضعف، في الحالات الشديدة إغماء",
        performance   = "نقصه أثناء التمرين الطويل يعمل cramping وانخفاض حاد في الأداء",
        bestSources   = listOf(
            "ملح الطعام" to "38750 مجم/100ج",
            "الجبنة" to "متنوع",
            "الزيتون" to "735 مجم/100ج||37",
            "المخللات" to "متنوع"
        ),
        supplement    = "Electrolytes أثناء التمارين >60 دقيقة. ملح البحر في الأكل عادةً كافي"
    )
)

@Composable
fun NutrientSourcesScreen(onBack: () -> Unit = {}) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Micronutrients",
                        style      = MaterialTheme.typography.displayLarge,
                        color      = OnSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "احتياجاتك الفعلية كرياضي",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        nutrients.forEach { nutrient ->
            item { NutrientCard(nutrient) }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun NutrientCard(n: NutrientData) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // ── Header، English name only ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        n.name,
                        style      = MaterialTheme.typography.titleLarge,
                        color      = OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        modifier   = Modifier.weight(1f)
                    )
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint     = OnSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Arabic name + performance always first in expanded
                        Text(
                            n.nameAr,
                            style      = MaterialTheme.typography.titleMedium,
                            color      = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            n.performance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        NutrientRow("شخص عادي", n.normalNeed)
                        Spacer(modifier = Modifier.height(8.dp))
                        NutrientRow("رياضي", n.athleteNeed, Primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        NutrientRow("تمرين شديد؟", n.highIntensity)
                        Spacer(modifier = Modifier.height(8.dp))
                        NutrientRow("أعراض النقص", n.defSigns)

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "أفضل المصادر",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        n.bestSources.forEach { (food, rawAmount) ->
                            val parts = rawAmount.split("||")
                            val displayAmount = parts[0]
                            val pct = parts.getOrNull(1)?.toIntOrNull()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(food, style = MaterialTheme.typography.bodySmall, color = OnSurface, modifier = Modifier.weight(1f))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(displayAmount, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                    if (pct != null) {
                                        val pctColor = when {
                                            pct >= 100 -> Color(0xFF4CAF50)
                                            pct >= 50  -> Primary
                                            else       -> OnSurfaceVariant
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(pctColor.copy(0.12f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "${pct}%",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = pctColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.04f), modifier = Modifier.padding(vertical = 1.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Primary.copy(alpha = 0.06f))
                                .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.Top
                            ) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                                Text("مكمل: ${n.supplement}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientRow(label: String, value: String, labelColor: Color = OnSurfaceVariant.copy(alpha = 0.7f)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Text(
            "$label:",
            style      = MaterialTheme.typography.labelSmall,
            color      = labelColor,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.width(82.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f))
    }
}
