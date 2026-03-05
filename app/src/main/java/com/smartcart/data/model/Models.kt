package com.smartcart.data.model

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "EN"), RU("ru", "RU"), KK("kk", "KZ")
}

data class User(
    val id: String,
    val name: String,
    val email: String = "",
    val sessionToken: String = "",
    val isLoyaltyMember: Boolean = true,
)

data class Product(
    val id: String,
    val nameEn: String,
    val nameRu: String,
    val nameKk: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isNew: Boolean = false,
    val barcode: String = "",
    val unit: String = "шт",
) {
    fun localizedName(lang: AppLanguage) = when (lang) {
        AppLanguage.RU -> nameRu
        AppLanguage.KK -> nameKk
        AppLanguage.EN -> nameEn
    }

    fun formattedPrice() = "${(price * 130).toInt()}₸"
    fun formattedPriceOld() = "${(price * 130 * 1.1).toInt()}₸"
}

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val addedByCamera: Boolean = false,
    val addedManually: Boolean = false,
) {
    val lineTotal get() = product.price * quantity
}

data class ShoppingListItem(
    val product: Product,
    val plannedQuantity: Int,
    val isInCart: Boolean = false,
    val inCartQuantity: Int = 0,
)

data class CartSession(
    val sessionId: String,
    val userId: String,
    val cartId: String,
    val items: List<CartItem>,
    val shoppingList: List<ShoppingListItem>,
    val startTime: Long,
    val userName: String = "",
)

data class QrSessionPayload(
    val sessionToken: String,
    val userId: String,
    val userName: String,
    val cartId: String,
    val shoppingListId: String,
)

data class Deal(
    val product: Product,
    val discount: Int,
    val originalPrice: Double,
    val expiresIn: String? = null,
) {
    val discountedPrice get() = originalPrice * (1 - discount / 100.0)
    fun formattedOriginal() = "${originalPrice.toInt()}₸"
    fun formattedDiscounted() = "${discountedPrice.toInt()}₸"
}

data class CategoryItem(
    val id: String,
    val nameEn: String,
    val nameRu: String,
    val nameKk: String,
    val emoji: String,
    val color: Long,
    val itemCount: Int,
) {
    fun localizedName(lang: AppLanguage) = when (lang) {
        AppLanguage.RU -> nameRu
        AppLanguage.KK -> nameKk
        AppLanguage.EN -> nameEn
    }
}

// Строки приложения (mirrors STRINGS from types.ts)
data class AppStrings(
    val loginTitle: String,
    val scanToLogin: String,
    val scanInstruction: String,
    val readyToScan: String,
    val scanning: String,
    val authenticated: String,
    val altLogin: String,
    val needHelp: String,
    val home: String,
    val topDeals: String,
    val categories: String,
    val favorites: String,
    val searchPlaceholder: String,
    val flashSale: String,
    val weeklySavers: String,
    val bannerSub: String,
    val shopNow: String,
    val trendingOffers: String,
    val viewAll: String,
    val yourCart: String,
    val scanItems: String,
    val checkout: String,
    val subtotal: String,
    val tax: String,
    val discounts: String,
    val total: String,
    val myShoppingList: String,
    val foundItems: String,
    val addItemSearch: String,
    val allItems: String,
    val produce: String,
    val dairyEggs: String,
    val bakery: String,
    val sortByAisle: String,
    val addAllToCart: String,
    val estimatedTotal: String,
    val itemsInCart: String,
    val checkoutNow: String,
    val autoCart: String,
    val cartOnline: String,
    val callAssistant: String,
    val yourItems: String,
    val paymentMethod: String,
    val creditCard: String,
    val cash: String,
    val tapToPay: String,
    val orderSummary: String,
    val vat: String,
    val memberDiscount: String,
    val completePurchase: String,
    val terms: String,
    val pointsEarned: String,
    val add: String,
    val navDeals: String,
    val navCats: String,
    val navSaved: String,
    val dealsHeader: String,
    val filterAll: String,
    val filter20: String,
    val filter30: String,
    val filter50: String,
    val filterNew: String,
    val expiringSoon: String,
    val catsHeader: String,
    val wishlistHeader: String,
    val emptyWishlist: String,
    val goToCatalog: String,
    val sortPriceUp: String,
    val sortPriceDown: String,
    val sortNew: String,
    val itemsSuffix: String,
)

