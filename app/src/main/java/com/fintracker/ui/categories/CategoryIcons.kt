package com.fintracker.ui.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Полная карта иконок (ключ -> ImageVector)
val categoryIconMap: Map<String, ImageVector> = mapOf(
    // Расходы
    "restaurant" to Icons.Default.Restaurant,
    "directions_car" to Icons.Default.DirectionsCar,
    "local_cafe" to Icons.Default.LocalCafe,
    "movie" to Icons.Default.Movie,
    "health_and_safety" to Icons.Default.HealthAndSafety,
    "checkroom" to Icons.Default.Checkroom,
    "home" to Icons.Default.Home,
    "wifi" to Icons.Default.Wifi,
    "local_pharmacy" to Icons.Default.LocalPharmacy,
    "school" to Icons.Default.School,
    "card_giftcard" to Icons.Default.CardGiftcard,
    "pets" to Icons.Default.Pets,
    "build" to Icons.Default.Build,
    "shopping_cart" to Icons.Default.ShoppingCart,
    "flight" to Icons.Default.Flight,
    "fitness_center" to Icons.Default.FitnessCenter,
    "phone" to Icons.Default.Phone,
    "miscellaneous_services" to Icons.Default.MiscellaneousServices,
    // Доходы
    "work" to Icons.Default.Work,
    "computer" to Icons.Default.Computer,
    "trending_up" to Icons.Default.TrendingUp,
    "star" to Icons.Default.Star,
    "monetization_on" to Icons.Default.MonetizationOn,
    "emoji_events" to Icons.Default.EmojiEvents,
    "house" to Icons.Default.House,
    "pie_chart" to Icons.Default.PieChart,
    "account_balance" to Icons.Default.AccountBalance,
    "attach_money" to Icons.Default.AttachMoney,
    "euro" to Icons.Default.Euro,
    "account_balance_wallet" to Icons.Default.AccountBalanceWallet,
    // Дефолтная
    "folder" to Icons.Default.Folder
)

/**
 * Возвращает ImageVector по строковому ключу.
 * Если ключ не найден, возвращает Icons.Default.Folder.
 */
fun getIconVector(iconName: String): ImageVector {
    return categoryIconMap[iconName] ?: Icons.Default.Folder
}