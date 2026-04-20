package com.smartcart.data.model

import com.smartcart.data.CurrencyConfig

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
    val zoneId: String = id,
) {
    fun localizedName(lang: AppLanguage) = when (lang) {
        AppLanguage.RU -> nameRu
        AppLanguage.KK -> nameKk
        AppLanguage.EN -> nameEn
    }

    fun formattedPrice() = CurrencyConfig.format(price)
    fun formattedPriceOld() = CurrencyConfig.format(price * 1.1)
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
    fun formattedOriginal() = CurrencyConfig.format(originalPrice)
    fun formattedDiscounted() = CurrencyConfig.format(discountedPrice)
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
    val estimatedTotal: String,
    val itemsInCart: String,
    val checkoutNow: String,
    val autoCart: String,
    val cartOnline: String,
    val callAssistant: String,
    val yourItems: String,
    val plannedList: String,
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
    val planned: String,
    val inCartDetected: String,
    val willBeAddedByCamera: String,
    val outOfStock: String,
    val storeMap: String,
    val aisle: String,
    val shelf: String,
    val demoMode: String,
    val kaspiQrScan: String,
    val setBudget: String,
    val navSupport: String,
    val wishlistHint: String,
    val addedByCamera: String,
    val inCartQty: String,
)

val StringsEN = AppStrings(
    loginTitle = "Smart Cart", scanToLogin = "Scan to login",
    scanInstruction = "Open the Shopgram app on your phone to scan",
    readyToScan = "Ready to scan", scanning = "Scanning...", authenticated = "Authenticated",
    altLogin = "Alternative Login", needHelp = "Need help? Ask a store associate.",
    home = "Home", topDeals = "Top Deals", categories = "Categories", favorites = "Favorites",
    searchPlaceholder = "Search products...",
    flashSale = "FLASH SALE", weeklySavers = "Weekly\nSuper Savers",
    bannerSub = "Up to 50% Off Top Brands this week only!", shopNow = "Shop Now",
    trendingOffers = "Trending Offers", viewAll = "View All",
    yourCart = "Your Cart", scanItems = "Scan items to add automatically",
    checkout = "Checkout", subtotal = "Subtotal", tax = "Tax (8%)", discounts = "Discounts", total = "Total",
    myShoppingList = "My Shopping List", foundItems = "Found {n} items on your list",
    addItemSearch = "Add item or search...", allItems = "All Items",
    produce = "Produce", dairyEggs = "Dairy & Eggs", bakery = "Bakery",
    sortByAisle = "Sort by Aisle", estimatedTotal = "Estimated Total", itemsInCart = "Items in Cart", checkoutNow = "Checkout Now",
    autoCart = "Auto Cart", cartOnline = "Cart Online", callAssistant = "Call Assistant",
    yourItems = "Your Items", plannedList = "Your planned list",
    paymentMethod = "PAYMENT METHOD", creditCard = "Credit Card", cash = "Cash", tapToPay = "Tap to Pay",
    orderSummary = "Order Summary", vat = "VAT (12%)", memberDiscount = "Member Discount",
    completePurchase = "Complete Purchase", terms = "By completing purchase you agree to our Terms of Service",
    pointsEarned = "You will earn 24 points with this purchase.", add = "Add",
    navDeals = "Deals", navCats = "Categories", navSaved = "Favorites",
    dealsHeader = "Deals & Discounts", filterAll = "All", filter20 = "-20%", filter30 = "-30%", filter50 = "-50%", filterNew = "New",
    expiringSoon = "Ending soon ⏰", catsHeader = "Categories",
    wishlistHeader = "Favorites", emptyWishlist = "No items", goToCatalog = "Go to catalog",
    sortPriceUp = "Price ↑", sortPriceDown = "Price ↓", sortNew = "New",
    itemsSuffix = " items", planned = "Planned", inCartDetected = "In cart ✓",
    willBeAddedByCamera = "Will be added by camera", outOfStock = "Out of stock",
    storeMap = "Store map", aisle = "Aisle", shelf = "Shelf",
    demoMode = "Demo Mode", kaspiQrScan = "Scan with Kaspi to pay", setBudget = "Set budget",
    navSupport = "Support", 
    wishlistHint = "Items in this list will be auto-detected by AI camera",
    addedByCamera = "Auto-detected by AI camera",
    inCartQty = "In cart"
)

