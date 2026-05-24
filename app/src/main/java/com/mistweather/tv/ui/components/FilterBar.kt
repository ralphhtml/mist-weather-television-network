package com.mistweather.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.mistweather.tv.ui.theme.*
import com.mistweather.tv.viewmodel.FilterState

@Composable
fun FilterBar(
    filterState: FilterState,
    allCategories: List<String>,
    onFilterChanged: (FilterState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Filter:",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
        )

        // Category pill
        FilterPill(
            label = filterState.category ?: "All Categories",
            active = filterState.category != null,
            options = listOf(null) + allCategories,
            display = { it ?: "All Categories" },
            onSelected = { cat -> onFilterChanged(filterState.copy(category = cat)) },
        )

        // Live Now toggle
        TogglePill(
            label = "Live Now",
            active = filterState.liveOnly,
            onClick = { onFilterChanged(filterState.copy(liveOnly = !filterState.liveOnly)) },
        )
    }
}

@Composable
private fun <T> FilterPill(
    label: String,
    active: Boolean,
    options: List<T>,
    display: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box {
        Pill(
            text = "$label ▾",
            active = active,
            isFocused = isFocused,
            interactionSource = interactionSource,
            onClick = { expanded = true },
        )
        if (expanded) {
            Box(
                modifier = Modifier
                    .offset(y = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF001830))
                    .border(1.dp, MistBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .width(200.dp)
            ) {
                Column {
                    options.forEach { opt ->
                        val text = display(opt)
                        Text(
                            text = text,
                            fontSize = 13.sp,
                            color = if (display(opt) == label) MistBlue else TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelected(opt)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TogglePill(label: String, active: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Pill(
        text = if (active) "● $label" else "○ $label",
        active = active,
        isFocused = isFocused,
        interactionSource = interactionSource,
        onClick = onClick,
    )
}

@Composable
private fun Pill(
    text: String,
    active: Boolean,
    isFocused: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    active -> MistBlue.copy(alpha = 0.3f)
                    isFocused -> Color(0x22FFFFFF)
                    else -> Color(0x11FFFFFF)
                }
            )
            .then(
                if (isFocused || active)
                    Modifier.border(1.5.dp, if (active) MistBlue else FocusBorder, RoundedCornerShape(20.dp))
                else Modifier
            )
            .focusable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active) MistBlue else TextPrimary,
        )
    }
}
