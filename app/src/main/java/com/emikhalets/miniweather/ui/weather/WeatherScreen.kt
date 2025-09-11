package com.emikhalets.miniweather.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.emikhalets.miniweather.core.toast
import com.emikhalets.miniweather.domain.model.ForecastModel
import com.emikhalets.miniweather.domain.model.PollutionModel
import com.emikhalets.miniweather.domain.model.WeatherModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val requestLocationAccess = rememberLocationAccess(
        onReady = { viewModel.searchLocation() },
        rationaleText = stringResource(R.string.permission_location_rationale),
        permissionDeniedTitle = stringResource(R.string.permission_needed),
        permissionDeniedHint = stringResource(R.string.permission_denied_settings_hint),
        allowText = stringResource(R.string.allow),
        cancelText = stringResource(R.string.cancel),
        openSettingsText = stringResource(R.string.open_settings),
        servicesDisabledToast = stringResource(R.string.location_services_disabled)
    )

    val refreshErrorMessage = (state.refreshing as? LoadState.Error)?.message
    LaunchedEffect(refreshErrorMessage) {
        refreshErrorMessage?.let {
            context.toast(R.string.error_refreshing_weather)
            viewModel.consumeRefreshState()
        }
    }

    ScreenRoot(
        state = state,
        onQueryChange = viewModel::setQuery,
        onSearchCity = viewModel::search,
        onLocationClick = requestLocationAccess,
        onRetryClick = viewModel::search,
        onPullRefresh = viewModel::refresh,
    )
}