val StringsRU = AppStrings(
    loginTitle = "Smart Cart", scanToLogin = "Сканируйте для входа",
    scanInstruction = "Откройте приложение Shopgram для сканирования",
    readyToScan = "Готов к сканированию", scanning = "Сканирование...", authenticated = "Аутентифицирован",
    altLogin = "Альтернативный вход", needHelp = "Нужна помощь? Спросите сотрудника.",
    home = "Главная", topDeals = "Акции", categories = "Категории", favorites = "Избранное",
    searchPlaceholder = "Поиск товаров...",
    flashSale = "РАСПРОДАЖА", weeklySavers = "Скидки Недели",
    bannerSub = "До 50% на топ бренды только на этой неделе!", shopNow = "Купить сейчас",
    trendingOffers = "Популярные предложения", viewAll = "Все",
    yourCart = "Ваша корзина", scanItems = "Сканируйте товары для добавления",
    checkout = "Оформить", subtotal = "Подытог", tax = "Налог (8%)", discounts = "Скидки", total = "Итого",
    myShoppingList = "Мой список покупок", foundItems = "Найдено {n} товаров",
    addItemSearch = "Добавить или найти...", allItems = "Все товары",
    produce = "Овощи и фрукты", dairyEggs = "Молочные продукты", bakery = "Выпечка",
    sortByAisle = "По рядам", estimatedTotal = "Примерная сумма", itemsInCart = "Товаров в корзине", checkoutNow = "Оплатить",
    autoCart = "Авто-Корзина", cartOnline = "Корзина онлайн", callAssistant = "Позвать помощника",
    yourItems = "Ваши товары", plannedList = "Ваш план покупок",
    paymentMethod = "СПОСОБ ОПЛАТЫ", creditCard = "Карта", cash = "Наличные", tapToPay = "Бесконтактно",
    orderSummary = "Сводка заказа", vat = "НДС (12%)", memberDiscount = "Скидка участника",
    completePurchase = "Завершить покупку", terms = "Завершая покупку, вы соглашаетесь с условиями",
    pointsEarned = "Вы получите 24 балла за эту покупку.", add = "Добавить",
    navDeals = "Акции", navCats = "Разделы", navSaved = "Избранное",
    dealsHeader = "Акции и скидки", filterAll = "Все", filter20 = "-20%", filter30 = "-30%", filter50 = "-50%", filterNew = "Новинки",
    expiringSoon = "Скоро закончится ⏰", catsHeader = "Категории",
    wishlistHeader = "Избранное", emptyWishlist = "Пусто", goToCatalog = "В каталог",
    sortPriceUp = "По цене ↑", sortPriceDown = "По цене ↓", sortNew = "По новизне",
    itemsSuffix = " шт", planned = "План", inCartDetected = "В корзине ✓",
    willBeAddedByCamera = "Будет добавлен камерой", outOfStock = "Нет в наличии",
    storeMap = "Карта магазина", aisle = "Ряд", shelf = "Полка",
    demoMode = "Демо режим", kaspiQrScan = "Отсканируйте в Kaspi", setBudget = "Задать бюджет",
    navSupport = "Поддержка",
    wishlistHint = "Товары из этого списка будут автоматически отмечены AI-камерой",
    addedByCamera = "Добавлено AI-камерой",
    inCartQty = "В корзине"
)

val StringsKK = AppStrings(
    loginTitle = "Smart Cart", scanToLogin = "Кіру үшін сканерлеңіз",
    scanInstruction = "Сканерлеу үшін Shopgram қолданбасын ашыңыз",
    readyToScan = "Сканерлеуге дайын", scanning = "Сканерлеу...", authenticated = "Расталданды",
    altLogin = "Балама кіру", needHelp = "Көмек керек пе? Қызметкерден сұраңыз.",
    home = "Басты", topDeals = "Акциялар", categories = "Санаттар", favorites = "Таңдаулылар",
    searchPlaceholder = "Тауарларды іздеу...",
    flashSale = "Жедел сату", weeklySavers = "Апталық Жеңілдіктер",
    bannerSub = "Тек осы аптада үздік брендтерге 50% дейін жеңілдік!", shopNow = "Қазір алу",
    trendingOffers = "Трендті ұсыныстар", viewAll = "Барлығы",
    yourCart = "Сіздің себетіңіз", scanItems = "Қосу үшін тауарларды сканерлеңіз",
    checkout = "Төлеу", subtotal = "Барлығы", tax = "Салық (8%)", discounts = "Жеңілдіктер", total = "Жалпы",
    myShoppingList = "Менің тізімім", foundItems = "{n} тауар табылды",
    addItemSearch = "Қосу немесе іздеу...", allItems = "Барлық тауарлар",
    produce = "Жеміс-жидек", dairyEggs = "Сүт өнімдері", bakery = "Нан өнімдері",
    sortByAisle = "Қатар бойынша", estimatedTotal = "Шамалы сома", itemsInCart = "Себеттегі тауар", checkoutNow = "Төлеу",
    autoCart = "Авто-Себет", cartOnline = "Себет онлайн", callAssistant = "Көмекшіні шақыру",
    yourItems = "Сіздің тауарларыңыз", plannedList = "Жоспарланған тізіміңіз",
    paymentMethod = "ТӨЛЕМ ӘДІСІ", creditCard = "Карта", cash = "Қолма-қол", tapToPay = "Жанасу арқылы",
    orderSummary = "Тапсырыс қорытындысы", vat = "ҚҚС (12%)", memberDiscount = "Мүшелік жеңілдік",
    completePurchase = "Сатып алуды аяқтау", terms = "Сатып алуды аяқтау арқылы ережелермен келісесіз",
    pointsEarned = "Бұл сатып алудан 24 ұпай аласыз.", add = "Қосу",
    navDeals = "Ұсыныс", navCats = "Бөлімдер", navSaved = "Таңдаулы",
    dealsHeader = "Ұсыныстар", filterAll = "Барлығы", filter20 = "-20%", filter30 = "-30%", filter50 = "-50%", filterNew = "Жаңа",
    expiringSoon = "Жақында аяқталады ⏰", catsHeader = "Санаттар",
    wishlistHeader = "Таңдаулы", emptyWishlist = "Бос", goToCatalog = "Каталогқа",
    sortPriceUp = "Баға ↑", sortPriceDown = "Баға ↓", sortNew = "Жаңалық",
    itemsSuffix = " тауар", planned = "Жоспарланған", inCartDetected = "Себетте ✓",
    willBeAddedByCamera = "Камера қосады", outOfStock = "Жоқ",
    storeMap = "Дүкен картасы", aisle = "Қатар", shelf = "Сөре",
    demoMode = "Демо режим", kaspiQrScan = "Kaspi-мен төлеңіз", setBudget = "Бюджет белгілеу",
    navSupport = "Қолдау",
    wishlistHint = "Бұл тізімдегі тауарларды AI-камера автоматты түрде белгілейді",
    addedByCamera = "AI-камера арқылы қосылды",
    inCartQty = "Себетте"
)

