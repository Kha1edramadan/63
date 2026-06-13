package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutDao) {

    val allPlans: Flow<List<WorkoutPlan>> = dao.getAllPlans()
    val allExercises: Flow<List<Exercise>> = dao.getAllExercises()
    val recentSessions: Flow<List<WorkoutSession>> = dao.getRecentSessions()

    fun getExercisesForPlan(planId: String): Flow<List<Exercise>> =
        dao.getExercisesForPlan(planId)

    // ─── Set Logs ─────────────────────────────────────────────────────────────

    suspend fun logSet(setLog: SetLog) = dao.insertSetLog(setLog)

    fun getLogsForExercise(exerciseId: String): Flow<List<SetLog>> =
        dao.getLogsForExercise(exerciseId)

    fun getLastSessionLogsForExercise(exerciseId: String): Flow<List<SetLog>> =
        dao.getLastSessionLogsForExercise(exerciseId)

    fun getRecentSessionLogsForExercise(exerciseId: String): Flow<List<SetLog>> =
        dao.getRecentSessionLogsForExercise(exerciseId)

    // ─── Workout Sessions ─────────────────────────────────────────────────────

    suspend fun recordWorkoutSession(session: WorkoutSession) =
        dao.insertWorkoutSession(session)

    fun getSessionsSince(since: Long): Flow<List<WorkoutSession>> =
        dao.getSessionsSince(since)

    fun getSessionCountSince(since: Long): Flow<Int> =
        dao.getSessionCountSince(since)

    fun getTotalSessionCount(): Flow<Int> =
        dao.getTotalSessionCount()

    // ─── Nutrition Logs ───────────────────────────────────────────────────────

    suspend fun logNutrition(log: NutritionLog): Long =
        dao.insertNutritionLog(log)

    suspend fun removeNutritionLog(id: Int) =
        dao.deleteNutritionLog(id)

    fun getNutritionLogsForDay(dayKey: Long): Flow<List<NutritionLog>> =
        dao.getNutritionLogsForDay(dayKey)

    // ─── Body Weight ──────────────────────────────────────────────────────────

    suspend fun logBodyWeight(log: BodyWeightLog) =
        dao.insertBodyWeightLog(log)

    fun getRecentBodyWeightLogs(): Flow<List<BodyWeightLog>> =
        dao.getRecentBodyWeightLogs()

    // ─── Seed Data ────────────────────────────────────────────────────────────

    suspend fun populateInitialData() {
        val upperPlan    = WorkoutPlan("plan_upper",   "UPPER",            "UPPER",            1)
        val lowerPlan    = WorkoutPlan("plan_lower",   "LOWER",            "LOWER",            2)
        val chestBackPlan= WorkoutPlan("plan_cb",      "CHEST & BACK",     "CHEST & BACK",     3)
        val shouldersPlan= WorkoutPlan("plan_sa",      "SHOULDERS & ARMS", "SHOULDERS & ARMS", 4)
        val posturePlan  = WorkoutPlan("plan_posture", "POSTURE & CORE",   "POSTURE & CORE",   5)

        dao.insertPlans(listOf(upperPlan, lowerPlan, chestBackPlan, shouldersPlan, posturePlan))

        val exercises = listOf(

            // ══════════════════════════════════════════════════════════
            // UPPER
            // ══════════════════════════════════════════════════════════
            Exercise("ub1","plan_upper",1,"Machine Chest Press","بريس الصدر بالجهاز",2,"6-8","1-2","3-5 دقائق","الصدر",
                "اضبط الكرسي لتكون المقابض بمستوى منتصف الصدر\n• حافظ على استقامة ظهرك وتثبيته جيدًا",
                "ادفع الوزن بثبات بدون أن تفرد كوعك للآخر\n• ارجع بالوزن ببطء وتحكم",
                "1. دمبل مسطح (أفضل مدى حركي)\n2. بار مسطح حر (أثقل وزن)\n3. تجميع كيبل منتصف (عزل أفضل)"),

            Exercise("ub2","plan_upper",2,"T-Bar Row","تجديف تي-بار",1,"5-7","1-2","3-5 دقائق","الظهر",
                "اسحب الوزن باتجاه بطنك وليس صدرك\n• حافظ على استقامة أسفل ظهرك تماماً",
                "ركز في السحب على كوعك كأنه خطاف\n• اعصر عضلات ظهرك في أعلى نقطة",
                "1. تجديف دمبل أحادي (عزل + مدى أعمق)\n2. تجديف بار بنط (نفس الميكانيكا)\n3. سحب أرضي بالكيبل (تحكم في الشد)"),

            Exercise("ub3","plan_upper",3,"Incline Machine Press","بريس الصدر المائل بالجهاز",1,"6-10","1-2","3-5 دقائق","الصدر",
                "ركز على الدفع بعضلات الصدر العلوية\n• انزل بالوزن ببطء لاستطالة العضلة",
                "المدى الحركي الكامل أهم من رفع وزن ثقيل\n• ثبت قدميك في الأرض بقوة",
                "1. دمبل مائل 30° (أفضل بديل نفس الألياف)\n2. كيبل كروس مائل للأعلى\n3. بار مائل حر (للمتقدمين)"),

            Exercise("ub4","plan_upper",4,"Single Arm Lat Pulldown","سحب اللات أحادي",1,"6-10","0","3-5 دقائق","الظهر",
                "بلاش تقعد تلف حوالين نفسك\n• والتزم بثبات جسمك علي مدار الحركه",
                "الحركة تبدأ من سحب الكوع للأسفل\n• تحكم في الوزن وأنت راجع لفوق",
                "1. Cable SA Lat Row\n2. DB SA Lat Row"),

            Exercise("ub5","plan_upper",5,"Cable Bicep Curl","كيرل البايسبس بالكيبل",1,"6-10","0","3-5 دقائق","البايسبس",
                "قف مستقيماً وثبت كوعك بجانب جسمك\n• ارفع الوزن بتركيز بدون تحريك كتفك",
                "النزول ببطء (التحكم السلبي) يبني العضلة بشكل ممتاز\n• لا تستخدم وزن يجعل جسمك يتأرجح",
                "1. كيرل دمبل تبادلي (نفس الحركة)\n2. كيرل بار بنط (أريح للرسغ)\n3. كيرل دمبل على دكة مائلة 45° (استطالة أكبر)"),

            Exercise("ub6","plan_upper",6,"Overhead Triceps Ext","مد الترايسبس فوق الرأس",2,"6-10","0","3-5 دقائق","الترايسبس",
                "ثبت كوعك بجوار رأسك قدر الإمكان\n• ادفع للأعلى للحصول على أقصى استطالة",
                "لو كوعك وجعك العب بوش داون عادي جدااااا",
                "1. DB Skull Crusher\n2. كيبل أوفرهيد"),

            Exercise("ub7","plan_upper",7,"Cable Shrugs","شراغ الكيبل",1,"6-8","1","3-5 دقائق","الترابيس",
                "ارفع كتفك في خط مستقيم باتجاه أذنك\n• اعصر العضلة ثانية في الأعلى",
                "تجنب الدوران بالكتف لحماية مفاصل رقبتك\n• انزل بالوزن ببطء شديد",
                "1. شراغز دمبل (نطاق حركي أفضل)\n2. شراغز بار (أثقل وزن)\n3. شراغز جهاز سميث (استقرار أكثر)"),

            // ══════════════════════════════════════════════════════════
            // LOWER
            // ══════════════════════════════════════════════════════════
            Exercise("lo1","plan_lower",1,"Machine Lateral Raise","رفرفة الكتف الجانبي على الجهاز",2,"6-8","1-2","3-5 دقائق","الكتف",
                "حاول علي قد متقدر الحركه تبقي طالعه من كتفك لوحده مش جسمك كله",
                "النزول البطيء سر التمرين — لا تسقط الوزن بسرعة",
                "1. Cable Lateral Raises\n2. DB Lateral Raises"),

            Exercise("lo2","plan_lower",2,"Leg Press Calf Raise","رفعات السمانة بجهاز الليج بريس",2,"5-7","1-2","3-5 دقائق","السمانة",
                "ادفع بمشط قدمك ببطء للأعلى\n• اثبت في الأعلى لمدة ثانية واكمل العصر",
                "النزول لأقصى استطالة في الأسفل هو سر تطور السمانة\n• لا تسرع في الأداء",
                "1. سمانة جالس بالجهاز (عزل أفضل للرأس الداخلي)\n2. سمانة واقف بالدمبل أحادي\n3. سمانة جهاز سميث (وزن أثقل)"),

            Exercise("lo3","plan_lower",3,"Hack Squat","هاك سكوات",1,"5-8","1-3","3-5 دقائق","الأرجل",
                "120 درجه من ثني الركبه يكفي لاستهداف الكوادز\n• بس حاول تنزل للاخر",
                "لا تفرد ركبتك بالكامل في الأعلى للحماية\n• ثبت أسفل ظهرك كاملاً على المسند",
                "1. Smith Squat\n2. Leg Press"),

            Exercise("lo4","plan_lower",4,"Prone Leg Curl","كيرل الأرجل المستلقي",1,"8-12","1-2","3-5 دقائق","الأرجل",
                "اسحب كعبيك للأعلى نحو جسمك بقوة واعصر في الأعلى\n• ارجع ببطء لأقصى استطالة",
                "لو الجهازين مش موجودين العبه SLDL",
                "1. Seated Leg Curl\n2. SLDL"),

            Exercise("lo5","plan_lower",5,"Leg Extension","مد الأرجل",1,"8-12","1-2","3-5 دقائق","الأرجل",
                "افرد رجلك للأعلى بثبات وتوقف لجزء من الثانية\n• النزول البطيء يقوي الأربطة",
                "لو الجهاز مش موجود العب BANDED LEG EXTENSION",
                "1. Banded Leg Extension\n2. Leg Press بوضع قدم ضيق"),

            Exercise("lo6","plan_lower",6,"Hip Adductor Machine","الضامة",1,"6-8","1-2","3-5 دقائق","الأرجل",
                "ثبت نفسك بالمقبض كويس ومتحركش نفسك ع الجهاز",
                "الحركة بطيئة وتحكم — تجنب قذف الوزن لإغلاق الجهاز",
                "1. Cable Hip Adduction\n2. Copenhagen Adductor"),

            Exercise("lo7","plan_lower",7,"Wrist Curl","كيرل الرسغ",1,"6-10","0","3-5 دقائق","السواعد",
                "فط صوابعك تحت ف الاستطاله\n• واختار وزن يخليك تعمل انقباض كامل",
                "الحركة من المعصم فقط — ثبت ساعدك بالكامل على الدكة",
                "1. Reverse Wrist Curl\n2. كيرل بار مقلوب"),

            // ══════════════════════════════════════════════════════════
            // CHEST & BACK
            // ══════════════════════════════════════════════════════════
            Exercise("ua1","plan_cb",1,"Smith High Incline Press","بريس المائل العالي بجهاز سميث",2,"4-6","1-2","3-5 دقائق","الصدر",
                "ضم ايدك لجوه عشان تحاكي اتجاه الياف الصدر العالي\n• بلاش تفتح كوعك 90 درجه",
                "ركز على الضغط من الصدر العلوي مش من يديك\n• انزل ببطء وتحكم حتى البار يلامس أعلى الصدر",
                "1. دمبل مائل عالي DB High Incline Press\n2. كيبل كروس من أسفل للأعلى"),

            Exercise("ua2","plan_cb",2,"Machine Wide Grip Lat Pulldown","سحب اللات العريض على الجهاز",2,"6-8","1-3","3-5 دقائق","الظهر",
                "ركز ف مسار كوعك وانك بتضم كتافك علي بعض مش بتسحب علي ضهرك العلوي",
                "التحكم في الرجوع (Eccentric) مهم جداً — متسيبش الوزن يطلع لفوق بسرعة",
                "1. Cable Wide Grip Lat Pulldown\n2. عقلة واسعة (الأصعب والأفضل للظهر)"),

            Exercise("ua3","plan_cb",3,"Machine Chest Press","بريس الصدر بالجهاز",1,"6-10","1-2","3-5 دقائق","الصدر",
                "اجعل لوحي كتفك مشدودين للخلف وثبتهما بالكرسي\n• ادفع الوزن وتجنب فرد الكوع لآخره",
                "الأداء البطيء يضاعف من تفعيل ألياف الصدر\n• حافظ على صدرك مرفوعاً",
                "1. دمبل مسطح (مدى أعمق للصدر)\n2. بار مسطح حر (أثقل)\n3. غطس بوزن Dips (استطالة ممتازة)"),

            Exercise("ua4","plan_cb",4,"T-Bar Row","تجديف تي-بار",1,"5-7","1-2","3-5 دقائق","الظهر",
                "افتح كيعانك لبره علي قد متقدر\n• حاول تقرب من زاوية الـ 90 علي حسب راحة كتفك ومرونتك",
                "الهدف سماكة الظهر — اقبض عضلات الظهر في الأعلى\n• خلي ضهرك مستقيم طول الوقت",
                "1. Incline DB Row\n2. Cable Row"),

            Exercise("ua5","plan_cb",5,"Cable Shrugs","شراغ الترابيس بالكيبل",1,"6-8","1","3-5 دقائق","الترابيس",
                "شد الكيبل من أسفل وارفع كتفيك مستقيماً ناحية أذنيك — الحركة عمودية بحتة\n• اعصر الترابيس في الأعلى لمدة ثانية كاملة",
                "تجنب الدوران بالكتف أو الفرد — الحركة رفع ونزول فقط\n• النزول البطيء بيضاعف من تفعيل العضلة",
                "1. شراغ دمبل (مدى حركي أوسع)\n2. شراغ بار (أثقل وزن)\n3. شراغ جهاز سميث (ثبات أكثر، وزن أثقل)"),

            Exercise("ua6","plan_cb",6,"Seated Cable Row","تجديف الكيبل الأرضي",1,"6-10","0","3-5 دقائق","الظهر",
                "اجلس منتصباً واسحب نحو معدتك مع سحب لوحي كتفك للخلف بقوة\n• اثبت للأعلى لثانية واعصر عضلات الظهر المتوسطة",
                "الجذع ثابت طوال الوقت — تجنب الانحناء للأمام والرجوع\n• الحركة تبدأ من سحب الكوع للخلف لا من الأكتاف",
                "1. تجديف دمبل أحادي (مدى أعمق + عزل جانب)\n2. تجديف T-Bar (وزن أثقل وسماكة أكثر)\n3. جهاز الروينج"),

            // ══════════════════════════════════════════════════════════
            // SHOULDERS & ARMS — كتف + دراع (بوش داون هنا، أوفرهيد في Upper)
            // ══════════════════════════════════════════════════════════
            Exercise("lb1","plan_sa",1,"SA Tricep Pushdown","بوش داون الترايسبس كيبل أحادي",2,"6-10","0","3-5 دقائق","الترايسبس",
                "متدخلش الكور بزياده وتتمرجح وتحرك كتفك\n• هي فرد للكوع فقط",
                "استخدم وزن يخليك تتحكم في الحركة بالكامل\n• ارجع للأعلى ببطء للتحكم السلبي",
                "1. Double Rope Pushdown\n2. V-Bar Pushdown"),

            Exercise("lb2","plan_sa",2,"Cable Lateral Raise","رفرفة الكتف الجانبي بالكيبل",2,"5-8","1-2","3-5 دقائق","الكتف",
                "اسحب الكيبل من مستوى الكاحل بمحاذاة كتفك بدون مرجحة\n• اعتمد على الكتف فقط — جسمك ثابت والحركة كلها من الكتف",
                "النزول البطيء يعطي نتائج أفضل بكثير من رفع أوزان تقيلة بمرجحة\n• ارفع لمستوى الكتف بالضبط مش فوقه",
                "1. رفرفة جانبي دمبل جالس (تقليل المرجحة)\n2. جهاز الكتف الجانبي (ثبات وتحكم)\n3. رفرفة كيبل جالس"),

            Exercise("lb3","plan_sa",3,"Seated DB Bicep Curl","كيرل البايسبس دمبل جالس على الدكة",2,"6-10","0","3-5 دقائق","البايسبس",
                "بلاش مدي حركي زياده من الكتف\n• ومتمرجحش جسمك خلي الحركه كلها جايه من كوعك",
                "ارجع بالوزن ببطء (Eccentric) — ده اللي بيبني البايسبس بجد\n• لا تستخدم وزن يجبرك على المرجحة",
                "1. Face Away Curl\n2. DB Curls واقف"),

            Exercise("lb4","plan_sa",4,"Forearm Cable Curl","كيرل السواعد بالكيبل",1,"6-10","0","3-5 دقائق","السواعد",
                "تقدر تغيير القبضات براحتك علي حسب الي يريحك\n• المهم كله نفس الاداء",
                "فط صوابعك تحت ف الاستطاله\n• اختار وزن يخليك تعمل انقباض كامل",
                "1. كيرل رسغ بالبار جالس\n2. كيرل معكوس Reverse Curl"),

            Exercise("lb5","plan_sa",5,"Rear Delt Butterfly","رفرفة الكتف الخلفي على جهاز الفراشة",1,"6-10","0","3-5 دقائق","الكتف",
                "فك لوحين كتفك من قبل متبدا الحركه\n• وحاول تركز علي كتفك الخلفي",
                "الوزن الخفيف مع التركيز العالي أفضل بكثير\n• اعكس جلستك على الجهاز",
                "1. Reverse Pec Dec\n2. Face Pulls بالكيبل"),

            Exercise("lb6","plan_sa",6,"Front Raise Lat Machine","رفع الكتف الأمامي على جهاز السحب لفوق",1,"6-10","0","3-5 دقائق","الكتف",
                "استخدم ذراع جهاز السحب لفوق وارفع بشكل أمامي نحو مستوى كتفك\n• الحركة طالعة من الكتف الأمامي بتحكم",
                "متكملش فوق مستوى الكتف لتجنب الضغط على المفصل\n• النزول البطيء يفرق كثيراً",
                "1. رفع أمامي دمبل (أبسط وأكثر انتشاراً)\n2. رفع أمامي كيبل (شد مستمر طول الحركة)\n3. رفع أمامي بار (وزن أثقل)"),

            // ══════════════════════════════════════════════════════════
            // ══════════════════════════════════════════════════════════
            // POSTURE & CORE للي بيقعد فترات طويلة
            // ══════════════════════════════════════════════════════════
            Exercise("pc1","plan_posture",1,"Face Pulls","الكيبل للوجه | كتف خلفي وثبات الكتف",3,"12-15","0","60-90 ثانية","الكتف",
                "اسحب الحبل نحو وجهك وافتح كوعيك للجانبين وللأعلى في نفس الوقت\n• اعصر الكتف الخلفي جامد لمدة ثانية في النهاية وعدّل ببطء",
                "ده التمرين الأهم لمواجهة أثر القعدة الطويلة، بيفتح الكتف اللي بيتلف للأمام من الشاشة\n• استخدم وزن خفيف وركز تحس بالعضلة مش برفع الوزن",
                "1. رفرفة خلفي بالدمبل جالساً (ممتاز)\n2. Band Pull-Apart يومياً بدون جهاز\n3. فراشة خلفي على الجهاز"),

            Exercise("pc2","plan_posture",2,"Hip Thrust","هيب ثراست | عضلات الجلوس والأرداف",3,"10-12","1","60-90 ثانية","الأرجل",
                "ثبّت كتفيك على الدكة وادفع من كعبيك للأعلى\n• في الأعلى اعصر عضلات الجلوس بقوة وخلي ظهرك مستوي في الهواء",
                "القعدة الطويلة بتخمّد عضلات الجلوس خالص، وده التمرين اللي بيوقف الضرر ده\n• لو ركبتك بتوجعك ادفع من الكعب مش من أصابع القدم",
                "1. Glute Bridge أرضي (أسهل، لو مفيش جهاز)\n2. Cable Kickback (عزل أحسن)\n3. ديدليفت روماني RDL"),

            Exercise("pc3","plan_posture",3,"Dead Bug","ديد باج | ثبات الكور",3,"8-10","0","60 ثانية","البطن",
                "انقر ظهرك بالأرض تماماً ومتحركش في الهواء طول الوقت\n• مد ذراع وساق في الجهتين المتقابلين ببطء شديد وارجع",
                "أفضل تمرين علمياً للكور من غير أي ضغط على الفقرات، مثالي لألم الظهر من القعدة الطويلة\n• لو حسيت ظهرك رفع عن الأرض ولو لمليمتر — صغّر الحركة",
                "1. Bird Dog على الأربعة (نفس الفكرة)\n2. Hollow Body Hold (أصعب نسخة)\n3. بلانك مع رفع ذراع وساق"),

            Exercise("pc4","plan_posture",4,"Cable Pull-Through","كيبل بول ثرو | سلسلة الظهر الخلفية",3,"10-12","0","60-90 ثانية","الأرجل",
                "قف وظهرك للجهاز وامسك الكيبل من بين ساقيك\n• ادفع بحوضك للأمام ببطء وتحكم وانتهي في وقفة منتصبة",
                "بيشغّل كل سلسلة الظهر الخلفية (الأرداف + أسفل الظهر + هامسترينج) اللي بتضعف من القعدة\n• الظهر مستقيم طوال الوقت — الحركة كلها من منطقة الحوض بس",
                "1. Kettlebell Swing (نفس الحركة بس أقوى)\n2. Hip Hinge بالدمبل\n3. RDL بالبار"),

            Exercise("pc5","plan_posture",5,"Seated Cable Row","تجديف جالس بالكيبل | تصحيح الوضعية",3,"10-12","1","60-90 ثانية","الظهر",
                "اجلس منتصب خالص واسحب نحو معدتك وسحب لوحي كتفك للخلف في نفس الوقت\n• اثبّت لثانيتين واعصر عضلات الظهر المتوسطة بجد",
                "بيعالج مباشرة الانحناء للأمام اللي بيسببه الجلوس أمام الشاشة\n• تخيل إنك بتحاول تمسك قلم بين لوحي كتفك في أعلى الحركة",
                "1. تجديف دمبل أحادي (عزل + مدى أعمق)\n2. تجديف بار (أثقل)\n3. جهاز الروينج"),

            Exercise("pc6","plan_posture",6,"Thoracic Extension","فرد الظهر العلوي | علاج الانحناء",2,"8-10","2","60 ثانية","الظهر",
                "حط الفوم رولر أو حافة الدكة تحت الجزء الأوسط من ظهرك\n• انبسط للخلف ببطء وإيديك خلف دماغك وحس بالفتح في الصدر",
                "العمود الفقري الصدري بيتصلّب بشكل خاص من القعدة الطويلة، وده التمرين بيفتحه مباشرة\n• تقدر تعمله كل يوم وقبل أي تمرين صدر أو كتف كإحماء ممتاز",
                "1. تمديد الصدر على حافة الدكة\n2. Cat-Cow يومياً\n3. Door Frame Chest Stretch"),

            Exercise("pc7","plan_posture",7,"Hip Flexor Stretch","تمديد أمام الفخذ | تخفيف ألم الظهر",2,"30-45ث","0","30 ثانية","الأرجل",
                "اركع وحرّك حوضك للخلف (تخيل زنار بيشدك من تحت) أثناء التمديد، ده سر فاعليته\n• استنّى 30-45 ثانية على كل جهة مع تنفس عميق بطيء",
                "عضلات أمام الفخذ هي الأكثر تضرراً من القعدة الطويلة، تقصيرها بيعمل ألم أسفل الظهر وبيضعّف الأرداف\n• متحاولش تتمدد بالقوة — خلي الجسم يسيب ببطء",
                "1. Couch Stretch (أقوى نسخة)\n2. Pigeon Pose\n3. Hip Flexor Stretch بالكيبل واقفاً")
        )
        dao.insertExercises(exercises)
    }

    // ─── Personal Records ─────────────────────────────────────────────────────

    /** Epley formula. Returns 0 if reps = 1 (the weight itself IS the 1RM). */
    fun epley(weight: Float, reps: Int): Float =
        if (reps <= 1) weight else weight * (1f + reps / 30f)

    suspend fun checkAndSavePR(
        exerciseId: String,
        exerciseName: String,
        exerciseNameAr: String,
        weightKg: Float,
        reps: Int
    ): Boolean {
        val orm = epley(weightKg, reps)
        val existing = dao.getPersonalRecord(exerciseId)
        return if (existing == null || orm > existing.estimatedOneRepMax) {
            dao.upsertPersonalRecord(
                PersonalRecord(exerciseId, exerciseName, exerciseNameAr, weightKg, reps, orm)
            )
            true
        } else false
    }

    fun getAllPersonalRecords(): Flow<List<PersonalRecord>> = dao.getAllPersonalRecords()

    // ─── Water Tracking ───────────────────────────────────────────────────────

    fun getTodayWaterMl(dayKey: Long): Flow<Int> = dao.getTodayWaterMl(dayKey)

    suspend fun addWater(dayKey: Long, amountMl: Int) =
        dao.insertWaterLog(WaterLog(dayKey = dayKey, amountMl = amountMl))

    suspend fun resetTodayWater(dayKey: Long) = dao.clearTodayWater(dayKey)

    // ─── Body Measurements ────────────────────────────────────────────────────

    suspend fun insertBodyMeasurement(m: BodyMeasurement) = dao.insertBodyMeasurement(m)

    fun getRecentBodyMeasurements(): Flow<List<BodyMeasurement>> = dao.getRecentBodyMeasurements()

    // ─── Workout Notes ────────────────────────────────────────────────────────

    suspend fun saveWorkoutNote(sessionId: Long, note: String) =
        dao.insertWorkoutNote(WorkoutNote(sessionId = sessionId, note = note))

    suspend fun getNoteForSession(sessionId: Long): WorkoutNote? =
        dao.getNoteForSession(sessionId)

    // ─── RPE ──────────────────────────────────────────────────────────────────

    suspend fun logRpe(sessionId: Long, exerciseId: String, setNumber: Int, rpe: Int) =
        dao.insertRpeLog(RpeLog(sessionId = sessionId, exerciseId = exerciseId, setNumber = setNumber, rpeValue = rpe))

    // ─── Custom Meals ─────────────────────────────────────────────────────────

    suspend fun insertCustomMeal(meal: CustomMeal) = dao.insertCustomMeal(meal)

    fun getAllCustomMeals(): Flow<List<CustomMeal>> = dao.getAllCustomMeals()

    suspend fun deleteCustomMeal(id: Int) = dao.deleteCustomMeal(id)
}
