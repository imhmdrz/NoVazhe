package mohaamadreza.saemipour.no.vazheh.database

import mohaamadreza.saemipour.no.vazheh.database.tables.*
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

            val numbersId = seedCategory("اعداد", "Numbers", 7)
            val familyId = seedCategory("خانواده", "Family", 9)
            val clothesId = seedCategory("لباس‌ ها", "Clothes", 10)
            val shapesId = seedCategory("اشکال", "Shapes", 11)
            
            // Seed fruits - میوه‌ها (with audio and images for quiz)
            seedWord(fruitsId, "سیب", "Apple", 1, 
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/apple.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/apple.mp3")
            seedWord(fruitsId, "موز", "Banana", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/banana.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/banana.mp3")
            seedWord(fruitsId, "پرتقال", "Orange", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/orange.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/orange.mp3")
            seedWord(fruitsId, "انگور", "Grape", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/grape.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/grape.mp3")
            seedWord(fruitsId, "توت فرنگی", "Strawberry", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/strawberry.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/strawberry.mp3")
            seedWord(fruitsId, "هندوانه", "Watermelon", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/watermelon.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/watermelon.mp3")
            seedWord(fruitsId, "گیلاس", "Cherry", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/cherry.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/cherry.mp3")
            seedWord(fruitsId, "آناناس", "Pineapple", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/pineapple.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/pineapple.mp3")
            seedWord(fruitsId, "گلابی", "Pear", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/pear.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/pear.mp3")
            seedWord(fruitsId, "هلو", "Peach", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/words/fruits/peach.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/fruits/peach.mp3")
            
            // Seed animals - حیوانات (with audio and images for quiz)
            seedWord(animalsId, "سگ", "Dog", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/dog.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/dog.mp3")
            seedWord(animalsId, "گربه", "Cat", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/cat.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/cat.mp3")
            seedWord(animalsId, "پرنده", "Bird", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/bird.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/bird.mp3")
            seedWord(animalsId, "ماهی", "Fish", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/fish.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/fish.mp3")
            seedWord(animalsId, "فیل", "Elephant", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/elephant.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/elephant.mp3")
            seedWord(animalsId, "شیر", "Lion", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/lion.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/lion.mp3")
            seedWord(animalsId, "خرگوش", "Rabbit", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/rabbit.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/rabbit.mp3")
            seedWord(animalsId, "زرافه", "Giraffe", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/giraffe.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/giraffe.mp3")
            seedWord(animalsId, "میمون", "Monkey", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/monkey.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/monkey.mp3")
            seedWord(animalsId, "پروانه", "Butterfly", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/butterfly.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/butterfly.mp3")
            seedWord(animalsId, "اسب", "Horse", 11,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/horse.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/horse.mp3")
            seedWord(animalsId, "گاو", "Cow", 12,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/cow.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/cow.mp3")
            seedWord(animalsId, "مرغ", "Chicken", 13,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/chicken.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/chicken.mp3")
            seedWord(animalsId, "اردک", "Duck", 14,
                "https://ik.imagekit.io/mhmdrzsaemi/words/animals/duck.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/animals/duck.mp3")
            
            // Seed colors - رنگ‌ها (with audio and images for quiz)
            seedWord(colorsId, "قرمز", "Red", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/red.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/red.mp3")
            seedWord(colorsId, "آبی", "Blue", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/blue.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/blue.mp3")
            seedWord(colorsId, "سبز", "Green", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/green.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/green.mp3")
            seedWord(colorsId, "زرد", "Yellow", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/yellow.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/yellow.mp3")
            seedWord(colorsId, "نارنجی", "Orange", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/orange.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/orange.mp3")
            seedWord(colorsId, "بنفش", "Purple", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/purple.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/purple.mp3")
            seedWord(colorsId, "صورتی", "Pink", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/pink.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/pink.mp3")
            seedWord(colorsId, "سفید", "White", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/white.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/white.mp3")
            seedWord(colorsId, "سیاه", "Black", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/black.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/black.mp3")
            seedWord(colorsId, "قهوه‌ای", "Brown", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/words/colors/brown.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/colors/brown.mp3")
            
            // Seed numbers - اعداد (with audio and images for quiz)
            seedWord(numbersId, "یک", "One", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/one.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/one.mp3")
            seedWord(numbersId, "دو", "Two", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/two.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/two.mp3")
            seedWord(numbersId, "سه", "Three", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/three.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/three.mp3")
            seedWord(numbersId, "چهار", "Four", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/four.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/four.mp3")
            seedWord(numbersId, "پنج", "Five", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/five.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/five.mp3")
            seedWord(numbersId, "شش", "Six", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/six.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/six.mp3")
            seedWord(numbersId, "هفت", "Seven", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/seven.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/seven.mp3")
            seedWord(numbersId, "هشت", "Eight", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/eight.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/eight.mp3")
            seedWord(numbersId, "نه", "Nine", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/nine.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/nine.mp3")
            seedWord(numbersId, "ده", "Ten", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/words/numbers/ten.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/numbers/ten.mp3")

            // Seed vehicles - وسایل نقلیه (with audio and images for quiz)
            seedWord(vehiclesId, "ماشین", "Car", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/car.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/car.mp3")
            seedWord(vehiclesId, "اتوبوس", "Bus", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/bus.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/bus.mp3")
            seedWord(vehiclesId, "قطار", "Train", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/train.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/train.mp3")
            seedWord(vehiclesId, "هواپیما", "Airplane", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/airplane.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/airplane.mp3")
            seedWord(vehiclesId, "دوچرخه", "Bicycle", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/bicycle.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/bicycle.mp3")
            seedWord(vehiclesId, "کشتی", "Ship", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/ship.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/ship.mp3")
            seedWord(vehiclesId, "موتور", "Motorcycle", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/motorcycle.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/motorcycle.mp3")
            seedWord(vehiclesId, "کامیون", "Truck", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/truck.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/truck.mp3")
            seedWord(vehiclesId, "تاکسی", "Taxi", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/taxi.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/taxi.mp3")
            seedWord(vehiclesId, "آمبولانس", "Ambulance", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vehicles/ambulance.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vehicles/ambulance.mp3")
            
            // Seed family - خانواده (with audio and images for quiz)
            seedWord(familyId, "مادر", "Mother", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/mother.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/mother.mp3")
            seedWord(familyId, "پدر", "Father", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/father.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/father.mp3")
            seedWord(familyId, "خواهر", "Sister", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/sister.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/sister.mp3")
            seedWord(familyId, "برادر", "Brother", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/brother.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/brother.mp3")
            seedWord(familyId, "مادربزرگ", "Grandmother", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/grandmother.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/grandmother.mp3")
            seedWord(familyId, "پدربزرگ", "Grandfather", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/grandfather.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/grandfather.mp3")
            seedWord(familyId, "عمو", "Uncle", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/uncle.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/uncle.mp3")
            seedWord(familyId, "خاله", "Aunt", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/aunt.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/aunt.mp3")
            seedWord(familyId, "بچه", "Baby", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/family/baby.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/family/baby.mp3")
            
            // Seed clothes - لباس‌ها (with audio and images for quiz)
            seedWord(clothesId, "پیراهن", "Shirt", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/shirt.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/shirt.mp3")
            seedWord(clothesId, "شلوار", "Pants", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/pants.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/pants.mp3")
            seedWord(clothesId, "کفش", "Shoes", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/shoes.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/shoes.mp3")
            seedWord(clothesId, "کلاه", "Hat", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/hat.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/hat.mp3")
            seedWord(clothesId, "جوراب", "Socks", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/socks.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/socks.mp3")
            seedWord(clothesId, "ژاکت", "Jacket", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/jacket.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/jacket.mp3")
            seedWord(clothesId, "دامن", "Skirt", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/skirt.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/skirt.mp3")
            seedWord(clothesId, "لباس", "Dress", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/clothes/dress.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/clothes/dress.mp3")
            
            // Seed food - غذاها (with audio and images for quiz)
            seedWord(foodId, "نان", "Bread", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/bread.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/bread.mp3")
            seedWord(foodId, "آب", "Water", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/water.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/water.mp3")
            seedWord(foodId, "شیر", "Milk", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/milk.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/milk.mp3")
            seedWord(foodId, "تخم مرغ", "Egg", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/egg.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/egg.mp3")
            seedWord(foodId, "برنج", "Rice", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/rice.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/rice.mp3")
            seedWord(foodId, "مرغ", "Chicken", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/chicken.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/chicken.mp3")
            seedWord(foodId, "ماست", "Yogurt", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/yogurt.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/yogurt.mp3")
            seedWord(foodId, "پنیر", "Cheese", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/food/cheese.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/food/cheese.mp3")
            
            // Seed shapes - اشکال (with audio and images for quiz)
            seedWord(shapesId, "دایره", "Circle", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/shapes/circle.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/shapes/circle.mp3")
            seedWord(shapesId, "مربع", "Square", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/shapes/square.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/shapes/square.mp3")
            seedWord(shapesId, "مثلث", "Triangle", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/shapes/triangle.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/shapes/triangle.mp3")
            seedWord(shapesId, "مستطیل", "Rectangle", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/shapes/rectangle.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/shapes/rectangle.mp3")
            seedWord(shapesId, "ستاره", "Star", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/shapes/star.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/shapes/star.mp3")
            seedWord(shapesId, "قلب", "Heart", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/shapes/heart.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/shapes/heart.mp3")
            
            // Seed vegetables - سبزیجات (with audio and images for quiz)
            seedWord(vegetablesId, "هویج", "Carrot", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/carrot.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/carrot.mp3")
            seedWord(vegetablesId, "سیب زمینی", "Potato", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/potato.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/potato.mp3")
            seedWord(vegetablesId, "گوجه فرنگی", "Tomato", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/tomato.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/tomato.mp3")
            seedWord(vegetablesId, "پیاز", "Onion", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/onion.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/onion.mp3")
            seedWord(vegetablesId, "خیار", "Cucumber", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/cucumber.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/cucumber.mp3")
            seedWord(vegetablesId, "کاهو", "Lettuce", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/lettuce.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/lettuce.mp3")
            seedWord(vegetablesId, "فلفل", "Pepper", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/pepper.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/pepper.mp3")
            seedWord(vegetablesId, "بادمجان", "Eggplant", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/eggplant.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/eggplant.mp3")
            seedWord(vegetablesId, "کدو", "Zucchini", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/zucchini.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/zucchini.mp3")
            seedWord(vegetablesId, "اسفناج", "Spinach", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/words/vegetables/spinach.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/vegetables/spinach.mp3")
            
            // Seed Iranian cars - ماشین‌های ایرانی (with audio and images for quiz)
            seedWord(carsId, "پراید", "Pride", 1,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/pride.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/pride.mp3")
            seedWord(carsId, "پژو ۴۰۵", "Peugeot 405", 2,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/peugeot405.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/peugeot405.mp3")
            seedWord(carsId, "پژو ۲۰۶", "Peugeot 206", 3,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/peugeot206.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/peugeot206.mp3")
            seedWord(carsId, "سمند", "Samand", 4,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/samand.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/samand.mp3")
            seedWord(carsId, "تیبا", "Tiba", 5,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/tiba.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/tiba.mp3")
            seedWord(carsId, "پیکان", "Paykan", 6,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/paykan.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/paykan.mp3")
            seedWord(carsId, "دنا", "Dena", 7,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/dena.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/dena.mp3")
            seedWord(carsId, "رانا", "Runna", 8,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/runna.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/runna.mp3")
            seedWord(carsId, "کوییک", "Quick", 9,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/quick.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/quick.mp3")
            seedWord(carsId, "شاهین", "Shahin", 10,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/shahin.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/shahin.mp3")
            seedWord(carsId, "ساینا", "Saina", 11,
                "https://ik.imagekit.io/mhmdrzsaemi/words/cars/saina.png",
                "https://ik.imagekit.io/mhmdrzsaemi/audio/cars/saina.mp3")
            
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