val StringsEN = AppStrings(
    loginTitle = "Smart Shopping Cart", scanToLogin = "Scan to login",
    scanInstruction = "Open the Shopgram app on your phone to scan",
    readyToScan = "Ready to scan", scanning = "Scanning...", authenticated = "Authenticated",
    altLogin = "Alternative Login", needHelp = "Need help? Ask a store associate.",
    home = "Home", topDeals = "Top Deals", categories = "Categories", favorites = "Favorites",
    searchPlaceholder = "Search for products, brands, or categories...",
    flashSale = "FLASH SALE", weeklySavers = "Weekly\nSuper Savers",
    bannerSub = "Up to 50% Off Top Brands this week only!", shopNow = "Shop Now",
    trendingOffers = "Trending Offers", viewAll = "View All",
    yourCart = "Your Cart", scanItems = "Scan items to add automatically",
    checkout = "Checkout", subtotal = "Subtotal", tax = "Tax (8%)", discounts = "Discounts", total = "Total",
    myShoppingList = "My Shopping List", foundItems = "Found {n} items on your list",
    addItemSearch = "Add item or search...", allItems = "All Items",
    produce = "Produce", dairyEggs = "Dairy & Eggs", bakery = "Bakery",
    sortByAisle = "Sort by Aisle", addAllToCart = "Add all to cart",
    estimatedTotal = "Estimated Total", itemsInCart = "Items in Cart", checkoutNow = "Checkout Now",
    autoCart = "Auto Cart", cartOnline = "Cart Online", callAssistant = "Call Assistant",
    yourItems = "Your Items", paymentMethod = "PAYMENT METHOD",
    creditCard = "Credit Card", cash = "Cash", tapToPay = "Tap to Pay",
    orderSummary = "Order Summary", vat = "VAT (12%)", memberDiscount = "Member Discount",
    completePurchase = "Complete Purchase",
    terms = "By completing purchase you agree to our Terms of Service",
    pointsEarned = "You will earn 24 points with this purchase.", add = "Add",
    navDeals = "Deals", navCats = "Cats", navSaved = "Saved",
    dealsHeader = "Deals & Discounts", filterAll = "All", filter20 = "-20%", filter30 = "-30%", filter50 = "-50%", filterNew = "New",
    expiringSoon = "Ending soon ⏰",
    catsHeader = "Categories",
    wishlistHeader = "Saved", emptyWishlist = "No saved items", goToCatalog = "Go to catalog",
    sortPriceUp = "Price ↑", sortPriceDown = "Price ↓", sortNew = "New",
    itemsSuffix = " items"  // EN
)

val StringsRU = AppStrings(
    loginTitle = "Умная Корзина", scanToLogin = "Сканируйте для входа",
    scanInstruction = "Откройте приложение Shopgram для сканирования",
    readyToScan = "Готов к сканированию", scanning = "Сканирование...", authenticated = "Аутентифицирован",
    altLogin = "Альтернативный вход", needHelp = "Нужна помощь? Спросите сотрудника.",
    home = "Главная", topDeals = "Акции", categories = "Категории", favorites = "Избранное",
    searchPlaceholder = "Поиск продуктов, брендов или категорий...",
    flashSale = "РАСПРОДАЖА", weeklySavers = "Скидки\nНедели",
    bannerSub = "До 50% на топ бренды только на этой неделе!", shopNow = "Купить сейчас",
    trendingOffers = "Популярные предложения", viewAll = "Все",
    yourCart = "Ваша корзина", scanItems = "Сканируйте товары для добавления",
    checkout = "Оформить", subtotal = "Подытог", tax = "Налог (8%)", discounts = "Скидки", total = "Итого",
    myShoppingList = "Мой список покупок", foundItems = "Найдено {n} товаров в списке",
    addItemSearch = "Добавить или найти...", allItems = "Все товары",
    produce = "Овощи и фрукты", dairyEggs = "Молочные продукты", bakery = "Выпечка",
    sortByAisle = "По рядам", addAllToCart = "Добавить все",
    estimatedTotal = "Примерная сумма", itemsInCart = "Товаров в корзине", checkoutNow = "Оплатить",
    autoCart = "Авто-Корзина", cartOnline = "Корзина онлайн", callAssistant = "Позвать помощника",
    yourItems = "Ваши товары", paymentMethod = "СПОСОБ ОПЛАТЫ",
    creditCard = "Карта", cash = "Наличные", tapToPay = "Бесконтактно",
    orderSummary = "Сводка заказа", vat = "НДС (12%)", memberDiscount = "Скидка участника",
    completePurchase = "Завершить покупку",
    terms = "Завершая покупку, вы соглашаетесь с условиями",
    pointsEarned = "Вы получите 24 балла за эту покупку.", add = "Добавить",
    navDeals = "Акции", navCats = "Разделы", navSaved = "Избранное",
    dealsHeader = "Акции и скидки", filterAll = "Все", filter20 = "-20%", filter30 = "-30%", filter50 = "-50%", filterNew = "Новинки",
    expiringSoon = "Скоро закончится ⏰",
    catsHeader = "Категории",
    wishlistHeader = "Избранное", emptyWishlist = "Нет избранных товаров", goToCatalog = "Перейти в каталог",
    sortPriceUp = "По цене ↑", sortPriceDown = "По цене ↓", sortNew = "По новизне",
    itemsSuffix = " шт"
)

