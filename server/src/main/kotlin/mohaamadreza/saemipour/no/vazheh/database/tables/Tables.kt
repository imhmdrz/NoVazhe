package mohaamadreza.saemipour.no.vazheh.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/** Parents table - Mother accounts (والدین) نام کاربری، پسورد و نام نمایشی */
object Parents : IntIdTable("parents") {
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName = varchar("display_name", 100) // نام انتخابی مادر
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

/** Children table - فرزندان حساب والد */
object Children : IntIdTable("children") {
    val parentId = reference("parent_id", Parents, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100) // اسم فرزند
    val age = integer("age") // سن فرزند
    val gender = varchar("gender", 10).default("BOY") // جنسیت فرزند - BOY یا GIRL
    val avatarUrl = varchar("avatar_url", 255).nullable() // آواتار فرزند
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

/** Categories table - دسته‌بندی کلمات میوه‌ها، حیوانات، رنگ‌ها، وسایل نقلیه، خانواده و ... */
object Categories : IntIdTable("categories") {
    val nameFa = varchar("name_fa", 100) // نام فارسی
    val nameEn = varchar("name_en", 100) // نام انگلیسی
    val iconUrl = text("icon_url").nullable()
    val displayOrder = integer("display_order").default(0)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

/** Words table - کلمات به همراه صوت هر کلمه متعلق به یک دسته‌بندی است */
object Words : IntIdTable("words") {
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.CASCADE)
    val wordFa = varchar("word_fa", 100) // کلمه فارسی
    val wordEn = varchar("word_en", 100) // کلمه انگلیسی
    val imageUrl = text("image_url").nullable() // تصویر کلمه (base64 data URL)
    val audioUrl = text("audio_url") // صوت ضبط شده توسط مادر (base64 data URL)
    val displayOrder = integer("display_order").default(0)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

/**
 * Custom Words table - کلمات سفارشی مادر برای فرزندان مادر می‌تواند کلمه و صوت خودش را اضافه کند
 */
object CustomWords : IntIdTable("custom_words") {
    val parentId = reference("parent_id", Parents, onDelete = ReferenceOption.CASCADE)
    val wordFa = varchar("word_fa", 100) // کلمه فارسی
    val wordEn = varchar("word_en", 100).nullable() // کلمه انگلیسی (اختیاری)
    val imageUrl = text("image_url").nullable() // تصویر کلمه (base64 data URL)
    val audioUrl = text("audio_url") // صوت ضبط شده توسط مادر (base64 data URL)
    val categoryId =
            reference("category_id", Categories, onDelete = ReferenceOption.SET_NULL)
                    .nullable() // دسته‌بندی (اختیاری)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

/**
 * Quiz Attempts table - نتایج آزمون‌ها صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند برای
 * پیگیری پیشرفت فرزند
 */
object QuizAttempts : IntIdTable("quiz_attempts") {
    val childId = reference("child_id", Children, onDelete = ReferenceOption.CASCADE)
    val wordId = reference("word_id", Words, onDelete = ReferenceOption.CASCADE)
    val isCorrect = bool("is_correct") // آیا پاسخ درست بود؟
    val responseTimeMs = integer("response_time_ms").nullable() // زمان پاسخ به میلی‌ثانیه
    val attemptedAt = datetime("attempted_at").default(LocalDateTime.now())
}

/** Child Progress table - خلاصه پیشرفت فرزند برای هر کلمه میزان پیشرفت فرزند */
object ChildProgress : IntIdTable("child_progress") {
    val childId = reference("child_id", Children, onDelete = ReferenceOption.CASCADE)
    val wordId = reference("word_id", Words, onDelete = ReferenceOption.CASCADE)
    val correctAttempts = integer("correct_attempts").default(0) // تعداد پاسخ‌های درست
    val totalAttempts = integer("total_attempts").default(0) // تعداد کل تلاش‌ها
    val isLearned =
            bool("is_learned").default(false) // آیا یاد گرفته شده؟ (مثلاً ۳ پاسخ درست متوالی)
    val lastAttemptAt = datetime("last_attempt_at").nullable()
    val learnedAt = datetime("learned_at").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    init {
        uniqueIndex(childId, wordId)
    }
}

/**
 * Memory Progress table - پیشرفت بازی حافظه به‌ازای هر فرزند؛ یک رکورد برای کل بازی
 * ابعاد برد (dimension_index) و تعداد برد‌های موفق در آن ابعاد (successful_games)
 */
object MemoryProgress : IntIdTable("memory_progress") {
    val childId = reference("child_id", Children, onDelete = ReferenceOption.CASCADE)
    val dimensionIndex = integer("dimension_index").default(0) // جایگاه در نردبان ابعاد (۲x۲ تا ۴x۴)
    val successfulGames = integer("successful_games").default(0) // برد‌های موفق در ابعاد فعلی (۰ تا ۳)
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    init {
        uniqueIndex(childId) // یک رکورد برای هر فرزند
    }
}

/**
 * Color Sorting Progress table - پیشرفت بازی رنگ‌ها به‌ازای هر فرزند؛ یک رکورد برای کل بازی
 * level_index تعداد رنگ‌های فعال را تعیین می‌کند (۰=۳ رنگ، ۱=۴ رنگ، ۲=۵ رنگ)
 */
object ColorSortingProgress : IntIdTable("color_sorting_progress") {
    val childId = reference("child_id", Children, onDelete = ReferenceOption.CASCADE)
    val levelIndex = integer("level_index").default(0)
    val successfulGames = integer("successful_games").default(0)
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    init {
        uniqueIndex(childId)
    }
}
