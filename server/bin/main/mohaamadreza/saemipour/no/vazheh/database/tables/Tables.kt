package mohaamadreza.saemipour.no.vazheh.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Parents table - Mother accounts (والدین)
 * ایمیل، پسورد و نام نمایشی
 */
object Parents : IntIdTable("parents") {
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName = varchar("display_name", 100) // نام انتخابی مادر
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

/**
 * Children table - فرزندان (بدون لاگین)
 * مادر فرزند را اضافه می‌کند
 * حداکثر ۲ فرزند برای هر مادر
 */
object Children : IntIdTable("children") {
    val parentId = reference("parent_id", Parents, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100) // اسم فرزند
    val age = integer("age") // سن فرزند
    val avatarUrl = varchar("avatar_url", 255).nullable() // آواتار فرزند
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

/**
 * Categories table - دسته‌بندی کلمات
 * میوه‌ها، حیوانات، رنگ‌ها، وسایل نقلیه، خانواده و ...
 */
object Categories : IntIdTable("categories") {
    val nameFa = varchar("name_fa", 100) // نام فارسی
    val nameEn = varchar("name_en", 100) // نام انگلیسی
    val iconUrl = varchar("icon_url", 255).nullable()
    val displayOrder = integer("display_order").default(0)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

/**
 * Words table - کلمات به همراه صوت
 * هر کلمه متعلق به یک دسته‌بندی است
 */
object Words : IntIdTable("words") {
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.CASCADE)
    val wordFa = varchar("word_fa", 100) // کلمه فارسی
    val wordEn = varchar("word_en", 100) // کلمه انگلیسی
    val imageUrl = varchar("image_url", 255).nullable() // تصویر کلمه
    val audioUrl = varchar("audio_url", 255).nullable() // صوت کلمه
    val displayOrder = integer("display_order").default(0)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

/**
 * Custom Words table - کلمات سفارشی مادر برای فرزندان
 * مادر می‌تواند کلمه و صوت خودش را اضافه کند
 */
object CustomWords : IntIdTable("custom_words") {
    val parentId = reference("parent_id", Parents, onDelete = ReferenceOption.CASCADE)
    val wordFa = varchar("word_fa", 100) // کلمه فارسی
    val wordEn = varchar("word_en", 100).nullable() // کلمه انگلیسی (اختیاری)
    val imageUrl = varchar("image_url", 255).nullable() // تصویر کلمه
    val audioUrl = varchar("audio_url", 255) // صوت ضبط شده توسط مادر
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.SET_NULL).nullable() // دسته‌بندی (اختیاری)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

/**
 * Quiz Attempts table - نتایج آزمون‌ها
 * صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند
 * برای پیگیری پیشرفت فرزند
 */
object QuizAttempts : IntIdTable("quiz_attempts") {
    val childId = reference("child_id", Children, onDelete = ReferenceOption.CASCADE)
    val wordId = reference("word_id", Words, onDelete = ReferenceOption.CASCADE)
    val isCorrect = bool("is_correct") // آیا پاسخ درست بود؟
    val responseTimeMs = integer("response_time_ms").nullable() // زمان پاسخ به میلی‌ثانیه
    val attemptedAt = datetime("attempted_at").default(LocalDateTime.now())
}

/**
 * Child Progress table - خلاصه پیشرفت فرزند برای هر کلمه
 * میزان پیشرفت فرزند
 */
object ChildProgress : IntIdTable("child_progress") {
    val childId = reference("child_id", Children, onDelete = ReferenceOption.CASCADE)
    val wordId = reference("word_id", Words, onDelete = ReferenceOption.CASCADE)
    val correctAttempts = integer("correct_attempts").default(0) // تعداد پاسخ‌های درست
    val totalAttempts = integer("total_attempts").default(0) // تعداد کل تلاش‌ها
    val isLearned = bool("is_learned").default(false) // آیا یاد گرفته شده؟ (مثلاً ۳ پاسخ درست متوالی)
    val lastAttemptAt = datetime("last_attempt_at").nullable()
    val learnedAt = datetime("learned_at").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    init {
        uniqueIndex(childId, wordId)
    }
}