val StringsKK = AppStrings(
    loginTitle = "Ақылды Себет", scanToLogin = "Кіру үшін сканерлеңіз",
    scanInstruction = "Сканерлеу үшін Shopgram қолданбасын ашыңыз",
    readyToScan = "Сканерлеуге дайын", scanning = "Сканерлеу...", authenticated = "Расталды",
    altLogin = "Балама кіру", needHelp = "Көмек керек пе? Қызметкерден сұраңыз.",
    home = "Басты", topDeals = "Акциялар", categories = "Санаттар", favorites = "Таңдаулылар",
    searchPlaceholder = "Өнімдерді, брендтерді немесе санаттарды іздеу...",
    flashSale = "ЖАППАЙ САТЫЛЫМ", weeklySavers = "Апталық\nЖеңілдіктер",
    bannerSub = "Тек осы аптада үздік брендтерге 50% дейін жеңілдік!", shopNow = "Қазір алу",
    trendingOffers = "Танымал ұсыныстар", viewAll = "Барлығы",
    yourCart = "Сіздің себетіңіз", scanItems = "Қосу үшін тауарларды сканерлеңіз",
    checkout = "Төлеу", subtotal = "Барлығы", tax = "Салық (8%)", discounts = "Жеңілдіктер", total = "Жалпы",
    myShoppingList = "Менің сатып алу тізімім", foundItems = "Тізімде {n} тауар табылды",
    addItemSearch = "Қосу немесе іздеу...", allItems = "Барлық тауарлар",
    produce = "Жеміс-жидек", dairyEggs = "Сүт өнімдері", bakery = "Нан өнімдері",
    sortByAisle = "Қатар бойынша", addAllToCart = "Барлығын қосу",
    estimatedTotal = "Болжалды сома", itemsInCart = "Себеттегі заттар", checkoutNow = "Төлеу",
    autoCart = "Авто-Себет", cartOnline = "Себет онлайн", callAssistant = "Көмекшіні шақыру",
    yourItems = "Сіздің тауарларыңыз", paymentMethod = "ТӨЛЕМ ӘДІСІ",
    creditCard = "Карта", cash = "Қолма-қол", tapToPay = "Жанасу арқылы",
    orderSummary = "Тапсырыс қорытындысы", vat = "ҚҚС (12%)", memberDiscount = "Мүшелік жеңілдік",
    completePurchase = "Сатып алуды аяқтау",
    terms = "Сатып алуды аяқтау арқылы ережелермен келісесіз",
    pointsEarned = "Бұл сатып алудан 24 ұпай аласыз.", add = "Қосу",
    navDeals = "Ұсыныс", navCats = "Бөлімдер", navSaved = "Таңдаулы",
    dealsHeader = "Ұсыныстар", filterAll = "Барлығы", filter20 = "-20%", filter30 = "-30%", filter50 = "-50%", filterNew = "Жаңа",
    expiringSoon = "Жақында аяқталады ⏰",
    catsHeader = "Санаттар",
    wishlistHeader = "Таңдаулы", emptyWishlist = "Таңдаулы жоқ", goToCatalog = "Каталогқа",
    sortPriceUp = "Баға ↑", sortPriceDown = "Баға ↓", sortNew = "Жаңалық",
    itemsSuffix = " тауар"
)

fun AppLanguage.strings() = when (this) {
    AppLanguage.RU -> StringsRU
    AppLanguage.KK -> StringsKK
    AppLanguage.EN -> StringsEN
}

