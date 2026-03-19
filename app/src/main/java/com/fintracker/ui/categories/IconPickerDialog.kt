package com.fintracker.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fintracker.ui.categories.getIconVector

// Список (ключ -> отображаемое имя, но сохраняем ключ)
val categoryIcons = listOf(
    "restaurant" to "Еда",
    "shopping_cart" to "Покупки",
    "directions_car" to "Транспорт",
    "local_cafe" to "Кафе",
    "movie" to "Развлечения",
    "health_and_safety" to "Здоровье",
    "checkroom" to "Одежда",
    "home" to "Дом",
    "wifi" to "Интернет",
    "local_pharmacy" to "Аптека",
    "school" to "Образование",
    "card_giftcard" to "Подарки",
    "pets" to "Животные",
    "build" to "Ремонт",
    "work" to "Работа",
    "computer" to "Фриланс",
    "trending_up" to "Инвестиции",
    "star" to "Премия",
    "monetization_on" to "Кэшбэк",
    "emoji_events" to "Выигрыш",
    "house" to "Аренда",
    "pie_chart" to "Дивиденды"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerDialog(
    onIconSelected: (String) -> Unit,  // теперь передаём ключ (String)
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите иконку") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(categoryIcons) { (key, name) ->
                    IconButton(
                        onClick = {
                            onIconSelected(key)  // передаём ключ, а не имя
                            onDismiss()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = categoryIconMap[key] ?: Icons.Default.Folder,
                            contentDescription = name,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}