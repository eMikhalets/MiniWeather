package com.emikhalets.miniweather.ui.weather

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.emikhalets.miniweather.R
import com.emikhalets.miniweather.core.LoadState
import com.emikhalets.miniweather.core.formatDoubleOneDigit
import com.emikhalets.miniweather.core.rememberShimmerBrush
import com.emikhalets.miniweather.core.roundToIntOrDash
import com.emikhalets.miniweather.core.theme.MiniWeatherTheme
import com.emikhalets.miniweather.domain.model.WeatherModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val refreshErrorMessage = (state.refreshing as? LoadState.Error)?.message
    LaunchedEffect(refreshErrorMessage) {
        refreshErrorMessage?.let {
            Toast.makeText(
                context,
                context.getString(R.string.error_refreshing_weather),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    ScreenRoot(
        state = state,
        onQueryChange = viewModel::setQuery,
        onSearchCity = viewModel::search,
        onLocationClick = {
            // TODO: set location feature
        },
        onRetryClick = viewModel::search,
        onPullRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenRoot(
    state: WeatherUiState,
    onPullRefresh: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchCity: () -> Unit,
    onLocationClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather)) },
                actions = {
                    IconButton(onClick = onLocationClick, enabled = false) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.my_location)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshBox(
                isRefreshing = state.refreshing == LoadState.Loading,
                onRefresh = onPullRefresh,
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.enter_city)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focus.clearFocus()
                                    onSearchCity()
                                }
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        focus.clearFocus()
                                        onSearchCity()
                                    },
                                    enabled = state.query.isNotBlank()
                                ) { Icon(Icons.Default.Search, null) }
                            }
                        )

                        if (state.weather == null) {
                            when (state.loading) {
                                LoadState.Idle -> {
                                    EmptyStub()
                                }

                                LoadState.Loading -> {
                                    LoadingSkeleton()
                                }

                                is LoadState.Error -> {
                                    ErrorStub(state.loading.message, onRetryClick)
                                }
                            }
                        } else {
                            when {
                                state.loading is LoadState.Loading -> {
                                    LoadingSkeleton()
                                }

                                state.loading is LoadState.Error -> {
                                    ErrorStub(state.loading.message, onRetryClick)
                                }

                                else -> {
                                    WeatherHeroCard(state.weather)
                                    DetailsGrid(state.weather)
                                    DaylightArc(
                                        sunriseEpochSec = state.weather.sunrise,
                                        sunsetEpochSec = state.weather.sunset,
                                        timezoneOffset = state.weather.timeOffset
                                    )
                                }
                            }
                        }
                    }

                    var showSheet by rememberSaveable { mutableStateOf(false) }
                    var hiddenForSheet by remember { mutableStateOf<List<String>>(emptyList()) }

                    SavedCitiesRow(
                        cities = state.savedCities,
                        onCityClick = { city ->
                            onQueryChange(city)
                            onSearchCity()
                        },
                        onMoreClick = { hidden ->
                            hiddenForSheet = hidden
                            showSheet = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(bottom = 16.dp)
                    )

                    if (showSheet) {
                        HiddenCitiesSheet(
                            hidden = hiddenForSheet,
                            onSelect = { city ->
                                onQueryChange(city)
                                onSearchCity()
                            },
                            onDismiss = { showSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherHeroCard(model: WeatherModel) {
    val gradient = heroGradient(model)
    val updated = remember(model.updatedAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(model.updatedAt * 1000))
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.hero_city_updated_at, model.city, updated),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${model.temperature?.roundToIntOrDash()}°",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                Text(
                    text = stringResource(
                        R.string.feels_like,
                        model.feelsLike.roundToIntOrDash()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = model.description.replaceFirstChar { it.titlecase() }
                        .ifEmpty { "—" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            AsyncImage(
                model = model.iconUrl,
                contentDescription = null,
                onError = { error ->
                    Timber.e(error.result.throwable)
                },
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
private fun DetailsGrid(model: WeatherModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailChip(
            title = stringResource(R.string.humidity),
            value = model.humidity?.let { "${it}%" } ?: "—",
            modifier = Modifier.weight(1f)
        )
        DetailChip(
            title = stringResource(R.string.wind),
            value = model.windSpeed?.let { "${formatDoubleOneDigit(it)} м/с" } ?: "—",
            modifier = Modifier.weight(1f)
        )
        DetailChip(
            title = stringResource(R.string.pressure),
            value = model.pressure?.let { stringResource(R.string.pressure_value, it) } ?: "—",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetailChip(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun LoadingSkeleton() {
    val shimmer = rememberShimmerBrush()

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(shimmer)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shimmer)
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shimmer)
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shimmer)
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(shimmer)
        )
    }
}

@Composable
private fun EmptyStub() {
    Text(
        text = stringResource(R.string.enter_city_to_show_weather),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ErrorStub(message: String, onRetryClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.error_value, message),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetryClick) {
            Text(stringResource(R.string.repeat))
        }
    }
}

@Composable
private fun SavedCitiesRow(
    cities: List<String>,
    onCityClick: (String) -> Unit,
    onMoreClick: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    chipSpacing: Dp = 8.dp,
) {
    SubcomposeLayout(modifier) { constraints ->
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


@Preview
@Composable
private fun Preview1() {
    val model = WeatherModel(
        city = "Москва",
        temperature = 21.0,
        feelsLike = 18.0,
        humidity = 65,
        windSpeed = 2.5,
        description = "Какое-то описание хз",
        iconUrl = "",
        updatedAt = System.currentTimeMillis(),
        pressure = 762,
        sunrise = System.currentTimeMillis() - 500000,
        sunset = System.currentTimeMillis() + 300000,
        timeOffset = 3,
    )
    val state = WeatherUiState(
        weather = model,
        query = "Лондон",
    )
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    val model = WeatherModel(
        city = "Москва",
        temperature = 15.0,
        feelsLike = 18.0,
        humidity = 65,
        windSpeed = 2.5,
        description = "Какое-то описание хз",
        iconUrl = "",
        updatedAt = System.currentTimeMillis(),
        pressure = 762,
        sunrise = 0,
        sunset = System.currentTimeMillis() + 300000,
        timeOffset = 3,
    )
    val state = WeatherUiState(
        weather = model,
        savedCities = listOf("Москва", "Лондон", "Сыктывкар", "Тында", "Бахчи-Сарай")
    )
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}

@Preview
@Composable
private fun Preview3() {
    val state = WeatherUiState(loading = LoadState.Error("Ошибка загрузки"))
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}

@Preview
@Composable
private fun Preview4() {
    val state = WeatherUiState(loading = LoadState.Idle)
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}

@Preview
@Composable
private fun Preview5() {
    val state = WeatherUiState(loading = LoadState.Loading)
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}