object MockData {
    val products = listOf(
        Product(
            id = "1",
            nameEn = "Organic Avocado",
            nameRu = "Авокадо органик",
            nameKk = "Органикалық Авокадо",
            price = 8.90,
            imageUrl = "https://picsum.photos/seed/avocado/300/300",
            category = "Fruits",
            isNew = false,
            barcode = "123456",
            unit = "шт",
        ),
        Product(
            id = "2",
            nameEn = "Whole Milk",
            nameRu = "Молоко цельное",
            nameKk = "Толық сүт",
            price = 4.50,
            imageUrl = "https://picsum.photos/seed/milk/300/300",
            category = "Dairy",
            isNew = false,
            barcode = "234567",
            unit = "л",
        ),
        Product(
            id = "3",
            nameEn = "Sourdough Bread",
            nameRu = "Хлеб на закваске",
            nameKk = "Ашыған нан",
            price = 6.50,
            imageUrl = "https://picsum.photos/seed/bread/300/300",
            category = "Bakery",
            isNew = false,
            barcode = "345678",
            unit = "шт",
        ),
        Product(
            id = "4",
            nameEn = "Greek Yogurt",
            nameRu = "Йогурт греческий",
            nameKk = "Грек йогурты",
            price = 3.80,
            imageUrl = "https://picsum.photos/seed/yogurt/300/300",
            category = "Dairy",
            isNew = false,
            barcode = "456789",
            unit = "шт",
        ),
        Product(
            id = "5",
            nameEn = "Chicken Breast",
            nameRu = "Куриная грудка",
            nameKk = "Тауық омырауы",
            price = 12.00,
            imageUrl = "https://picsum.photos/seed/chicken/300/300",
            category = "Meat",
            isNew = false,
            barcode = "567890",
            unit = "кг",
        ),
        Product(
            id = "6",
            nameEn = "Cherry Tomatoes",
            nameRu = "Помидоры черри",
            nameKk = "Шие қызанақтар",
            price = 5.20,
            imageUrl = "https://picsum.photos/seed/tomatoes/300/300",
            category = "Vegetables",
            isNew = false,
            barcode = "678901",
            unit = "уп",
        ),
        Product(
            id = "7",
            nameEn = "Orange Juice",
            nameRu = "Сок апельсиновый",
            nameKk = "Апельсин шырыны",
            price = 7.50,
            imageUrl = "https://picsum.photos/seed/juice/300/300",
            category = "Beverages",
            isNew = true,
            barcode = "789012",
            unit = "л",
        ),
        Product(
            id = "8",
            nameEn = "Pasta Barilla",
            nameRu = "Паста Барилла",
            nameKk = "Барилла пастасы",
            price = 4.20,
            imageUrl = "https://picsum.photos/seed/pasta/300/300",
            category = "Grocery",
            isNew = false,
            barcode = "890123",
            unit = "уп",
        ),
    )

    val shoppingList = listOf(
        ShoppingListItem(product = products[0], plannedQuantity = 2, isInCart = true,  inCartQuantity = 2),
        ShoppingListItem(product = products[1], plannedQuantity = 1, isInCart = false, inCartQuantity = 0),
        ShoppingListItem(product = products[2], plannedQuantity = 1, isInCart = true,  inCartQuantity = 1),
        ShoppingListItem(product = products[4], plannedQuantity = 1, isInCart = false, inCartQuantity = 0),
        ShoppingListItem(product = products[5], plannedQuantity = 3, isInCart = true,  inCartQuantity = 2),
    )

    val deals = listOf(
        Deal(products[0], discount = 20, originalPrice = 1450.0),
        Deal(products[1], discount = 30, originalPrice = 835.0),
        Deal(products[2], discount = 50, originalPrice = 1690.0),
        Deal(products[6], discount = 15, originalPrice = 882.0, expiresIn = "2д 14ч"),
        Deal(products[3], discount = 25, originalPrice = 659.0),
    )

    val categories = listOf(
        CategoryItem("produce", "Fruits & Veg", "Овощи и фрукты", "Жеміс-жидек", "🥦", 0xFF4CAF50, 124),
        CategoryItem("dairy", "Dairy", "Молочные", "Сүт", "🥛", 0xFF2196F3, 89),
        CategoryItem("meat", "Meat & Fish", "Мясо и рыба", "Ет балық", "🥩", 0xFFF44336, 67),
        CategoryItem("bakery", "Bakery", "Выпечка", "Нан", "🍞", 0xFFFF9800, 45),
        CategoryItem("beverages", "Beverages", "Напитки", "Сусындар", "🧃", 0xFF9C27B0, 98),
        CategoryItem("household", "Household", "Бытовые", "Үй", "🧹", 0xFF009688, 156),
        CategoryItem("snacks", "Snacks", "Снеки", "Тәтті", "🍪", 0xFFFFC107, 73),
        CategoryItem("frozen", "Frozen", "Заморозка", "Мұздатылған", "❄️", 0xFF00BCD4, 34),
    )
}