@Composable
private fun ScreenRoot(
    state: WeatherUiState,
    onPullRefresh: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onSearchCity: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onRetryClick: () -> Unit = {},
) {
    RootScaffoldBox(onLocationClick) {
        PullRefreshBox(
            query = state.query,
            refreshing = state.refreshing,
            onPullRefresh = onPullRefresh
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                CitiesSearchRow(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    onSearchCity = onSearchCity,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                when (state.loading) {
                    is LoadState.Error -> {
                        ErrorStub(
                            message = state.loading.message,
                            onRetryClick = onRetryClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    LoadState.Loading -> {
                        LoadingSkeleton()
                    }

                    LoadState.Idle -> {
                        if (state.weather == null) {
                            EmptyStub(Modifier.padding(horizontal = 16.dp))
                        } else {
                            WeatherHeroCard(
                                model = state.weather,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailsGrid(
                                model = state.weather,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            state.forecast?.let {
                                HourlyForecastRow(
                                    forecast = it,
                                    modifier = Modifier
                                )
                            }
                            state.airPollution?.let { data ->
                                Spacer(Modifier.height(12.dp))
                                AirQualityDetails(
                                    data = data,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            DaylightArc(
                                sunriseEpochSec = state.weather.sunrise,
                                sunsetEpochSec = state.weather.sunset,
                                timezoneOffset = state.weather.timeOffset
                            )
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }

            SavedCitiesRow(
                cities = state.savedCities,
                onCityClick = { city ->
                    onQueryChange(city)
                    onSearchCity()
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootScaffoldBox(
    onLocationClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather)) },
                actions = {
                    IconButton(onClick = onLocationClick) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.my_location)
                        )
                    }
                }
            )
        },
        content = { padding ->
            Box(
                content = { content() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PullRefreshBox(
    query: String,
    refreshing: LoadState,
    onPullRefresh: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = refreshState,
        content = { content() },
        isRefreshing = refreshing == LoadState.Loading,
        onRefresh = {
            if (query.isBlank()) {
                scope.launch { refreshState.animateToHidden() }
            } else {
                onPullRefresh()
            }
        }
    )
}

@Composable
private fun CitiesSearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchCity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
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
                enabled = query.isNotBlank()
            ) { Icon(Icons.Default.Search, null) }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun WeatherHeroCard(model: WeatherModel, modifier: Modifier = Modifier) {
    val gradient = heroGradient(model)
    val updated = remember(model.updatedAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(model.updatedAt * 1000))
    }
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(gradient), RoundedCornerShape(24.dp))
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
private fun DetailsGrid(model: WeatherModel, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
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
private fun LoadingSkeleton(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmerBrush()

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(180.dp)
                .background(shimmer, RoundedCornerShape(24.dp))
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            repeat(3) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(72.dp)
                        .background(shimmer, RoundedCornerShape(16.dp))
                )
            }
        }
        Column {
            Text(
                text = stringResource(R.string.next_24h),
                color = Color.Transparent,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .background(shimmer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Spacer(Modifier.width(8.dp))
                }
                repeat(6) {
                    item {
                        Box(
                            Modifier
                                .size(72.dp, 120.dp)
                                .background(shimmer, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(200.dp)
                .background(shimmer, RoundedCornerShape(24.dp))
        )
    }
}

@Composable
private fun EmptyStub(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.enter_city_to_show_weather),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun ErrorStub(message: String, onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun HourlyForecastRow(forecast: ForecastModel, modifier: Modifier = Modifier) {
    val hours = remember(forecast) { forecast.hours.take(8) } // 8 точек * 3 часа = 24ч
    if (hours.isEmpty()) return

    Column {
        Text(
            text = stringResource(R.string.next_24h),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
            item {
                Spacer(Modifier.width(8.dp))
            }
            items(hours) { hour ->
                HourCard(hour = hour, tzOffsetSec = forecast.timeOffset)
            }
        }
    }
}

@Composable
private fun HourCard(hour: ForecastModel.Hour, tzOffsetSec: Int) {
    val time = remember(hour.timeEpoch, tzOffsetSec) {
        formatTime(hour.timeEpoch, tzOffsetSec)
    }

    Column(
        Modifier
            .width(72.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(4.dp))
        AsyncImage(
            model = hour.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = hour.temperature?.roundToIntOrDash()?.plus("°") ?: "—",
            style = MaterialTheme.typography.bodyMedium
        )
        hour.popPercent?.let {
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$it%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==============
//  Preview Data
// ==============

private fun previewWeatherGenerator(onlyMain: Boolean = false): WeatherModel {
    val now = System.currentTimeMillis()
    return WeatherModel(
        city = "Москва",
        temperature = Random.nextDouble(-15.0, 30.0),
        feelsLike = Random.nextDouble(-15.0, 30.0),
        humidity = Random.nextInt(50, 85),
        windSpeed = Random.nextDouble(0.5, 3.0),
        description = "Описание погоды",
        iconUrl = "",
        updatedAt = Random.nextLong(now - 1000 * 60 * 60 * 24, now),
        pressure = Random.nextInt(740, 770),
        sunrise = if (!onlyMain) now - 500000 else 0,
        sunset = if (!onlyMain) now + 300000 else 0,
        timeOffset = 3,
    )
}

private fun previewForecastGenerator(): ForecastModel {
    val hours = (0 until 8).map { i ->
        ForecastModel.Hour(
            timeEpoch = System.currentTimeMillis() / 1000 + i * 3 * 3600,
            temperature = 15.0 + i,
            iconUrl = "",
            popPercent = listOf(10, 20, 30, 40, 50, 40, 30, 20)[i],
            precipMm3h = if (i in 4..6) listOf(0.2, 0.6, 1.1)[i - 4] else null
        )
    }
    return ForecastModel(
        timeOffset = 3 * 3600,
        hours = hours
    )
}

@Preview
@Composable
private fun PreviewIdle() {
    MiniWeatherTheme {
        ScreenRoot(state = WeatherUiState(loading = LoadState.Idle))
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    MiniWeatherTheme {
        ScreenRoot(state = WeatherUiState(loading = LoadState.Loading))
    }
}

@Preview
@Composable
private fun PreviewError() {
    MiniWeatherTheme {
        ScreenRoot(state = WeatherUiState(loading = LoadState.Error("Ошибка загрузки")))
    }
}

@Preview
@Composable
private fun PreviewData() {
    val state = WeatherUiState(
        weather = previewWeatherGenerator(),
        forecast = previewForecastGenerator(),
        query = "Лондон",
        savedCities = listOf("Москва", "Лондон", "Сыктывкар", "Тында", "Бахчи-Сарай")
    )
    MiniWeatherTheme {
        ScreenRoot(state = state)
    }
}

@Preview
@Composable
private fun PreviewDataOnlyMain() {
    val state = WeatherUiState(
        weather = previewWeatherGenerator(onlyMain = true),
        forecast = previewForecastGenerator(),
        query = "Лондон",
        savedCities = listOf("Москва", "Лондон", "Сыктывкар", "Тында", "Бахчи-Сарай")
    )
    MiniWeatherTheme {
        ScreenRoot(state = state)
    }
}

@Preview
@Composable
private fun PreviewDataPollution() {
    val state = WeatherUiState(
        weather = previewWeatherGenerator(onlyMain = true),
        airPollution = PollutionModel(
            aqi = Random.nextInt(1, 6),
            updatedAt = System.currentTimeMillis() / 1000,
            co = Random.nextDouble(0.0, 15400.0),
            no = Random.nextDouble(0.0, 1.0),
            no2 = Random.nextDouble(0.0, 200.0),
            o3 = Random.nextDouble(0.0, 180.0),
            so2 = Random.nextDouble(0.0, 350.0),
            pm2_5 = Random.nextDouble(0.0, 75.0),
            pm10 = Random.nextDouble(0.0, 200.0),
            nh3 = Random.nextDouble(0.0, 1.0),
        ),
    )
    MiniWeatherTheme {
        ScreenRoot(state = state)
    }
}