package com.fintracker.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fintracker.ui.categories.getIconVector

// Палитра лучших цветов
val categoryColors = listOf(
    Color(0xFFF44336), // Красный
    Color(0xFFE91E63), // Розовый
    Color(0xFF9C27B0), // Фиолетовый
    Color(0xFF673AB7), // Глубокий фиолетовый
    Color(0xFF3F51B5), // Индиго
    Color(0xFF2196F3), // Синий
    Color(0xFF03A9F4), // Голубой
    Color(0xFF00BCD4), // Бирюзовый
    Color(0xFF009688), // Зелёно-голубой
    Color(0xFF4CAF50), // Зелёный
    Color(0xFF8BC34A), // Светло-зелёный
    Color(0xFFCDDC39), // Лайм
    Color(0xFFFFEB3B), // Жёлтый
    Color(0xFFFFC107), // Янтарный
    Color(0xFFFF9800), // Оранжевый
    Color(0xFFFF5722), // Глубокий оранжевый
    Color(0xFF795548), // Коричневый
    Color(0xFF9E9E9E), // Серый
    Color(0xFF607D8B), // Сине-серый
    Color(0xFF000000), // Чёрный
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(categoryColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                onColorSelected(color)
                                onDismiss()
                            }
                    )
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