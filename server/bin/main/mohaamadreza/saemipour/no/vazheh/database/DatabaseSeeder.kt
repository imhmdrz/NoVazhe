package mohaamadreza.saemipour.no.vazheh.database

import mohaamadreza.saemipour.no.vazheh.database.tables.Categories
import mohaamadreza.saemipour.no.vazheh.database.tables.Words
import org.jetbrains.exposed.sql.deleteAll
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
     * Clears existing data and reseeds every time
     */
    fun seed() {
        transaction {
            // Clear existing data first (Words must be deleted before Categories due to foreign key)
            println("🗑️ Clearing existing data...")
            Words.deleteAll()
            Categories.deleteAll()
            
            println("🌱 Seeding database with fresh data...")
            
            // Seed categories
            val fruitsId = seedCategory("میوه‌ها", "Fruits", 1, "https://ik.imagekit.io/mhmdrzsaemi/Fruits.png")
            val vegetablesId = seedCategory("سبزیجات", "Vegetables", 2, "https://ik.imagekit.io/mhmdrzsaemi/Vegetables.png")
            val foodId = seedCategory("غذاها", "Food", 3, "https://ik.imagekit.io/mhmdrzsaemi/Foods.png")
            val animalsId = seedCategory("حیوانات", "Animals", 4, "https://ik.imagekit.io/mhmdrzsaemi/Animals.png")
            val carsId = seedCategory("ماشیــــن ها", "Cars", 5, "https://ik.imagekit.io/mhmdrzsaemi/Cars.png")
            val vehiclesId = seedCategory("وسایل نقلیه", "Transportations", 6, "https://ik.imagekit.io/mhmdrzsaemi/Transportations.png")
            val colorsId = seedCategory("رنگ‌ها", "Colors", 7, "https://ik.imagekit.io/mhmdrzsaemi/Colors.png")

            val numbersId = seedCategory("اعداد", "Numbers", 7)
            val familyId = seedCategory("خانواده", "Family", 9)
            val clothesId = seedCategory("لباس‌ ها", "Clothes", 10)
            val shapesId = seedCategory("اشکال", "Shapes", 11)
            
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
            
            // Seed vegetables - سبزیجات
            seedWord(vegetablesId, "هویج", "Carrot", 1)
            seedWord(vegetablesId, "سیب زمینی", "Potato", 2)
            seedWord(vegetablesId, "گوجه فرنگی", "Tomato", 3)
            seedWord(vegetablesId, "پیاز", "Onion", 4)
            seedWord(vegetablesId, "خیار", "Cucumber", 5)
            seedWord(vegetablesId, "کاهو", "Lettuce", 6)
            seedWord(vegetablesId, "فلفل", "Pepper", 7)
            seedWord(vegetablesId, "بادمجان", "Eggplant", 8)
            seedWord(vegetablesId, "کدو", "Zucchini", 9)
            seedWord(vegetablesId, "اسفناج", "Spinach", 10)
            
            // Seed Iranian cars - ماشین‌های ایرانی
            seedWord(carsId, "پراید", "Pride", 1)
            seedWord(carsId, "پژو ۴۰۵", "Peugeot 405", 2)
            seedWord(carsId, "پژو ۲۰۶", "Peugeot 206", 3)
            seedWord(carsId, "سمند", "Samand", 4)
            seedWord(carsId, "تیبا", "Tiba", 5)
            seedWord(carsId, "پیکان", "Paykan", 6)
            seedWord(carsId, "دنا", "Dena", 7)
            seedWord(carsId, "رانا", "Runna", 8)
            seedWord(carsId, "کوییک", "Quick", 9)
            seedWord(carsId, "شاهین", "Shahin", 10)
            seedWord(carsId, "ساینا", "Saina", 11)
            
            println("✅ Database seeded successfully with ${Words.selectAll().count()} words in ${Categories.selectAll().count()} categories!")
        }
    }
    
    private fun seedCategory(nameFa: String, nameEn: String, order: Int, imageUrl: String? = null): Int {
        return Categories.insert {
            it[Categories.nameFa] = nameFa
            it[Categories.nameEn] = nameEn
            it[displayOrder] = order
            it[createdAt] = LocalDateTime.now()
            it[Categories.iconUrl] = imageUrl
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