fun AppLanguage.strings() = when (this) {
    AppLanguage.RU -> StringsRU
    AppLanguage.KK -> StringsKK
    AppLanguage.EN -> StringsEN
}

object MockData {
    val products = listOf(
        Product(id = "1", nameEn = "Organic Avocado", nameRu = "Авокадо органик", nameKk = "Органикалық Авокадо", price = 1157.0, imageUrl = "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400", category = "Fruits", barcode = "123456"),
        Product(id = "2", nameEn = "Whole Milk", nameRu = "Молоко цельное", nameKk = "Толық сүт", price = 585.0, imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", category = "Dairy", barcode = "234567"),
        Product(id = "3", nameEn = "Sourdough Bread", nameRu = "Хлеб на закваске", nameKk = "Ашыған нан", price = 845.0, imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400", category = "Bakery", barcode = "345678"),
        Product(id = "4", nameEn = "Greek Yogurt", nameRu = "Йогурт греческий", nameKk = "Грек йогурты", price = 494.0, imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400", category = "Dairy", barcode = "456789"),
        Product(id = "5", nameEn = "Chicken Breast", nameRu = "Куриная грудка", nameKk = "Тауық омырауы", price = 1560.0, imageUrl = "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400", category = "Meat", barcode = "567890"),
        Product(id = "6", nameEn = "Cherry Tomatoes", nameRu = "Помидоры черри", nameKk = "Шие қызанақтар", price = 676.0, imageUrl = "https://images.unsplash.com/photo-1546470427-22706f4f4d82?w=400", category = "Vegetables", barcode = "678901"),
        Product(id = "7", nameEn = "Orange Juice", nameRu = "Сок апельсиновый", nameKk = "Апельсин шырыны", price = 975.0, imageUrl = "https://images.unsplash.com/photo-1613478223719-2ab802602423?w=400", category = "Beverages", isNew = true, barcode = "789012"),
        Product(id = "8", nameEn = "Pasta Barilla", nameRu = "Паста Барилла", nameKk = "Барилла пастасы", price = 546.0, imageUrl = "https://images.unsplash.com/photo-1556761223-4c4282c73f77?w=400", category = "Grocery", barcode = "890123"),
    )

    val shoppingList = listOf(
        ShoppingListItem(product = products[0], plannedQuantity = 2, isInCart = false, inCartQuantity = 0),
        ShoppingListItem(product = products[1], plannedQuantity = 1, isInCart = false, inCartQuantity = 0),
        ShoppingListItem(product = products[2], plannedQuantity = 1, isInCart = false, inCartQuantity = 0),
        ShoppingListItem(product = products[4], plannedQuantity = 1, isInCart = false, inCartQuantity = 0),
        ShoppingListItem(product = products[5], plannedQuantity = 3, isInCart = false, inCartQuantity = 0),
    )

    val deals = listOf(
        Deal(products[0], discount = 20, originalPrice = 11.12),
        Deal(products[1], discount = 30, originalPrice = 6.42),
        Deal(products[2], discount = 50, originalPrice = 13.0),
        Deal(products[6], discount = 15, originalPrice = 6.78, expiresIn = "2д 14ч"),
        Deal(products[3], discount = 25, originalPrice = 5.07),
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
