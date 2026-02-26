package mohaamadreza.saemipour.no.vazheh.database

import mohaamadreza.saemipour.no.vazheh.database.tables.Categories
import mohaamadreza.saemipour.no.vazheh.database.tables.ChildProgress
import mohaamadreza.saemipour.no.vazheh.database.tables.Children
import mohaamadreza.saemipour.no.vazheh.database.tables.Parents
import mohaamadreza.saemipour.no.vazheh.database.tables.QuizAttempts
import mohaamadreza.saemipour.no.vazheh.database.tables.Words
import mohaamadreza.saemipour.no.vazheh.security.PasswordUtils
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
            val numbersId = seedCategory("اعداد", "Numbers", 8, "https://ik.imagekit.io/mhmdrzsaemi/Numbers.png")
            val clothesId = seedCategory("لباس‌ ها", "Clothes", 9, "https://ik.imagekit.io/mhmdrzsaemi/Clothes.png")
            val shapesId = seedCategory("اشکال", "Shapes", 10, "https://ik.imagekit.io/mhmdrzsaemi/Shapes.png")
            
            // Seed fruits - میوه‌ها (with audio and images for quiz)
            seedWord(fruitsId, "سیب", "Apple", 1, 
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/LS20260226133347.png",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/apple.mp3")
            seedWord(fruitsId, "موز", "Banana", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771614242153.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/Banana.mp3")
            seedWord(fruitsId, "پرتقال", "Orange", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771614366888.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/Orange.mp3")
            seedWord(fruitsId, "انگور", "Grape", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771614560368.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/grape.mp3")
            seedWord(fruitsId, "توت فرنگی", "Strawberry", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771614726317.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/Strawberries.mp3")
            seedWord(fruitsId, "هندوانه", "Watermelon", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771614787434.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/Watermelon.mp3")
            seedWord(fruitsId, "گیلاس", "Cherry", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771615008487.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/Cherry.mp3")
            seedWord(fruitsId, "آناناس", "Pineapple", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771614957258.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/Pineapple.mp3")
            seedWord(fruitsId, "گلابی", "Pear", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771615064123.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/Pear.mp3")
            seedWord(fruitsId, "هلو", "Peach", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/images/froots/copilot_image_1771615154913.jpeg",
                "https://ik.imagekit.io/mhmdrzsaemi/miveee/novazh/peach.mp3")
            
            // Seed animals - حیوانات (with audio and images for quiz)
            seedWord(animalsId, "سگ", "Dog", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/copilot_image_1771615186461.jpeg?updatedAt=1772047645392",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D8%B3%DA%AF.mp3")
            seedWord(animalsId, "گربه", "Cat", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/cat_ihPvLZiM8.png?updatedAt=1772046847603",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%DA%AF%D8%B1%D8%A8%D9%87.mp3")
            seedWord(animalsId, "پرنده", "Bird", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/bird_g02dq3aPa.png?updatedAt=1772046847599",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D9%BE%D8%B1%D9%86%D8%AF%D9%87.mp3")
            seedWord(animalsId, "ماهی", "Fish", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/fish_Z4hXlRttF.png?updatedAt=1772046841071",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D9%85%D8%A7%D9%87%DB%8C.mp3")
            seedWord(animalsId, "فیل", "Elephant", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/ellefent_caSavzK5Np.png?updatedAt=1772046833001",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D9%81%DB%8C%D9%84.mp3")
            seedWord(animalsId, "شیر", "Lion", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/lion_TcmSAXB-P.png?updatedAt=1772046838020",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/lion.mp3")
            seedWord(animalsId, "خرگوش", "Rabbit", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/rabbit_xaUXlqDBC.png?updatedAt=1772046846556",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D8%AE%D8%B1%DA%AF%D9%88%D8%B4.mp3")
            seedWord(animalsId, "زرافه", "Giraffe", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/girrafe_6pslf4oiv.png?updatedAt=1772046846298",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D8%B2%D8%B1%D8%A7%D9%81%D9%87.mp3")
            seedWord(animalsId, "میمون", "Monkey", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/images/monkey_owv1FAeOe.png?updatedAt=1772046847143",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D9%85%DB%8C%D9%85%D9%88%D9%86.mp3")
            seedWord(animalsId, "پروانه", "Butterfly", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/images/butterfly_kR6aT7Qaoh.png?updatedAt=1772046848282",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D9%BE%D8%B1%D9%88%D8%A7%D9%86%D9%87.mp3")
            seedWord(animalsId, "اسب", "Horse", 11,
                "https://ik.imagekit.io/mhmdrzsaemi/images/horse_4Dd9AqrE2.png?updatedAt=1772046845423",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D8%A7%D8%B3%D8%A8.mp3")
            seedWord(animalsId, "گاو", "Cow", 12,
                "https://ik.imagekit.io/mhmdrzsaemi/images/cow_d1YmKBE_E.png?updatedAt=1772046847302",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%DA%AF%D8%A7%D9%88.mp3")
            seedWord(animalsId, "مرغ", "Chicken", 13,
                "https://ik.imagekit.io/mhmdrzsaemi/images/chicken_NErE8ENczH.png?updatedAt=1772046848261",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D9%85%D8%B1%D8%BA.mp3")
            seedWord(animalsId, "اردک", "Duck", 14,
                "https://ik.imagekit.io/mhmdrzsaemi/images/ord_mQTy11zYUt.png?updatedAt=1772046845858",
                "https://ik.imagekit.io/mhmdrzsaemi/animal/novazh/%D8%A7%D8%B1%D8%AF%DA%A9.mp3")
            
            // Seed colors - رنگ‌ها (with audio and images for quiz)
            seedWord(colorsId, "قرمز", "Red", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng.com-red-paint-splash-png-3161x2531.png?updatedAt=1772046964028",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D9%82%D8%B1%D9%85%D8%B2.mp3")
            seedWord(colorsId, "آبی", "Blue", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng.com-blue-paint-splash-png-541x527.png?updatedAt=1772046965047",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%A7%D9%93%D8%A8%DB%8C.mp3")
            seedWord(colorsId, "سبز", "Green", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng.com-free-download-green-brush-stroke-680x302.png?updatedAt=1772046965068",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%B3%D8%A8%D8%B2.mp3")
            seedWord(colorsId, "زرد", "Yellow", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng_com_brush_stroke_png_transparent_vol_onlygfx_orange_paint.png?updatedAt=1772046965045",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%B2%D8%B1%D8%AF.mp3")
            seedWord(colorsId, "بنفش", "Purple", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng_com_clipart_transparent_library_watercolor_painting_drop.png?updatedAt=1772046964946",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%A8%D9%86%D9%81%D8%B4.mp3")
            seedWord(colorsId, "صورتی", "Pink", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng.com-24-pink-paint-brush-stroke-onlygfx-drawi-881x309.png?updatedAt=1772046963836",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%B5%D9%88%D8%B1%D8%AA%DB%8C.mp3")
            seedWord(colorsId, "سفید", "White", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng_com_white_paint_splatter_clip_white_paint_splat_clipart_600x387.png?updatedAt=1772046963160",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%B3%D9%81%DB%8C%D8%AF.mp3")
            seedWord(colorsId, "سیاه", "Black", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng_com_splatter_transparent_pictures_free_black_paint_splash.png?updatedAt=1772046963016",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%B3%DB%8C%D8%A7%D9%87.mp3")
            seedWord(colorsId, "قهوه‌ای", "Brown", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/images/colors/toppng.com-dirt-png-298x300.png?updatedAt=1772046965069",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D9%82%D9%87%D9%88%D9%87%D8%A7%DB%8C.mp3")
            
            // Seed numbers - اعداد (with audio and images for quiz)
            seedWord(numbersId, "یک", "One", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-24-32-595.png?updatedAt=1772047554223",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%DB%8C%DA%A9.mp3")
            seedWord(numbersId, "دو", "Two", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-25-23-111.png?updatedAt=1772047554200",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%AF%D9%88.mp3")
            seedWord(numbersId, "سه", "Three", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-25-43-173.png?updatedAt=1772047554385",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%B3%D9%87.mp3")
            seedWord(numbersId, "چهار", "Four", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-29-56-013.png?updatedAt=1772047554090",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%DA%86%D9%87%D8%A7%D8%B1.mp3")
            seedWord(numbersId, "پنج", "Five", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-28-10-799.png?updatedAt=1772047554558",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D9%BE%D9%86%D8%AC.mp3")
            seedWord(numbersId, "شش", "Six", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-28-33-580.png?updatedAt=1772047622611",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%B4%D8%B4.mp3")
            seedWord(numbersId, "هفت", "Seven", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-28-44-705.png?updatedAt=1772047554373",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D9%87%D9%81%D8%AA.mp3")
            seedWord(numbersId, "هشت", "Eight", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-30-07-783.png?updatedAt=1772047554374",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D9%87%D8%B4%D8%AA.mp3")
            seedWord(numbersId, "نه", "Nine", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-30-15-851.png?updatedAt=1772047554400",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D9%86%D9%87.mp3")
            seedWord(numbersId, "ده", "Ten", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/images/numbers/Picsart_26-02-21_01-30-27-106.png?updatedAt=1772047554346",
                "https://ik.imagekit.io/mhmdrzsaemi/numbers/novazh/%D8%AF%D9%87.mp3")

            // Seed vehicles - وسایل نقلیه (with audio and images for quiz)
            seedWord(vehiclesId, "ماشین", "Car", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/IMG_2558.PNG?updatedAt=1772047838349",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D9%85%D8%A7%D8%B4%DB%8C%D9%86.mp3")
            seedWord(vehiclesId, "اتوبوس", "Bus", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/IMG_2559.PNG?updatedAt=1772047838034",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D8%A7%D8%AA%D9%88%D8%A8%D9%88%D8%B3.mp3")
            seedWord(vehiclesId, "قطار", "Train", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/IMG_2564.PNG?updatedAt=1772047839874",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/train.mp3")
            seedWord(vehiclesId, "هواپیما", "Airplane", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771870932019.jpeg?updatedAt=1772047837194",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D9%87%D9%88%D8%A7%D9%BE%DB%8C%D9%85%D8%A7.mp3")
            seedWord(vehiclesId, "دوچرخه", "Bicycle", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771870992116.jpeg?updatedAt=1772047836739",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D8%AF%D9%88%DA%86%D8%B1%D8%AE%D9%87.mp3")
            seedWord(vehiclesId, "کشتی", "Ship", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771871119226.jpeg?updatedAt=1772047837954",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%DA%A9%D8%B4%D8%AA%DB%8C.mp3")
            seedWord(vehiclesId, "موتور", "Motorcycle", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771870992116.jpeg?updatedAt=1772047836739",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D9%85%D9%88%D8%AA%D9%88%D8%B1.mp3")
            seedWord(vehiclesId, "کامیون", "Truck", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771871838589.jpeg?updatedAt=1772047838215",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%DA%A9%D8%A7%D9%85%DB%8C%D9%88%D9%86.mp3")
            seedWord(vehiclesId, "تاکسی", "Taxi", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/Picsart_26-02-23_22-54-17-536.png?updatedAt=1772047835510",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D8%AA%D8%A7%DA%A9%D8%B3%DB%8C.mp3")
            seedWord(vehiclesId, "آمبولانس", "Ambulance", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771874835259.jpeg?updatedAt=1772047838941",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D8%A7%D9%93%D9%85%D8%A8%D9%88%D9%84%D8%A7%D9%86%D8%B3.mp3")

            // Seed clothes - لباس‌ها (with audio and images for quiz)
            seedWord(clothesId, "پیراهن", "Shirt", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771875169500.jpeg?updatedAt=1772047841764",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D9%BE%DB%8C%D8%B1%D8%A7%D9%87%D9%86.mp3")
            seedWord(clothesId, "شلوار", "Pants", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771875242331.jpeg?updatedAt=1772047843139",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D8%B4%D9%84%D9%88%D8%A7%D8%B1.mp3")
            seedWord(clothesId, "کفش", "Shoes", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771875342885.jpeg?updatedAt=1772047837600",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%DA%A9%D9%81%D8%B4.mp3")
            seedWord(clothesId, "کلاه", "Hat", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/Picsart_26-02-23_23-07-40-804.png?updatedAt=1772047832979",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%DA%A9%D9%84%D8%A7%D9%87.mp3")
            seedWord(clothesId, "جوراب", "Socks", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771875665201.jpeg?updatedAt=1772047837995",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D8%AC%D9%88%D8%B1%D8%A7%D8%A8.mp3")
            seedWord(clothesId, "ژاکت", "Jacket", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1771875754983.jpeg?updatedAt=1772047840868",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%DA%98%D8%A7%DA%A9%D8%AA.mp3")
            seedWord(clothesId, "دامن", "Skirt", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/travl/copilot_image_1772016387641.jpeg?updatedAt=1772047837430",
                "https://ik.imagekit.io/mhmdrzsaemi/fooood/novazh/%D8%AF%D8%A7%D9%85%D9%86.mp3")
            
            // Seed food - غذاها (with audio and images for quiz)
            seedWord(foodId, "نان", "Bread", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772016528431.jpeg?updatedAt=1772100136571",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%86%D8%A7%D9%86.mp3")
            seedWord(foodId, "آب", "Water", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772016811788.jpeg?updatedAt=1772100134534",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%A7%D9%93%D8%A8.mp3")
            seedWord(foodId, "شیر", "Milk", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772017053178.jpeg?updatedAt=1772100133355",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%B4%DB%8C%D8%B1.mp3")
            seedWord(foodId, "تخم مرغ", "Egg", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772017058945.jpeg?updatedAt=1772100135368",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%AA%D8%AE%D9%85%20%D9%85%D8%B1%D8%BA.mp3")
            seedWord(foodId, "برنج", "Rice", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/Picsart_26-02-25_14-54-12-512.png?updatedAt=1772100125694",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%A8%D8%B1%D9%86%D8%AC.mp3")
            seedWord(foodId, "مرغ", "Chicken", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/Picsart_26-02-25_14-52-42-474.png?updatedAt=1772100124358",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%85%D8%B1%D8%BA.mp3")
            seedWord(foodId, "ماست", "Yogurt", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/Picsart_26-02-25_14-53-36-006.png?updatedAt=1772100120910",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%85%D8%A7%D8%B3%D8%AA.mp3")
            seedWord(foodId, "پنیر", "Cheese", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/Picsart_26-02-25_14-53-00-969.png?updatedAt=1772100122349",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%BE%D9%86%DB%8C%D8%B1.mp3?updatedAt=1771347060581")
            
            // Seed shapes - اشکال (with audio and images for quiz)
            seedWord(shapesId, "دایره", "Circle", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772018795274.jpeg?updatedAt=1772100134764",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%AF%D8%A7%DB%8C%D8%B1%D9%87.mp3?updatedAt=1771347060552")
            seedWord(shapesId, "مربع", "Square", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772018858058.jpeg?updatedAt=1772100134005",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%85%D8%B1%D8%A8%D8%B9.mp3?updatedAt=1771347061224")
            seedWord(shapesId, "مثلث", "Triangle", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772018904127.jpeg?updatedAt=1772100135327",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%85%D8%AB%D9%84%D8%AB.mp3?updatedAt=1771347061207")
            seedWord(shapesId, "مستطیل", "Rectangle", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020302893.jpeg?updatedAt=1772100133811",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%85%D8%B3%D8%AA%D8%B7%DB%8C%D9%84.mp3?updatedAt=1771347061239")

            // Seed vegetables - سبزیجات (with audio and images for quiz)
            seedWord(vegetablesId, "هویج", "Carrot", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020463294.jpeg?updatedAt=1772100133963",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%87%D9%88%DB%8C%D8%AC.mp3?updatedAt=1771347060955")
            seedWord(vegetablesId, "سیب زمینی", "Potato", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020506085.jpeg?updatedAt=1772100134818",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%B3%DB%8C%D8%A8%20%D8%B2%D9%85%DB%8C%D9%86%DB%8C.mp3?updatedAt=1771347061126")
            seedWord(vegetablesId, "گوجه فرنگی", "Tomato", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020556368.jpeg?updatedAt=1772100135607",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%DA%AF%D9%88%D8%AC%D9%87%20%D9%81%D8%B1%D9%86%DA%AF%DB%8C.mp3?updatedAt=1771347060571")
            seedWord(vegetablesId, "پیاز", "Onion", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020603566.jpeg?updatedAt=1772100132410",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%BE%DB%8C%D8%A7%D8%B2.mp3?updatedAt=1771347060964")
            seedWord(vegetablesId, "خیار", "Cucumber", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020696570.jpeg?updatedAt=1772100135057",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%AE%DB%8C%D8%A7%D8%B1.mp3?updatedAt=1771347060584")
            seedWord(vegetablesId, "کاهو", "Lettuce", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020859107.jpeg?updatedAt=1772100135254",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%DA%A9%D8%A7%D9%87%D9%88.mp3?updatedAt=1771347061171")
            seedWord(vegetablesId, "فلفل", "Pepper", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772020939175.jpeg?updatedAt=1772100132626",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D9%81%D9%84%D9%81%D9%84.mp3?updatedAt=1771347061182")
            seedWord(vegetablesId, "بادمجان", "Eggplant", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772021356961.jpeg?updatedAt=1772100129404",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%A8%D8%A7%D8%AF%D9%85%D8%AC%D8%A7%D9%86.mp3?updatedAt=1771347061150")
            seedWord(vegetablesId, "کدو", "Zucchini", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772021443701.jpeg?updatedAt=1772100132886",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%DA%A9%D8%AF%D9%88.mp3?updatedAt=1771347060974")
            seedWord(vegetablesId, "اسفناج", "Spinach", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/images/foods/copilot_image_1772021584124.jpeg?updatedAt=1772100134775",
                "https://ik.imagekit.io/mhmdrzsaemi/some/novazh/%D8%A7%D8%B3%D9%81%D9%86%D8%A7%D8%AC.mp3?updatedAt=1771347061136")
            
            // Seed Iranian cars - ماشین‌های ایرانی (with audio and images for quiz)
            seedWord(carsId, "پراید", "Pride", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/pngpraid.parspng.com-5.png?updatedAt=1772100186388",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D9%BE%D8%B1%D8%A7%DB%8C%D8%AF.mp3")
            seedWord(carsId, "پژو ۴۰۵", "Peugeot 405", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/LS20260225163620.png?updatedAt=1772100187070",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D9%BE%DA%98%D9%88%20%DB%B4%DB%B0%DB%B5.mp3")
            seedWord(carsId, "پژو ۲۰۶", "Peugeot 206", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/LS20260225163509.png?updatedAt=1772100185601",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D9%BE%DA%98%D9%88%20%DB%B2%DB%B0%DB%B6.mp3")
            seedWord(carsId, "سمند", "Samand", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/copilot_image_1772022513889.jpeg?updatedAt=1772100200808",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D8%B3%D9%85%D9%86%D8%AF.mp3")
            seedWord(carsId, "تیبا", "Tiba", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/copilot_image_1772022606609.jpeg?updatedAt=1772100201097",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D9%BE%DA%98%D9%88%20%DB%B4%DB%B0%DB%B5.mp3")
            seedWord(carsId, "پیکان", "Paykan", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/copilot_image_1772023778338.jpeg?updatedAt=1772100203141",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D9%BE%DB%8C%DA%A9%D8%A7%D9%86.mp3")
            seedWord(carsId, "دنا", "Dena", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/copilot_image_1772023913259.jpeg?updatedAt=1772100200427",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D8%AF%D9%86%D8%A7.mp3")
            seedWord(carsId, "رانا", "Runna", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/copilot_image_1772024165371.jpeg?updatedAt=1772100201741",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D8%B1%D8%A7%D9%86%D8%A7.mp3")
            seedWord(carsId, "کوییک", "Quick", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/copilot_image_1772024263658.jpeg?updatedAt=1772100201621",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%DA%A9%D9%88%DB%8C%DB%8C%DA%A9.mp3")
            seedWord(carsId, "شاهین", "Shahin", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/LS20260225163417.png?updatedAt=1772100183120",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D8%B4%D8%A7%D9%87%DB%8C%D9%86.mp3")
            seedWord(carsId, "ساینا", "Saina", 11,
                "https://ik.imagekit.io/mhmdrzsaemi/images/car/LS20260225163649.png?updatedAt=1772100184650",
                "https://ik.imagekit.io/mhmdrzsaemi/cars/novazh/%D8%B3%D8%A7%DB%8C%D9%86%D8%A7.mp3")
            
            // ==================== Seed Quiz Data ====================
            println("🎯 Seeding quiz data...")
            
            // Clear quiz-related data
            QuizAttempts.deleteAll()
            ChildProgress.deleteAll()
            Children.deleteAll()
            Parents.deleteAll()
            
            // Create test parent
            val testParentId = seedParent("test", "test123", "مادر تست")
            
            // Create test children
            val aliId = seedChild(testParentId, "علی", 5, "BOY")
            val saraId = seedChild(testParentId, "سارا", 4, "GIRL")
            
            // Get word IDs for quiz attempts
            val fruitWords = Words.selectAll()
                .where { Words.categoryId eq fruitsId }
                .map { it[Words.id].value }
            
            val animalWords = Words.selectAll()
                .where { Words.categoryId eq animalsId }
                .map { it[Words.id].value }
            
            val colorWords = Words.selectAll()
                .where { Words.categoryId eq colorsId }
                .map { it[Words.id].value }
            
            // Seed quiz attempts for Ali (علی) - simulating quiz history
            println("📝 Adding quiz attempts for علی...")
            
            // Ali has attempted fruits quiz - mostly correct
            fruitWords.take(6).forEachIndexed { index, wordId ->
                val isCorrect = index != 2 // All correct except one
                seedQuizAttempt(aliId, wordId, isCorrect, (1500..3000).random())
                if (isCorrect) {
                    // Add more attempts to show learning
                    repeat(2) {
                        seedQuizAttempt(aliId, wordId, true, (1000..2500).random())
                    }
                }
            }
            
            // Ali has attempted animals quiz - learning in progress
            animalWords.take(8).forEachIndexed { index, wordId ->
                val isCorrect = index % 3 != 0 // Some mistakes
                seedQuizAttempt(aliId, wordId, isCorrect, (2000..4000).random())
                if (isCorrect && index < 4) {
                    seedQuizAttempt(aliId, wordId, true, (1500..3000).random())
                }
            }
            
            // Ali has attempted colors - just started
            colorWords.take(4).forEach { wordId ->
                seedQuizAttempt(aliId, wordId, true, (2500..4500).random())
            }
            
            // Seed quiz attempts for Sara (سارا) - beginner
            println("📝 Adding quiz attempts for سارا...")
            
            // Sara has attempted fewer quizzes
            fruitWords.take(4).forEachIndexed { index, wordId ->
                val isCorrect = index < 2
                seedQuizAttempt(saraId, wordId, isCorrect, (3000..5000).random())
            }
            
            colorWords.take(3).forEach { wordId ->
                seedQuizAttempt(saraId, wordId, true, (2500..4000).random())
            }
            
            // Seed child progress summaries
            println("📊 Creating progress summaries...")
            
            // Ali's progress - has learned some words
            seedChildProgress(aliId, fruitWords[0], 3, 3, true)   // سیب - learned!
            seedChildProgress(aliId, fruitWords[1], 3, 3, true)   // موز - learned!
            seedChildProgress(aliId, fruitWords[2], 0, 1, false)  // پرتقال - not learned
            seedChildProgress(aliId, fruitWords[3], 3, 4, true)   // انگور - learned!
            seedChildProgress(aliId, fruitWords[4], 2, 2, false)  // توت فرنگی - in progress
            seedChildProgress(aliId, fruitWords[5], 2, 3, false)  // هندوانه - in progress
            
            seedChildProgress(aliId, animalWords[0], 2, 3, false) // سگ
            seedChildProgress(aliId, animalWords[1], 2, 2, false) // گربه
            seedChildProgress(aliId, animalWords[2], 1, 2, false) // پرنده
            seedChildProgress(aliId, animalWords[3], 3, 3, true)  // ماهی - learned!
            
            seedChildProgress(aliId, colorWords[0], 1, 1, false)  // قرمز
            seedChildProgress(aliId, colorWords[1], 1, 1, false)  // آبی
            
            // Sara's progress - just started
            seedChildProgress(saraId, fruitWords[0], 1, 2, false) // سیب
            seedChildProgress(saraId, fruitWords[1], 1, 1, false) // موز
            seedChildProgress(saraId, colorWords[0], 1, 1, false) // قرمز
            
            println("✅ Database seeded successfully!")
            println("   📚 ${Words.selectAll().count()} words in ${Categories.selectAll().count()} categories")
            println("   👨‍👩‍👧‍👦 ${Parents.selectAll().count()} parents, ${Children.selectAll().count()} children")
            println("   🎯 ${QuizAttempts.selectAll().count()} quiz attempts")
            println("   📊 ${ChildProgress.selectAll().count()} progress records")
            println("")
            println("   🔐 Test login: username='test', password='test123'")
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
    
    private fun seedWord(
        categoryId: Int, 
        wordFa: String, 
        wordEn: String, 
        order: Int,
        imageUrl: String? = null,
        audioUrl: String? = null
    ) {
        Words.insert {
            it[Words.categoryId] = categoryId
            it[Words.wordFa] = wordFa
            it[Words.wordEn] = wordEn
            it[displayOrder] = order
            it[createdAt] = LocalDateTime.now()
            imageUrl?.let { url -> it[Words.imageUrl] = url }
            audioUrl?.let { url -> it[Words.audioUrl] = url }
        }
    }
    
    // ==================== Quiz-related seeders ====================
    
    private fun seedParent(username: String, password: String, displayName: String): Int {
        return Parents.insert {
            it[Parents.username] = username
            it[Parents.passwordHash] = PasswordUtils.hashPassword(password)
            it[Parents.displayName] = displayName
            it[Parents.isActive] = true
            it[Parents.createdAt] = LocalDateTime.now()
            it[Parents.updatedAt] = LocalDateTime.now()
        }[Parents.id].value
    }
    
    private fun seedChild(parentId: Int, name: String, age: Int, gender: String): Int {
        return Children.insert {
            it[Children.parentId] = parentId
            it[Children.name] = name
            it[Children.age] = age
            it[Children.gender] = gender
            it[Children.isActive] = true
            it[Children.createdAt] = LocalDateTime.now()
            it[Children.updatedAt] = LocalDateTime.now()
        }[Children.id].value
    }
    
    private fun seedQuizAttempt(
        childId: Int, 
        wordId: Int, 
        isCorrect: Boolean, 
        responseTimeMs: Int
    ) {
        QuizAttempts.insert {
            it[QuizAttempts.childId] = childId
            it[QuizAttempts.wordId] = wordId
            it[QuizAttempts.isCorrect] = isCorrect
            it[QuizAttempts.responseTimeMs] = responseTimeMs
            it[QuizAttempts.attemptedAt] = LocalDateTime.now().minusMinutes((0..60).random().toLong())
        }
    }
    
    private fun seedChildProgress(
        childId: Int,
        wordId: Int,
        correctAttempts: Int,
        totalAttempts: Int,
        isLearned: Boolean
    ) {
        val now = LocalDateTime.now()
        ChildProgress.insert {
            it[ChildProgress.childId] = childId
            it[ChildProgress.wordId] = wordId
            it[ChildProgress.correctAttempts] = correctAttempts
            it[ChildProgress.totalAttempts] = totalAttempts
            it[ChildProgress.isLearned] = isLearned
            it[ChildProgress.lastAttemptAt] = now.minusMinutes((5..30).random().toLong())
            if (isLearned) {
                it[ChildProgress.learnedAt] = now.minusHours((1..24).random().toLong())
            }
            it[ChildProgress.createdAt] = now
            it[ChildProgress.updatedAt] = now
        }
    }
}
