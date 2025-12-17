package mohaamadreza.saemipour.no.vazheh.database

import mohaamadreza.saemipour.no.vazheh.database.tables.Categories
import mohaamadreza.saemipour.no.vazheh.database.tables.Words
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Database seeder for initial sample data
 * پر کردن پایگاه داده با داده‌های اولیه نمونه
 */
object DatabaseSeeder {
    
    /**
     * Seed the database with sample categories and words
     */
    fun seed() {
        transaction {
            // Only seed if categories table is empty
            if (Categories.selectAll().count() > 0) {
                println("📦 Database already has data, skipping seed.")
                return@transaction
            }
            
            println("🌱 Seeding database with sample data...")
            
            // Seed categories
            val fruitsId = seedCategory("میوه‌ها", "Fruits", 1)
            val animalsId = seedCategory("حیوانات", "Animals", 2)
            val colorsId = seedCategory("رنگ‌ها", "Colors", 3)
            val numbersId = seedCategory("اعداد", "Numbers", 4)
            val bodyPartsId = seedCategory("اعضای بدن", "Body Parts", 5)
            val vehiclesId = seedCategory("وسایل نقلیه", "Vehicles", 6)
            val familyId = seedCategory("خانواده", "Family", 7)
            val clothesId = seedCategory("لباس‌ها", "Clothes", 8)
            val foodId = seedCategory("غذاها", "Food", 9)
            val shapesId = seedCategory("اشکال", "Shapes", 10)
            
            // Seed fruits - میوه‌ها
            seedWord(fruitsId, "سیب", "Apple", 1)
            seedWord(fruitsId, "موز", "Banana", 2)
            seedWord(fruitsId, "پرتقال", "Orange", 3)
            seedWord(fruitsId, "انگور", "Grape", 4)
            seedWord(fruitsId, "توت فرنگی", "Strawberry", 5)
            seedWord(fruitsId, "هندوانه", "Watermelon", 6)
            seedWord(fruitsId, "گیلاس", "Cherry", 7)
            seedWord(fruitsId, "آناناس", "Pineapple", 8)
            seedWord(fruitsId, "گلابی", "Pear", 9)
            seedWord(fruitsId, "هلو", "Peach", 10)
            
            // Seed animals - حیوانات
            seedWord(animalsId, "سگ", "Dog", 1)
            seedWord(animalsId, "گربه", "Cat", 2)
            seedWord(animalsId, "پرنده", "Bird", 3)
            seedWord(animalsId, "ماهی", "Fish", 4)
            seedWord(animalsId, "فیل", "Elephant", 5)
            seedWord(animalsId, "شیر", "Lion", 6)
            seedWord(animalsId, "خرگوش", "Rabbit", 7)
            seedWord(animalsId, "زرافه", "Giraffe", 8)
            seedWord(animalsId, "میمون", "Monkey", 9)
            seedWord(animalsId, "پروانه", "Butterfly", 10)
            seedWord(animalsId, "اسب", "Horse", 11)
            seedWord(animalsId, "گاو", "Cow", 12)
            seedWord(animalsId, "مرغ", "Chicken", 13)
            seedWord(animalsId, "اردک", "Duck", 14)
            
            // Seed colors - رنگ‌ها
            seedWord(colorsId, "قرمز", "Red", 1)
            seedWord(colorsId, "آبی", "Blue", 2)
            seedWord(colorsId, "سبز", "Green", 3)
            seedWord(colorsId, "زرد", "Yellow", 4)
            seedWord(colorsId, "نارنجی", "Orange", 5)
            seedWord(colorsId, "بنفش", "Purple", 6)
            seedWord(colorsId, "صورتی", "Pink", 7)
            seedWord(colorsId, "سفید", "White", 8)
            seedWord(colorsId, "سیاه", "Black", 9)
            seedWord(colorsId, "قهوه‌ای", "Brown", 10)
            
            // Seed numbers - اعداد
            seedWord(numbersId, "یک", "One", 1)
            seedWord(numbersId, "دو", "Two", 2)
            seedWord(numbersId, "سه", "Three", 3)
            seedWord(numbersId, "چهار", "Four", 4)
            seedWord(numbersId, "پنج", "Five", 5)
            seedWord(numbersId, "شش", "Six", 6)
            seedWord(numbersId, "هفت", "Seven", 7)
            seedWord(numbersId, "هشت", "Eight", 8)
            seedWord(numbersId, "نه", "Nine", 9)
            seedWord(numbersId, "ده", "Ten", 10)
            
            // Seed body parts - اعضای بدن
            seedWord(bodyPartsId, "سر", "Head", 1)
            seedWord(bodyPartsId, "چشم", "Eye", 2)
            seedWord(bodyPartsId, "گوش", "Ear", 3)
            seedWord(bodyPartsId, "بینی", "Nose", 4)
            seedWord(bodyPartsId, "دهان", "Mouth", 5)
            seedWord(bodyPartsId, "دست", "Hand", 6)
            seedWord(bodyPartsId, "پا", "Foot", 7)
            seedWord(bodyPartsId, "انگشت", "Finger", 8)
            seedWord(bodyPartsId, "مو", "Hair", 9)
            seedWord(bodyPartsId, "دندان", "Tooth", 10)
            
            // Seed vehicles - وسایل نقلیه
            seedWord(vehiclesId, "ماشین", "Car", 1)
            seedWord(vehiclesId, "اتوبوس", "Bus", 2)
            seedWord(vehiclesId, "قطار", "Train", 3)
            seedWord(vehiclesId, "هواپیما", "Airplane", 4)
            seedWord(vehiclesId, "دوچرخه", "Bicycle", 5)
            seedWord(vehiclesId, "کشتی", "Ship", 6)
            seedWord(vehiclesId, "موتور", "Motorcycle", 7)
            seedWord(vehiclesId, "کامیون", "Truck", 8)
            seedWord(vehiclesId, "تاکسی", "Taxi", 9)
            seedWord(vehiclesId, "آمبولانس", "Ambulance", 10)
            
            // Seed family - خانواده
            seedWord(familyId, "مادر", "Mother", 1)
            seedWord(familyId, "پدر", "Father", 2)
            seedWord(familyId, "خواهر", "Sister", 3)
            seedWord(familyId, "برادر", "Brother", 4)
            seedWord(familyId, "مادربزرگ", "Grandmother", 5)
            seedWord(familyId, "پدربزرگ", "Grandfather", 6)
            seedWord(familyId, "عمو", "Uncle", 7)
            seedWord(familyId, "خاله", "Aunt", 8)
            seedWord(familyId, "بچه", "Baby", 9)
            
            // Seed clothes - لباس‌ها
            seedWord(clothesId, "پیراهن", "Shirt", 1)
            seedWord(clothesId, "شلوار", "Pants", 2)
            seedWord(clothesId, "کفش", "Shoes", 3)
            seedWord(clothesId, "کلاه", "Hat", 4)
            seedWord(clothesId, "جوراب", "Socks", 5)
            seedWord(clothesId, "ژاکت", "Jacket", 6)
            seedWord(clothesId, "دامن", "Skirt", 7)
            seedWord(clothesId, "لباس", "Dress", 8)
            
            // Seed food - غذاها
            seedWord(foodId, "نان", "Bread", 1)
            seedWord(foodId, "آب", "Water", 2)
            seedWord(foodId, "شیر", "Milk", 3)
            seedWord(foodId, "تخم مرغ", "Egg", 4)
            seedWord(foodId, "برنج", "Rice", 5)
            seedWord(foodId, "مرغ", "Chicken", 6)
            seedWord(foodId, "ماست", "Yogurt", 7)
            seedWord(foodId, "پنیر", "Cheese", 8)
            
            // Seed shapes - اشکال
            seedWord(shapesId, "دایره", "Circle", 1)
            seedWord(shapesId, "مربع", "Square", 2)
            seedWord(shapesId, "مثلث", "Triangle", 3)
            seedWord(shapesId, "مستطیل", "Rectangle", 4)
            seedWord(shapesId, "ستاره", "Star", 5)
            seedWord(shapesId, "قلب", "Heart", 6)
            
            println("✅ Database seeded successfully with ${Words.selectAll().count()} words in ${Categories.selectAll().count()} categories!")
        }
    }
    
    private fun seedCategory(nameFa: String, nameEn: String, order: Int): Int {
        return Categories.insert {
            it[Categories.nameFa] = nameFa
            it[Categories.nameEn] = nameEn
            it[displayOrder] = order
            it[createdAt] = LocalDateTime.now()
        }[Categories.id].value
    }
    
    private fun seedWord(categoryId: Int, wordFa: String, wordEn: String, order: Int) {
        Words.insert {
            it[Words.categoryId] = categoryId
            it[Words.wordFa] = wordFa
            it[Words.wordEn] = wordEn
            it[displayOrder] = order
            it[createdAt] = LocalDateTime.now()
        }
    }
}
