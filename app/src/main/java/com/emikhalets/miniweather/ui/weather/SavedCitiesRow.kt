package com.emikhalets.miniweather.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emikhalets.miniweather.core.theme.MiniWeatherTheme

@Composable
fun BoxScope.SavedCitiesRow(
    cities: List<String>,
    onCityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var hiddenForSheet by remember { mutableStateOf<List<String>>(emptyList()) }

    if (cities.isNotEmpty()) {
        SavedCities(
            cities = cities,
            onCityClick = onCityClick,
            onMoreClick = { hidden ->
                hiddenForSheet = hidden
                showSheet = true
            },
            modifier = modifier.align(Alignment.BottomCenter)
        )
    }

    if (showSheet) {
        HiddenCitiesSheet(
            hidden = hiddenForSheet,
            onSelect = onCityClick,
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
private fun SavedCities(
    cities: List<String>,
    onCityClick: (String) -> Unit,
    onMoreClick: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    chipSpacing: Dp = 8.dp,
) {
    SubcomposeLayout(
        modifier.background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    Pair(0.0f, Color.Transparent),
                    Pair(0.2f, Color.White),
                    Pair(0.8f, Color.White),
                    Pair(1.0f, Color.Transparent)
                ),
            )
        )
    ) { constraints ->
        val spacingPx = chipSpacing.roundToPx()
        val maxW = constraints.maxWidth

        // 1) Черновая подкомпозиция для измерения чипов
        val chipMeasurables = cities.map { city ->
            subcompose("measure-chip-$city") {
                AssistChip(onClick = { onCityClick(city) }, label = { Text(city) })
            }.first()
        }
        // Измеряем «…» чтобы знать резерв
        val moreProbe = subcompose("measure-more") {
            AssistChip(
                onClick = {},
                label = { Icon(Icons.Default.MoreHoriz, null) },
            )
        }.first().measure(constraints)
        val moreWidth = moreProbe.width

        val measuredChips = chipMeasurables.map { it.measure(constraints) }

        // 2) Считаем сколько чипов влезает (с учётом места под «…», если будут скрытые)
        var used = 0
        var visibleCount = 0
        for (i in measuredChips.indices) {
            val p = measuredChips[i]
            val next = if (visibleCount == 0) p.width else used + spacingPx + p.width
            val isLast = i == measuredChips.lastIndex
            val needReserveForMore =
                !isLast // если это не последний, значит будет скрытое → надо место под «…»
            val fits =
                if (needReserveForMore) next + spacingPx + moreWidth <= maxW else next <= maxW
            if (fits) {
                used = next
                visibleCount++
            } else break
        }

        val hidden = if (visibleCount < cities.size) cities.drop(visibleCount) else emptyList()

        // 3) Подкомпонуем реальные видимые чипы (с onClick) и «…»
        val visiblePlaceables = (0 until visibleCount).map { idx ->
            subcompose("chip-$idx") {
                val city = cities[idx]
                AssistChip(onClick = { onCityClick(city) }, label = { Text(city) })
            }.first().measure(constraints)
        }

        val morePlaceable = if (hidden.isNotEmpty()) {
            subcompose("more-active") {
                AssistChip(
                    onClick = { onMoreClick(hidden) },
                    label = { Icon(Icons.Default.MoreHoriz, null) },
                )
            }.first().measure(constraints)
        } else null

        val height =
            (visiblePlaceables.map { it.height } + listOfNotNull(morePlaceable?.height))
                .maxOrNull() ?: 0

        layout(width = maxW, height = height) {
            var x = 0
            visiblePlaceables.forEachIndexed { i, p ->
                if (i > 0) x += spacingPx
                p.placeRelative(x, 0)
                x += p.width
            }
            morePlaceable?.let {
                if (visiblePlaceables.isNotEmpty()) x += spacingPx
                it.placeRelative(x, 0)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HiddenCitiesSheet(
    hidden: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "Скрытые города",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(hidden) { city ->
                ListItem(
                    headlineContent = { Text(city) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(city)
                            onDismiss()
                        }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun HiddenCitiesSheetContent(
    hidden: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Column {
        Text(
            text = "Скрытые города",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(hidden) { city ->
                ListItem(
                    headlineContent = { Text(city) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(city)
                            onDismiss()
                        }
                )
                HorizontalDivider()
            }
        }
    }
}

// ==============
//  Preview Data
// ==============

@Preview
@Composable
private fun PreviewData() {
    MiniWeatherTheme {
        Box(Modifier.fillMaxSize()) {
            SavedCitiesRow(
                cities = listOf("Москва", "Лондон", "Сыктывкар", "Тында", "Бахчи-Сарай"),
                onCityClick = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewBottomSheet() {
    MiniWeatherTheme {
        HiddenCitiesSheetContent(
            hidden = listOf("Москва", "Лондон", "Сыктывкар", "Тында", "Бахчи-Сарай"),
            onSelect = {},
            onDismiss = {},
        )
    }
}