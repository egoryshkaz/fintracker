package com.fintracker.ui.addtransaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberKeyboard(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "⌫")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {
                                "⌫" -> onDeleteClick()
                                else -> onNumberClick(label)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(text = label, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }

        Button(
            onClick = onDoneClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Готово", style = MaterialTheme.typography.titleLarge)
        }
    }
}